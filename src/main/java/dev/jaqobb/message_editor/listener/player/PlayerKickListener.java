package dev.jaqobb.message_editor.listener.player;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class PlayerKickListener implements Listener {
    
    private final MessageEditorPlugin plugin;
    
    public PlayerKickListener(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        this.plugin.removeCurrentMessageEditData(event.getPlayer().getUniqueId());
    }
}
