package dev.jaqobb.message_editor;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.jaqobb.message_editor.command.MessageEditorCommand;
import dev.jaqobb.message_editor.command.MessageEditorCommandTabCompleter;
import dev.jaqobb.message_editor.listener.packet.BossBarPacketListener;
import dev.jaqobb.message_editor.listener.packet.ChatPacketListener;
import dev.jaqobb.message_editor.listener.packet.DisconnectPacketListener;
import dev.jaqobb.message_editor.listener.packet.EntityNamePacketListener;
import dev.jaqobb.message_editor.listener.packet.InventoryItemsPacketListener;
import dev.jaqobb.message_editor.listener.packet.InventoryTitlePacketListener;
import dev.jaqobb.message_editor.listener.packet.KickPacketListener;
import dev.jaqobb.message_editor.listener.packet.ScoreboardEntryPacketListener;
import dev.jaqobb.message_editor.listener.packet.ScoreboardTitlePacketListener;
import dev.jaqobb.message_editor.listener.player.PlayerChatListener;
import dev.jaqobb.message_editor.listener.player.PlayerInventoryClickListener;
import dev.jaqobb.message_editor.listener.player.PlayerInventoryCloseListener;
import dev.jaqobb.message_editor.listener.player.PlayerJoinListener;
import dev.jaqobb.message_editor.listener.player.PlayerKickListener;
import dev.jaqobb.message_editor.listener.player.PlayerQuitListener;
import dev.jaqobb.message_editor.listener.plugin.PluginDisableListener;
import dev.jaqobb.message_editor.listener.plugin.PluginEnableListener;
import dev.jaqobb.message_editor.menu.MenuManager;
import dev.jaqobb.message_editor.message.MessageData;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessageEditData;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.updater.Updater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

// TODO: Fix disconnect message place (it just does not seem to be working at all?)?
public class MessageEditorPlugin extends JavaPlugin {
    
    static {
        ConfigurationSerialization.registerClass(MessageEdit.class);
    }
    
    private boolean updateNotify;
    private Updater updater;
    private List<MessageEdit> messageEdits;
    private boolean attachSpecialHoverAndClickEvents;
    private boolean placeholderApiPresent;
    private MenuManager menuManager;
    private Cache<String, Map.Entry<MessageEdit, String>> cachedMessages;
    private Cache<String, MessageData> cachedMessagesData;
    private Map<UUID, MessageEditData> currentMessageEditsData;
    
    @Override
    public void onLoad() {
        MinecraftVersion requiredVersion = null;
        for (MessagePlace place : MessagePlace.VALUES) {
            MinecraftVersion version = place.getMinimumRequiredMinecraftVersion();
            if (requiredVersion == null || requiredVersion.compareTo(version) > 0) {
                requiredVersion = version;
            }
        }
        if (!requiredVersion.atOrAbove()) {
            this.getLogger().log(Level.WARNING, "Your server does not support any message places.");
            this.getLogger().log(Level.WARNING, "The minimum required server version is " + requiredVersion.getVersion() + ".");
            this.getLogger().log(Level.WARNING, "Disabling plugin...");
            this.setEnabled(false);
            return;
        }
        this.getLogger().log(Level.INFO, "Loading configuration...");
        this.saveDefaultConfig();
        this.reloadConfig();
        PluginManager pluginManager = this.getServer().getPluginManager();
        this.placeholderApiPresent = pluginManager.getPlugin("PlaceholderAPI") != null;
        this.getLogger().log(Level.INFO, "PlaceholderAPI: " + (this.placeholderApiPresent ? "found" : "not found") + ".");
        this.cachedMessages = CacheBuilder.newBuilder()
            .expireAfterAccess(15L, TimeUnit.MINUTES)
            .build();
        this.cachedMessagesData = CacheBuilder.newBuilder()
            .expireAfterAccess(15L, TimeUnit.MINUTES)
            .build();
        this.currentMessageEditsData = new HashMap<>();
    }
    
