package club.somc.chatsync;

import club.somc.protos.MinecraftPlayerDied;
import club.somc.protos.MinecraftPlayerJoined;
import club.somc.protos.MinecraftPlayerQuit;
import io.nats.client.Connection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEventsListener implements Listener {

    Connection nc;
    String serverName;

    public PlayerEventsListener(Connection nc, String serverName) {
        this.nc = nc;
        this.serverName = serverName;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        MinecraftPlayerJoined msg = MinecraftPlayerJoined.newBuilder()
                .setServerName(serverName)
                .setPlayerUuid(player.getUniqueId().toString())
                .setPlayerName(player.getName())
                .build();

        nc.publish("minecraft.player.joined",  msg.toByteArray());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        MinecraftPlayerQuit msg = MinecraftPlayerQuit.newBuilder()
                .setServerName(serverName)
                .setPlayerUuid(player.getUniqueId().toString())
                .setPlayerName(player.getName())
                .build();

        nc.publish("minecraft.player.quit", msg.toByteArray());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location location = player.getLocation();

        MinecraftPlayerDied msg = MinecraftPlayerDied.newBuilder()
                .setServerName(serverName)
                .setPlayerUuid(player.getUniqueId().toString())
                .setPlayerName(player.getName())
                .setDeathMessage(event.getDeathMessage())
                .setWorld(location.getWorld().getName())
                .setX((long)location.getX())
                .setY((long)location.getY())
                .setZ((long)location.getZ())
                .build();

        nc.publish("minecraft.player.died", msg.toByteArray());
    }
}
