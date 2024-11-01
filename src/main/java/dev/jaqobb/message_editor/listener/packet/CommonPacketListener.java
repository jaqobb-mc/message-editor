package dev.jaqobb.message_editor.listener.packet;

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
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.regex.Matcher;

public class CommonPacketListener extends PacketAdapter {
    
    private final MessagePlace messagePlace;
    
    public CommonPacketListener(MessageEditorPlugin plugin, MessagePlace messagePlace) {
        super(plugin, ListenerPriority.HIGHEST, messagePlace.getPacketTypes());
        this.messagePlace = messagePlace;
    }
    
    @Override
    public MessageEditorPlugin getPlugin() {
        return (MessageEditorPlugin) this.plugin;
    }
    
    public boolean shouldProcess(PacketContainer packet) {
        return true;
    }
    
    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }
        PacketContainer packet = event.getPacket().shallowClone();
        if (!this.shouldProcess(packet)) {
            return;
        }
        Player player = event.getPlayer();
        String originalMessage = this.messagePlace.getMessage(packet);
        String message = originalMessage;
        if (message == null) {
            return;
        }
        Map.Entry<MessageEdit, String> cachedMessage = this.getPlugin().getCachedMessage(message);
        MessageEdit messageEdit = null;
        Matcher messageEditMatcher = null;
        if (cachedMessage == null) {
            for (MessageEdit edit : this.getPlugin().getMessageEdits()) {
                MessagePlace place = edit.getMessageBeforePlace();
                if (place != null && place != this.messagePlace) {
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
                this.getPlugin().cacheMessage(message, messageEdit, newMessage);
                message = newMessage;
            }
        }
        boolean json = MessageUtils.isJson(message);
        String id = MessageUtils.generateId(this.messagePlace);
        this.getPlugin().cacheMessageData(id, new MessageData(id, this.messagePlace, message, json));
        if (this.messagePlace.isAnalyzing()) {
            MessageUtils.logMessage(this.getPlugin().getLogger(), this.messagePlace, player, id, json, message);
        }
        if (!message.equals(originalMessage)) {
            this.messagePlace.setMessage(packet, message, json);
            event.setPacket(packet);
        }
    }
}
