package stream.flarebot.flarebot_suggestions;

import java.util.Map;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

public class SuggestionsManager {

    private static final SuggestionsManager instance = new SuggestionsManager();

    public void submitSuggestion(Suggestion suggestion) {
        DatabaseManager.insertSuggestion(suggestion);
        TextChannel tc =
                FlareBotSuggestions.getInstance().getClient().getTextChannelById(Constants.SUGGESTIONS_CHANNEL);
        if (suggestion.getMessageId() == -1) {
            tc.sendMessage(getSuggestionEmbed(suggestion).build()).queue(msg -> {
                suggestion.setMessageId(msg.getIdLong());
                DatabaseManager.updateMessageId(suggestion.getId(), suggestion.getMessageId());
            });
        } else {
            tc.getMessageById(suggestion.getMessageId()).queue(msg -> msg.editMessage(getSuggestionEmbed(suggestion)
                    .build()).queue());
        }
    }

    public void removeSuggesstion(int id) {
        Suggestion s = DatabaseManager.getSuggestion(id);
        if (s != null) {
            FlareBotSuggestions.getInstance().getSuggestionsChannel().getMessageById(s.getMessageId())
                    .queue(msg -> msg.delete().queue(), fail -> {
                    }); // Ignore a fail, already deleted.
            DatabaseManager.deleteSuggestion(s);
        }
    }

    public void voteOnSuggestion(int id, long userId) {
        Suggestion s = DatabaseManager.getSuggestion(id);
        if (s != null) {
            s.getVotedUsers().add(userId);
            submitSuggestion(s);
        }
    }

    public void mergeSuggestions(int id, int dupeId) {
        Suggestion s = DatabaseManager.getSuggestion(id);
        Suggestion dupe = DatabaseManager.getSuggestion(dupeId);
        if (s != null && dupe != null) {
            // Combine the votes
            for (Long voter : dupe.getVotedUsers()) {
                s.getVotedUsers().add(voter);
            }
            SuggestionsManager.getInstance().removeSuggesstion(dupeId);
            SuggestionsManager.getInstance().submitSuggestion(s);
        }
    }

    public EmbedBuilder getSuggestionEmbed(Suggestion suggestion) {
        return new EmbedBuilder()
                .setTitle(suggestion.getId()
                        + " - Suggestion by " + suggestion.getSuggestedByTag() + " (" + suggestion.getSuggestedBy() + ")")
                .setDescription(suggestion.getSuggestion())
                .addField("Votes", String.valueOf(suggestion.getVotes()), false)
                .addField("Status", upperCaseFirst(suggestion.getStatus().name().toLowerCase()), false)
                .setColor(suggestion.getStatus().getColor());
    }

    public static String upperCaseFirst(String value) {
        char[] array = value.toCharArray();
        array[0] = Character.toUpperCase(array[0]);
        return new String(array);
    }

    public static SuggestionsManager getInstance() {
        return instance;
    }
}
