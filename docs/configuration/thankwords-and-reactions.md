---
title: Thankwords and reactions
sidebar_position: 2
---

Thankwords and reactions are the triggers for reputation. A thankword in a message starts a
mention, answer, fuzzy or embed reputation. A reaction with a reputation emoji starts a reaction
reputation. How each way works is on [Giving reputation](../reputation/giving-reputation.md).

## Thankwords

A new server has no thankwords. Until you add some, the bot finds no thanks in messages, and the
**Problems** page of the dashboard reminds you of that.

You manage thankwords on the **Thankwords** page of the [dashboard](../getting-started/dashboard.md)
settings, or with the `/thankwords` command.

### How thankwords are matched

Each thankword is a regular expression. The bot looks for it as a whole word, ignoring upper and
lower case. So `thanks` matches "Thanks a lot!", but not "thanksgiving".

You can use simple patterns. `thank[sx]?` matches "thank", "thanks" and "thanx".

A thankword you add yourself can't contain spaces or any of these characters: `(` `)` `{` `}` `*`
`.` `#` `<` `>`. With the command it can be at most 25 characters long.

:::warning
Thankwords in a script other than the Latin alphabet, for example Cyrillic, Greek, Chinese,
Japanese or Korean, are currently only found when they stand alone on a line. "спасибо" on its
own gives reputation, "спасибо друг" doesn't.
:::

### Default thankwords

The bot comes with a set of thankwords for these languages:

| Language | Thankwords |
|---|---|
| English | `thx`, `thank[sx]?`, `ty`, `tyvm`, `tysm` |
| Deutsch | `dan[kg]e?`, `dankeschön` |
| Español | `agradezco`, `agradecid[oa]s?`, `gracias` |
| Français | `(?:re)?merci` |
| Italiano | `grazie`, `graz`, `ringrazio` |
| Nederlands | `dank(?:je)?(?:wel)?`, `bedankt` |
| Norsk | `takk`, `tusen takk` |
| Polski | `dzięk[iu](?:ję)?`, `dziękuję`, `thx` |
| Português | `obrigad[oa]s?`, `obrigadíssim[oa]`, `agradecid[oa]`, `grat[oa]` |
| Suomi | `kiit(?:os\|oksia)`, `kiitti` |
| Svenska | `tack`, `tackar` |
| Türkçe | `teşekkür(?:ler)?`, `sağol(?:un)?`, `thx` |
| Čeština | `díky`, `děkuj[iu]`, `dík` |
| Ελληνικά | `ευχαριστώ`, `ευχαριστ[οω]`, `ευχ` |
| Русский | `спс`, `спасиб[ао]?`, `благодарю` |
| Українська | `дяк[уую]ю?`, `спс`, `спасибі` |
| 中文 | `谢+`, `感谢`, `多谢`, `thx` |
| 日本語 | `ありがと[うゆ]?(?:ございま[すした])?`, `あざ[すした]`, `感謝` |
| 한국어 | `감사(?:합니다)?`, `고마워(?:요)?`, `ㄱㅅ` |

Load a set with `/thankwords loaddefault <language>`, or with **Default Thankwords** on the
Thankwords page. `/thankwords loaddefault` without a language lists the available languages. Loading
a set adds its words to the ones you already have.

### Testing a message

`/thankwords check <message>` tells you whether a message would give reputation, how it would be
given and to whom. Pass the id or the link of a message in the channel where you run the command.

For a message that was already handled, the [analyzer log](./logs-and-diagnostics.md#analyzer-log)
tells you what actually happened.

### Commands

All `/thankwords` commands need the Administrator permission by default.

| Command | What it does |
|---|---|
| `/thankwords add <pattern>` | Adds a thankword. |
| `/thankwords remove <pattern>` | Removes a thankword. |
| `/thankwords list` | Lists your thankwords. |
| `/thankwords loaddefault [language]` | Adds the default thankwords of a language. |
| `/thankwords check <message>` | Checks whether a message would give reputation. |

## Reactions

A user gives reputation by reacting to a message with a reputation emoji. The author of the
message receives it. Reaction reputation must be turned on, see
[Giving reputation](../reputation/giving-reputation.md).

You manage the emojis on the **Reactions** page of the dashboard settings, or with the `/reactions`
command.

### Main reaction

Every server has one main reaction. It is 🏅 until you change it. The bot also uses it to mark
messages that gave reputation.

You can pick a standard emoji or a custom emoji of your server. Set it with
`/reactions main <emote>`. The command takes the emoji itself, the name of a custom emoji or its id.

### Additional reactions

You can add more emojis that also give reputation. Additional reactions are a
[supporter feature](../supporter.md). They only count while the feature is active on your server.

### Commands

All `/reactions` commands need the Administrator permission by default.

| Command | What it does |
|---|---|
| `/reactions main <emote>` | Sets the main reaction. |
| `/reactions add <emote>` | Adds an additional reaction. |
| `/reactions remove <emote>` | Removes an additional reaction. |
| `/reactions info` | Shows the main reaction and the additional reactions. |
