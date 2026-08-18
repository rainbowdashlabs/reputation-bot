# Concept: Reputation announcement messages

Status: implemented
Scope: backend (bot + web api), database, dashboard, localization

Deviations from this concept made during implementation:

- `ReactionListener` no longer pre-checks `MESSAGE_SEND` before submitting reaction reputation. The check moved into
  the announcement itself, so a channel the bot cannot write in no longer blocks the reputation.
- `States` (the `messages states` command) was restructured around a list of `State` records instead of hand-written
  select menus, since the number of switches grew from two to ten.
- The setting update logic of `ReputationTypeSettings.vue` was extracted into `reputationview/useSettingUpdate.ts` and
  is shared with the new section.
- Audit log labels for the new settings were added to the frontend locales. `messages_reactionconfirmation` is kept
  there, because audit entries recorded before the migration still reference that key.
- `MessagesPOJO` got a `@JsonCreator` constructor. Without one Jackson could not construct it at all, so
  `POST v1/settings/messages` failed for every body. Every property is `required`, so an incomplete body is rejected
  instead of silently resetting the switches it omits — `Messages#apply` writes every value which differs from the
  current state. Seven sibling request POJOs (`ReputationPOJO`, `GeneralPOJO`, `ProfilePOJO`, `ThankingPOJO`,
  `ChannelsPOJO`, `ReactionsPOJO`, `ThankwordsPOJO`) have the same defect and are left alone here.
- The announcement rule for the reputation command lives in `Messages` alone: `isAnnounced(COMMAND)` for the extra
  message, `isAnnouncedInReply()` for the reply which carries it. `Give` asks instead of re-deriving the complement.
- The select entries of the `messages states` command derive their id and name from `ThankType`, so the locale keys of
  the enum are not repeated. `Messages#toLocalizedString` keeps its literals, because the `@PropertyKey` annotation on
  `getSetting` validates them against the bundle.

Covered by tests: `MessagesTest` pins the announcement gating (defaults, per type, the command rule),
`MessagesPOJOTest` pins the request body handling.

## Summary

When a user receives reputation the bot posts a message into the channel where the reputation was
given, naming donor and receiver and stating the receiver's current reputation count as computed
from the guild's active reputation mode.

The message can be enabled per reputation type (`ThankType`). A single additional switch controls
whether announcements are deleted again after a short delay. Both live in a new section of
`/settings/edit/reputation` in the dashboard, and in `/messages states` on Discord.

Example output:

```
@Donor gave reputation to @Receiver.
@Receiver now has 42 reputation.
```

## Decisions taken

| Question | Decision |
| --- | --- |
| Message form | Plain text message in the channel where the reputation happened, permanent by default |
| Count shown | Mode based count only — `RepProfile#reputation()`, which honours `reputation_mode` and `reset_date` |
| Auto delete | One global on/off switch; when on, every announcement is deleted after a fixed 30 s |
| Storage | Flat boolean columns on the existing `message_states` table, exposed through the existing `messages` settings object |
| Existing `reaction_confirmation` | Replaced. Value is migrated into `announce_reaction` + `announce_delete`, old column dropped |
| Command reputation | Conditional on the reply visibility: ephemeral reply → separate announcement; public reply → the reply itself carries the count |
| Rollout | Strictly opt in. No guild may receive a bot message in a place it did not receive one before |

## Opt-in guarantee

The feature must not make the bot talk where it was silent. Every new switch therefore defaults to
off, and the one switch that does not (`announce_reaction`) exists only to carry the old
`reaction_confirmation` behaviour forward unchanged.

