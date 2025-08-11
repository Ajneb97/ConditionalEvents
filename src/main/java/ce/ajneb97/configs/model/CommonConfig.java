package ce.ajneb97.configs.model;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CommonConfig {

    private String fileName;
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private String route;
    private ConditionalEvents plugin;
    private String folderName;
    private boolean newFile;
    private boolean isFirstTime;

    public CommonConfig(String fileName, ConditionalEvents plugin, String folderName, boolean newFile){
        this.fileName = fileName;
        this.plugin = plugin;
        this.newFile = newFile;
        this.folderName = folderName;
        this.isFirstTime = false;
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
            isFirstTime = true;
            if(newFile) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                if(folderName != null){
                    plugin.saveResource(folderName+File.separator+fileName, false);
                }else{
                    plugin.saveResource(fileName, false);
                }

            }
        }

        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
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
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        if(file != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
            fileConfiguration.setDefaults(defConfig);
        }
        return true;
    }

    public String getRoute() {
        return route;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public void setFirstTime(boolean firstTime) {
        isFirstTime = firstTime;
    }
}
