package stream.flarebot.flarebot_suggestions;

import stream.flarebot.flarebot_suggestions.commands.SubmitCommand;
import stream.flarebot.flarebot_suggestions.commands.admin.DupeCommand;
import stream.flarebot.flarebot_suggestions.commands.admin.QuitCommand;
import stream.flarebot.flarebot_suggestions.commands.admin.RemoveCommand;
import stream.flarebot.flarebot_suggestions.commands.VoteCommand;
import com.walshydev.jba.Config;
import com.walshydev.jba.JBA;
import com.walshydev.jba.SQLController;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*

 Still TODO
 * Ordering
 * Better saving
 * Limiting suggestions - Make regulars have more (maybe just not limited?)

 */
public class FlareBotSuggestions extends JBA {

    private static FlareBotSuggestions instance;
    private Config config;

    public static void main(String[] args) {
        (instance = new FlareBotSuggestions()).init();
    }

    private void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::quit));
        config = new Config("config");

        setupMySQL(config.getString("mysql.user"), config.getString("mysql.password"),
                config.getString("mysql.host"), config.getString("mysql.database"));

        super.init(AccountType.BOT, config.getString("token"), config.getString("prefix"));
    }

    @Override
    public void run() {
        load();

        registerCommand(new SubmitCommand());
        registerCommand(new VoteCommand());

        registerCommand(new DupeCommand());
        registerCommand(new RemoveCommand());
        registerCommand(new QuitCommand());
    }

    public static FlareBotSuggestions getInstance() {
        return instance;
    }

    public TextChannel getSuggestionsChannel() {
        return getClient().getTextChannelById(Constants.SUGGESTIONS_CHANNEL);
    }

    public boolean isStaff(User user) {
        return getClient().getGuildById(Constants.GUILD).getMember(user) != null &&
                getClient().getGuildById(Constants.GUILD).getMember(user).getRoles().contains(getClient()
                        .getGuildById(Constants.GUILD).getRoleById(Constants.STAFF_ROLE));
    }

    private void load() {
        try {
            SQLController.runSqlTask(conn -> {
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS suggestions (suggestion_id INT(10) PRIMARY KEY, " +
                        "suggested_by VARCHAR(20), suggested_by_tag VARCHAR(40), suggestion TEXT, voted_users TEXT, " +
                        "message_id VARCHAR(20))");

                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM suggestions");
                while(set.next()) {
                    Set<Long> voted = new HashSet<>();
                    for (String s : set.getString("voted_users").split(", ?"))
                        voted.add(Long.parseLong(s));

                    SuggestionsManager.getInstance().submitSuggestion(new Suggestion(set.getInt("suggestion_id"),
                            Long.parseLong(set.getString("suggested_by")), set.getString("suggested_by_tag"),
                            set.getString("suggestion"), voted, Long.parseLong(set.getString("message_id"))), false);

                }
            });
        } catch (SQLException e) {
            LOGGER.error("Failed to load suggestions!", e);
        }
    }

    private void quit() {
        try {
            SQLController.runSqlTask(conn -> {
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS suggestions (suggestion_id INT(10) PRIMARY KEY, " +
                        "suggested_by VARCHAR(20), suggested_by_tag VARCHAR(40), suggestion TEXT, voted_users TEXT, " +
                        "message_id VARCHAR(20))");

                conn.createStatement().execute("TRUNCATE suggestions");

                PreparedStatement ps = conn.prepareStatement("INSERT INTO suggestions (suggestion_id, suggested_by, " +
                        "suggested_by_tag, suggestion, voted_users, message_id) VALUES (?, ?, ?, ?, ?, ?)");
                for (Suggestion s : SuggestionsManager.getInstance().getSuggestions().values()) {
                    ps.setInt(1, s.getId());
                    ps.setString(2, String.valueOf(s.getSuggestedBy()));
                    ps.setString(3, s.getSuggestedByTag());
                    ps.setString(4, s.getSuggestion());
                    ps.setString(5, (s.getVotedUsers().stream().map(String::valueOf).collect(Collectors.joining(","))));
                    ps.setString(6, String.valueOf(s.getMessageId()));
                    ps.execute();
                }
            });
        } catch (SQLException e) {
            System.err.println("Shit");
            e.printStackTrace();
            LOGGER.error("Failed to save suggestions!", e);
        }
    }
}
