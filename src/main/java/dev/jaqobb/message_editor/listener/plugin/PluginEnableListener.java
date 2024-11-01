package dev.jaqobb.message_editor.listener.plugin;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import java.util.logging.Level;

public class PluginEnableListener implements Listener {
    
    private final MessageEditorPlugin plugin;
    
    public PluginEnableListener(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin plugin = event.getPlugin();
        if (!plugin.getName().equals("PlaceholderAPI")) {
            return;
        }
        this.plugin.setPlaceholderApiPresent(true);
        this.plugin.getLogger().log(Level.INFO, "PlaceholderAPI integration has been enabled.");
    }
}
