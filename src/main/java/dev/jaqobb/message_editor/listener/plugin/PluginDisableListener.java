package dev.jaqobb.message_editor.listener.plugin;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import java.util.logging.Level;

public class PluginDisableListener implements Listener {
    
    private final MessageEditorPlugin plugin;
    
    public PluginDisableListener(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin plugin = event.getPlugin();
        if (!plugin.getName().equals("PlaceholderAPI")) {
            return;
        }
        this.plugin.setPlaceholderApiPresent(false);
        this.plugin.getLogger().log(Level.INFO, "PlaceholderAPI integration has been disabled.");
    }
}
