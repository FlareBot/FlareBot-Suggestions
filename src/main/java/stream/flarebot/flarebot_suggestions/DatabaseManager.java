package stream.flarebot.flarebot_suggestions;

import com.walshydev.jba.SQLController;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseManager {

    public static void insertSuggestion(Suggestion s) {
        try {
            SQLController.runSqlTask(connection -> {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO suggestions (suggestion_id, "
                                + "suggested_by, suggested_by_tag, suggestion, voted_users, message_id, status) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE suggested_by = VALUES(suggested_by), "
                                + "suggested_by_tag = VALUES(suggested_by_tag), suggestion = VALUES(suggestion), "
                                + "voted_users = VALUES(voted_users), message_id = VALUES(message_id), status = VALUES(status)",
                        Statement.RETURN_GENERATED_KEYS);
                statement.setInt(1, s.getId() > 0 ? s.getId() : 0);
                statement.setString(2, String.valueOf(s.getSuggestedBy()));
                statement.setString(3, s.getSuggestedByTag());
                statement.setString(4, s.getSuggestion());
                statement.setString(5, (s.getVotedUsers().stream().map(String::valueOf).collect(Collectors.joining(","))));
                statement.setString(6, String.valueOf(s.getMessageId()));
                statement.setString(7, s.getStatus().name());

                int rowsChanged = statement.executeUpdate();
                ResultSet keys = statement.getGeneratedKeys();
                if (rowsChanged == 1 && keys.next()) {
                    s.setId(keys.getInt(1));
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSuggestion(Suggestion s) {
        try {
            SQLController.runSqlTask(connection -> {
                connection.createStatement().execute("DELETE FROM flarebot_suggestions.suggestions WHERE suggestion_id = " + s.getId());
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Suggestion getSuggestion(int id) {
        Suggestion[] suggestion = new Suggestion[1];
        try {
            SQLController.runSqlTask(connection -> {
                ResultSet set = connection.createStatement().executeQuery("SELECT * FROM suggestions WHERE suggestion_id = " + id);
                if (set.next()) {
                    Set<Long> voted = new HashSet<>();
                    for (String s : set.getString("voted_users").split(", ?"))
                        voted.add(Long.parseLong(s));

                    suggestion[0] = new Suggestion(set.getInt("suggestion_id"),
                            Long.parseLong(set.getString("suggested_by")),
                            set.getString("suggested_by_tag"),
                            set.getString("suggestion"),
                            voted,
                            Long.parseLong(set.getString("message_id"))
                            , Suggestion.Status.valueOf(set.getString("status")));
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suggestion[0];
    }

    public static List<Suggestion> getSuggestions() {
        List<Suggestion> suggestion = new ArrayList<>();
        try {
            SQLController.runSqlTask(connection -> {
                ResultSet set = connection.createStatement().executeQuery("SELECT * FROM suggestions");
                while (set.next()) {
                    Set<Long> voted = new HashSet<>();
                    for (String s : set.getString("voted_users").split(", ?"))
                        voted.add(Long.parseLong(s));

                    suggestion.add(new Suggestion(set.getInt("suggestion_id"),
                            Long.parseLong(set.getString("suggested_by")), set.getString("suggested_by_tag"),
                            set.getString("suggestion"), voted, Long.parseLong(set.getString("message_id")),
                            Suggestion.Status.valueOf(set.getString("status"))));
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suggestion;
    }

    public static void updateMessageId(int suggestionID, long messageID) {
        try {
            SQLController.runSqlTask(connection -> {
                connection.createStatement().execute(String.format("UPDATE suggestions SET message_id = %d WHERE suggestion_id = %d", messageID, suggestionID));
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