| Setting | Column default | Java default (`new Messages(settings)`) | Existing guild after migration | What changes for that guild |
| --- | --- | --- | --- | --- |
| `announce_reaction` | `TRUE` | `true` | value of `reaction_confirmation` | nothing new is sent — the reaction confirmation it already got becomes the announcement |
| `announce_answer` | `FALSE` | `false` | `false` | nothing |
| `announce_mention` | `FALSE` | `false` | `false` | nothing |
| `announce_fuzzy` | `FALSE` | `false` | `false` | nothing |
| `announce_embed` | `FALSE` | `false` | `false` | nothing |
| `announce_direct` | `FALSE` | `false` | `false` | nothing |
| `announce_command` | `FALSE` | `false` | `false` | nothing — neither an extra message nor a reworded reply |
| `announce_delete` | `TRUE` | `true` | value of `reaction_confirmation` | nothing — the reaction message kept its 30 s lifetime |

`announce_reaction` / `announce_delete` default to `TRUE` because that is exactly today's default
(`reaction_confirmation BOOLEAN DEFAULT TRUE`, `Messages(settings)` → `reactionConfirmation = true`).
A guild created after this change therefore behaves like a guild created before it. Both the column
default and the Java default must be kept in sync, because `message_states` rows are created lazily —
a guild that never touched a message setting has no row and is served by the Java defaults.

**The one content delta:** guilds that had the reaction confirmation enabled will see the count line
appended to a message they were already getting. No new message, same channel, same trigger — but
the wording changes. If even that must be opt in, the fallback is to migrate
`announce_reaction = false` and let those guilds lose the reaction message entirely, which trades a
wording change for a disappearing message. The concept assumes the wording change is acceptable.

## Behaviour

### Trigger point

`ReputationService#log(...)`
(`src/main/java/de/chojo/repbot/service/reputation/ReputationService.java:369`) is the single funnel
every reputation passes through, for every type, after all abuse-protection checks and after the
database insert succeeded. The announcement is sent there, next to the existing rank announcement
block, so it fires exactly once per persisted reputation entry and never for a rejected or duplicate
one.

Order inside `log(...)`:

1. `addReputation(...)` — abort if it returns `false` (`ALREADY_PRESENT`)
2. log-channel entry (unchanged)
3. mark message with the reaction (unchanged)
4. role update / rank announcement (unchanged)
5. **new: reputation announcement**

### Send logic

```java
private void announceReputation(
        RepGuild repGuild, Settings settings, Member donor, Member receiver,
        ReputationContext context, ThankType type) {
    var messages = settings.messages();
    if (!messages.isAnnounced(type)) return;

    var channel = context.getChannel();
    if (PermissionErrorHandler.assertAndHandle(
            repGuild, channel, localizer.context(LocaleProvider.guild(repGuild.guild())),
            configuration, "Reputation announcement", donor.getUser(), Permission.MESSAGE_SEND)) {
        return;
    }

    var action = channel.sendMessage(announcementMessage(repGuild.guild(), donor, receiver))
            .setAllowedMentions(Collections.emptyList());

    if (messages.isAnnounceDelete()) {
        action.delay(ANNOUNCEMENT_DELETE_SECONDS, TimeUnit.SECONDS).flatMap(Message::delete);
    }
    action.queue(RestAction.getDefaultSuccess(), ErrorResponseException.ignore(
            ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.UNKNOWN_CHANNEL,
            ErrorResponse.MISSING_PERMISSIONS, ErrorResponse.ILLEGAL_OPERATION_ARCHIVED_THREAD));
}

/** Localized announcement text. Public so the command reply can render the same message. */
public String announcementMessage(Guild guild, Member donor, Member receiver) {
    long count = guildRepository.guild(guild).reputation().user(receiver).profile().reputation();
    return localizer.localize(
            "listener.reputation.announcement", guild,
            Replacement.createMention("DONOR", donor),
            Replacement.createMention("RECEIVER", receiver),
            Replacement.create("COUNT", count));
}
```

