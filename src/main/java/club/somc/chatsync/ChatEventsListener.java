package club.somc.chatsync;

import club.somc.protos.MinecraftMessageSent;
import io.nats.client.Connection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEventsListener implements Listener {

    Connection nc;
    String serverName;

    public ChatEventsListener(Connection nc, String serverName) {
        this.nc = nc;
        this.serverName = serverName;
    }

    @EventHandler
    public void onAsyncChatEvent(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();

        MinecraftMessageSent msg = MinecraftMessageSent.newBuilder()
                .setServerName(serverName)
                .setPlayerUuid(player.getUniqueId().toString())
                .setPlayerName(player.getName())
                .setMessage(event.getMessage())
                .build();

        nc.publish("minecraft.chat.message_sent", msg.toByteArray());
    }
}
