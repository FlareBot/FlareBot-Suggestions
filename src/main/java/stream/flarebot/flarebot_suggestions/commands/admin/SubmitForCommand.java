package stream.flarebot.flarebot_suggestions.commands.admin;

import com.walshydev.jba.commands.Command;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot_suggestions.FlareBotSuggestions;
import stream.flarebot.flarebot_suggestions.Suggestion;
import stream.flarebot.flarebot_suggestions.SuggestionsManager;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SubmitForCommand implements Command {

    @Override
    public void onCommand(User user, MessageChannel channel, Message message, String[] args, Member member) {
        if (!FlareBotSuggestions.getInstance().isStaff(user)) return;
        if (args.length >= 3) {
            long id;
            try {
                id = Long.parseLong(args[0].replaceAll("[^\\d]", ""));
            } catch (NumberFormatException e) {
                channel.sendMessage(user.getAsMention() + " I couldn't quite get the user! Use their ID or mention them!")
                        .queue();
                return;
            }

            Member m = FlareBotSuggestions.getInstance().getSuggestionsChannel().getGuild().getMemberById(id);
            if (m == null) {
                channel.sendMessage(user.getAsMention() + " I couldn't find that user! Make sure they're in this server!")
                        .queue();
                return;
            }

            String msg = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
            if (msg.length() > 500) {
                user.openPrivateChannel().queue(pm -> pm.sendMessage("Woah there! That is a very long suggestion! " +
                        "Please keep it at a maximum of 500 " +
                        "characters.\n\n" + msg).queue(), fail ->
                        channel.sendMessage(user.getAsMention() + " I couldn't DM you! Check your privacy settings!").queue());
                // ignore a fail
                return;
            }

            SuggestionsManager.getInstance().submitSuggestion(new Suggestion(m.getUser(), msg), false);
        } else {
            channel.sendMessage(user.getAsMention() + " **Usage**: `submitfor <user> <suggestion>`").queue();
        }
    }

    @Override
    public String getCommand() {
        return "submitfor";
    }

    @Override
    public String getDescription() {
        return "Submit a suggestion for a user";
    }

    @Override
    public boolean deleteMessage() {
        return true;
    }
}
