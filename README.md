# FlareBot Suggestions

FlareBot suggestions is a small bot built for the FlareBot official Discord, it allows for our users to create suggestions for us to easily see and it also allows for other members to vote on suggestions they want! This creates a much more organised and prioritised list of what we should work on.

## How it works
Users have two commands they can use, `submit` and `vote` these allow the user to submit a suggestion and vote on existing suggestions. Note that you need to put **at least** 5 words in the submission and a maximum of 500 characters or it will return a usage. To vote simply use the ID which is displayed in the suggestion embed.  
Embed structure:  
(id) - Suggested by Username#discrim  
Suggestion

Votes
X

## Developers
You will need to do two things before you can run the program, one is make a `config.json`. There is a sample one below which you can copy and use, just put that in the root directory and you will be able to use the bot. Two, you will need to make a table called `suggestions` and the query to create that is also below. You should also note there is a `Constants.java` file which holds information relating to the guild, channel and staff role. Modify these values to test this yourself, make sure to not commit them though!

```json
{
  "token": "<Discord bot token>",
  "prefix": "-",

  "mysql": {
    "user": "root",
    "password": "password",
    "host": "localhost",
    "database": "flarebot_suggestions"
  }
}
```

```sql
CREATE TABLE `suggestions` (
  `suggestion_id` int(10) NOT NULL AUTO_INCREMENT,
  `suggested_by` varchar(20) NOT NULL,
  `suggested_by_tag` varchar(40) NOT NULL,
  `suggestion` text NOT NULL,
  `voted_users` text NOT NULL,
  `message_id` varchar(20) NOT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`suggestion_id`)
)
```
