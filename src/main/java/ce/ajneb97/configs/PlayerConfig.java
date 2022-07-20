package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;


public class PlayerConfig {

	private FileConfiguration config;
	private File configFile;
	private String filePath;
	private ConditionalEvents plugin;
	
	public PlayerConfig(String filePath, ConditionalEvents plugin){
		this.config = null;
		this.configFile = null;
		this.filePath = filePath;
		this.plugin = plugin;
	}
	
	public String getPath(){
		return this.filePath;
	}
	
	public FileConfiguration getConfig(){
		 if (config == null) {
		        reloadPlayerConfig();
		    }
		return this.config;
	}
	
	public void registerPlayerConfig(){
		  configFile = new File(plugin.getDataFolder() +File.separator + "players",filePath);
		  if(!configFile.exists()){
			  try {
				configFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }		  
		  config = new YamlConfiguration();
		  try {
	            config.load(configFile);
	      } catch (IOException e) {
	            e.printStackTrace();
	      } catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void savePlayerConfig() {
		 try {
			 config.save(configFile);
		 } catch (IOException e) {
			 e.printStackTrace();
	 	}
	 }
	  
	public void reloadPlayerConfig() {
		    if (config == null) {
		    	configFile = new File(plugin.getDataFolder() +File.separator + "players", filePath);
		    }
		    config = YamlConfiguration.loadConfiguration(configFile);

			if (configFile != null) {
			    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(configFile);
			    config.setDefaults(defConfig);
			}	    
		}
}
