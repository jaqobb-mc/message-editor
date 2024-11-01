package dev.jaqobb.message_editor.updater;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import javax.net.ssl.HttpsURLConnection;

public class Updater implements Runnable {
    
    private final MessageEditorPlugin plugin;
    private final int pluginId;
    private final String currentVersion;
    private String latestVersion;
    private Integer versionDifference;
    
    public Updater(MessageEditorPlugin plugin, int pluginId) {
        this.plugin = plugin;
        this.pluginId = pluginId;
        this.currentVersion = this.plugin.getDescription().getVersion();
        this.latestVersion = null;
        this.versionDifference = null;
    }
    
    public String getCurrentVersion() {
        return this.currentVersion;
    }
    
    public String getLatestVersion() {
        return this.latestVersion;
    }
    
    public Integer getVersionDifference() {
        return this.versionDifference;
    }
    
    public String getUpdateMessage() {
        if (this.currentVersion.contains("-SNAPSHOT")) {
            return "&cYou are running a development version of &7Message Editor &c(&7" + this.currentVersion + "&c). Development versions may be unstable. As such, please avoid running them on production servers.";
        }
        if (this.versionDifference == null || this.latestVersion == null) {
            return "&cCould not retrieve the latest version of &7Message Editor&c.";
        }
        if (this.versionDifference < 0) {
            return "&7You are running an outdated version of &eMessage Editor &7(&e" + this.currentVersion + " &7< &e" + this.latestVersion + "&7). Consider updating to receive new features, bug fixes and more.";
        }
        if (this.versionDifference > 0) {
            return "&7You are running a future version of &eMessage Editor &7(&e" + this.currentVersion + " &7> &e" + this.latestVersion + "&7). I suppose you are a time traveler.";
        }
        return "&7You are running the latest version of &eMessage Editor&7.";
    }
    
    @Override
    public void run() {
        if (this.currentVersion.contains("-SNAPSHOT")) {
            return;
        }
        try {
            HttpsURLConnection connection = (HttpsURLConnection) URL.of(URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + this.pluginId), null).openConnection();
            connection.setRequestMethod("GET");
            try (InputStream input = connection.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                this.latestVersion = reader.readLine();
                String[] currentVersionData = this.currentVersion.split("\\.");
                String[] latestVersionData = this.latestVersion.split("\\.");
                if (currentVersionData.length == 3 && latestVersionData.length == 3) {
                    int majorDifference = Integer.compare(Integer.parseInt(currentVersionData[0]), Integer.parseInt(latestVersionData[0]));
                    if (majorDifference != 0) {
                        this.versionDifference = majorDifference;
                    } else {
                        int minorDifference = Integer.compare(Integer.parseInt(currentVersionData[1]), Integer.parseInt(latestVersionData[1]));
                        if (minorDifference != 0) {
                            this.versionDifference = minorDifference;
                        } else {
                            this.versionDifference = Integer.compare(Integer.parseInt(currentVersionData[2]), Integer.parseInt(latestVersionData[2]));
                        }
                    }
                }
            }
            connection.disconnect();
        } catch (Exception exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not retrieve the latest version data.", exception);
        }
    }
}
