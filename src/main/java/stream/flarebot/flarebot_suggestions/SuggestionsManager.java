package stream.flarebot.flarebot_suggestions;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class SuggestionsManager {

    private static final SuggestionsManager instance = new SuggestionsManager();

    private Map<Integer, Suggestion> suggestions = new HashMap<>();

    public void submitSuggestion(Suggestion suggestion, boolean orderSuggestions) {
        TextChannel tc = FlareBotSuggestions.getInstance().getClient().getTextChannelById(Constants.SUGGESTIONS_CHANNEL);
        if (suggestion.getMessageId() == -1) {
            suggestion.setId(suggestions.size() + 1);
            tc.sendMessage(getSuggestionEmbed(suggestion).build()).queue(msg -> {
                suggestion.setMessageId(msg.getIdLong());
                suggestions.put(suggestion.getId(), suggestion);
            });
        } else {
            suggestions.put(suggestion.getId(), suggestion);
            tc.getMessageById(suggestion.getMessageId()).queue(msg -> msg.editMessage(getSuggestionEmbed(suggestion)
                    .build()).queue());
        }

        if (orderSuggestions) {
            // TODO
        }
    }

    public void removeSuggesstion(int id) {
        for (Suggestion s : suggestions.values()) {
            if (s.getId() == id) {
                suggestions.remove(s.getId());
                FlareBotSuggestions.getInstance().getSuggestionsChannel().getMessageById(s.getMessageId())
                        .queue(msg -> msg.delete().queue(), fail -> {
                        }); // Ignore a fail, already deleted.
            }
        }
    }

    public void voteOnSuggestion(int id, long userId) {
        Suggestion s = getSuggestionById(id);
        if (s != null) {
            s.getVotedUsers().add(userId);
            submitSuggestion(s, true);
        }
    }

    public void mergeSuggestions(int id, int dupeId) {
        Suggestion s = SuggestionsManager.getInstance().getSuggestionById(id);
        Suggestion dupe = SuggestionsManager.getInstance().getSuggestionById(dupeId);
        if (s != null && dupe != null) {
            // Combine the votes
            for (Long voter : dupe.getVotedUsers()) {
                s.getVotedUsers().add(voter);
            }
            SuggestionsManager.getInstance().removeSuggesstion(dupeId);
            SuggestionsManager.getInstance().submitSuggestion(s, true);
        }
    }

    public EmbedBuilder getSuggestionEmbed(Suggestion suggestion) {
        return new EmbedBuilder().setTitle(suggestion.getId()
                + " - Suggestion by " + suggestion.getSuggestedByTag() + " (" + suggestion.getSuggestedBy() + ")")
                .setDescription(suggestion.getSuggestion()).addField("Votes", String.valueOf(suggestion.getVotes()), false);
    }

    public Map<Integer, Suggestion> getSuggestions() {
        return suggestions;
    }

    public Suggestion getSuggestionById(int id) {
        for (Suggestion s : suggestions.values()) {
            if (s.getId() == id)
                return s;
        }
        return null;
    }

    public static SuggestionsManager getInstance() {
        return instance;
    }
}
