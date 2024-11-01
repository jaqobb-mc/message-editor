package dev.jaqobb.message_editor.listener.packet;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessagePlace;

public class EntityNamePacketListener extends CommonPacketListener {
    
    public EntityNamePacketListener(MessageEditorPlugin plugin) {
        super(plugin, MessagePlace.ENTITY_NAME);
    }
}