    @Override
    public void onEnable() {
        this.getLogger().log(Level.INFO, "Starting updater...");
        this.updater = new Updater(this, 82154);
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, this.updater, 0L, 20L * 60L * 60L);
        this.getLogger().log(Level.INFO, "Starting menu manager...");
        this.menuManager = new MenuManager(this);
        this.getLogger().log(Level.INFO, "Registering command...");
        this.getCommand("message-editor").setExecutor(new MessageEditorCommand(this));
        this.getCommand("message-editor").setTabCompleter(new MessageEditorCommandTabCompleter());
        this.getLogger().log(Level.INFO, "Registering listeners...");
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PluginEnableListener(this), this);
        pluginManager.registerEvents(new PluginDisableListener(this), this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
        pluginManager.registerEvents(new PlayerKickListener(this), this);
        pluginManager.registerEvents(new PlayerInventoryCloseListener(this), this);
        pluginManager.registerEvents(new PlayerInventoryClickListener(this), this);
        pluginManager.registerEvents(new PlayerChatListener(this), this);
        this.getLogger().log(Level.INFO, "Registering packet listeners...");
        Map<String, PacketAdapter> packetListeners = new HashMap<>();
        packetListeners.put("chat", new ChatPacketListener(this));
        packetListeners.put("kick", new KickPacketListener(this));
        packetListeners.put("disconnect", new DisconnectPacketListener(this));
        packetListeners.put("bossbar", new BossBarPacketListener(this));
        packetListeners.put("scoreboard-title", new ScoreboardTitlePacketListener(this));
        packetListeners.put("scoreboard-entry", new ScoreboardEntryPacketListener(this));
        packetListeners.put("inventory-title", new InventoryTitlePacketListener(this));
        packetListeners.put("inventory-item", new InventoryItemsPacketListener(this));
        packetListeners.put("entity-name", new EntityNamePacketListener(this));
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        for (Map.Entry<String, PacketAdapter> packetListener : packetListeners.entrySet()) {
            if (this.getConfig().getBoolean("packet-listeners." + packetListener.getKey(), true)) {
                protocolManager.addPacketListener(packetListener.getValue());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.updateNotify = this.getConfig().getBoolean("update.notify", true);
        this.attachSpecialHoverAndClickEvents = this.getConfig().getBoolean("attach-special-hover-and-click-events", true);
        this.messageEdits = (List<MessageEdit>) this.getConfig().getList("message-edits");
        File editsDirectory = new File(this.getDataFolder(), "edits");
        if (!editsDirectory.exists()) {
            if (!editsDirectory.mkdir()) {
                this.getLogger().log(Level.WARNING, "Could not create 'edits' directory.");
                return;
            }
            List<String> resources = new ArrayList<>();
            URL resourceDirectory = this.getClassLoader().getResource("edits");
            String jarPath = resourceDirectory.getPath().substring(5, resourceDirectory.getPath().indexOf('!'));
            try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!name.startsWith("edits")) {
                        continue;
                    }
                    String lastCharacter = name.substring(name.length() - 1);
                    if (lastCharacter.equals(File.separator)) {
                        continue;
                    }
                    String clearName = name.substring("edits".length() + 1);
                    if (!clearName.endsWith(".yml")) {
                        continue;
                    }
                    if (clearName.substring(0, clearName.length() - 4).isEmpty()) {
                        continue;
                    }
                    resources.add("edits/" + clearName);
                }
            } catch (IOException exception) {
                this.getLogger().log(Level.WARNING, "Could not copy default edits.", exception);
            }
            for (String resource : resources) {
                this.saveResource(resource, false);
            }
        }
        for (File editFile : editsDirectory.listFiles()) {
            String name = editFile.getName();
            if (!name.isEmpty() && name.charAt(0) == '#') {
                continue;
            }
            if (!name.endsWith(".yml")) {
                continue;
            }
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(editFile);
            this.messageEdits.add(MessageEdit.deserialize(configuration.getRoot().getValues(false)));
        }
    }
    
    public boolean isUpdateNotify() {
        return this.updateNotify;
    }
    
    public Updater getUpdater() {
        return this.updater;
    }
    
    public List<MessageEdit> getMessageEdits() {
        return Collections.unmodifiableList(this.messageEdits);
    }
    
    public void addMessageEdit(MessageEdit messageEdit) {
        this.messageEdits.add(messageEdit);
    }
    
    public boolean isAttachSpecialHoverAndClickEvents() {
        return this.attachSpecialHoverAndClickEvents;
    }
    
    public boolean isPlaceholderApiPresent() {
        return this.placeholderApiPresent;
    }
    
    public void setPlaceholderApiPresent(boolean present) {
        this.placeholderApiPresent = present;
    }
    
    public MenuManager getMenuManager() {
        return this.menuManager;
    }
    
    public Set<String> getCachedMessages() {
        return Collections.unmodifiableSet(this.cachedMessages.asMap().keySet());
    }
    
    public Map.Entry<MessageEdit, String> getCachedMessage(String messageBefore) {
        return this.cachedMessages.getIfPresent(messageBefore);
    }
    
    public void cacheMessage(String messageBefore, MessageEdit edit, String messageAfter) {
        this.cachedMessages.put(messageBefore, new AbstractMap.SimpleEntry<>(edit, messageAfter));
    }
    
    public void uncacheMessage(String messageBefore) {
        this.cachedMessages.invalidate(messageBefore);
    }
    
    public void clearCachedMessages() {
        this.cachedMessages.invalidateAll();
    }
    
    public Set<String> getCachedMessagesData() {
        return Collections.unmodifiableSet(this.cachedMessagesData.asMap().keySet());
    }
    
    public MessageData getCachedMessageData(String id) {
        return this.cachedMessagesData.getIfPresent(id);
    }
    
    public void cacheMessageData(String id, MessageData data) {
        this.cachedMessagesData.put(id, data);
    }
    
    public void uncacheMessageData(String id) {
        this.cachedMessagesData.invalidate(id);
    }
    
    public void clearCachedMessagesData() {
        this.cachedMessagesData.invalidateAll();
    }
    
    public Map<UUID, MessageEditData> getCurrentMessageEditsData() {
        return Collections.unmodifiableMap(this.currentMessageEditsData);
    }
    
    public MessageEditData getCurrentMessageEditData(UUID uuid) {
        return this.currentMessageEditsData.get(uuid);
    }
    
    public void setCurrentMessageEdit(UUID uuid, MessageEditData editData) {
        this.currentMessageEditsData.put(uuid, editData);
    }
    
    public void removeCurrentMessageEditData(UUID uuid) {
        this.currentMessageEditsData.remove(uuid);
    }
    
    public void clearCurrentMessageEditsData() {
        this.currentMessageEditsData.clear();
    }
}
