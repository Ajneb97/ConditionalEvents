package ce.ajneb97.configs;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlayerConfig {

	private FileConfiguration config;
	private Path filePath;
	
	public PlayerConfig(Path path){
		this.config = null;
		this.filePath = path;
	}
	
	public Path getPath(){
		return this.filePath;
	}
	
	public FileConfiguration getConfig(){
		 if (config == null) {
		        reloadPlayerConfig();
		    }
		return this.config;
	}
	
	public void registerPlayerConfig(){
		if(Files.notExists(filePath)){
			try {
				Files.createFile(filePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	  
		config = new YamlConfiguration();
		try {
			config.load(filePath.toFile());
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void savePlayerConfig() {
		 try {
			 config.save(filePath.toFile());
		 } catch (IOException e) {
			 e.printStackTrace();
	 	}
	 }
	  
	public void reloadPlayerConfig() {
		config = YamlConfiguration.loadConfiguration(filePath.toFile());    
	}
}
