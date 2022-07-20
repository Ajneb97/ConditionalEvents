package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CEConfig {

    private String fileName;
    private FileConfiguration fileConfiguration = null;
    private final Path file;
    private ConditionalEvents plugin;
    private boolean firstTime;
    public CEConfig(String fileName,ConditionalEvents plugin){
        this.file = plugin.getDataFolder().toPath().resolve(fileName);
        this.plugin = plugin;
        this.firstTime = false;
    }

    public void registerConfig(){
        if(Files.notExists(file)){
            firstTime = true;
            this.getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }
    public void saveConfig() {
        try {
            fileConfiguration.save(file.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }
        return fileConfiguration;
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file.toFile());

        if(firstTime){
            Reader defConfigStream = new InputStreamReader(plugin.getResource(fileName), StandardCharsets.UTF_8);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            fileConfiguration.setDefaults(defConfig);
        }

    }

    public String getRoute() {
        return this.file.toString();
    }
}
