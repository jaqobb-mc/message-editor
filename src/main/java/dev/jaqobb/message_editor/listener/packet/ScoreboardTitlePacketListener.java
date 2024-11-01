package dev.jaqobb.message_editor.listener.packet;

import com.comphenix.protocol.events.PacketContainer;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessagePlace;

public class ScoreboardTitlePacketListener extends CommonPacketListener {
    
    public ScoreboardTitlePacketListener(MessageEditorPlugin plugin) {
        super(plugin, MessagePlace.SCOREBOARD_TITLE);
    }
    
    @Override
    public boolean shouldProcess(PacketContainer packet) {
        // We are only interested in packets that create or update scoreboard objectives, as they contain the title.
        // In this case, action of 0 indicates that a scoreboard objective is being created, and action of 2 indicates that a scoreboard objective is being updated.
        int action = packet.getIntegers().readSafely(0);
        return action == 0 || action == 2;
    }
}
