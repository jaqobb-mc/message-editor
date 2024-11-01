package dev.jaqobb.message_editor.listener.packet;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessagePlace;

public class KickPacketListener extends CommonPacketListener {
    
    public KickPacketListener(MessageEditorPlugin plugin) {
        super(plugin, MessagePlace.KICK);
    }
}
