package stream.flarebot.flarebot_suggestions.commands;

import com.walshydev.jba.commands.Command;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot_suggestions.Constants;
import stream.flarebot.flarebot_suggestions.DatabaseManager;
import stream.flarebot.flarebot_suggestions.Suggestion;
import stream.flarebot.flarebot_suggestions.SuggestionsManager;

public class SuggestionCommand implements Command {

    @Override
    public void onCommand(User user, MessageChannel channel, Message message, String[] args, Member member) {
        if (channel.getIdLong() == Constants.SUGGESTIONS_CHANNEL) return;

        if (args.length == 1) {
            int id;
            try {
                id = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                channel.sendMessage(user.getAsMention() + " Invalid ID!").queue();
                return;
            }

            Suggestion s = DatabaseManager.getSuggestion(id);
            if (s != null) {
                channel.sendMessage(SuggestionsManager.getInstance().getSuggestionEmbed(s).build()).queue();
            } else {
                channel.sendMessage(user.getAsMention() + " Invalid suggestion ID! Please refer to the number at the start of the title in " +
                        "the suggestion embed").queue();
            }
        } else
            channel.sendMessage(user.getAsMention() + " **Usage**: `suggestion <id>`").queue();
    }

    @Override
    public String getCommand() {
        return "suggestion";
    }

    @Override
    public String getDescription() {
        return "Get a suggestion";
    }

    @Override
    public boolean deleteMessage() {
        return true;
    }
}
