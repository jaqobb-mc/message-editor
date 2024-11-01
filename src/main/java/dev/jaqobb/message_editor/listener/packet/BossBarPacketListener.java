package dev.jaqobb.message_editor.listener.packet;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.message.bossbar.BossBarAction;

public class BossBarPacketListener extends CommonPacketListener {
    
    public BossBarPacketListener(MessageEditorPlugin plugin) {
        super(plugin, MessagePlace.BOSS_BAR);
    }
    
    @Override
    public boolean shouldProcess(PacketContainer packet) {
        if (!MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
            BossBarAction action = packet.getEnumModifier(BossBarAction.class, 1).readSafely(0);
            return action == BossBarAction.ADD || action == BossBarAction.UPDATE_NAME;
        }
        // Chat component being present means it is either a packet for adding a boss bar or one for updating its name.
        return packet.getStructures().readSafely(1).getChatComponents().size() == 1;
    }
}
