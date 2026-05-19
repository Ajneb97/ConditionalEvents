package ce.ajneb97.configs.model;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CommonConfig {

    private String filePath;
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private String fullPath;
    private ConditionalEvents plugin;
    private String folderName;
    private boolean newFile;
    private boolean isFirstTime;

    public CommonConfig(String filePath, ConditionalEvents plugin, String folderName, boolean newFile){
        this.filePath = filePath;
        this.plugin = plugin;
        this.newFile = newFile;
        this.folderName = folderName;
        this.isFirstTime = false;
    }

    public String getFilePath(){
        return this.filePath;
    }

    public String getFileName(){
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
    }

    public void registerConfig(){
        if(folderName != null){
            file = new File(plugin.getDataFolder() +File.separator + folderName, filePath);
        }else{
            file = new File(plugin.getDataFolder(), filePath);
        }

        fullPath = file.getPath();

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
                    plugin.saveResource(folderName+File.separator+filePath, false);
                }else{
                    plugin.saveResource(filePath, false);
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
                file = new File(plugin.getDataFolder() +File.separator + folderName, filePath);
            }else{
                file = new File(plugin.getDataFolder(), filePath);
            }

        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        if(file != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
            fileConfiguration.setDefaults(defConfig);
        }
        return true;
    }

    public String getFullPath() {
        return fullPath;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public void setFirstTime(boolean firstTime) {
        isFirstTime = firstTime;
    }
}
