package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Level;

public class CEConfig {

    private String fileName;
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private String route;
    private ConditionalEvents plugin;
    private String folderName;
    private boolean firstTime;

    public CEConfig(String fileName,ConditionalEvents plugin, String folderName){
        this.fileName = fileName;
        this.plugin = plugin;
        this.firstTime = false;
        this.folderName = folderName;
    }

    public String getPath(){
        return this.fileName;
    }

    public void registerConfig(){
        if(folderName != null){
            file = new File(plugin.getDataFolder() +File.separator + folderName,fileName);
        }else{
            file = new File(plugin.getDataFolder(), fileName);
        }

        route = file.getPath();
        if(!file.exists()){
            firstTime = true;
            this.getConfig().options().copyDefaults(true);
            saveConfig();
            firstTime = false;
        }

    }
    public void saveConfig() {
        try {
            fileConfiguration.save(file);
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

    public boolean reloadConfig() {
        if (fileConfiguration == null) {
            if(folderName != null){
                file = new File(plugin.getDataFolder() +File.separator + folderName, fileName);
            }else{
                file = new File(plugin.getDataFolder(), fileName);
            }

        }
        fileConfiguration = loadConfiguration(file);

        if(firstTime){
            Reader defConfigStream;
            try {
                if(folderName != null){
                    defConfigStream = new InputStreamReader(plugin.getResource(folderName+"/"+fileName), "UTF8");
                }else{
                    defConfigStream = new InputStreamReader(plugin.getResource(fileName), "UTF8");
                }

                if (defConfigStream != null) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                    fileConfiguration.setDefaults(defConfig);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if(fileConfiguration == null){
            return false;
        }

        return true;
    }

    public static YamlConfiguration loadConfiguration(@NotNull File file) {
        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException ex) {

        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
            return null;
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
            return null;
        }

        return config;
    }

    public String getRoute() {
        return route;
    }
}
