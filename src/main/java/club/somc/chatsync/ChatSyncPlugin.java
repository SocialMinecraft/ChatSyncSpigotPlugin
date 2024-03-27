package club.somc.chatsync;

import club.somc.protos.MinecraftChat;
import com.google.protobuf.InvalidProtocolBufferException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import org.bukkit.Bukkit;
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

        Dispatcher dispatcher = nc.createDispatcher((msg) -> {
            Bukkit.getScheduler().runTask(this, () -> {
                try {
                    if (msg.getSubject().equals("minecraft.chat.message_sent")) {
                        MinecraftChat.MinecraftMessageSent event = null;
                        event = MinecraftChat.MinecraftMessageSent.parseFrom(msg.getData());
                        if (!event.getServerName().equals(serverName))
                            Bukkit.broadcastMessage("<" + event.getPlayerName() + "> " +
                                    event.getMessage());
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        dispatcher.subscribe("minecraft.chat.*");
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
