package dev.jaqobb.message_editor.listener.packet;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessagePlace;

public class DisconnectPacketListener extends CommonPacketListener {
    
    public DisconnectPacketListener(MessageEditorPlugin plugin) {
        super(plugin, MessagePlace.DISCONNECT);
    }
}
