package ce.ajneb97.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.Variable;
import ce.ajneb97.eventos.Evento;
import ce.ajneb97.eventos.TipoEvento;
import ce.ajneb97.libs.armorequipevent.ArmorEquipEvent;
import ce.ajneb97.libs.itemselectevent.ItemSelectEvent;
import ce.ajneb97.utils.BlockUtils;
import ce.ajneb97.utils.EventoUtils;
import ce.ajneb97.utils.ItemUtils;
import net.md_5.bungee.api.ChatColor;

public class JugadorListener implements Listener{

	public ConditionalEvents plugin;
	public JugadorListener(ConditionalEvents plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alEntrar(PlayerJoinEvent event) {
		Player jugador = event.getPlayer();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_JOIN);
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alSalir(PlayerQuitEvent event) {
		Player jugador = event.getPlayer();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_LEAVE);
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alRespawnear(PlayerRespawnEvent event) {
		Player jugador = event.getPlayer();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_RESPAWN);
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alMorir(PlayerDeathEvent event) {
		Player jugador = event.getEntity();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_DEATH);
		String cause = "";
		if(jugador.getLastDamageCause() != null) {
			cause = jugador.getLastDamageCause().getCause().name();
		}
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%cause%", cause);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alInteractuarBloque(PlayerInteractEvent event) {
		Player jugador = event.getPlayer();
		Block block = event.getClickedBlock();
		if(!Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.9")) {
			if(!event.getAction().equals(Action.PHYSICAL) && (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND))) {
				return;
			}
		}
		
