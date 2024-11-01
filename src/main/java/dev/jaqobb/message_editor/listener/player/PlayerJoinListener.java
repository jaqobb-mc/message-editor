package dev.jaqobb.message_editor.listener.player;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.util.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final MessageEditorPlugin plugin;
    
    public PlayerJoinListener(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("messageeditor.use") || !this.plugin.isUpdateNotify()) {
            return;
        }
        MessageUtils.sendPrefixedMessage(player, this.plugin.getUpdater().getUpdateMessage());
    }
}
