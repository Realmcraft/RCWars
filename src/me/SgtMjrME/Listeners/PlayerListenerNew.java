package me.SgtMjrME.Listeners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Util;
import me.SgtMjrME.ClassUpdate.WarClass;
import me.SgtMjrME.ClassUpdate.WarRank;
import me.SgtMjrME.ClassUpdate.Abilities.AbilityTimer;
import me.SgtMjrME.ClassUpdate.Abilities.BaseAbility;
import me.SgtMjrME.Object.Base;
import me.SgtMjrME.Object.Kit;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;
import me.SgtMjrME.Object.WarPoints;
import me.SgtMjrME.SiegeUpdate.Cannon;
import me.SgtMjrME.SiegeUpdate.Siege;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class PlayerListenerNew implements Listener {
	private HashMap<String, Long> times = new HashMap<String, Long>();
	private RCWars pl;
	public HashMap<Player, String> modifyInv = new HashMap<Player, String>();

	public HashMap<String, Integer> invType = new HashMap<String, Integer>();
	
	public HashMap<String, ItemStack[]> itemsReturn = new HashMap<String, ItemStack[]>();

	// Soon to be replaced
//	public static HashMap<String, Long> lastClick = new HashMap<String, Long>();

	public static HashSet<Location> headLoc = new HashSet<Location>();
	YamlConfiguration cfg;
	static HashMap<String, Long> invTime = new HashMap<String, Long>();

	public PlayerListenerNew(RCWars plugin) {
		pl = plugin;
		cfg = new YamlConfiguration();
		try {
			File f = new File(pl.getDataFolder().getAbsolutePath()
					+ "/heads.yml");
			if (!f.exists())
				f.createNewFile();
			cfg.load(pl.getDataFolder().getAbsolutePath() + "/heads.yml");
			String[] s = cfg.getString("heads", "").split(";");
			for (String str : s)
				if (!str.equals("")) {
					Location l = RCWars.returnPlugin().str2Loc(str);
					headLoc.add(l);
				}
		} catch (FileNotFoundException localFileNotFoundException) {
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerExp(PlayerLevelChangeEvent e) {
		WarRank w = WarRank.getPlayer(e.getPlayer());
		if (w != null) {
			WarRank next = w.nextRankLevel();
			if ((next != null) && (next.getLevel() - e.getNewLevel() <= 0)) {
				w.removeOther(e.getPlayer());
				next.rankUp(e.getPlayer());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerFood(FoodLevelChangeEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		Race r = WarPlayers.getRace((Player) e.getEntity());
		if (r == null)
			return;
		if (r.isRef())
			e.setFoodLevel(20);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		if (e.isCancelled())
			return;
		if (e.getMessage() == null)
			return;
		if (e.getMessage().split(" ").length < 1)
			return;
		if ((e.getMessage().split(" ")[0].equalsIgnoreCase("/spawn"))
				&& (WarPlayers.getRace(e.getPlayer()) != null)) {
			Util.sendMessage(e.getPlayer(), ChatColor.RED + "Nuh uh");
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerBow(ProjectileLaunchEvent e) {
		if (!(e.getEntity().getShooter() instanceof Player))
			return;
		AbilityTimer.onLaunch(e);

		if (!RCWars.leaving.contains(((Player) e.getEntity().getShooter())
				.getName()))
			return;
		RCWars.leaving.remove(((Player) e.getEntity().getShooter()).getName());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(final PlayerJoinEvent e) {
		if (e.getPlayer().getWorld().equals(pl.getWarWorld())) {
			pl.getServer().getScheduler()
					.scheduleSyncDelayedTask(pl, new Runnable() {
						public void run() {
							e.getPlayer().teleport(RCWars.lobbyLocation());
						}
					}, 20L);
			AbilityTimer.onJoin(e.getPlayer(), e);
		}
	}

	private void applyTag(PlayerReceiveNameTagEvent e, ChatColor color) {
		if (e.getNamedPlayer().getName().length() < 15)
			e.setTag(color + e.getNamedPlayer().getName());
		else
			e.setTag(color + e.getNamedPlayer().getName().substring(0, 14));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGetTag(PlayerReceiveNameTagEvent e) {
		Race r = WarPlayers.getRace(e.getNamedPlayer());
		if (r != null) {
			if ((e.getNamedPlayer().hasPermission("rcchat.m")) && (!r.isRef())) {
				ChatColor c = r.getCcolor();
				try {
					applyTag(e, ChatColor.valueOf("DARK_" + c.name()));
					return;
				} catch (Exception localException) {
				}
			}
			applyTag(e, r.getCcolor());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTP(PlayerTeleportEvent e) {
		AbilityTimer.onTeleport(e);
		if (e.isCancelled())
			return;
		if (e.getFrom().getWorld().equals(e.getTo().getWorld()))
			return;
		if ((WarPlayers.getRace(e.getPlayer()) != null)
				&& (!e.getTo().getWorld()
						.equals(RCWars.returnPlugin().getWarWorld()))) {
			if (WarPlayers.gotDamaged(e.getPlayer())) {
				e.getPlayer().setHealth(0);
				Util.sendMessage(e.getPlayer(), 
						"Killed due to damage before teleportation");
			}
			WarPlayers.remove(e.getPlayer(),
					"Removed from wars due to teleportation");
		}
		try {
			if ((e.getFrom().getWorld().equals(pl.getWarWorld()))
					&& (!e.getTo().getWorld().equals(e.getFrom().getWorld()))) {
				Util.readInv(e.getPlayer(), true);
				e.getPlayer().setAllowFlight(false);
			}
			if ((e.getTo().getWorld().equals(pl.getWarWorld()))
					&& (!e.getTo().getWorld().equals(e.getFrom().getWorld()))) {
				Util.readInv(e.getPlayer(), false);
				final Player p = e.getPlayer();
				Race r = WarPlayers.getRace(p);
				if (r != null && r.isRef())
					Bukkit.getScheduler().runTaskLater(pl, new BukkitRunnable() {
						public void run() {
							p.setAllowFlight(true);
							Util.sendMessage(p, "Fly: " + p.getAllowFlight());
						}
					}, 30L);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onLeave(PlayerQuitEvent e) {
		invTime.remove(e.getPlayer().getName());
		times.remove(e.getPlayer().getName());
//		lastClick.remove(e.getPlayer().getName());
		Player p = e.getPlayer();
		if (p == null) {
			return;
		}
		AbilityTimer.onLeave(e.getPlayer(), e);

		if (WarPlayers.getRace(p) != null) {
			WarPoints.saveWarPoints(p);
			if (WarPlayers.gotDamaged(p)) {
				p.damage(1000);
				RCWars.returnPlugin().sendToMySQL(p, "death", 0);
			}
			WarPlayers.remove(p, "Disconnect");
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventory(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		if ((modifyInv.containsKey(e.getWhoClicked()))
				&& (((Integer) invType.get(modifyInv.get(e.getWhoClicked())))
						.intValue() == 1) && (e.getSlot() >= 40)) {
			Util.sendMessage(((Player) e.getWhoClicked()), "Modifying this would erase the item");
			e.setCancelled(true);
			return;
		}

		if (!e.getInventory().getType().equals(InventoryType.CRAFTING))
			return;
		Player p = (Player) e.getWhoClicked();
		Race r = WarPlayers.getRace(p);

		if (r == null)
			return;
		ItemStack item = e.getCurrentItem();
		if (item == null) return;
		ItemMeta im = item.getItemMeta();
		if (im == null) return;
		String display = im.getDisplayName();
		if (AbilityTimer.isUsedBaseAbility(display)) e.setCancelled(true); //If it's a used ability, can't change.  Otherwise, I don't give a F***
//		int sl = e.getRawSlot();
//		int end = 37;
//		WarRank wr = WarRank.getPlayer(p);
//		if (wr != null)
//			end = 36 + wr.otherItems.size();
//		if (((sl >= 5) && (sl <= 8)) || ((sl >= 36) && (sl < end)))
//			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(final PlayerRespawnEvent e) {
		Race r;
		if ((r = WarPlayers.getRace(e.getPlayer())) == null) {
			return;
		}
		if (r.getSpawn() != null)
			e.setRespawnLocation(r.getSpawn());
//		removeItems(e.getPlayer());
//		e.getPlayer().getInventory().setArmorContents(null);
		try{
		if (pl.kitOnSpawn != null) {
			pl.kitOnSpawn.addKit(e.getPlayer());
		}
		e.getPlayer().getInventory()
				.setHelmet(new ItemStack(35, 1, r.getColor().byteValue()));
		final WarRank wr = WarRank.getPlayer(e.getPlayer());
		if (wr == null)
			Bukkit.getScheduler().runTaskLater(pl, new Runnable(){

				@Override
				public void run() {
					if (itemsReturn.containsKey(e.getPlayer().getName())){
						ItemStack[] returning = itemsReturn.get(e.getPlayer().getName());
						for(int i = 0; i < 9; i++){
							e.getPlayer().getInventory().setItem(8 - i, returning[i]);
						}
					}
					WarClass.defaultClass.enterClass(e.getPlayer());
				}
				
			}, 10L);
		else{
			Bukkit.getScheduler().runTaskLater(pl, new Runnable(){

				@Override
				public void run() {
					if (itemsReturn.containsKey(e.getPlayer().getName())){
						ItemStack[] returning = itemsReturn.get(e.getPlayer().getName());
						for(int i = 0; i < 9 && i < returning.length; i++){
							e.getPlayer().getInventory().setItem(8 - i, returning[i]);
						}
					}
					wr.c.resetRank(e.getPlayer());
				}
				
			}, 10L);
		}
		}
		catch(Exception e1){System.out.println("damn");}
	}
	
	@SuppressWarnings("unused")
	private void removeItems(Player p){
		for (int i = 0; i < 54; i++) {
			if (p.getInventory().getItem(i) != null
					&& !RCWars.allowedItems.contains(p.getInventory()
							.getItem(i).getTypeId()))
				p.getInventory().setItem(i, null);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerAim(PlayerMoveEvent e) {
		if (!e.getFrom().getWorld().equals(e.getTo().getWorld()))
			return;
		Cannon c = Cannon.getCannon(e.getPlayer());
		if (c == null)
			return;
		Location l = (Location) Cannon.cannons.get(c);
		if ((l.getWorld().equals(e.getTo().getWorld()))
				&& (l.distance(e.getTo()) > 2.0D)) {
			Util.sendMessage(e.getPlayer(), "You have exit the cannon's seat");
			Cannon.leaveCannon(c);
			return;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerHit(PlayerInteractEvent e) {
//		if ((WarPlayers.isPlaying(e.getPlayer().getName()))
//				&& ((e.getAction().equals(Action.RIGHT_CLICK_AIR)) || (e
//						.getAction().equals(Action.RIGHT_CLICK_BLOCK)))) {
//			if (lastClick.containsKey(e.getPlayer().getName())) {
//				long timedif = System.currentTimeMillis()
//						- ((Long) lastClick.get(e.getPlayer().getName()))
//								.longValue();
//
//				if (timedif < 250L) {
//					WarRank wr = WarRank.getPlayer(e.getPlayer());
//					if (wr == null)
//						WarClass.defaultClass.enterClass(e.getPlayer());
//					wr.cycleAbility(e.getPlayer());
//				}
//			}
//
//			lastClick.put(e.getPlayer().getName(),
//					Long.valueOf(System.currentTimeMillis()));
//		}

		if (RCWars.leaving.contains(e.getPlayer().getName()))
			RCWars.leaving.remove(e.getPlayer().getName());
		if (!e.getPlayer().getWorld().equals(pl.getWarWorld()))
			return;
		if ((RCWars.headmodifier != null)
				&& (e.getPlayer().getName().equals(RCWars.headmodifier
						.getName()))) {
			headLoc.add(e.getClickedBlock().getLocation());
			String oldStr = cfg.getString("heads", "");
			String add = RCWars.loc2str(e.getClickedBlock().getLocation());
			if (!oldStr.equals(""))
				add = ";" + add;
			cfg.set("heads", oldStr + add);
			try {
				cfg.save(pl.getDataFolder().getAbsolutePath() + "/heads.yml");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Util.sendMessage(e.getPlayer(), "Block set");
			return;
		}
		String s;
		if ((s = Base.isOperating(e.getPlayer())) != null) {
			Base.distributeAction(e.getPlayer(), s, e);
			return;
		}
		if (Race.isOperating(e.getPlayer())) {
			Race.distributeAction(e.getPlayer(), e);
			return;
		}
		if (Cannon.setItem.containsKey(e.getPlayer())) {
			((Cannon) Cannon.setItem.get(e.getPlayer())).setButton(
					e.getPlayer(), e.getClickedBlock().getLocation());
			return;
		}
		if (Siege.isEditing(e.getPlayer().getName())) {
			Siege.setLocation(e.getPlayer(), e.getClickedBlock().getLocation());
			return;
		}
		Race r;
		if ((r = WarPlayers.getRace(e.getPlayer())) == null)
			return;
		Cannon c = Cannon.getCannon(e.getPlayer());
		if (c != null) {
			c.fireTNT();
		}

		if ((e.getAction().equals(Action.LEFT_CLICK_AIR))
				|| (e.getAction().equals(Action.LEFT_CLICK_BLOCK)))
			AbilityTimer.onInteract(e.getPlayer(), e);
		if (e.getClickedBlock() == null)
			return;
		if (!e.getClickedBlock().getWorld().equals(pl.getWarWorld()))
			return;
		if ((e.getPlayer().getItemInHand().getTypeId() == 397)
				&& (headLoc.contains(e.getClickedBlock().getLocation()))) {
			if (!e.getClickedBlock().getType().equals(Material.SKULL))
				e.getClickedBlock().setType(Material.SKULL);
			Skull skull = (Skull) e.getClickedBlock().getState();
			skull.setSkullType(SkullType.PLAYER);
			SkullMeta meta = (SkullMeta) e.getPlayer().getItemInHand()
					.getItemMeta();
			skull.setOwner(meta.getOwner());

			e.setCancelled(true);
			skull.update(true);
			if ((!e.getBlockFace().equals(BlockFace.UP))
					&& (!e.getBlockFace().equals(BlockFace.DOWN)))
				skull.setRotation(e.getBlockFace());
			skull.update(true);
			e.getPlayer().setItemInHand(null);
		}

		c = Cannon.isCannonButton(e.getClickedBlock().getLocation());
		if (c != null) {
			Cannon.enterCannon(e.getPlayer(), c.getName());
			return;
		}
		if ((e.getClickedBlock().getState() instanceof Sign))
			dealWithSign(e.getPlayer(), (Sign) e.getClickedBlock().getState(),
					e);
		Siege siege;
		if (((siege = Siege.isWall(e.getClickedBlock().getLocation())) != null)
				&& ((siege.b.getOwner().equals(r)) || (r.isRef()))) {
			BlockFace b = e.getBlockFace();
			int x = 0;
			int z = 0;
			if (b.equals(BlockFace.EAST))
				x = -1;
			else if (b.equals(BlockFace.WEST))
				x = 1;
			else if (b.equals(BlockFace.NORTH))
				z = 1;
			else if (b.equals(BlockFace.SOUTH))
				z = -1;
			else
				return;
			Location l = e.getClickedBlock().getLocation().clone();
			l.setY(e.getPlayer().getEyeLocation().getY());
			int count = 0;
			while (l.getBlock().getTypeId() != 0) {
				l.setX(l.getX() + x);
				l.setZ(l.getZ() + z);
				if (count++ > 10) {
					Util.sendMessage(e.getPlayer(), 
							ChatColor.RED + "No viable spot found");
					return;
				}
			}
			l.setX(l.getX() + x);
			l.setY(e.getPlayer().getLocation().getY());
			l.setZ(l.getZ() + z);
			l.setPitch(e.getPlayer().getLocation().getPitch());
			l.setYaw(e.getPlayer().getLocation().getYaw());
			e.getPlayer().teleport(l);
		}

		if (e.getPlayer().getItemInHand().getType().equals(Material.FIREBALL))
			e.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	private void dealWithSign(Player p, Sign state, PlayerInteractEvent event) {
		if (state.getLine(0) == null)
			return;
		if (times.containsKey(p.getName())) {
			long time = times.get(p.getName());
			if ((System.currentTimeMillis() - time)/1000 < 5) {
				return;
			}
		}
		times.put(p.getName(),
				System.currentTimeMillis());
		if (state.getLine(0).equals("[Cannon]")) {
			Cannon.enterCannon(p, state.getLine(1));
			return;
		}
		if (state.getLine(0).equals("[Class]")) {
			WarClass.dealWithSign(p, state, event);
		}
		if (!state.getLine(0).equals("[WarShop]")) {
			return;
		}
		try {
			if (state.getLine(1).equals("MobHead")) {
				Util.sendMessage(p, "itemid " + p.getItemInHand().getTypeId());
				if (p.getItemInHand().getTypeId() != 397)
					return;
				int val = Integer.parseInt(state.getLine(3));
				WarPoints.giveWarPoints(p,
						val * p.getItemInHand().getAmount());
				p.setItemInHand(null);
				return;
			}
		} catch (Exception e) {
			Util.sendMessage(p, "error");
		}
		try {
			if (state.getLine(1).equalsIgnoreCase("Enchantment")) {
				ItemStack item = p.getItemInHand().clone();
				int cost = Integer.parseInt(state.getLine(3));
				item = Util.addEnchant(item, state.getLine(2));
				if (item == null) {
					Util.sendMessage(p, ChatColor.RED
							+ "Enchantment will not work");
					return;
				}
				if (!WarPoints.spendWarPoints(p, cost))
					return;
				final Player hold = p;
				final ItemStack holdi = item;
				Bukkit.getScheduler().runTask(pl, new Runnable() {
					public void run() {
						hold.setItemInHand(holdi);
					}
				});
				return;
			}
		} catch (Exception e3) {
			Util.sendMessage(p, "ERROR");
			Util.sendLog("Sign at "
					+ state.getBlock().getLocation().toString()
					+ " error enchanting");
			return;
		}
		if (!p.hasPermission("rcwars.admin")) {
			event.setCancelled(true);
		}
		if ((state.getLine(1) == null) || (state.getLine(3) == null)) {
			return;
		}
		if (state.getLine(1).equals("repair")) {
			if (state.getLine(2) == null) {
				Util.sendMessage(p, "Error with 3rd line of sign, should be base name");
				return;
			}
			Base b = Base.getBase(state.getLine(2));
			if (b == null) {
				Util.sendMessage(p, "Error with 3rd line of sign, should be base name");
				return;
			}
			Siege s = Siege.getSiege(b);
			if (s == null) {
				Util.sendMessage(p, "Error with 3rd line of sign, could not find siege");
				return;
			}
			String[] str = state.getLine(3).split(":");
			if (str.length != 2) {
				Util.sendMessage(p, "Error with 4th line, should be \"#:#\"");
				return;
			}
			if (WarPoints.spendWarPoints(p, Integer.parseInt(str[1]))){
				s.repair(Integer.parseInt(str[0]));
				return;
			}
		}
		try {
		if (state.getLine(1).equalsIgnoreCase("kit")) {
			Kit k = Kit.getKit(state.getLine(2));
			if (k == null) {
				Util.sendMessage(p, "Kit not found");
				return;
			}
			if (!WarPoints.spendWarPoints(p,
					Integer.parseInt(state.getLine(3)))) {
				return;
			}
			k.addKit(p);
			p.updateInventory();
			return;
		}
		} catch (Exception e) {
			return;
		}
		int num = 1;
		int cost = 0;
		Material mat = Material.AIR;
		int DMG = 0;
		try {
			String s = state.getLine(3);
			if (s == null)
				return;
			String[] split = s.split(":");
			if (split.length == 2) {
				num = Integer.parseInt(split[0]);
				cost = Integer.parseInt(split[1]);
			} else if (split.length == 1) {
				num = 1;
				cost = Integer.parseInt(split[0]);
			} else {
				Util.sendMessage(p, "Incorrect sign setup");
				Util.sendLog("Sign at "
						+ state.getBlock().getLocation().toString()
						+ " has incorrect cost/num");
				return;
			}
			if ((WarPoints.getWarPoints(p) == null)
					|| (WarPoints.getWarPoints(p) < cost)) {
				Util.sendMessage(p, ChatColor.RED
						+ "You don't have enough WarPoints");
				return;
			}
			mat = Material.getMaterial(state.getLine(1));
			if (mat == null) {
				mat = Material.getMaterial(Integer.parseInt(state
						.getLine(1)));
				if (mat == null) {
					Util.sendMessage(p, "Error with sign(mat)");
					return;
				}
				if (mat.name().length() < 16) {
					state.setLine(1, mat.name());
					state.update(true);
				}
			}
			ItemStack item;
			if ((state.getLine(2) != null)
					&& (!state.getLine(2).equals(""))) {
				if (state.getLine(1).equals("POTION")) {
					DMG = Integer.parseInt(state.getLine(2));
					item = new ItemStack(mat, num, (short) DMG);
				} else if ((isArmor(mat)) || (isBow(mat))
						|| (isSword(mat)) || (isTool(mat))) {
					item = new ItemStack(mat, num);
					item = Util.addEnchant(item, state.getLine(2));
				} else {
					item = new ItemStack(mat, num,
							(short) Integer.parseInt(state.getLine(2)));
				}
			} else {
				item = new ItemStack(mat, num);
			}
				WarPoints.spendWarPoints(p, cost);
			ItemStack leftover = addItemSGT(p.getInventory(), item);
			if (leftover != null) {
				p.getLocation().getWorld()
						.dropItem(p.getLocation(), leftover);
				Util.sendMessage(p, "Inv full, dropping at your feet");
			}
			p.updateInventory();
		} catch (Exception e) {
			Util.sendMessage(p, ChatColor.RED + "ERROR");
			e.printStackTrace();
			RCWars.returnPlugin()
					.getLogger()
					.severe("[RCWARS] SEVERE! Sign at "
							+ event.getClickedBlock().getLocation()
									.toString() + " has an error");
			return;
		}
	}

	private ItemStack addItemSGT(PlayerInventory inventory, ItemStack item) {
		ItemStack out = item;
		int count = item.getAmount();
		HashMap<Integer, ? extends ItemStack> invItems = inventory.all(item.getType());
		for (Entry<Integer, ? extends ItemStack> entry : invItems.entrySet()) {
			ItemStack i = (ItemStack) entry.getValue();
			if ((i.getAmount() < i.getMaxStackSize()) && (count > 0)) {
				int transfer = i.getMaxStackSize() - i.getAmount();

				if (transfer >= count) {
					i.setAmount(i.getAmount() + count);
					count = 0;
					break;
				}
				if (transfer != 0) {
					count -= transfer;

					i.setAmount(i.getMaxStackSize());
				}
			}
		}
		if (count <= 0)
			return null;
		out.setAmount(count);

		while ((inventory.firstEmpty() != -1) && (count > 0)) {
			ItemStack temp = new ItemStack(out);
			int lowest = count;
			if (temp.getMaxStackSize() < lowest)
				lowest = temp.getMaxStackSize();
			count -= lowest;
			temp.setAmount(lowest);

			inventory.setItem(inventory.firstEmpty(), temp);
		}
		if (count <= 0)
			return null;
		out.setAmount(count);
		return out;
	}

	private boolean isBow(Material i) {
		return i.getId() == 261;
	}

	private boolean isTool(Material i) {
		int id = i.getId();
		return ((id >= 256) && (id <= 258)) || ((id >= 269) && (id <= 271))
				|| ((id >= 273) && (id <= 275)) || ((id >= 277) && (id <= 279))
				|| ((id >= 284) && (id <= 286));
	}

	private boolean isSword(Material i) {
		int id = i.getId();
		return (id == 267) || (id == 268) || (id == 272) || (id == 276)
				|| (id == 283);
	}

	private boolean isArmor(Material mat) {
		int id = mat.getId();
		return (id >= 298) && (id <= 317);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if ((!e.isCancelled())
				&& (e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
			Block block = e.getClickedBlock();
			if (block.getTypeId() == 130) {
				if (e.getPlayer().getWorld().equals(pl.getWarWorld())) {
					e.setCancelled(true);
					if ((invTime.containsKey(e.getPlayer().getName()))
							&& ((System.currentTimeMillis() - ((Long) invTime
									.get(e.getPlayer().getName())).longValue()) / 1000L < 10L)) {
						Util.sendMessage(e.getPlayer(), 
								ChatColor.RED
										+ "You must wait to re-open the chest");
						return;
					}

					invTime.put(e.getPlayer().getName(),
							Long.valueOf(System.currentTimeMillis()));
					openBankOwn(e.getPlayer());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInvClose(InventoryCloseEvent e) {
		if (!e.getInventory().getType().equals(InventoryType.CHEST))
			return;
		if (modifyInv.containsKey(e.getPlayer())) {
			if (((Integer) invType.get(modifyInv.get(e.getPlayer())))
					.intValue() == 0)
				savePlayer((String) modifyInv.get(e.getPlayer()), e
						.getInventory().getContents());
			else if (((Integer) invType.get(modifyInv.get(e.getPlayer())))
					.intValue() == 1)
				Util.savePlayer((String) modifyInv.get(e.getPlayer()),
						"WarItems", e.getInventory().getContents());
			invType.remove(modifyInv.get(e.getPlayer()));
			modifyInv.remove(e.getPlayer());
			return;
		}
		if (!e.getPlayer().getWorld().equals(pl.getWarWorld()))
			return;
		savePlayer(e.getPlayer().getName(), e.getInventory().getContents());
	}

	public void openBankOther(Player player, String other) {
		if (checkValid(other)) {
			Util.sendMessage(player, "Already being edited");
			return;
		}
		Inventory i = openBank(other, player);
		if (i == null) {
			Util.sendMessage(player, other + " does not have a warchest");
			return;
		}
		modifyInv.put(player, other);
		invType.put(other, Integer.valueOf(0));
		player.openInventory(i);
	}

	public void openBankOwn(Player player) {
		if (checkValid(player.getName())) {
			Util.sendMessage(player, "Bank is being checked, ask a mod for details");
			return;
		}
		Inventory i = openBank(player.getName(), player);
		if (i == null)
			i = pl.getServer().createInventory(player, 54,
					player.getName() + " War Bank");
		player.openInventory(i);
	}

	private boolean checkValid(String p) {
		return modifyInv.values().contains(p);
	}

	private Inventory openBank(String player, Player owner) {
		Inventory i = pl.getServer().createInventory(owner, 54,
				ChatColor.stripColor(player) + " War Bank");
		try {
			if (!readInv(player, i))
				return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}

	public static void savePlayer(String p, ItemStack[] items) {
		try {
			String directory = RCWars.returnPlugin().getDataFolder()
					.getAbsolutePath()
					+ "/Banks/";
			if (p == null)
				return;
			File f = new File(directory + p.toLowerCase() + ".txt");
			if (!f.exists())
				f.createNewFile();
			FileWriter fstream = new FileWriter(directory + p.toLowerCase()
					+ ".txt");
			BufferedWriter out = new BufferedWriter(fstream);
			if (items == null) {
				for (int i = 0; i < 54; i++)
					out.write("0\n");
				out.close();
				fstream.close();
				return;
			}
			for (ItemStack tempItem : items) {
				if (tempItem == null) {
					out.write("0");
					out.newLine();
				} else {
					out.write(tempItem.getTypeId() + " " + tempItem.getAmount()
							+ " " + tempItem.getDurability() + " "
							+ tempItem.getData().getData());
					for (Enchantment e : tempItem.getEnchantments().keySet()) {
						out.write(" " + e.getId() + ' '
								+ tempItem.getEnchantmentLevel(e));
					}

					if ((tempItem.getItemMeta() instanceof EnchantmentStorageMeta)) {
						for (Enchantment tempEnchant : ((EnchantmentStorageMeta) tempItem
								.getItemMeta()).getStoredEnchants().keySet())
							out.write(" "
									+ tempEnchant.getId()
									+ ' '
									+ ((EnchantmentStorageMeta) tempItem
											.getItemMeta())
											.getStoredEnchantLevel(tempEnchant));
					}
					out.newLine();
				}
			}
			out.close();
			fstream.close();
		} catch (Exception e1) {
			System.err.println("Error: " + e1.getMessage());
			if (p == null)
				return;
			Player player = Bukkit.getPlayer(p);
			if (player == null)
				return;
			Util.sendMessage(player, "Error: Please tell a mod and refrain from re-entering wars (savePlayer)");
		}
	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static boolean readInv(String p, Inventory i) throws Exception {
		int count = 0;
		try {
			if (p == null)
				return false;
			String directory = RCWars.returnPlugin().getDataFolder()
					.getAbsolutePath()
					+ "/Banks/";
			File f = new File(directory + p.toLowerCase() + ".txt");
			if (!f.exists()) {
				f.createNewFile();
				return false;
			}
			FileReader in = new FileReader(f);
			BufferedReader data = new BufferedReader(in);
			String s;
			while (((s = data.readLine()) != null) && (count < 54)) {
				if (s.startsWith("0")) {
					i.setItem(count, null);
					count++;
				} else {
					String[] items = s.split(" ");
					int type = Race.toInt(items[0]);
					int amt = Race.toInt(items[1]);
					short dur = (short) Race.toInt(items[2]);

					ItemStack setItem = new ItemStack(type, amt, dur);
					if ((items.length > 4) && (items.length % 2 == 0)) {
						if (setItem.getTypeId() == 403) {
							for (int x = 4; x < items.length; x += 2)
								((EnchantmentStorageMeta) setItem.getItemMeta())
										.addStoredEnchant(Enchantment
												.getById(Race.toInt(items[x])),
												Race.toInt(items[(x + 1)]),
												true);
						} else {
							for (int x = 4; x < items.length; x += 2) {
								setItem.addUnsafeEnchantment(Enchantment
										.getById(Race.toInt(items[x])), Race
										.toInt(items[(x + 1)]));
							}
						}
					}
					i.setItem(count, setItem);
					count++;
				}
			}
			data.close();
			in.close();
			return true;
		} catch (Exception e) {
			Bukkit.getLogger()
					.info("Error saving player " + p + " at " + count);
		}
		return false;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemBreak(PlayerItemBreakEvent e) {
		if (WarPlayers.getRace(e.getPlayer()) != null) {
			e.getBrokenItem().setAmount(e.getBrokenItem().getAmount());
			e.getBrokenItem().setDurability((short) 0);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemDrop(PlayerDropItemEvent e) {
		WarRank wr = WarRank.getPlayer(e.getPlayer());
		if (wr == null)
			return;
		int typeid = e.getItemDrop().getItemStack().getTypeId();
		if (typeid == 366 || typeid == 262) return;
		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPickup(PlayerPickupItemEvent e) {
		Race r = WarPlayers.getRace(e.getPlayer());
		if ((r != null) && (r.isRef()))
			e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemChange(PlayerItemHeldEvent e){
		Player p = e.getPlayer();
		Race r = WarPlayers.getRace(p);
		if (r == null) return;
		ItemStack item = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
		if (item == null) return;
		ItemMeta im = item.getItemMeta();
		if (im == null) return;
		if (im.getDisplayName() == null) return;
		BaseAbility b = AbilityTimer.str2abil.get(im.getDisplayName());
		if (b != null)
			b.clearAffects(p);
		else{
			b = AbilityTimer.str2abil.get(im.getDisplayName().substring(1, im.getDisplayName().length() - 1));
			if (b != null)
				b.clearAffects(p);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	  public void ondeath(PlayerDeathEvent e) { 
		Player dead = e.getEntity();
	    Player killer = e.getEntity().getKiller();
	    if ((killer == null) && 
	      (EntityListener.explDmg.containsKey(dead.getName()))) {
	      killer = Bukkit.getPlayer((String)EntityListener.explDmg.remove(dead.getName()));
	    }
	    ExperienceOrb o = (ExperienceOrb)dead.getWorld().spawnEntity(dead.getLocation(), EntityType.EXPERIENCE_ORB);
	    o.setExperience(e.getDroppedExp());
	    e.setDroppedExp(0);
	    ArrayList<ItemStack> toreturn = new ArrayList<ItemStack>();
	    if (WarPlayers.isPlaying(dead.getName())) {
	    	Iterator<ItemStack> i = e.getDrops().iterator();
	    	while(i.hasNext()) {
	    		ItemStack itemStack = i.next();
	    		if (itemStack == null){
	    			continue;
	    		}
	    		else if (RCWars.allowedItems.contains(itemStack.getTypeId())){
	    			toreturn.add(itemStack);
	    		}
	    		if (!RCWars.dropItems.contains(itemStack.getTypeId()))
	    			i.remove();
	    	}
	    	if (toreturn.size() != 0) itemsReturn.put(e.getEntity().getName(), toreturn.toArray(new ItemStack[0]));
	    	ItemStack is = new ItemStack(Material.SKULL_ITEM, 1);
	    	is.setDurability((short)3);
	    	SkullMeta meta = (SkullMeta)is.getItemMeta();
	    	meta.setOwner(dead.getName());
	    	is.setItemMeta(meta);
	    	e.getDrops().add(is);
	    	RCWars.logKill(dead, killer);
	    }
	  }
}