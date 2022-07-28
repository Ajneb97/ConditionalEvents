package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class CEConfig {

    private String fileName;
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private String route;
    private ConditionalEvents plugin;
    private boolean firstTime;
    public CEConfig(String fileName,ConditionalEvents plugin){
        this.fileName = fileName;
        this.plugin = plugin;
        this.firstTime = false;
    }

    public void registerConfig(){
        file = new File(plugin.getDataFolder(), fileName);
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

    public void reloadConfig() {
        if (fileConfiguration == null) {
            file = new File(plugin.getDataFolder(), fileName);
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        if(firstTime){
            Reader defConfigStream;
            try {
                defConfigStream = new InputStreamReader(plugin.getResource(fileName), "UTF8");
                if (defConfigStream != null) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                    fileConfiguration.setDefaults(defConfig);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

    public String getRoute() {
        return route;
    }
}
