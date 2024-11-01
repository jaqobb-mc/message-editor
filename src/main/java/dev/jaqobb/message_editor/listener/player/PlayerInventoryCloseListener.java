package dev.jaqobb.message_editor.listener.player;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageEditData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class PlayerInventoryCloseListener implements Listener {
    
    private final MessageEditorPlugin plugin;
    
    public PlayerInventoryCloseListener(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        MessageEditData editData = this.plugin.getCurrentMessageEditData(player.getUniqueId());
        if (editData == null || !editData.getCurrentMode().shouldInvalidateCache()) {
            return;
        }
        this.plugin.removeCurrentMessageEditData(player.getUniqueId());
    }
}
