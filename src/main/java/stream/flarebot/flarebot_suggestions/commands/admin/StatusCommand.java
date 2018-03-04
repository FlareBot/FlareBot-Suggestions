package stream.flarebot.flarebot_suggestions.commands.admin;

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
import java.util.Objects;
import java.util.stream.Collectors;

public class StatusCommand implements Command {

    @Override
    public void onCommand(User user, MessageChannel channel, Message message, String[] args, Member member) {
        if (FlareBotSuggestions.getInstance().isStaff(user)) {
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
                    Suggestion.Status status;
                    String statusComment;
                    try {
                        status = Suggestion.Status.valueOf(args[1].toUpperCase());
                        statusComment = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
                        if (statusComment.isEmpty()) statusComment = null;
                    } catch (IllegalArgumentException e) {
                        statusComment = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                        channel.sendMessage("Changing the status comment to: `" + statusComment + "`").queue();
                        s.setStatusComment(statusComment);
                        SuggestionsManager.getInstance().submitSuggestion(s, false);
                        return;
                    }
                    if (s.getStatus() == status && (Objects.equals(s.getStatusComment(), statusComment))) {
                        channel.sendMessage(user.getAsMention() + " This suggestion already has that status!").queue();
                    } else if (status == Suggestion.Status.COMPLETED) {
                        s.setStatus(status);
                        FlareBotSuggestions.getInstance().getSuggestionsChannel().getMessageById(s.getMessageId())
                                .queue(msg -> msg.delete().queue(), e -> {});
                        DatabaseManager.insertSuggestion(s);
                    } else {
                        channel.sendMessage(user.getAsMention() + " Changed #" + s.getId() + " to status: **"
                                + SuggestionsManager.upperCaseFirst(status.name().replace("_", " ").toLowerCase()) + "**" +
                                (statusComment == null ? "" : " with the comment: `" + statusComment + "`")).queue();
                        s.setStatus(status);
                        s.setStatusComment(statusComment);
                        SuggestionsManager.getInstance().submitSuggestion(s, false);
                    }
                } else {
                    channel.sendMessage(user.getAsMention() + " Invalid suggestion ID! Please refer to the number at the start of the title in " +
                            "the suggestion embed").queue();
                }
            } else {
                channel.sendMessage(user.getAsMention() + " **Usage**: `status <id> <status> [comment]`").queue();
            }
        }
    }

    @Override
    public String getCommand() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "Changes the status of a suggestion";
    }

    @Override
    public boolean deleteMessage() {
        return true;
    }
}
