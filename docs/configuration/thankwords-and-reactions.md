---
title: Thankwords and reactions
sidebar_position: 2
---

Thankwords and reactions are the triggers for reputation. A thankword in a message starts a
mention, answer, fuzzy or embed reputation. A reaction with a reputation emoji starts a reaction
reputation. How each way works is on [Giving reputation](../reputation/giving-reputation.md).

## Thankwords

A new server has no thankwords. Until you add some, the bot finds no thanks in messages, and the
**Problems** tab of the dashboard reminds you of that.

You manage thankwords in the [dashboard](../getting-started/dashboard.md) under **Settings > Thankwords**.
Type a word into the field and click **Add Thankword**. The list below shows your thankwords, each
with a button to delete it.

### How thankwords are matched

Each thankword is a regular expression. The bot looks for it as a whole word, ignoring upper and
lower case. So `thanks` matches "Thanks a lot!", but not "thanksgiving".
This works in every script. In Chinese and Japanese, which are written without spaces, a thankword also counts
when it stands between other characters, so `ありがとう` matches "本当にありがとうございます".

You can use simple patterns. `thank[sx]?` matches "thank", "thanks" and "thanx".

A thankword you add yourself can't contain spaces or any of these characters: `(` `)` `{` `}` `*`
`.` `#` `<` `>`. The dashboard tells you when a word contains one of them, or when you already have
the word.

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

Load a set under **Settings > Thankwords > Default Thankwords**. Click a language to add its words
to the ones you already have. The number next to each language tells you how many words it has.

### Testing a message

Under **Settings > Thankwords > Check a Message**, paste a message and click **Check**. The dashboard
tells you whether the bot finds a thankword in it, and which of your thankwords match. It looks for
thankwords the same way the bot does when someone writes the message.

The check only looks at the text. Whether the message gives reputation also depends on whom it
thanks, see [Giving reputation](../reputation/giving-reputation.md), and on your other settings.

`/thankwords check <message>` checks a real message: pass the id or the link of a message in the
channel where you run the command. It tells you whether the message would give reputation, how it
would be given and to whom.

For a message that was already handled, the [analyzer log](./logs-and-diagnostics.md#analyzer-log)
tells you what actually happened.

## Reactions

A user gives reputation by reacting to a message with a reputation emoji. The author of the
message receives it. **Reaction Reputation** must be turned on, see
[Giving reputation](../reputation/giving-reputation.md).

You manage the emojis in the dashboard under **Settings > Reactions**.

### Main reaction

Every server has one main reaction. It is 🏅 until you change it. The bot also uses it to mark
messages that gave reputation.

Click the emoji under **Main Reaction** to open the emoji picker. You can pick a standard emoji or a
custom emoji of your server. Your server's own emojis are listed first. The bot needs the **Manage
Expressions** permission to list them.

### Additional reactions

Under **Additional Reactions**, click **Add Reaction** to add more emojis that also give
reputation. Additional reactions are a [supporter feature](../supporter.md). They only count while
the feature is active on your server.
