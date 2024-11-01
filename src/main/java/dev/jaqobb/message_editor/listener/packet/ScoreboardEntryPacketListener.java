package dev.jaqobb.message_editor.listener.packet;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessagePlace;

public class ScoreboardEntryPacketListener extends CommonPacketListener {
    
    public ScoreboardEntryPacketListener(MessageEditorPlugin plugin) {
        super(plugin, MessagePlace.SCOREBOARD_ENTRY);
    }
    
    @Override
    public boolean shouldProcess(PacketContainer packet) {
        return packet.getScoreboardActions().readSafely(0) != EnumWrappers.ScoreboardAction.REMOVE;
    }
}
