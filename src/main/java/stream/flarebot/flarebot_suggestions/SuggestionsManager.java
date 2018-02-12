package stream.flarebot.flarebot_suggestions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;

public class SuggestionsManager {

    private static final SuggestionsManager instance = new SuggestionsManager();

    public void submitSuggestion(Suggestion suggestion) {
        DatabaseManager.insertSuggestion(suggestion);
        sendSuggestionMessage(suggestion);
        orderSuggestions();
    }

    public void sendSuggestionMessage(Suggestion suggestion) {
        TextChannel tc =
                FlareBotSuggestions.getInstance().getClient().getTextChannelById(Constants.SUGGESTIONS_CHANNEL);
        try {
            Message msg = tc.getMessageById(suggestion.getMessageId()).complete();
            msg.editMessage(getSuggestionEmbed(suggestion)
                    .build()).complete();
        } catch (ErrorResponseException e) {
            Message msg = tc.sendMessage(getSuggestionEmbed(suggestion).build()).complete();
            suggestion.setMessageId(msg.getIdLong());
            DatabaseManager.updateMessageId(suggestion.getId(), suggestion.getMessageId());
        }
    }

    public void orderSuggestions() {

        List<Suggestion> suggestions = DatabaseManager.getSuggestions();

        suggestions.sort(Comparator.comparingInt(Suggestion::getVotes)); // Sort ascending
        Collections.reverse(suggestions); // Reverse to sort descending

        List<Long> messageIDs =
                suggestions.stream().map(Suggestion::getMessageId).sorted().collect(Collectors.toList()); // Sort IDs by time

        for (int i = 0; i < suggestions.size(); i++) {
            Suggestion s = suggestions.get(i);
            s.setMessageId(messageIDs.get(i));
            DatabaseManager.insertSuggestion(s);
            FlareBotSuggestions.getInstance()
                    .getSuggestionsChannel()
                    .getMessageById(s.getMessageId())
                    .queue(m ->
                            m.editMessage(getSuggestionEmbed(s).build())
                                    .queue());
        }
    }

    public void removeSuggestion(int id) {
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
            SuggestionsManager.getInstance().removeSuggestion(dupeId);
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
        // Uppercase first letter.
        array[0] = Character.toUpperCase(array[0]);

        // Uppercase all letters that follow a whitespace character.
        for (int i = 1; i < array.length; i++) {
            if (Character.isWhitespace(array[i - 1]) || array[i - 1] == '_') {
                array[i] = Character.toUpperCase(array[i]);
            }
        }
        return new String(array);
    }

    public static SuggestionsManager getInstance() {
        return instance;
    }
}
