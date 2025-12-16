package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.configs.model.CommonConfig;

import java.io.File;
import java.util.ArrayList;

public abstract class DataFolderConfigManager {
    protected String folderName;
    protected ConditionalEvents plugin;

    public DataFolderConfigManager(ConditionalEvents plugin, String folderName){
        this.plugin = plugin;
        this.folderName = folderName;
    }

    public void configure() {
        createFolder();
        loadConfigs();
    }

    public void createFolder(){
        File folder;
        try {
            folder = new File(plugin.getDataFolder() + File.separator + folderName);
            if(!folder.exists()){
                folder.mkdirs();
                createFiles();
            }
        } catch(SecurityException e) {
            folder = null;
        }
    }

    public CommonConfig getConfigFile(String pathName,boolean create) {
        String pathFile = plugin.getDataFolder() + File.separator + folderName;
        File folder = new File(pathFile);
        File file = new File(folder, pathName);
        if(!file.exists() && !create) {
            return null;
        }

        CommonConfig commonConfig = new CommonConfig(pathName, plugin, folderName, true);
        commonConfig.registerConfig();
        return commonConfig;
    }

    public ArrayList<CommonConfig> getConfigs(){
        ArrayList<CommonConfig> configs = new ArrayList<>();

        String pathFile = plugin.getDataFolder() + File.separator + folderName;
        File folder = new File(pathFile);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String pathName = file.getName();
                CommonConfig commonConfig = new CommonConfig(pathName, plugin, folderName, true);
                commonConfig.registerConfig();
                configs.add(commonConfig);
            }
        }

        return configs;
    }

    public abstract void createFiles();

    public abstract void loadConfigs();

    public abstract void saveConfigs();
}
