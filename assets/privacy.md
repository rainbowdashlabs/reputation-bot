# Privacy

Your privacy is important, it always is.    
Your data will not be shared publicly or with third parties. Only data which is required to maintain this service is
stored. I do my best to keep your data secured and safe at any time. By using this Service you accept that I may process
every personal data I need to provide this service in the best possible quality.

These statements only apply for the instance with the ID `{{ bot_id }}`.

## What we store:

This list is without any guarantees for completeness or currency

### Users in voice channels

**Discord data:**  
`user id`, `guild id`  

**What:**  
We store the `user ids` of users which shared a voice channel in a guild recently.

**Why:**  
We do this to allow users to give users reputation which shared a voice channel with them recently.  

**Deletion:**  
24 hours

### Reputation

**Discord data:**  
`guild id`, `user id`

**What:**  
We store the total and rolling reputation of users. We also store manual reputation offsets.

**Why:**  
`guild id`: Required to identify the guild for which the reputation is stored.  
`user id`: Required to identify the user for which the reputation is stored.  
`reputation offset`: Required to allow server administrators to manually adjust the reputation of users.

**Deletion:**  
14 days after the user left the guild.

### Reputation Log

**Discord Data:**  
`guild id`, `channel id`, `message id`, `user id`

**Why:**  
The reputation log is required to compute the total and rolling reputation number of users. It contains one entry 
per given reputation.

`guild id`, `channel id`, `message id`: This data is required to create jump links to the message which triggered 
a reputation donation. This is important for server owners or managers to keep track of the bot behavior.  
`user id`: The user id of the receiver and donor. We require the donor to prevent the users from backthanking and to 
enforce the cooldown between users. The receiver is required to compile leaderboards.

**Deletion:**  
Instantly after deletion of the message or the trigger which triggered the reputation donation.
14 days after the receiver left the guild.
14 days after the donor left the guild the donor id will be removed from the reputation entry.

### GDPR data

**Discord Data:**  
`user id`

**Why:**  
We save this data to keep track of data requests.

`user id`: We save the id of the user who requested his data. This is required to track 
if the user received the data already and to facilitate some rate limit to avoid users requesting their data over 
and over again to overload our system.

**Deletion:**  
90 days after the receiver received their data.

### Guild Settings & Audit Log

**Discord data:**  
`guild id`, `channel id`, `role id`, `emote id`, `user id`

**What:**  
We store the guild id, together with the settings of the guild. We also store an audit log of setting changes.

**Why:**  
`guild id`: The guild id is required to identify the guild the settings are saved for.  
`channel id`: The channels where reputation is allowed to be given.    
`role id`: The roles which users get assigned when reaching a reputation goal.  
`emote id`: The emotes which give reputation or should be added on reputation receival.  
`user id`: Required for the audit log to track who changed settings.

**Deletion:**  
Settings: 14 days after removal of the bot from the guild.  
Audit Log: 14 days after the user who made the change left the guild.

### Verification and Purchases

**Discord Data:**  
`user id`, `guild id`

**Personal Data:**  
`hashed email`

**What:**  
We store a hashed version of your email address for verification and Ko-fi purchase tracking. We also store transaction details.

**Why:**  
`hashed email`: Required to verify Ko-fi purchases without storing your actual email address.  
`user id`: Required to link the purchase or verification to your Discord account.  
`guild id`: Required to link a purchase to a specific guild if applicable.

**Deletion:**  
Upon user data deletion request.

### Voting

**Discord Data:**  
`user id`

**What:**  
We store voting history and tokens earned from voting on botlists.

**Why:**  
`user id`: Required to track who voted and to award tokens to the correct user.  
`tokens`: Earned through voting and can be used for features.

**Deletion:**  
Upon user data deletion request.

### Cleanup Tasks

**Discord Data:**  
`user id`, `guild id`

**What:**  
We store scheduled tasks for data removal.

**Why:**  
To ensure that data is deleted as promised (e.g., after leaving a server).

**Deletion:**  
14 days after the task has been executed.

### User Settings

**Discord Data:**  
`user id`, `guild id`

**What:**  
Personal settings for the bot (e.g., your preferred guild for voting).

**Why:**  
To provide a personalized experience and store your preferences.

**Deletion:**  
Upon user data deletion request.
