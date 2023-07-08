### What is a context?

In order to resolve targets of a message and check which users are qualified to receive reputation, we build a context.
This context contains all users which are qualified to receive reputation and is build for every message when the bot
detects a thankword.

If you are interested in how it works, here is a short explanation:  
To build the message context we search for the oldest message within a fixed time (This is the max message age. 30
minutes on default).  
After we found the older message of the reputation donor we collect all users which have written a message after the
oldest message. If we haven't found any message within the max message age we add the users of the last 10 messages (The
amount depends on the server settings) to the context. We do this because often users come back after some time and the
thank duration already ran out.  
Finally we add also the users which share a voice channel with the message author and the users which shared a voice
channel with the users withing the max message age.

You can read more about this mechanic on our [abuse protection page](abuse_protection.md)
