package dev.jaqobb.message_editor.listener.player;

import com.comphenix.protocol.utility.MinecraftVersion;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessageEditData;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerInventoryClickListener implements Listener {
    
    private final MessageEditorPlugin plugin;
    
    public PlayerInventoryClickListener(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        InventoryView view = event.getView();
        if (!view.getTitle().equals(MessageUtils.translate("&8Message Editor"))) {
            return;
        }
        event.setCancelled(true);
        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
            return;
        }
        if (event.getAction() == InventoryAction.NOTHING) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot < 0 && slot >= inventory.getSize()) {
            return;
        }
        MessageEditData editData = this.plugin.getCurrentMessageEditData(player.getUniqueId());
        if (editData == null) {
            return;
        }
        if (slot == 4) {
            editData.setCurrentMode(MessageEditData.Mode.EDITING_FILE_NAME);
            player.closeInventory();
            MessageUtils.sendSuccessSound(player);
            MessageUtils.sendPrefixedMessage(player, "&7Enter new file name where you want your message edit to be stored or '&edone&7' if you changed your mind about editing file name.");
        } else if (slot == 20) {
            editData.setCurrentMode(MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_KEY);
            editData.setOldMessagePatternKey("");
            player.closeInventory();
            MessageUtils.sendSuccessSound(player);
            MessageUtils.sendPrefixedMessage(player, "&7Enter what you want to replace or '&edone&7' if you are done replacing.");
        } else if (slot == 24) {
            ClickType click = event.getClick();
            if (click.isLeftClick()) {
                editData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE);
                editData.setNewMessageCache("");
                player.closeInventory();
                MessageUtils.sendSuccessSound(player);
                MessageUtils.sendPrefixedMessage(player, "&7Enter new message. Once you are done, enter '&edone&7'.");
                MessagePlace place = editData.getNewMessagePlace();
                if (place == MessagePlace.GAME_CHAT || place == MessagePlace.SYSTEM_CHAT || place == MessagePlace.ACTION_BAR) {
                    MessageUtils.sendPrefixedMessage(player, "&7You can also enter '&eremove&7' if you do not want the new message to be sent to the players.");
                }
            } else if (click.isRightClick()) {
                editData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE_KEY);
                editData.setNewMessageKey("");
                player.closeInventory();
                MessageUtils.sendSuccessSound(player);
                MessageUtils.sendPrefixedMessage(player, "&7Enter what you want to replace or '&edone&7' if you are done replacing.");
            }
        } else if (slot == 33) {
            MessagePlace place = editData.getOldMessagePlace();
            if (place != MessagePlace.GAME_CHAT && place != MessagePlace.SYSTEM_CHAT && place != MessagePlace.ACTION_BAR) {
                MessageUtils.sendErrorSound(player);
                MessageUtils.sendPrefixedMessage(player, "&cYou cannot change new message place of this message.");
                return;
            }
            editData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE_PLACE);
            player.closeInventory();
            MessageUtils.sendSuccessSound(player);
            MessageUtils.sendPrefixedMessage(player, "&7Enter new message place or '&edone&7' if you changed your mind about editing message place.");
            MessageUtils.sendPrefixedMessage(player, "&7Available message places:");
            Collection<MessagePlace> availableMessagePlaces = new ArrayList<>(3);
            if (!MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                availableMessagePlaces.add(MessagePlace.GAME_CHAT);
            }
            availableMessagePlaces.add(MessagePlace.SYSTEM_CHAT);
            availableMessagePlaces.add(MessagePlace.ACTION_BAR);
            for (MessagePlace availableMessagePlace : availableMessagePlaces) {
                MessageUtils.sendMessage(player, " &8- &e" + availableMessagePlace.name() + " &7(&e" + availableMessagePlace.getFriendlyName() + "&7)");
            }
        } else if (slot == 48) {
            player.closeInventory();
            MessageUtils.sendSuccessSound(player);
        } else if (slot == 50) {
            File file = new File(this.plugin.getDataFolder(), "edits" + File.separator + editData.getFileName() + ".yml");
            if (file.exists()) {
                MessageUtils.sendErrorSound(player);
                MessageUtils.sendPrefixedMessage(player, "&cThere is already a message edit that uses a file with the name '&7" + editData.getFileName() + ".yml&c'.");
                return;
            }
            String oldMessagePatternString = editData.getOldMessagePattern();
            Pattern oldMessagePattern = Pattern.compile(oldMessagePatternString);
            Matcher oldMessagePatternMatcher = oldMessagePattern.matcher(editData.getOriginalOldMessage());
            MessagePlace oldMessagePlace = editData.getOldMessagePlace();
            String newMessage = editData.getNewMessage();
            newMessage = newMessage.replace("\\", "\\\\");
            if (oldMessagePatternMatcher.matches()) {
                StringJoiner excludePattern = new StringJoiner("|", "(?!", ")");
                excludePattern.add("\\$0");
                for (int groupId = 0; groupId < oldMessagePatternMatcher.groupCount(); groupId += 1) {
                    excludePattern.add("\\$" + (groupId + 1));
                }
                String excludePatternString = excludePattern + "\\$[0-9]+";
                newMessage = newMessage.replaceAll(excludePatternString, "\\\\$0");
            } else {
                newMessage = newMessage.replace("$", "\\$");
            }
            MessagePlace newMessagePlace = editData.getNewMessagePlace();
            MessageEdit edit = new MessageEdit(oldMessagePatternString, oldMessagePlace, newMessage, newMessagePlace);
            this.plugin.addMessageEdit(edit);
            this.plugin.clearCachedMessages();
            try {
                if (file.createNewFile()) {
                    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                    for (Map.Entry<String, Object> entry : edit.serialize().entrySet()) {
                        configuration.set(entry.getKey(), entry.getValue());
                    }
                    configuration.save(file);
                    MessageUtils.sendSuccessSound(player);
                    MessageUtils.sendPrefixedMessage(player, "&7Message edit has been saved and applied.");
                } else {
                    MessageUtils.sendErrorSound(player);
                    MessageUtils.sendPrefixedMessage(player, "&cCould not create message edit file.");
                }
            } catch (IOException exception) {
                this.plugin.getLogger().log(Level.WARNING, "Could not save message edit.", exception);
                MessageUtils.sendErrorSound(player);
                MessageUtils.sendPrefixedMessage(player, "&cCould not save message edit, check console for more information.");
            }
            player.closeInventory();
        }
    }
}
