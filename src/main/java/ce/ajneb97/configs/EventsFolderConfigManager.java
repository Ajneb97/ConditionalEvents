package ce.ajneb97.configs;


import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.configs.model.CommonConfig;

public class EventsFolderConfigManager extends DataFolderConfigManager{

    public EventsFolderConfigManager(ConditionalEvents plugin, String folderName) {
        super(plugin, folderName);
    }

    @Override
    public void createFiles() {
        new CommonConfig("more_events.yml",plugin,folderName,false).registerConfig();
    }

    @Override
    public void loadConfigs() {

    }

    @Override
    public void saveConfigs() {

    }


}
