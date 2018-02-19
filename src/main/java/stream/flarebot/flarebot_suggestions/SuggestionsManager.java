package stream.flarebot.flarebot_suggestions;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SuggestionsManager {

    private static final SuggestionsManager instance = new SuggestionsManager();
    private static final Logger logger = LoggerFactory.getLogger(SuggestionsManager.class);

    public void submitSuggestion(Suggestion suggestion, boolean order) {
        DatabaseManager.insertSuggestion(suggestion);
        sendSuggestionMessage(suggestion);
        if (order)
            orderSuggestions();
    }

    public void sendSuggestionMessage(Suggestion suggestion) {
        TextChannel tc = FlareBotSuggestions.getInstance().getSuggestionsChannel();
        if (suggestion.getMessageId() == -1)
            tc.sendMessage(getSuggestionEmbed(suggestion).build()).queue(msg -> {
                logger.info("Sent new message for " + suggestion.getId());
                suggestion.setMessageId(msg.getIdLong());
                DatabaseManager.updateMessageId(suggestion.getId(), suggestion.getMessageId());
            });
        else
            editSuggestionMessage(suggestion);
    }

    public void editSuggestionMessage(Suggestion suggestion) {
        TextChannel tc = FlareBotSuggestions.getInstance().getSuggestionsChannel();
        tc.getMessageById(suggestion.getMessageId()).queue(msg -> msg.editMessage(getSuggestionEmbed(suggestion)
                .build()).queue(), fail -> {
            logger.info("Couldn't find message by ID: " + suggestion.getMessageId());
            suggestion.setMessageId(-1);
            sendSuggestionMessage(suggestion);
        });
    }

    public void orderSuggestions() {
        List<Suggestion> suggestions = DatabaseManager.getSuggestions();

        //noinspection ComparatorMethodParameterNotUsed
        suggestions.sort((s1, s2) -> s1.getVotes() > s2.getVotes() ? 1 :
                (s1.getVotes() == s2.getVotes() ?
                        (s1.getId() < s2.getId() ? 1 : -1)
                        : -1)); // Sort ascending
        Collections.reverse(suggestions); // Reverse to sort descending

        List<Long> oldestToNewest =
                suggestions.stream().map(Suggestion::getMessageId).sorted(Long::compare)
                        .collect(Collectors.toList()); // Sort IDs by time

        for (long messageId : oldestToNewest) {
            int index = oldestToNewest.indexOf(messageId);
            Suggestion s = suggestions.get(index);

            if (s.getMessageId() != messageId) {
                logger.info("Ordering " + s.getId() + "(Old ID: " + s.getMessageId() + ", new ID: " + messageId + ")");
                s.setMessageId(messageId);
                DatabaseManager.insertSuggestion(s);
                FlareBotSuggestions.getInstance()
                        .getSuggestionsChannel()
                        .getMessageById(s.getMessageId())
                        .queue(m -> m.editMessage(getSuggestionEmbed(s).build()).queue());
            }
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
            submitSuggestion(s, true);
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
            SuggestionsManager.getInstance().submitSuggestion(s, true);
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
