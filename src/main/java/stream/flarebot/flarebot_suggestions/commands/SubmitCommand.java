package stream.flarebot.flarebot_suggestions.commands;

import com.walshydev.jba.commands.Command;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot_suggestions.Suggestion;
import stream.flarebot.flarebot_suggestions.SuggestionsManager;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SubmitCommand implements Command {

    @Override
    public void onCommand(User user, MessageChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 5) {
            String msg = Arrays.stream(args).collect(Collectors.joining(" "));
            if (msg.length() > 500) {
                user.openPrivateChannel().queue(pm -> pm.sendMessage("Woah there! That is a very long suggestion! " +
                        "Please keep it at a maximum of 500 " +
                        "characters.\n\n" + msg).queue(), fail ->
                        channel.sendMessage(user.getAsMention() + " I couldn't DM you! Check your privacy settings!").queue());
                // ignore a fail
                return;
            }

            SuggestionsManager.getInstance().submitSuggestion(new Suggestion(user, msg), false);
        } else {
            channel.sendMessage(user.getAsMention() + " The suggestion needs to be at least 5 words!\n" +
                    "**Usage**: `submit <suggestion>`").queue();
        }
    }

    @Override
    public String getCommand() {
        return "submit";
    }

    @Override
    public String getDescription() {
        return "Submit a suggestion";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"submit", "s"};
    }

    @Override
    public boolean deleteMessage() {
        return true;
    }
}