- `ANNOUNCEMENT_DELETE_SECONDS = 30`, matching the value the reaction confirmation used.
- Mentions are suppressed (`setAllowedMentions(emptyList())`) as in `message.levelAnnouncement`;
  donor and receiver still render as mentions, they just do not ping. Note that today's reaction
  confirmation *does* ping the donor via `.mention(...)` — see [Open questions](#open-questions).
- `queue()` instead of `complete()`: nothing depends on the result, and `log(...)` runs on JDA
  event threads. (The neighbouring rank announcement uses `complete()`; not copied deliberately.)
- The count query is `RepMember#profile()`, one extra query per given reputation. It contains
  window functions for the rank; see [Open questions](#open-questions) for a cheaper variant.

### Count semantics

`RepProfile#reputation()` is the number the whole bot already treats as "the user's reputation": it
is computed from `general().reputationMode().dateInit()` plus `general().resetDate()`, so it
automatically matches the mode configured in the same settings page (total, rolling week, rolling
month, this week, this month) and the ranking output of `/top`. No new query semantics are
introduced.

### Command reputation is conditional

`/reputation give` already answers in the channel, so a separate announcement would either duplicate
it or be pointless. `announce_command` therefore means different things depending on
`command_reputation_ephemeral`:

| `command_reputation_ephemeral` | `announce_command` | Result |
| --- | --- | --- |
| `true` (reply only visible to the donor) | `true` | reply as today **plus** a public announcement |
| `true` | `false` | reply as today, nothing else — unchanged |
| `false` (reply visible to everyone) | `true` | **no** extra message; the reply itself uses the announcement text, so it carries the count |
| `false` | `false` | reply as today, unchanged |

Two places implement this:

- `Messages#isAnnounced(ThankType)` returns `announceCommand && commandReputationEphemeral` for
  `COMMAND`, so `ReputationService` never adds a second message to a publicly visible reply.
- `commands/reputation/handler/Give.java` picks its reply text:

```java
var messages = guild.settings().messages();
boolean ephemeral = messages.isCommandReputationEphemeral();
String reply = !ephemeral && messages.isAnnounceCommand()
        ? reputationService.announcementMessage(event.getGuild(), donor, receiver)
        : context.guildLocale(
                "command.reputation.give.message.success",
                Replacement.createMention("DONOR", donor),
                Replacement.createMention("RECEIVER", receiver));
event.reply(reply).mentionUsers(Collections.emptyList()).setEphemeral(ephemeral).queue();
```

Both branches read the same locale keys they read today unless the guild opted in, so a guild that
leaves `announce_command` off sees no difference at all.

`announce_delete` does not apply to the command reply — an interaction reply is not deleted, only
standalone announcements are.

## Data model

New patch `src/main/resources/database/postgresql/1/patch_54.sql`, and
`src/main/resources/database/version` bumped `1.53` → `1.54`.

```sql
ALTER TABLE repbot_schema.message_states
    ADD announce_reaction BOOLEAN DEFAULT TRUE  NOT NULL,
    ADD announce_answer   BOOLEAN DEFAULT FALSE NOT NULL,
    ADD announce_mention  BOOLEAN DEFAULT FALSE NOT NULL,
    ADD announce_fuzzy    BOOLEAN DEFAULT FALSE NOT NULL,
    ADD announce_embed    BOOLEAN DEFAULT FALSE NOT NULL,
    ADD announce_direct   BOOLEAN DEFAULT FALSE NOT NULL,
    ADD announce_command  BOOLEAN DEFAULT FALSE NOT NULL,
    ADD announce_delete   BOOLEAN DEFAULT TRUE  NOT NULL;

-- Carry the old reaction confirmation over: guilds that had it enabled keep an announcement on
-- reaction reputation, and keep the 30s auto delete that message had.
UPDATE repbot_schema.message_states
SET announce_reaction = reaction_confirmation,
    announce_delete   = reaction_confirmation;

ALTER TABLE repbot_schema.message_states
    DROP COLUMN reaction_confirmation;
```

See [Opt-in guarantee](#opt-in-guarantee) for why `announce_reaction` and `announce_delete` are the
only columns defaulting to `TRUE`, and why the Java defaults in `Messages(Settings)` must mirror the
column defaults exactly.

Consequence worth knowing: `announce_delete` is global, so a guild that inherited
`announce_delete = true` from its reaction confirmation will also get auto-deleted announcements for
any type it enables later, until it turns the switch off.

## Backend changes

### `web/pojo/settings/sub/MessagesPOJO.java`

- remove `reactionConfirmation`
- add `announceReaction`, `announceAnswer`, `announceMention`, `announceFuzzy`, `announceEmbed`,
  `announceDirect`, `announceCommand`, `announceDelete` with `isX()` getters
- constructor signature grows accordingly (callers: `Messages` super call, `MessagesRoute`
  old-value snapshot)

### `dao/access/guild/settings/sub/Messages.java`

- `build(Settings, Row)` reads the new columns
- the no-arg-ish default constructor `Messages(Settings)` must mirror the column defaults:
  `announceReaction = true`, `announceDelete = true`, all others `false`
- one setter per column, using the existing `set(parameter, binder)` helper against `message_states`
- `apply(MessagesPOJO)` writes only changed values, as today
- `toLocalizedString()` / `prettyString()` list the new switches
- new dispatcher, mirroring `Bypass#isEnabled(ThankType)`
  (`dao/access/guild/settings/sub/integrationbypass/Bypass.java:119`):

```java
public boolean isAnnounced(ThankType type) {
    return switch (type) {
        case FUZZY -> announceFuzzy;
        case MENTION -> announceMention;
        case ANSWER -> announceAnswer;
        case DIRECT -> announceDirect;
        case REACTION -> announceReaction;
        case EMBED -> announceEmbed;
        // A publicly visible command reply already is the announcement, see "Command reputation".
        case COMMAND -> announceCommand && commandReputationEphemeral;
    };
}
```

### `dao/access/guild/settings/Settings.java`

`messages()` selects the new columns (`Settings.java:181`).

### `service/reputation/ReputationService.java`

Add `announceReputation(...)` and the public `announcementMessage(...)` as described above, and call
`announceReputation(...)` from `log(...)`.

### `commands/reputation/handler/Give.java`

Pick the reply text as described in [Command reputation is conditional](#command-reputation-is-conditional).
`ReputationService` is already injected here, so no new wiring.

### `listener/ReactionListener.java`

- delete the `guildSettings.messages().isReactionConfirmation()` block that sends
  `listener.reaction.confirmation` (`ReactionListener.java:145-161`) — the central announcement
  replaces it
- re-gate the removal message (`ReactionListener.java:195`) on `messages().isAnnounceReaction()`
- keep the `PermissionErrorHandler` MESSAGE_SEND pre-check that currently guards the submit path, or
  move it down to the removal message only; decide during implementation

## Web API

`web/routes/v1/settings/sub/MessagesRoute.java`:

- update the old-value snapshot in `updateMessagesSettings`
- drop `POST v1/settings/messages/reactionconfirmation`
- add, all `Role.GUILD_ADMIN`, body `Boolean`, each recording an audit entry via
  `session.recordChange("messages.<name>", old, new)`:

```
POST v1/settings/messages/announcereaction
POST v1/settings/messages/announceanswer
POST v1/settings/messages/announcemention
POST v1/settings/messages/announcefuzzy
POST v1/settings/messages/announceembed
POST v1/settings/messages/announcedirect
POST v1/settings/messages/announcecommand
POST v1/settings/messages/announcedelete
```

`SettingsPOJO` needs no change — it already carries `settings.messages()`.

## Dashboard

### New section in `/settings/edit/reputation`

`frontend/src/views/settings/ReputationView.vue` gains a third block:

```
Reputation Mode
──────────────
Reset Date            (only when mode = TOTAL)
──────────────
Reputation Types
──────────────
Reputation Messages   ← new
```

New component `frontend/src/views/settings/reputationview/ReputationAnnouncementSettings.vue`:

- section heading + description
- one `ReputationTypeToggle` per thank type, reusing the existing type labels
  (`general.reputation.types.<type>.label`) with announcement-specific descriptions
- the command toggle stays visible whenever command reputation is active, but its description follows
  `session.settings.messages.commandReputationEphemeral`: with an ephemeral reply it announces
  ("Send a message when reputation is given via command."), with a public reply it annotates
  ("Add the reputation count to the command reply."). Two locale keys,
  `announcement.types.command.ephemeral` and `announcement.types.command.public`, selected in a
  computed property
- one `ReputationTypeToggle` for `announceDelete`
- writes through the same helper pattern as `ReputationTypeSettings.vue`
  (`updateSetting(key, value, 'messages')`, optimistic local update + revert on error). That helper
  currently lives inside `ReputationTypeSettings.vue`; extract it into a small composable
  (e.g. `reputationview/useSettingUpdate.ts`) so both components share it.

The helper derives the api client method name as `update` + `Messages` + PascalCase(key), so the
client methods must be named exactly `updateMessagesAnnounceReaction`, …,
`updateMessagesAnnounceDelete`.

### Other frontend touch points

- `frontend/src/api/types.ts` — `MessagesPOJO`: remove `reactionConfirmation`, add the eight flags
- `frontend/src/api/index.ts` — remove `updateMessagesReactionConfirmation`, add the eight
  `updateMessagesAnnounceX(active: boolean)` methods
- `frontend/src/views/settings/reputationview/ReputationTypeSettings.vue` — remove the nested
  "Reaction Confirmation" toggle (it moves into the new section)
- `frontend/src/views/presets/DefaultPreset.vue` — `reactionConfirmation: true` becomes
  `announceReaction: true, announceDelete: true`
- `frontend/src/composables/useSession.ts` — no change, `updateMessagesSettings` is generic

### Frontend locale keys

`frontend/src/locales/en-US.json` is authoritative, the other 19 files follow (Crowdin):

```json
"general": { "reputation": { "announcement": {
  "label": "Reputation Messages",
  "description": "Post a message when reputation is given, including the receiver's current count.",
  "types": {
    "reaction": "Announce reputation given by reaction.",
    "answer":   "Announce reputation given in an answer.",
    "mention":  "Announce reputation given by mention.",
    "fuzzy":    "Announce reputation given by fuzzy matching.",
    "embed":    "Announce reputation given through the embed.",
    "direct":   "Announce reputation given directly to a single user.",
    "command": {
      "ephemeral": "Send a message when reputation is given via command.",
      "public":    "Add the reputation count to the command reply."
    }
  },
  "delete": {
    "label": "Delete after 30 seconds",
    "description": "Remove announcements again 30 seconds after they were sent."
  }
} } }
```

## Discord command parity

`commands/messages/handler/States.java` is the Discord-side editor for `message_states`. It gains
one select entry per new switch (8 → 10 entries total, well under Discord's 25 option limit).

To keep the number of new locale keys small:

- option names reuse the existing `thankType.<type>.name` keys
- all seven type entries share one choice pair
  `command.messages.states.message.choice.announce.true` / `.false` and one description
  `command.messages.states.message.option.announce.description`
- `announce_delete` gets its own name, description and choice pair

`Messages#toLocalizedString()` output (used in the settings embed) is extended with the new values.

## Backend localization

New key, English text in `locale_en_US.properties`, and present in **all** 20 `locale_*.properties`
files — `TestLocalization#checkKeys` collects the union of all bundles' keys and fails on any bundle
missing one, so English placeholders must be added everywhere until Crowdin returns translations:

```properties
listener.reputation.announcement=%DONOR% gave $words.reputation$ to %RECEIVER%.\n%RECEIVER% now has **%COUNT%** $words.reputation$.
```

Constraints from `TestLocalization`:

- every `%REPLACEMENT%` used in the English value must appear in every translation
- every `$locale.key$` reference used in the English value must appear in every translation
- keys ending in `.description` must be ≤ 100 characters

`listener.reaction.confirmation` becomes unused and should be removed from all bundles;
`listener.reaction.removal` stays (still used for reaction removal).

## Test / verification

- `./gradlew testLocale` — key parity and description length across all bundles
- `./gradlew testDatabase` — `TestSQL#verifyDeployment` applies `patch_54.sql` against a fresh
  Postgres container, `verifyStructure` checks the patch set
- `./gradlew spotlessApply` before committing (Palantir format + AGPL headers on all new
  Java/TS/Vue files)
- manual: with a test guild, enable one type at a time and confirm exactly one announcement per
  persisted reputation, the correct count for each reputation mode, and no message for rejected
  reputations (cooldown, self vote, limits)
- manual, opt-in regression: run a guild through the migration with all message settings untouched
  and with `reaction_confirmation = false`, then give reputation by every type and confirm the bot
  writes nothing it did not write before the update
- manual, command matrix: all four combinations of `command_reputation_ephemeral` ×
  `announce_command` from the table above

## Docs

`docs/configuration.md` documents the reputation types and the announcement channel feature; it
should get a short paragraph on reputation announcements next to the level-up message section
(around `docs/configuration.md:31`).

## Risks and edge cases

- **Multiple announcements per message.** `abuse_protection.max_message_reputation` (default 3)
  allows one message to hand out up to three reputations, and `log(...)` runs per donor→receiver
  pair, so a single thank message can produce up to three announcements. See open questions.
- **Chatty channels.** Enabling all types in a busy guild produces one bot message per reputation.
  The auto-delete switch is the mitigation; a dedicated announcement channel is a possible
  follow-up (see below).
- **Permissions.** Channels where the bot may read but not write: handled by the
  `PermissionErrorHandler` pre-check, which already notifies guild staff once instead of spamming
  errors.
- **Extra query per reputation.** One `profile()` query per given reputation on top of the existing
  inserts.
- **Threads / archived threads.** Sending into an archived thread fails;
  `ILLEGAL_OPERATION_ARCHIVED_THREAD` is ignored explicitly.

## Open questions

1. **Should the receiver be pinged?** The old reaction confirmation pinged the donor
   (`.mention(event.getUser())`); the rank announcement suppresses all mentions. The concept assumes
   suppressed mentions — worth confirming.
2. **One announcement per reputation or one per message?** With `max_message_reputation > 1`, should
   the three entries collapse into a single message listing all receivers, or stay as three
   messages? Collapsing needs a different hook point than `log(...)`.
3. **Cheaper count query.** `profile()` computes rank via window functions. If the announcement
   turns out to be hot, add a narrow "reputation count for mode" query to `RepMember` and use that
   instead.
4. **Dedicated announcement channel.** Rank announcements can be routed to a fixed channel
   (`announcements.same_channel` / `channel_id`). Should reputation announcements get the same
   option later, and if so should they reuse that channel setting or their own?
5. **Premium gating.** `log_channel` and `integration_bypass` are SKU-gated; reputation
   announcements are assumed free like rank announcements.

## Touched files

```
src/main/resources/database/version                                    (1.53 -> 1.54)
src/main/resources/database/postgresql/1/patch_54.sql                  (new)
src/main/resources/locale_*.properties                                 (20 files)
src/main/java/de/chojo/repbot/web/pojo/settings/sub/MessagesPOJO.java
src/main/java/de/chojo/repbot/dao/access/guild/settings/sub/Messages.java
src/main/java/de/chojo/repbot/dao/access/guild/settings/Settings.java
src/main/java/de/chojo/repbot/service/reputation/ReputationService.java
src/main/java/de/chojo/repbot/listener/ReactionListener.java
src/main/java/de/chojo/repbot/commands/reputation/handler/Give.java
src/main/java/de/chojo/repbot/commands/messages/handler/States.java
src/main/java/de/chojo/repbot/web/routes/v1/settings/sub/MessagesRoute.java
frontend/src/api/types.ts
frontend/src/api/index.ts
frontend/src/views/settings/ReputationView.vue
frontend/src/views/settings/reputationview/ReputationAnnouncementSettings.vue   (new)
frontend/src/views/settings/reputationview/useSettingUpdate.ts                  (new)
frontend/src/views/settings/reputationview/ReputationTypeSettings.vue
frontend/src/views/presets/DefaultPreset.vue
frontend/src/locales/*.json                                            (20 files)
docs/configuration.md
```
