### A user has left the server but still shows up in top command

The reputation of a user will be removed 14 days after the user has left the server.

The bot does not check if a user is still on the server when the toplist is retrieved. That is the reason why those
users are still in the top list.

You can remove all reputation of a single user with the `prune user <userid>` command.  
Alternatively you can remove reputation of all users which are no longer on this guild with the `prune guild` command.
