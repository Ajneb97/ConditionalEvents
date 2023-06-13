package ce.ajneb97.configs;


import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.utils.OtherUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataFolderConfigManager {

    protected ArrayList<CEConfig> configs;
    protected ConditionalEvents plugin;
    private String folderName;

    public DataFolderConfigManager(ConditionalEvents plugin, String folderName) {
        this.plugin = plugin;
        this.folderName = folderName;
        this.configs = new ArrayList<CEConfig>();
    }

    public void configure() {
        createFolder();
        reloadConfigs();
    }

    public void reloadConfigs(){
        this.configs = new ArrayList<CEConfig>();
        registerConfigs();
    }

    public void createFolder(){
        File folder;
        try {
            folder = new File(plugin.getDataFolder() + File.separator + folderName);
            if(!folder.exists()){
                folder.mkdirs();
                createExample();
            }
        } catch(SecurityException e) {
            folder = null;
        }
    }

    public void createExample(){
        String pathName = "more_events.yml";
        CEConfig config = new CEConfig(pathName,plugin,folderName);
        config.registerConfig();
    }

    public void saveConfigs() {
        for(int i=0;i<configs.size();i++) {
            configs.get(i).saveConfig();
        }
    }

    public void registerConfigs(){
        String path = plugin.getDataFolder() + File.separator + folderName;
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (int i=0;i<listOfFiles.length;i++) {
            if(listOfFiles[i].isFile()) {
                String pathName = listOfFiles[i].getName();
                String ext = OtherUtils.getFileExtension(pathName);
                if(!ext.equals("yml")) {
                    continue;
                }
                CEConfig config = new CEConfig(pathName, plugin, folderName);
                config.registerConfig();
                configs.add(config);
            }
        }
    }

    public ArrayList<CEConfig> getConfigs(){
        return this.configs;
    }

    public boolean fileAlreadyRegistered(String pathName) {
        for(int i=0;i<configs.size();i++) {
            if(configs.get(i).getPath().equals(pathName)) {
                return true;
            }
        }
        return false;
    }

    public CEConfig getConfig(String pathName) {
        for(int i=0;i<configs.size();i++) {
            if(configs.get(i).getPath().equals(pathName)) {
                return configs.get(i);
            }
        }
        return null;
    }

    public boolean registerConfig(String pathName) {
        if(!fileAlreadyRegistered(pathName)) {
            CEConfig config = new CEConfig(pathName, plugin, folderName);
            config.registerConfig();
            configs.add(config);
            return true;
        }else {
            return false;
        }
    }

}