		if(block != null) {
			ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.BLOCK_INTERACT);
			if(eventos.size() == 0) {
				return;
			}
			Location l = block.getLocation();
			String action = "";
			boolean shifteando = jugador.isSneaking();
			if(shifteando) {
				if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					action = "SHIFT_RIGHT_CLICK";
				}else if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
					action = "SHIFT_LEFT_CLICK";
				}
			}else {
				if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					action = "RIGHT_CLICK";
				}else if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
					action = "LEFT_CLICK";
				}else if(event.getAction().equals(Action.PHYSICAL)) {
					action = "PHYSICAL";
				}
			}

			ItemStack item = jugador.getItemInHand();
			ItemUtils itemUtils = new ItemUtils(item);
			String materialItem = itemUtils.getMaterial();
			short durability = itemUtils.getDurability();
			String nombreItem = itemUtils.getNombre();
			List<String> lore = itemUtils.getLoreList();
			String loreItem = itemUtils.getLoreString();
			String textureData = BlockUtils.getHeadTextureData(block);
						
			for(Evento e : eventos) {
				ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
				EventoUtils.remplazarVariable(variables, "%action_type%", action);
				EventoUtils.remplazarVariable(variables, "%block_x%", l.getBlockX()+"");
				EventoUtils.remplazarVariable(variables, "%block_y%", l.getBlockY()+"");
				EventoUtils.remplazarVariable(variables, "%block_z%", l.getBlockZ()+"");
				EventoUtils.remplazarVariable(variables, "%block_world%", l.getWorld().getName());
				EventoUtils.remplazarVariable(variables, "%block%", block.getType().name());
				EventoUtils.remplazarVariable(variables, "%item%", materialItem);
				EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
				EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
				for(int i=0;i<lore.size();i++) {
					EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
				}
				EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
				EventoUtils.remplazarVariable(variables, "%block_head_texture%", textureData);
				EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alInteractuarConEntidad(PlayerInteractEntityEvent event) {
		if(!Bukkit.getVersion().contains("1.8") && !event.getHand().equals(EquipmentSlot.HAND)) {
			return;
		}
		Player jugador = event.getPlayer();
		Entity entity = event.getRightClicked();
		if(entity != null) {
			ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.ENTITY_INTERACT);
			if(eventos.size() == 0) {
				return;
			}
			ItemStack item = jugador.getItemInHand();
			ItemUtils itemUtils = new ItemUtils(item);
			String materialItem = itemUtils.getMaterial();
			short durability = itemUtils.getDurability();
			String nombreItem = itemUtils.getNombre();
			List<String> lore = itemUtils.getLoreList();
			String loreItem = itemUtils.getLoreString();
			String tipoEntidad = entity.getType().name();
			String nombreEntidad = "";
			if(entity.getCustomName() != null) {
				nombreEntidad = entity.getCustomName();
			}
			for(Evento e : eventos) {
				ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
				if(entity instanceof Player) {
					variables = EventoUtils.getVariablesAVerificarTarget(variables,e.getCondiciones(), e.getAcciones(), (Player)entity, true);
				}

				EventoUtils.remplazarVariable(variables, "%entity_type%", tipoEntidad);
				EventoUtils.remplazarVariable(variables, "%entity_name%", nombreEntidad);
				EventoUtils.remplazarVariable(variables, "%item%", materialItem);
				EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
				EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
				for(int i=0;i<lore.size();i++) {
					EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
				}
				EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
				EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
			}
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alInteractuarItem(PlayerInteractEvent event) {
		Player jugador = event.getPlayer();
//		if(!Bukkit.getVersion().contains("1.8") && !event.getHand().equals(EquipmentSlot.HAND)) {
//			return;
//		}
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.ITEM_INTERACT);
		if(eventos.size() == 0) {
			return;
		}

		ItemStack item = event.getItem();
		ItemUtils itemUtils = new ItemUtils(item);
		String materialItem = itemUtils.getMaterial();
		short durability = itemUtils.getDurability();
		String nombreItem = itemUtils.getNombre();
		List<String> lore = itemUtils.getLoreList();
		String loreItem = itemUtils.getLoreString();
		String action = "";
		
		boolean shifteando = jugador.isSneaking();
		if(shifteando) {
			if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				action = "SHIFT_RIGHT_CLICK";
			}else if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				action = "SHIFT_LEFT_CLICK";
			}
		}else {
			if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				action = "RIGHT_CLICK";
			}else if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				action = "LEFT_CLICK";
			}
		}
		
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%action_type%", action);
			EventoUtils.remplazarVariable(variables, "%item%", materialItem);
			EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
			EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
			for(int i=0;i<lore.size();i++) {
				EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
			}
			EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void jugadorAtacaEntidad(EntityDamageByEntityEvent event) {
		Entity atacante = event.getDamager();
		Entity dañado = event.getEntity();
		
		if(atacante == null || dañado == null) {
			return;
		}
			
		if(atacante.getType().equals(EntityType.ARROW)) {
			Arrow arrow = (Arrow) atacante;
			if(arrow.getShooter() instanceof Player) {
				atacante = (Player) arrow.getShooter();
			}	
		}
		
		if(!(atacante instanceof Player)) {
			return;
		}
		
		Player jugador = (Player) atacante;
		String tipoVictima = dañado.getType().name();
		String nombreVictima = "";
		if(dañado.getCustomName() != null) {
			nombreVictima = dañado.getCustomName();
		}
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_ATTACK);
		if(eventos.size() == 0) {
			return;
		}
		ItemStack item = jugador.getItemInHand();
		ItemUtils itemUtils = new ItemUtils(item);
		String materialItem = itemUtils.getMaterial();
		short durability = itemUtils.getDurability();
		String nombreItem = itemUtils.getNombre();
		List<String> lore = itemUtils.getLoreList();
		String loreItem = itemUtils.getLoreString();
		
		double damage = event.getFinalDamage();
		
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			if(dañado instanceof Player) {
				variables = EventoUtils.getVariablesAVerificarTarget(variables,e.getCondiciones(), e.getAcciones(), (Player)dañado, true);
			}
			
			EventoUtils.remplazarVariable(variables, "%damage%", damage+"");
			EventoUtils.remplazarVariable(variables, "%victim%", tipoVictima);
			EventoUtils.remplazarVariable(variables, "%victim_name%", nombreVictima);
			EventoUtils.remplazarVariable(variables, "%item%", materialItem);
			EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
			EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
			for(int i=0;i<lore.size();i++) {
				EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
			}
			EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void jugadorCambiaMundo(PlayerChangedWorldEvent event) {
		Player jugador = event.getPlayer();
		
		String fromWorld = event.getFrom().getName();
		String toWorld = jugador.getWorld().getName();
		int onlineFrom = event.getFrom().getPlayers().size();
		int onlineTo = jugador.getWorld().getPlayers().size();
		
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_WORLD_CHANGE);
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%world_from%", fromWorld);
			EventoUtils.remplazarVariable(variables, "%world_to%", toWorld);
			EventoUtils.remplazarVariable(variables, "%online_players_from%", onlineFrom+"");
			EventoUtils.remplazarVariable(variables, "%online_players_to%", onlineTo+"");
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void jugadorEsDañado(EntityDamageEvent event) {
		Entity dañado = event.getEntity();
		if(dañado != null && dañado instanceof Player && event.getCause() != null) {
			Player jugador = (Player) dañado;
			String causa = event.getCause().toString();
			
			double damage = event.getFinalDamage();
			String tipoDamager = "";
			String nameDamager = "";
			if(event instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;
				Entity damager = event2.getDamager();
				tipoDamager = damager.getType().name();
				if(damager.getCustomName() != null) {
					nameDamager = damager.getCustomName();
				}
			}
			
			ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_DAMAGE);
			for(Evento e : eventos) {
				ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
				EventoUtils.remplazarVariable(variables, "%damager_type%", tipoDamager);
				EventoUtils.remplazarVariable(variables, "%damager_name%", nameDamager);
				EventoUtils.remplazarVariable(variables, "%damage%", damage+"");
				EventoUtils.remplazarVariable(variables, "%cause%", causa);
				EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void jugadorMataEntidad(EntityDeathEvent event) {
		LivingEntity entidad = event.getEntity();
		if(entidad != null && entidad.getKiller() != null) {
			Player jugador = (Player) entidad.getKiller();
			String tipoVictima = entidad.getType().name();
			String nombreVictima = "";
			if(entidad.getCustomName() != null) {
				nombreVictima = entidad.getCustomName();
			}
			Location location = entidad.getLocation();
			ItemStack item = jugador.getItemInHand();
			ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_KILL);
			if(eventos.size() == 0) {
				return;
			}
			ItemUtils itemUtils = new ItemUtils(item);
			String materialItem = itemUtils.getMaterial();
			short durability = itemUtils.getDurability();
			String nombreItem = itemUtils.getNombre();
			List<String> lore = itemUtils.getLoreList();
			String loreItem = itemUtils.getLoreString();
			
			int blockX = location.getBlockX();
			int blockY = location.getBlockY();
			int blockZ = location.getBlockZ();
			String world = location.getWorld().getName();
			
			for(Evento e : eventos) {
				ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
				if(entidad instanceof Player) {
					variables = EventoUtils.getVariablesAVerificarTarget(variables,e.getCondiciones(), e.getAcciones(), (Player)entidad, true);
				}
				EventoUtils.remplazarVariable(variables, "%victim%", tipoVictima);
				EventoUtils.remplazarVariable(variables, "%victim_name%", nombreVictima);
				EventoUtils.remplazarVariable(variables, "%item%", materialItem);
				EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
				EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
				for(int i=0;i<lore.size();i++) {
					EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
				}
				EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
				EventoUtils.remplazarVariable(variables, "%victim_block_x%", blockX+"");
				EventoUtils.remplazarVariable(variables, "%victim_block_y%", blockY+"");
				EventoUtils.remplazarVariable(variables, "%victim_block_z%", blockZ+"");
				EventoUtils.remplazarVariable(variables, "%victim_world%", world);
				EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alRomperBloque(BlockBreakEvent event) {
		Player jugador = event.getPlayer();
		Block block = event.getBlock();
		Location l = block.getLocation();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.BLOCK_BREAK);
		if(eventos.size() == 0) {
			return;
		}
		ItemStack item = jugador.getItemInHand();
		ItemUtils itemUtils = new ItemUtils(item);
		String materialItem = itemUtils.getMaterial();
		short durability = itemUtils.getDurability();
		String nombreItem = itemUtils.getNombre();
		List<String> lore = itemUtils.getLoreList();
		String loreItem = itemUtils.getLoreString();
		String textureData = BlockUtils.getHeadTextureData(block);
		
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%item%", materialItem);
			EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
			EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
			for(int i=0;i<lore.size();i++) {
				EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
			}
			EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
			EventoUtils.remplazarVariable(variables, "%block_x%", l.getBlockX()+"");
			EventoUtils.remplazarVariable(variables, "%block_y%", l.getBlockY()+"");
			EventoUtils.remplazarVariable(variables, "%block_z%", l.getBlockZ()+"");
			EventoUtils.remplazarVariable(variables, "%block_world%", l.getWorld().getName());
			EventoUtils.remplazarVariable(variables, "%block%", block.getType().name());
			EventoUtils.remplazarVariable(variables, "%block_head_texture%", textureData);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alPonerBloque(BlockPlaceEvent event) {
		Player jugador = event.getPlayer();
		Block block = event.getBlock();
		Location l = block.getLocation();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.BLOCK_PLACE);
		if(eventos.size() == 0) {
			return;
		}
		ItemStack item = event.getItemInHand();
		ItemUtils itemUtils = new ItemUtils(item);
		String materialItem = itemUtils.getMaterial();
		short durability = itemUtils.getDurability();
		String nombreItem = itemUtils.getNombre();
		List<String> lore = itemUtils.getLoreList();
		String loreItem = itemUtils.getLoreString();
		
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%item%", materialItem);
			EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
			EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
			for(int i=0;i<lore.size();i++) {
				EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
			}
			EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
			EventoUtils.remplazarVariable(variables, "%block_x%", l.getBlockX()+"");
			EventoUtils.remplazarVariable(variables, "%block_y%", l.getBlockY()+"");
			EventoUtils.remplazarVariable(variables, "%block_z%", l.getBlockZ()+"");
			EventoUtils.remplazarVariable(variables, "%block_world%", l.getWorld().getName());
			EventoUtils.remplazarVariable(variables, "%block%", block.getType().name());
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alUsarComando(PlayerCommandPreprocessEvent event) {
		Player jugador = event.getPlayer();
		String comando = event.getMessage();
		String[] args = comando.split(" ");
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_COMMAND);
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%command%", comando);
			for(int i=1;i<args.length;i++) {
				EventoUtils.remplazarVariable(variables, "%arg_"+(i)+"%", args[i]);
			}
			EventoUtils.remplazarVariable(variables, "%args_length%", (args.length-1)+"");
			
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alChatear(AsyncPlayerChatEvent event) {
		Player jugador = event.getPlayer();
		String mensaje = event.getMessage();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_CHAT);
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%message%", mensaje);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alConsumir(PlayerItemConsumeEvent event) {
		Player jugador = event.getPlayer();
		ItemStack item = event.getItem();
		
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.ITEM_CONSUME);
		if(eventos.size() == 0) {
			return;
		}
		ItemUtils itemUtils = new ItemUtils(item);
		String materialItem = itemUtils.getMaterial();
		short durability = itemUtils.getDurability();
		String nombreItem = itemUtils.getNombre();
		List<String> lore = itemUtils.getLoreList();
		String loreItem = itemUtils.getLoreString();
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%item%", materialItem);
			EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
			EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
			for(int i=0;i<lore.size();i++) {
				EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
			}
			EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alRecogerItem(PlayerPickupItemEvent event) {
		Player jugador = event.getPlayer();
		ItemStack item = event.getItem().getItemStack();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.ITEM_PICKUP);
		if(eventos.size() == 0) {
			return;
		}

		ItemUtils itemUtils = new ItemUtils(item);
		int amount = item.getAmount();
		String materialItem = itemUtils.getMaterial();
		short durability = itemUtils.getDurability();
		String nombreItem = itemUtils.getNombre();
		List<String> lore = itemUtils.getLoreList();
		String loreItem = itemUtils.getLoreString();
		
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%item_amount%", amount+"");
			EventoUtils.remplazarVariable(variables, "%item%", materialItem);
			EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
			EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
			for(int i=0;i<lore.size();i++) {
				EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
			}
			EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alCambiarNivel(PlayerLevelChangeEvent event) {
		Player jugador = event.getPlayer();
		int oldLevel = event.getOldLevel();
		int newLevel = event.getNewLevel();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_LEVELUP);
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%old_level%", oldLevel+"");
			EventoUtils.remplazarVariable(variables, "%new_level%", newLevel+"");
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alEquiparArmadura(ArmorEquipEvent event) {
		Player jugador = event.getPlayer();
		
		String equip_type = "EQUIP";
		ItemStack itemNuevo = event.getNewArmorPiece();
		ItemStack itemAntes = event.getOldArmorPiece();
		ItemStack itemSeleccionado = null;
		if(itemNuevo == null || itemNuevo.getType().name().contains("AIR")) {
			equip_type = "UNEQUIP";
			//desequipó armadura
			itemSeleccionado = itemAntes;
		}else {
			itemSeleccionado = itemNuevo;
		}

		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.PLAYER_ARMOR);
		if(eventos.size() == 0) {
			return;
		}
		ItemUtils itemUtils = new ItemUtils(itemSeleccionado);
		String materialItem = itemUtils.getMaterial();
		short durability = itemUtils.getDurability();
		String nombreItem = itemUtils.getNombre();
		List<String> lore = itemUtils.getLoreList();
		String loreItem = itemUtils.getLoreString();
		String type = "";
		if(event.getType() != null) {
			type = event.getType().name();
		}
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%armor_type%", type);
			EventoUtils.remplazarVariable(variables, "%equip_type%", equip_type);
			EventoUtils.remplazarVariable(variables, "%item%", materialItem);
			EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
			EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
			for(int i=0;i<lore.size();i++) {
				EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
			}
			EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alMoverItem(InventoryClickEvent event) {
		Player jugador = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
//		jugador.sendMessage(item.getType()+" pasa1");
		if(event.getClick().equals(ClickType.NUMBER_KEY)) {
			int slotHotbar = event.getHotbarButton();
			
			ItemStack item2 = jugador.getInventory().getItem(slotHotbar);
			if(item2 == null || item2.getType().equals(Material.AIR)) {
//				item = jugador.getInventory().getItem(event.getSlot());
			}else if(item == null || item.getType().equals(Material.AIR)){
				item = item2;
			}
//			jugador.sendMessage(item.getType()+" pasa2");
		}
		
		if(item != null && !item.getType().equals(Material.AIR)) {
			ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.ITEM_MOVE);
			int slot = event.getSlot();
			if(eventos.size() == 0) {
				return;
			}
			String tipoInventario = "";
			InventoryView view = jugador.getOpenInventory();
			if(view != null) {
				tipoInventario = view.getType().name();
			}
			ItemUtils itemUtils = new ItemUtils(item);
			String materialItem = itemUtils.getMaterial();
			short durability = itemUtils.getDurability();
			String nombreItem = itemUtils.getNombre();
			List<String> lore = itemUtils.getLoreList();
			String loreItem = itemUtils.getLoreString();
			for(Evento e : eventos) {
				ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
				EventoUtils.remplazarVariable(variables, "%inventory_type%", tipoInventario);
				EventoUtils.remplazarVariable(variables, "%item%", materialItem);
				EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
				EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
				for(int i=0;i<lore.size();i++) {
					EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
				}
				EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
				EventoUtils.remplazarVariable(variables, "%slot%", slot+"");
				EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
			}
		}
	}
	
	@EventHandler
	public void alCraftear(CraftItemEvent event) {
		Player jugador = (Player) event.getWhoClicked();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.ITEM_CRAFT);
		if(eventos.size() == 0) {
			return;
		}
		ItemStack item = event.getRecipe().getResult();
		ItemUtils itemUtils = new ItemUtils(item);
		String materialItem = itemUtils.getMaterial();
		short durability = itemUtils.getDurability();
		String nombreItem = itemUtils.getNombre();
		List<String> lore = itemUtils.getLoreList();
		String loreItem = itemUtils.getLoreString();
		
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%item%", materialItem);
			EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
			EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
			for(int i=0;i<lore.size();i++) {
				EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
			}
			EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alDropear(PlayerDropItemEvent event) {
		Player jugador = event.getPlayer();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.ITEM_DROP);
		if(eventos.size() == 0) {
			return;
		}
		ItemStack item = event.getItemDrop().getItemStack();
		ItemUtils itemUtils = new ItemUtils(item);
		String materialItem = itemUtils.getMaterial();
		short durability = itemUtils.getDurability();
		String nombreItem = itemUtils.getNombre();
		List<String> lore = itemUtils.getLoreList();
		String loreItem = itemUtils.getLoreString();
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%item%", materialItem);
			EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
			EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
			for(int i=0;i<lore.size();i++) {
				EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(i+1)+"%", lore.get(i));
			}
			EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
	
	@EventHandler
	public void alCambiarDeItem(ItemSelectEvent event) {
		Player jugador = event.getPlayer();
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.ITEM_SELECT);
		if(eventos.size() == 0) {
			return;
		}
		
		String action = event.getSelectType().name();
		ItemStack item = event.getItem();
		ItemUtils itemUtils = new ItemUtils(item);
		String materialItem = itemUtils.getMaterial();
		short durability = itemUtils.getDurability();
		String nombreItem = itemUtils.getNombre();
		List<String> lore = itemUtils.getLoreList();
		String loreItem = itemUtils.getLoreString();
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			EventoUtils.remplazarVariable(variables, "%item%", materialItem);
			EventoUtils.remplazarVariable(variables, "%item_durability%", durability+"");
			EventoUtils.remplazarVariable(variables, "%item_name%", nombreItem);
			for(int c=0;c<lore.size();c++) {
				EventoUtils.remplazarVariable(variables, "%item_lore_line_"+(c+1)+"%", lore.get(c));
			}
			EventoUtils.remplazarVariable(variables, "%item_lore%", loreItem);
			EventoUtils.remplazarVariable(variables, "%select_type%", action);
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin, false);
		}
	}
}
