package stream.flarebot.flarebot_suggestions;

import com.walshydev.jba.Config;
import com.walshydev.jba.JBA;
import com.walshydev.jba.SQLController;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot_suggestions.commands.EditCommand;
import stream.flarebot.flarebot_suggestions.commands.SubmitCommand;
import stream.flarebot.flarebot_suggestions.commands.SuggestionCommand;
import stream.flarebot.flarebot_suggestions.commands.VoteCommand;
import stream.flarebot.flarebot_suggestions.commands.admin.DupeCommand;
import stream.flarebot.flarebot_suggestions.commands.admin.QuitCommand;
import stream.flarebot.flarebot_suggestions.commands.admin.RemoveCommand;
import stream.flarebot.flarebot_suggestions.commands.admin.StatusCommand;
import stream.flarebot.flarebot_suggestions.commands.admin.SubmitForCommand;

import java.sql.SQLException;

/*

 Still TODO
 * Limiting suggestions - Make regulars have more (maybe just not limited?)
 */
public class FlareBotSuggestions extends JBA {

    private static FlareBotSuggestions instance;
    private Config config;

    public static void main(String[] args) {
        (instance = new FlareBotSuggestions()).init();
    }

    private void init() {
        config = new Config("config");

        setupMySQL(config.getString("mysql.user"), config.getString("mysql.password"),
                config.getString("mysql.host"), config.getString("mysql.database"));

        super.init(AccountType.BOT, config.getString("token"), config.getString("prefix"));
    }

    @Override
    public void run() {
        load();

        getClient().addEventListener(new EventyThing());
        getClient().getPresence().setGame(Game.watching("Suggestions roll in | -submit"));

        registerCommand(new SubmitCommand());
        registerCommand(new VoteCommand());
        registerCommand(new SuggestionCommand());
        registerCommand(new EditCommand());

        registerCommand(new DupeCommand());
        registerCommand(new RemoveCommand());
        registerCommand(new QuitCommand());
        registerCommand(new StatusCommand());
        registerCommand(new SubmitForCommand());
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
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS suggestions (suggestion_id INT(10) PRIMARY KEY AUTO_INCREMENT, " +
                        "suggested_by VARCHAR(20), suggested_by_tag VARCHAR(40), suggestion TEXT, voted_users TEXT, " +
                        "message_id VARCHAR(20), status VARCHAR(15))");
            });
        } catch (SQLException e) {
            LOGGER.error("Failed to create table!", e);
        }
        for (Suggestion suggestion : DatabaseManager.getSuggestions()) {
            LOGGER.info("Sending message for " + suggestion.getId());
            SuggestionsManager.getInstance().sendSuggestionMessage(suggestion);
        }
        SuggestionsManager.getInstance().orderSuggestions();
    }
}
