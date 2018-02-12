package stream.flarebot.flarebot_suggestions;

import net.dv8tion.jda.core.entities.User;

import java.util.HashSet;
import java.util.Set;

public class Suggestion {

    private int id;
    private long suggestedBy;
    private String suggestedByTag;
    private String suggestion;
    private Set<Long> votedUsers = new HashSet<>();

    private long messageId = -1;

    public Suggestion(User user, String suggestion) {
        this.suggestedBy = user.getIdLong();
        this.suggestedByTag = user.getName() + "#" + user.getDiscriminator();
        this.suggestion = suggestion;
        votedUsers.add(suggestedBy);
    }

    public Suggestion(int id, long suggestedBy, String suggestedByTag, String suggestion, Set<Long> votedUsers, long messageId) {
        this.id = id;
        this.suggestedBy = suggestedBy;
        if (suggestedByTag == null) {
            User u = FlareBotSuggestions.getInstance().getClient().getUserById(suggestedBy);
            if (u != null)
                this.suggestedByTag = u.getName() + "#" + u.getDiscriminator();
            else
                this.suggestedByTag = "Unknown /shrug";
        } else
            this.suggestedByTag = suggestedByTag;
        this.suggestion = suggestion;
        this.votedUsers = votedUsers;
        this.messageId = messageId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSuggestedBy() {
        return suggestedBy;
    }

    public String getSuggestedByTag() {
        return suggestedByTag;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public Set<Long> getVotedUsers() {
        return votedUsers;
    }

    public int getVotes() {
        return votedUsers.size();
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
