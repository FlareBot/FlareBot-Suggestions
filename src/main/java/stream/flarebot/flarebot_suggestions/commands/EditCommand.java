package stream.flarebot.flarebot_suggestions.commands;

import com.walshydev.jba.commands.Command;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot_suggestions.DatabaseManager;
import stream.flarebot.flarebot_suggestions.FlareBotSuggestions;
import stream.flarebot.flarebot_suggestions.Suggestion;
import stream.flarebot.flarebot_suggestions.SuggestionsManager;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EditCommand implements Command {

    @Override
    public void onCommand(User user, MessageChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 2) {
            int id;
            try {
                id = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                channel.sendMessage(user.getAsMention() + " Invalid ID!").queue();
                return;
            }

            Suggestion s = DatabaseManager.getSuggestion(id);
            if (s != null) {
                if (s.getSuggestedBy() == user.getIdLong() || FlareBotSuggestions.getInstance().isStaff(user)) {
                    String msg = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                    if (msg.length() > 500) {
                        user.openPrivateChannel().queue(pm -> pm.sendMessage("Woah there! That is a very long suggestion! " +
                                "Please keep it at a maximum of 500 " +
                                "characters.\n\n" + msg).queue(), fail ->
                                channel.sendMessage(user.getAsMention() + " I couldn't DM you! Check your privacy settings!").queue());
                        // ignore a fail
                        return;
                    }

                    s.setSuggestion(msg);
                    SuggestionsManager.getInstance().editSuggestionMessage(s);
                    channel.sendMessage(user.getAsMention() + " Edited #" + s.getId()).queue();
                } else
                    channel.sendMessage(user.getAsMention() + " You can't edit a suggestion that isn't yours!").queue();
            } else {
                channel.sendMessage(user.getAsMention() + " Invalid suggestion ID! Please refer to the number at the start of the title in " +
                        "the suggestion embed").queue();
            }
        } else
            channel.sendMessage(user.getAsMention() + " **Usage**: `edit <id> <suggestion>`").queue();
    }

    @Override
    public String getCommand() {
        return "edit";
    }

    @Override
    public String getDescription() {
        return "Edit your suggestion";
    }

    @Override
    public boolean deleteMessage() {
        return true;
    }
}
