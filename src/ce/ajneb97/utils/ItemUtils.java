package ce.ajneb97.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.clip.placeholderapi.PlaceholderAPI;

public class ItemUtils {
	
	private String nombre;
	private String material;
	private short durability;
	private String loreString;
	private List<String> loreList;
	
	public ItemUtils(ItemStack item) {
		nombre = "";
		material = "";
		loreString = "";
		durability = 0;
		loreList = new ArrayList<String>();
		
		if(item != null) {
			durability = item.getDurability();
			material = item.getType().name();
			if(item.hasItemMeta()) {
				ItemMeta meta = item.getItemMeta();
				if(meta.hasDisplayName()) {
					nombre = ChatColor.stripColor(meta.getDisplayName());
				}
				if(meta.hasLore()) {
					List<String> lore = meta.getLore();
					for(int i=0;i<lore.size();i++) {
						loreList.add(ChatColor.stripColor(lore.get(i)));
						if(i == lore.size()-1) {
							loreString = loreString+ChatColor.stripColor(lore.get(i));
						}else {
							loreString = loreString+ChatColor.stripColor(lore.get(i))+" ";
						}
					}
				}
			}
		}
	}

	public String getNombre() {
		return nombre;
	}

	public String getMaterial() {
		return material;
	}

	public short getDurability() {
		return durability;
	}

	public String getLoreString() {
		return loreString;
	}

	public List<String> getLoreList() {
		return loreList;
	}

	@SuppressWarnings("deprecation")
	public static void removeItem(Player player,String textLine) {
		// remove_item: <id>;<amount>;datavalue: <datavalue>;name: <name>;lorecontains: <lore_line>
		// remove_item: %checkitem_remove...%
		if(textLine.startsWith("remove_item: %checkitem")) {
			if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
				return;
			}
			textLine = textLine.replace("remove_item: ", "");
			PlaceholderAPI.setPlaceholders(player, textLine);
			return;
		}
		String[] sep = textLine.replace("remove_item: ", "").split(";");
		
		String material = sep[0];
		int amount = Integer.valueOf(sep[1]);
		short datavalue = 0;
		String name = null;
		String loreContainsLoreLine = null;
		
		for(String sepLine : sep) {
			if(sepLine.startsWith("datavalue: ")) {
				datavalue = Short.valueOf(sepLine.replace("datavalue: ", ""));
			}else if(sepLine.startsWith("name: ")) {
				name = sepLine.replace("name: ", "");
			}else if(sepLine.startsWith("lorecontains: ")) {
				loreContainsLoreLine = sepLine.replace("lorecontains: ", "");
			}
		}
		
		ItemStack[] contents = player.getInventory().getContents();
		for(int i=0;i<contents.length;i++) {
			if(contents[i] != null && !contents[i].getType().equals(Material.AIR)) {
				if(!contents[i].getType().name().equals(material)) {
					continue;
				}
				
				if(contents[i].getDurability() != datavalue) {
					continue;
				}
				
				if(contents[i].hasItemMeta()) {
					ItemMeta meta = contents[i].getItemMeta();
					if(meta.hasDisplayName()) {
						if(name != null) {
							if(!ChatColor.stripColor(meta.getDisplayName()).equals(ChatColor.stripColor(name))) {
								continue;
							}
						}	
					}
					
					if(meta.hasLore()) {
						if(loreContainsLoreLine != null) {
							List<String> lore = meta.getLore();
							boolean contiene = false;
							for(String linea : lore) {
								if(ChatColor.stripColor(linea).contains(loreContainsLoreLine)) {
									contiene = true;
									break;
								}
							}
							if(!contiene) {
								continue;
							}
						}
					}
				}else {
					if(name != null || loreContainsLoreLine != null) {
						continue;
					}
				}
				
				int cantidadActual = contents[i].getAmount();
				if(cantidadActual > amount) {
					contents[i].setAmount(cantidadActual-amount);
					break;
				}else {
					
					amount = amount-cantidadActual;
					if(Utilidades.esVersionNueva()) {
						contents[i].setAmount(0);
					}else {
						player.getInventory().setItem(i, null);
					}
				}
			}
		}
	}
}
