package dev.jaqobb.message_editor.listener.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageData;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;

public class InventoryItemsPacketListener extends PacketAdapter {
    
    public InventoryItemsPacketListener(MessageEditorPlugin plugin) {
        super(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.WINDOW_ITEMS);
    }
    
    @Override
    public MessageEditorPlugin getPlugin() {
        return (MessageEditorPlugin) super.getPlugin();
    }
    
    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket().shallowClone();
        Iterable<ItemStack> items;
        if (packet.getItemArrayModifier().size() == 1) {
            items = Arrays.asList(packet.getItemArrayModifier().readSafely(0));
        } else {
            items = packet.getItemListModifier().readSafely(0);
        }
        boolean update = false;
        for (ItemStack item : items) {
            if (item == null) {
                continue;
            }
            if (!item.hasItemMeta()) {
                continue;
            }
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta.hasDisplayName()) {
                String originalMessage = itemMeta.getDisplayName();
                String message = originalMessage;
                Map.Entry<MessageEdit, String> cachedMessage = this.getPlugin().getCachedMessage(message, MessagePlace.INVENTORY_ITEM_NAME);
                MessageEdit messageEdit = null;
                Matcher messageEditMatcher = null;
                if (cachedMessage == null) {
                    for (MessageEdit edit : this.getPlugin().getMessageEdits()) {
                        MessagePlace place = edit.getMessageBeforePlace();
                        if (place != null && place != MessagePlace.INVENTORY_ITEM_NAME) {
                            continue;
                        }
                        Matcher matcher = edit.getMatcher(message);
                        if (matcher == null) {
                            continue;
                        }
                        messageEdit = edit;
                        messageEditMatcher = matcher;
                        break;
                    }
                }
                if (cachedMessage != null || (messageEdit != null && messageEditMatcher != null)) {
                    if (cachedMessage != null) {
                        message = cachedMessage.getValue();
                    } else {
                        String newMessage = messageEditMatcher.replaceAll(messageEdit.getMessageAfter());
                        newMessage = MessageUtils.translate(newMessage);
                        if (this.getPlugin().isPlaceholderApiPresent()) {
                            newMessage = PlaceholderAPI.setPlaceholders(player, newMessage);
                        }
                        this.getPlugin().cacheMessage(message, MessagePlace.INVENTORY_ITEM_NAME, messageEdit, newMessage);
                        message = newMessage;
                    }
                }
                boolean json = MessageUtils.isJson(message);
                if (json) {
                    message = BaseComponent.toLegacyText(MessageUtils.toBaseComponents(message));
                    json = false;
                }
                String id = MessageUtils.generateId(MessagePlace.INVENTORY_ITEM_NAME);
                this.getPlugin().cacheMessageData(id, new MessageData(id, MessagePlace.INVENTORY_ITEM_NAME, message, json));
                if (MessagePlace.INVENTORY_ITEM_NAME.isAnalyzing()) {
                    MessageUtils.logMessage(this.getPlugin().getLogger(), MessagePlace.INVENTORY_ITEM_NAME, player, id, json, message);
                }
                if (!message.equals(originalMessage)) {
                    itemMeta.setDisplayName(message);
                    item.setItemMeta(itemMeta);
                    update = true;
                }
            }
            if (itemMeta.hasLore()) {
                String originalMessage = String.join("\\n", itemMeta.getLore());
                String message = originalMessage;
                Map.Entry<MessageEdit, String> cachedMessage = this.getPlugin().getCachedMessage(message, MessagePlace.INVENTORY_ITEM_LORE);
                MessageEdit messageEdit = null;
                Matcher messageEditMatcher = null;
                if (cachedMessage == null) {
                    for (MessageEdit edit : this.getPlugin().getMessageEdits()) {
                        MessagePlace place = edit.getMessageBeforePlace();
                        if (place != null && place != MessagePlace.INVENTORY_ITEM_LORE) {
                            continue;
                        }
                        Matcher matcher = edit.getMatcher(message);
                        if (matcher == null) {
                            continue;
                        }
                        messageEdit = edit;
                        messageEditMatcher = matcher;
                        break;
                    }
                }
                if (cachedMessage != null || (messageEdit != null && messageEditMatcher != null)) {
                    if (cachedMessage != null) {
                        message = cachedMessage.getValue();
                    } else {
                        String newMessage = messageEditMatcher.replaceAll(messageEdit.getMessageAfter());
                        newMessage = MessageUtils.translate(newMessage);
                        if (this.getPlugin().isPlaceholderApiPresent()) {
                            newMessage = PlaceholderAPI.setPlaceholders(player, newMessage);
                        }
                        this.getPlugin().cacheMessage(message, MessagePlace.INVENTORY_ITEM_LORE, messageEdit, newMessage);
                        message = newMessage;
                    }
                }
                boolean json = MessageUtils.isJson(message);
                if (json) {
                    message = BaseComponent.toLegacyText(MessageUtils.toBaseComponents(message));
                    json = false;
                }
                String id = MessageUtils.generateId(MessagePlace.INVENTORY_ITEM_LORE);
                this.getPlugin().cacheMessageData(id, new MessageData(id, MessagePlace.INVENTORY_ITEM_LORE, message, json));
                if (MessagePlace.INVENTORY_ITEM_LORE.isAnalyzing()) {
                    MessageUtils.logMessage(this.getPlugin().getLogger(), MessagePlace.INVENTORY_ITEM_LORE, player, id, json, message);
                }
                if (!message.equals(originalMessage)) {
                    itemMeta.setLore(Arrays.asList(message.split("\\\\n")));
                    item.setItemMeta(itemMeta);
                    update = true;
                }
            }
        }
        if (!update) {
            return;
        }
        // Updating items in the cloned packet and then replacing the packet does seem to work only partially.
        // Sometimes, the items are not updated until the inventory itself is updated. 
        // However, editing the packet and sending it with the edited items a tick later seems to work.
        // Such solution however is not ideal and may break in the future, for example when a player somehow manages to open another inventory in the span of a tick (not actually tested, just a theory) or when the inventory is changed multiple times in a rapid succession.
        // If any of the issues were to happen, the solution would be to store the window id of the inventory and only update the items of the inventory with the same window id.
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.getPlugin(), () -> ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet));
    }
}
