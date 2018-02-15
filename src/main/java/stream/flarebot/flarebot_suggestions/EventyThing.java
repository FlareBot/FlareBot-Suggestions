package stream.flarebot.flarebot_suggestions;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.concurrent.TimeUnit;

public class EventyThing implements EventListener {

    @Override
    public void onEvent(Event event) {
        if (event instanceof GuildMessageReceivedEvent) {
            GuildMessageReceivedEvent e = (GuildMessageReceivedEvent) event;
            if (e.getChannel().getIdLong() == FlareBotSuggestions.getInstance().getSuggestionsChannel().getIdLong()) {
                if (e.getMember().hasPermission(Permission.MESSAGE_MANAGE) && !e.getMember().getUser().isBot()) return;
                if (e.getMember().getUser().getIdLong() == e.getGuild().getSelfMember().getUser().getIdLong()) {
                    if (e.getMessage().getEmbeds().isEmpty())
                        e.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
                } else
                    e.getMessage().delete().queue();
            }
        }
    }
}
