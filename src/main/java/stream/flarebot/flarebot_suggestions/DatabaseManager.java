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
                PreparedStatement statement;
                String query;
                if (s.getId() == null) {
                    query = "INSERT INTO suggestions (suggested_by, suggested_by_tag, suggestion, voted_users, message_id, status) VALUES (?, ?, ?, ?, ?, ?);";
                } else {
                    query =
                            "UPDATE suggestions SET suggested_by=?, suggested_by_tag=?, suggestion=?, voted_users=?, message_id=?, status=? WHERE suggestion_id=?;";
                }
                statement =
                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, String.valueOf(s.getSuggestedBy()));
                statement.setString(2, s.getSuggestedByTag());
                statement.setString(3, s.getSuggestion());
                statement.setString(4, (s.getVotedUsers().stream().map(String::valueOf).collect(Collectors.joining(","))));
                statement.setString(5, String.valueOf(s.getMessageId()));
                statement.setString(6, s.getStatus().name());
                if (s.getId() != null) {
                    statement.setInt(7, s.getId());
                }
                int affectedRows = statement.executeUpdate();

                if (affectedRows == 0) {
                    return;
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        s.setId(generatedKeys.getInt(1));
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSuggestion(Suggestion s) {
        try {
            SQLController.runSqlTask(connection -> {
                PreparedStatement statement;
                statement =
                        connection.prepareStatement("DELETE FROM flarebot_suggestions.suggestions WHERE suggestion_id=?;");

                statement.setInt(1, s.getId());
                statement.execute();
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Suggestion getSuggestion(int id) {
        Suggestion[] suggestion = new Suggestion[1];
        try {
            SQLController.runSqlTask(connection -> {
                ResultSet set = connection.createStatement().executeQuery("SELECT * FROM suggestions WHERE suggestion_id=" + id);
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
                            set.getString("suggestion"), voted, Long.parseLong(set.getString("message_id")), Suggestion.Status.valueOf(set.getString("status"))));
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
                PreparedStatement statement = connection.prepareStatement("UPDATE suggestions SET message_id=? WHERE suggestion_id=?");
                statement.setString(1, String.valueOf(messageID));
                statement.setString(2, String.valueOf(suggestionID));
                statement.execute();
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
