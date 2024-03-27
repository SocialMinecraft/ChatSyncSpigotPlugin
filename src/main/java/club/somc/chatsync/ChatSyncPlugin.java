package club.somc.chatsync;

import club.somc.protos.MinecraftMessageSent;
import club.somc.protos.MinecraftPlayerDied;
import club.somc.protos.MinecraftPlayerJoined;
import club.somc.protos.MinecraftPlayerQuit;
import com.google.protobuf.InvalidProtocolBufferException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class ChatSyncPlugin extends JavaPlugin {

    Connection nc;

    @Override
    public void onEnable() {
        super.onEnable();
        this.saveDefaultConfig();

        String serverName = getConfig().getString("serverName");

        try {
            this.nc = Nats.connect(getConfig().getString("natsUrl"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(new ChatEventsListener(nc, serverName), this);
        getServer().getPluginManager().registerEvents(new PlayerEventsListener(nc, serverName), this);

        Dispatcher dispatcher = nc.createDispatcher((msg) -> {
            Bukkit.getScheduler().runTask(this, () -> {
                try {
                    if (msg.getSubject().equals("minecraft.chat.message_sent")) {
                        MinecraftMessageSent event = null;
                        event = MinecraftMessageSent.parseFrom(msg.getData());
                        if (!event.getServerName().equals(serverName))
                            Bukkit.broadcastMessage("<" + event.getPlayerName() + "> " +
                                    event.getMessage());
                    }

                    if (msg.getSubject().equals("minecraft.player.joined")) {
                        MinecraftPlayerJoined event = null;
                        event = MinecraftPlayerJoined.parseFrom(msg.getData());
                        if (!event.getServerName().equals(serverName))
                            Bukkit.broadcastMessage(ChatColor.YELLOW + event.getPlayerName() +
                                    " joined server " + event.getServerName());
                    }

                    if (msg.getSubject().equals("minecraft.player.quit")) {
                        MinecraftPlayerQuit event = null;
                        event = MinecraftPlayerQuit.parseFrom(msg.getData());
                        if (!event.getServerName().equals(serverName))
                            Bukkit.broadcastMessage(ChatColor.YELLOW + event.getPlayerName() +
                                    " left server " + event.getServerName());
                    }

                    if (msg.getSubject().equals("minecraft.player.death")) {
                        MinecraftPlayerDied event = null;
                        event = MinecraftPlayerDied.parseFrom(msg.getData());
                        if (!event.getServerName().equals(serverName))
                            Bukkit.broadcastMessage(ChatColor.RED + event.getPlayerName() +
                                    " died: " + event.getDeathMessage());
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        dispatcher.subscribe("minecraft.chat.*");
        dispatcher.subscribe("minecraft.player.joined");
        dispatcher.subscribe("minecraft.player.quit");
        dispatcher.subscribe("minecraft.player.died");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (this.nc != null) {
            try {
                this.nc.drain(Duration.ofSeconds(5));
            } catch (TimeoutException e) {
                //throw new RuntimeException(e);
            } catch (InterruptedException e) {
                //throw new RuntimeException(e);
            }
        }
    }
}
