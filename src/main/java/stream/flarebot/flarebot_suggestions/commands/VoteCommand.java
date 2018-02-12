package stream.flarebot.flarebot_suggestions.commands;

import stream.flarebot.flarebot_suggestions.DatabaseManager;
import stream.flarebot.flarebot_suggestions.Suggestion;
import stream.flarebot.flarebot_suggestions.SuggestionsManager;
import com.walshydev.jba.commands.Command;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class VoteCommand implements Command {

    @Override
    public void onCommand(User user, MessageChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            int id;
            try {
                id = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                channel.sendMessage("Invalid ID!").queue();
                return;
            }

            Suggestion s = DatabaseManager.getSuggestion(id);
            if (s != null) {
                if (s.getVotedUsers().contains(user.getIdLong())) {
                    channel.sendMessage("You can't vote twice you silly goose!").queue();
                } else {
                    SuggestionsManager.getInstance().voteOnSuggestion(id, user.getIdLong());
                    channel.sendMessage("You have voted for suggestion #" + id).queue();
                }
            } else {
                channel.sendMessage("Invalid suggestion ID! Please refer to the number at the start of the title in " +
                        "the suggestion embed").queue();
            }
        } else
            channel.sendMessage("**Usage**: `vote <id>`").queue();
    }

    @Override
    public String getCommand() {
        return "vote";
    }

    @Override
    public String getDescription() {
        return "Vote on a suggestion";
    }
}
