package ce.ajneb97.utils;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Utilidades {

	public static int getNumeroAleatorio(int min, int max) {
		Random r = new Random();
		int numero = r.nextInt((max - min) + 1) + min;
		return numero;
	}
	
	public static boolean esVersionNueva() {
		if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") ||
				Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.16")
				 || Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18")
				 || Bukkit.getVersion().contains("1.19")) {
			return true;
		}else {
			return false;
		}
	}
}
