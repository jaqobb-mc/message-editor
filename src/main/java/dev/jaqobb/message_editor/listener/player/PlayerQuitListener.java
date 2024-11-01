package dev.jaqobb.message_editor.listener.player;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    
    private final MessageEditorPlugin plugin;
    
    public PlayerQuitListener(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.plugin.removeCurrentMessageEditData(event.getPlayer().getUniqueId());
    }
}
