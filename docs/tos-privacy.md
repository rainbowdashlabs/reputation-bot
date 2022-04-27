# Terms of Service

These Terms of Service and Privacy regulation only apply to the public hosted instance which can be invited with this
[link](https://discord.com/api/oauth2/authorize?client_id=871322553698906142&permissions=1342532672&scope=bot%20applications.commands)

These terms only apply for the instances with the IDs `871322553698906142` and
`834843896579489794`.

## Terms

By using the Reputation Bot, you are agreeing to be bound by these terms of service, all applicable laws and 
regulations,
and
agree that you are responsible for compliance with any applicable local laws. If you do not agree with any of these
terms, you are prohibited from using Reputation Bot.

## Licence

This service is provided "as is". You are not allowed...

- to use this service with any commercial purpose.
- to restrict access to this service for any user.
- abuse or try to bypass any limitations enforced by this service.

This Service is a service with limited availability. I will always try to maintain it as good as possible. This service
may be shutdown at any time. I shall restrict the general or personal usage of the Bot at any time.

## Limitations

In no event shall I be liable for any damages or loss of data arising out of the use or inability to this Service.

# Privacy

Your privacy is important, it always is.    
Your data will not be shared publicly or with third parties. Only data which is required to maintain this service is
stored. I do my best to keep your data secured and safe at any time. By using this Service you accept that I may process
every personal data I need to provide this service in the best possible quality.

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
12 hours

### Guild Settings

**Discord data:**  
`guild id`, `channel id`, `role id`, `emote id`

**What:**  
We store the guild id, together with the settings of the guild

**Why:**  
`guild id`: The guild id is required to identify the guild the settings are saved for.  
`channel id`: The channels where reputation is allowed to be given.    
`role id`: The roles which users get assigned when reaching a reputation goal.  
`emote id`: The emotes which give reputation or should be added on reputation receival.

**Deletion:**  
14 days after removal of the bot from the guild

### Reputation Log

**Discord Data:**  
`guild id`, `channel id`, `message id`, `user id`

**Why:**  
The reputation log is required to compute the total and rolling reputation amount of users. It contains one entry 
per given reputation.

`guild id`, `channel id`, `message id`: This data is required to create jump links to the message which triggered 
a reputation donation. This is important for server owners or managers to keep track of the bot behaviour.  
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

## Deleting or requesting your data 

If you want to delete or request your data take a look at the [FAQ](faq.md#how-can-i-request-my-data).
