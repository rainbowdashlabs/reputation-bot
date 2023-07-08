### What roles will be managed by the bot?

The bot will manage all roles which are registered as reputation roles.  
The bot will always check the user roles when a user receives reputation. If you add a new role, the bot will adjust the
user roles, when these users receive reputation again.

When the bot updates user roles it will first remove all roles registered as reputation roles from the user.  
It will then add the new reputation role to the user.
