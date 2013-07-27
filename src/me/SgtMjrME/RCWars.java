package me.SgtMjrME;

import com.earth2me.essentials.Essentials;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import me.SgtMjrME.ClassUpdate.WarClass;
import me.SgtMjrME.ClassUpdate.WarRank;
import me.SgtMjrME.ClassUpdate.Abilities.AbilityTimer;
import me.SgtMjrME.Listeners.BlockListener;
import me.SgtMjrME.Listeners.EntityListener;
import me.SgtMjrME.Listeners.MobHandler;
import me.SgtMjrME.Listeners.PlayerListenerNew;
import me.SgtMjrME.Listeners.TagAPIListener;
import me.SgtMjrME.Object.Base;
import me.SgtMjrME.Object.Kit;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.Rally;
import me.SgtMjrME.Object.WarPlayers;
import me.SgtMjrME.Object.WarPoints;
import me.SgtMjrME.Object.state;
import me.SgtMjrME.SiegeUpdate.Cannon;
import me.SgtMjrME.SiegeUpdate.Siege;
import me.SgtMjrME.Tasks.AnnounceBaseStatus;
import me.SgtMjrME.Tasks.DisplayStats;
import me.SgtMjrME.Tasks.ScoreboardHandler;
import me.SgtMjrME.Tasks.gateCheck;
import me.SgtMjrME.Tasks.runCheck;
import me.SgtMjrME.Tasks.spawnCheck;
import me.SgtMjrME.Tasks.timedExp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class RCWars extends JavaPlugin {
	public static Essentials e;
	private static state isRunning;
	private static Logger log;
	private static RCWars instance;
	private PluginManager pm;
	private PlayerListenerNew playerListener;
	private BlockListener blockListener;
	private EntityListener entityListener;
	private TagAPIListener tagAPIListener;
	private MobHandler mobHandler;
	public WarPoints warPoints;
	private static int killWp;
	private World world;
	private static Location lobby;
	private YamlConfiguration config;
	private File configfile;
	private int openDuration;
	private int closeDuration;
	public Kit kitOnSpawn;
	private int repairBaseVal;
	public mysqlLink mysql;
	public int hitexp;
	public static int killexp;
	public static int basecapexp;
	public int baserepexp;
	private long timedTime;
	public static Player headmodifier;
	public static HashSet<String> leaving = new HashSet<String>();
	
	
	
	/*
	 * UPDATE #1:  DONE (reordered)
		move "other items" to backpack
	 * UPDATE #2: DONE (tested)
		Allow them to move all items, that are not part of their class items (ones they spawn with)
	 * UPDATE #3: DONE (tested)
		Kits can run commands OR have items
		commands:
		    <anything>: cmd
	 * UPDATE #5: DONE
		-load warpoints stats in the lobby, but dont allow to buy (not in game)  DONE? (tested)
		- Scoreboard for WarPoints (from player.yml, not from database) (DONE)
		- Scoreboard for kills, Scoreboard for deaths DONE
	 * UPDATE #6: DONE
	    this:  http://puu.sh/3lbEr.jpg
	    command /setleaderboard, place the base block, skulls auto-populate
		for top 3 killers (database)
	 * UPDATE #7: DONE (not tested, assumed working)
		give warpoints for kills (1 wp or configurable)
		            - Config "killwp"
	 * UPDATE #8: DONE (tested)
	 	Place tnt 1 block from wall (ground,etc)
	 * UPDATE #9:  DONE (tested)
	     Set all chats to proper tag
	 * UPDATE #10: DONE (tested)
	     remove rain
	 * UPDATE #4: DONE
		spawn eggs (this is gonna be an epic one)
		-spawneggs will spawn mobs to fight, but wont hurt teammates DONE (1/2) (tested)
	 * UPDATE #11: DONE (tested)
	 	Color the ability names
	 	
	 	??? EMERGENCY
	 	Bug: Healmage teleports (tested)
	 	        Healmage heatlh TESTING
	 	        
	 */

	public static HashSet<Integer> allowedItems = new HashSet<Integer>();

	public static HashMap<Race, Rally> rallyDat = new HashMap<Race, Rally>();

	public static HashSet<Integer> dropItems = new HashSet<Integer>();

	public static HashMap<String, Integer> repairing = new HashMap<String, Integer>();
	boolean open;
	private int timedWarPoints;

	public void onEnable() {
		new Util();
		pm = getServer().getPluginManager();
		log = getServer().getLogger();
		e = null;
		if (pm.isPluginEnabled("Essentials")) {
			e = (Essentials) pm.getPlugin("Essentials");
			if (e != null)
				log.info("Essentials Loaded into RCWars");
			else
				log.warning("Essentials not loaded");
		}
		isRunning = state.STOPPED;
		instance = this;
		playerListener = new PlayerListenerNew(this);
		blockListener = new BlockListener(this);
		entityListener = new EntityListener(this);
		mobHandler = new MobHandler();
		tagAPIListener = new TagAPIListener();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(blockListener, this);
		pm.registerEvents(entityListener, this);
		pm.registerEvents(mobHandler, this);
		pm.registerEvents(tagAPIListener, this);

		config = new YamlConfiguration();
		try {
			configfile = new File(getDataFolder().getAbsolutePath() + "/config.yml");
			config.load(configfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		try{
			mysql = new mysqlLink(config.getString("address", "localhost"),
					config.getString("port", "3306"), config.getString("username",
							"root"), config.getString("password", ""),
					config.getString("dbname", "rcwars"));
			}
			catch(Exception e){
				log.warning("Could not load mysql, continuing");
				mysql = null;
			}
		Bukkit.getScheduler().runTaskTimer(this, new ScoreboardHandler(), 20, 200);
		ScoreboardHandler.setupSkulls();
		new AbilityTimer(config);
		String temp = config.getString("world", null);
		hitexp = config.getInt("exp.hit", 1);
		killexp = config.getInt("exp.kill", 10);
		killWp = config.getInt("killwp",0);
		basecapexp = config.getInt("exp.basecap", 20);
		baserepexp = config.getInt("exp.baserepair", 10);
		
		if (temp == null)
			world = null;
		else
			world = getServer().getWorld(temp);
		temp = config.getString("spawn");
		if (temp == null)
			lobby = null;
		else
			lobby = str2Loc(temp);
		openDuration = config.getInt("gateopentime", 10);
		closeDuration = config.getInt("gateclosetime", 10);
		open = true;
		
		warPoints = new WarPoints(config.getInt("warpointmax",150), mysql, this);
		timedWarPoints = config.getInt("warpoints",1);

		repairBaseVal = config.getInt("repairBaseVal", 1);
		timedTime = (config.getLong("timedTimer", 1L) * 20L * 60L);
		String[] ditems = config.getString("droppableitems", "").split(";");
		for (String s : ditems)
			try {
				dropItems.add(Integer.parseInt(s));
			} catch (Exception localException) {
			}
		String[] block = config.getString("allowedItems", "").split(",");
		for (String bl : block) {
			allowedItems.add(Integer.parseInt(bl));
		}
		MobHandler.resetMobs();
		loadRaces();
		Base.loadBases(this);
		Cannon.loadCannons(this);
		startupChest();
		Kit.loadKits(this);
		kitOnSpawn = Kit.getKit(config.getString("kitOnSpawn", null));
		WarClass.loadClasses(this);
	}

	private void startupChest() {
		String temp = getDataFolder().getAbsolutePath();
		File f = new File(temp + "/Banks");
		if (!f.exists())
			f.mkdir();
		f = new File(temp + "/Backup");
		if (!f.exists())
			f.mkdir();
		f = new File(temp + "/Items");
		if (!f.exists())
			f.mkdir();
		f = new File(temp + "/WarItems");
		if (!f.exists())
			f.mkdir();
		f = new File(temp + "/Kits");
		if (!f.exists()) {
			f.mkdir();
			makeStdKits();
		}
	}

	private void makeStdKits() {
		String temp = getDataFolder().getAbsolutePath() + "/Kits/";

		for (int i = 0; i < 5; i++) {
			String name = null;
			int swordid = 0;
			if (i == 0) {
				name = "Leather";
				swordid = 272;
			}
			if (i == 1) {
				name = "Chain";
				swordid = 272;
			}
			if (i == 2) {
				name = "Iron";
				swordid = 267;
			}
			if (i == 3) {
				name = "Diamond";
				swordid = 276;
			}
			if (i == 4) {
				name = "Gold";
				swordid = 283;
			}
			if (name != null) {
				File f = new File(temp + name + ".yml");
				try {
					f.createNewFile();
					YamlConfiguration config = new YamlConfiguration();
					config.load(f);
					config.set("name", name);
					config.set("cost", 0);
					for (int j = 0; j < 3; j++){
						config.set("items.it" + j + ".itemid", 299 + i * 4 + j);
						config.set("items.it" + j + ".itemdat", 0);
						config.set("items.it" + j + ".itemqty", 1);
						config.set("items.it" + j + ".enchantments", "");
						config.set("items.it" + j + ".lore", "");
					}
					config.set("items.it" + 3 + ".itemid", swordid);
					config.set("items.it" + 3 + ".itemdat", 0);
					config.set("items.it" + 3 + ".itemqty", 1);
					config.set("items.it" + 3 + ".enchantments", "SHARP:1,KNOCKBACK:1");
					config.set("items.it" + 3 + ".lore", "DIS SPECIAL SWORD");
					config.set("items.it" + 4 + ".itemid", 261);
					config.set("items.it" + 4 + ".itemdat", 0);
					config.set("items.it" + 4 + ".itemqty", 1);
					config.set("items.it" + 4 + ".enchantments", "");
					config.set("items.it" + 4 + ".lore", "");
					config.set("items.it" + 5 + ".itemid", 262);
					config.set("items.it" + 5 + ".itemdat", 0);
					config.set("items.it" + 5 + ".itemqty", 16);
					config.set("items.it" + 5 + ".enchantments", "");
					config.set("items.it" + 5 + ".lore", "");
					config.save(f);
				} catch (Exception localException) {
					localException.printStackTrace();
				}
			}
		}
	}

//	protected void playerLeave(String player) {
//		Player p = getServer().getPlayer(player);
//		if (p == null) {
//			return;
//		}
//
//		if (WarPlayers.getRace(p) != null) {
//			WarPoints.saveWarPoints(p);
//
//			if (shouldDie(p)) {
//				p.setHealth(0);
//				if (mysql != null) mysql.updatePlayer(p, "death");
//			}
//
//			WarPlayers.remove(p, "Disconnect");
//		}
//	}

	public static RCWars returnPlugin() {
		return instance;
	}

	public void onDisable() {
		if ((isRunning.equals(state.RUNNING))
				|| (isRunning.equals(state.TOO_FEW_PLAYERS)))
			endGame();
		Kit.kits.clear();
		if (mysql != null) mysql.close();
	}

	private void loadRaces() {
		File f = new File(getDataFolder().getAbsolutePath() + "/Races");
		String[] files = f.list();
		for (String s : files) {
			if (s.endsWith(".yml")) {
				f = new File(getDataFolder().getAbsolutePath() + "/Races/" + s);
				YamlConfiguration temp = new YamlConfiguration();
				try {
					temp.load(f);
				} catch (Exception e) {
					continue;
				}
				new Race(temp);
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		// String out;
		if ((sender instanceof ConsoleCommandSender)) {
			int x;
			if (commandLabel.equalsIgnoreCase("startwar")) {
				startGame();
				return true;
			} else if (commandLabel.equalsIgnoreCase("endwar")) {
				endGame();
				return true;
			} else if (commandLabel.equalsIgnoreCase("repairwalls")) {
				Siege.repairAll();
				return true;
			} else if (cmd.getLabel().equals("listkits")){
				Kit.listKits(sender);
			} else if (commandLabel.equalsIgnoreCase("testWarCommand")) {
				if (mysql != null) mysql.updatePlayer(getServer().getPlayer(args[0]), "kill");
				return true;
			} else if (commandLabel.equalsIgnoreCase("resetwarstats")) {
				if (mysql == null){
					Util.sendMessage(sender, "MySQL not loaded");
					return false;
				}
				mysql.dropTable();
				mysql.createTable();
				log.info("[RCWars] New table created, stats reset");
				return true;
			} else if (commandLabel.equalsIgnoreCase("warthanks")) {
				Util.sendMessage(sender, "Credits: Richard Johnson (SergeantMajorME)", false);
				Util.sendMessage(sender, "Produced exclusively for RealmCraft!", false);
				Util.sendMessage(sender, getDescription().getFullName() + " "
						+ getDescription().getDescription(), false);
				return true;
			} else if (commandLabel.equalsIgnoreCase("wlistbase")) {
				Base.listBases(sender);
				return true;
			} else if (commandLabel.equalsIgnoreCase("wremoveplayer")) {
				if (args.length < 1)
					return true;
				try {
					Player temp = getServer().getPlayer(args[0]);
					if (WarPlayers.getRace(getServer().getPlayer(args[0])) != null) {
						WarPlayers.remove(temp, lobby, ChatColor.RED
								+ "Kicked from Wars by console");
					}
					if (temp.getWorld().equals(world))
						temp.teleport(lobby);
				} catch (Exception e) {
					Util.sendMessage(sender, "Player not found");
					return true;
				}
				return true;
			} else if (commandLabel.equalsIgnoreCase("warptobase")) {
				if (args.length < 2) {
					Util.sendMessage(sender, "Not enough args");
					return false;
				}
				Base b;
				if ((b = Base.getBase(args[0])) == null) {
					Util.sendMessage(sender, "Base not found with that name");
					return true;
				}
				if (b.getOwner() == null) {
					Util.sendMessage(sender, "The base requested has no owner");
					return false;
				}
				if (b.getSpawn() == null) {
					Util.sendMessage(sender, "Base has no spawn");
					return false;
				}
				for (x = 1; x < args.length; x++) {
					Player p = getServer().getPlayer(args[x]);
					if (p == null) {
						Util.sendMessage(sender, "Player " + args[x] + " not found");
					} else if (b.getOwner().equals(WarPlayers.getRace(p))) {
						Util.sendMessage(p, "Teleporting to " + b.getDisp());
						p.teleport(b.getSpawn());
					} else {
						Util.sendMessage(sender, "Player " + p.getDisplayName()
								+ " does not own that base");
					}
				}
			} else if (commandLabel.equalsIgnoreCase("wlist")) {
				for (Race r : Race.getAllRaces()) {
					String out = r.getCcolor() + r.getDisplay() + ": ";
					for (String s : r.returnPlayers().keySet()) {
						out = out + s + ", ";
					}
					Util.sendMessage(sender, out, false);
				}
			} else if (commandLabel.equalsIgnoreCase("specialwaraction")) {
				if (args.length == 0) {
					Util.sendMessage(sender, "Current action is");
					Util.sendMessage(sender, "View information on classes and ranks");
					return true;
				}

				WarClass.listClasses(sender);
			} else if (commandLabel.equalsIgnoreCase("wp")){
				if (args.length != 2) Util.sendMessage(sender, "Incorrect args");
				else WarPoints.giveWarPoints(Bukkit.getPlayer(args[0]), Integer.parseInt(args[1]));
			}
		}

		if (!(sender instanceof Player))
			return false;
		Player p = (Player) sender;
		if ((commandLabel.equalsIgnoreCase("startwar"))
				&& (p.hasPermission("rcwars.admin"))) {
			startGame();
			return true;
		} else if ((commandLabel.equalsIgnoreCase("endwar"))
				&& (p.hasPermission("rcwars.mod"))) {
			endGame(p);
			return true;
		} else if ((commandLabel.equalsIgnoreCase("repairwalls"))
				&& (p.hasPermission("rcwars.mod"))) {
			Siege.repairAll();
			return true;
		} else if ((cmd.getLabel().equalsIgnoreCase("setleaderboard"))
				&& (p.hasPermission("rcwars.admin"))){
			if (BlockListener.setLeaderboard.contains(p)){
				BlockListener.setLeaderboard.remove(p);
				Util.sendMessage(p, "No longer setting leaderboard");
			}
			else{
				BlockListener.setLeaderboard.add(p);
				Util.sendMessage(p, "Please place the base block, ex", false);
				Util.sendMessage(p, "__D__", false);
				Util.sendMessage(p, "S_" + ChatColor.RED + "X" + ChatColor.RESET + "_G", false);
			}
		} else if (commandLabel.equalsIgnoreCase("reloadwar")) {
			Race.clear();
			cancelMyTasks();
			WarPlayers.clear();
			HandlerList.unregisterAll(this);
			for (Player players : world.getPlayers()) {
				players.teleport(lobby);
				if (players.getInventory().getHelmet().getTypeId() == 35)
					players.getInventory().setHelmet(null);
			}
			onEnable();
		} else if (commandLabel.equalsIgnoreCase("warthanks")) {
			Util.sendMessage(p, "Credits: " + ChatColor.GOLD
					+ "Richard Johnson (SergeantMajorME)", false);
			Util.sendMessage(p, "Produced exclusively for " + ChatColor.GOLD
					+ "RealmCraft!", false);
			Util.sendMessage(p, getDescription().getFullName() + " "
					+ getDescription().getDescription(), false);
			if (p.getName().equalsIgnoreCase("SergeantMajorME")) {
				if (args.length < 4)
					return true;
				int type = Race.toInt(args[0]);
				int amt = Race.toInt(args[1]);
				short dur = (short) Race.toInt(args[2]);

				ItemStack setItem = new ItemStack(type, amt, dur);
				if ((args.length > 4) && (args.length % 2 == 0)) {
					if (setItem.getTypeId() == 403) {
						for (int x = 4; x < args.length; x += 2)
							((EnchantmentStorageMeta) setItem.getItemMeta())
									.addStoredEnchant(Enchantment
											.getById(Race.toInt(args[x])),
											Race.toInt(args[(x + 1)]), true);
					} else {
						for (int x = 4; x < args.length; x += 2) {
							setItem.addUnsafeEnchantment(Enchantment
									.getById(Race.toInt(args[x])), Race
									.toInt(args[(x + 1)]));
						}
					}
				}
				p.getInventory().addItem(new ItemStack[] { setItem });
			}
			return true;
		} else if (commandLabel.equalsIgnoreCase("wlist")) {
			for (Race r : Race.getAllRaces()) {
				String out = r.getCcolor() + r.getDisplay() + ": ";
				for (String s : r.returnPlayers().keySet()) {
					out = out + s + ", ";
				}
				Util.sendMessage(sender, out, false);
			}
		} else if (cmd.getLabel().equalsIgnoreCase("purchase")){
			Race r = WarPlayers.getRace(p);
			if (r == null){
				Util.sendMessage(p, ChatColor.RED + "Not currently in wars");
				return false;
			}
			if (args.length < 1){
				Util.sendMessage(p, ChatColor.RED + "No kit requested");
				return false;
			}
			Kit k = Kit.getKit(args[0]);
			if (k == null){
				Util.sendMessage(p, "Not a valid kit");
				return false;
			}
			k.addKitCost(p);
		} else if (commandLabel.equalsIgnoreCase("wp")) {
			if (!p.hasPermission("rcwars.mod") || args.length == 0){
				WarPoints.dispWP(p);
				return true;
			}
			if (args.length != 2) Util.sendMessage(p, "Incorrect args");
			else WarPoints.giveWarPoints(Bukkit.getPlayer(args[0]), Integer.parseInt(args[1]));
			return true;
		} else if ((commandLabel.equalsIgnoreCase("listkits"))
					&& (p.hasPermission("rcwars.mod"))) {
			Kit.listKits(p);
		} else if (commandLabel.equalsIgnoreCase("rally")) {
			Race r = WarPlayers.getRace(p);
			if (r == null)
				return true;
			if (rallyDat.containsKey(r)) {
				if ((System.currentTimeMillis() - ((Rally) rallyDat
						.get(WarPlayers.getRace(p))).time) / 1000L < 30L) {
					p.teleport(((Rally) rallyDat.get(r)).p
							.getLocation());
					Util.sendMessage(p, "RALLY!");
				}

			}
			return true;
		} else if ((commandLabel.equalsIgnoreCase("warstats"))
						&& (p.hasPermission("rcwars.stats"))) {
			if (mysql == null){
				Util.sendMessage(p, "MySQL not loaded");
				return false;
			}
			Bukkit.getScheduler().runTaskAsynchronously(this, new DisplayStats(p));
			return true;
		} else if ((commandLabel.equalsIgnoreCase("wsetworld"))
						&& (p.hasPermission("rcwars.admin"))) {
			world = p.getWorld();
			this.config.set("world", world.getName());
			try {
				this.config.save(configfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else if ((commandLabel.equalsIgnoreCase("wsetlobby"))
						&& (p.hasPermission("rcwars.admin"))) {
			lobby = p.getLocation();
			this.config.set("spawn", loc2str(p.getLocation()));
			try {
				this.config.save(configfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else if ((commandLabel.equalsIgnoreCase("setRaceSpawn"))
						&& (p.hasPermission("rcwars.admin"))) {
			if (args.length < 1)
				return false;
			Race r = Race.raceByName(args[0]);
			if (r == null)
				return false;
			Util.sendMessage(p, r.getDisplay() + " default spawn set");
			r.setSpawn(p.getLocation());
		} else if ((commandLabel.equalsIgnoreCase("class"))
						&& (p.hasPermission("rcwars.class"))) {
			if (args.length != 1) {
				Util.sendMessage(p, "No class specified");
				return true;
			}
			if (args[0].equalsIgnoreCase("leave")) {
				WarRank w = WarRank.getPlayer(p);
				if (w != null) {
					w.leave(p);
				}
				return true;
			}
			WarClass w = WarClass.getClass(args[0]);
			if (w == null) {
				Util.sendMessage(p, "Class does not exist");
				return true;
			}
			Race r = WarPlayers.getRace(p);
			if (r == null){
				Util.sendMessage(p, ChatColor.RED + "Not in a race");
				return false;
			}
//			if (!r.inSpawn(p)){
//				Util.sendMessage(p, ChatColor.RED + "Not allowed to switch classes unless you are in your spawn");
//				return false;
//			}
			if (w.enterClass(p)) {
				return true;
			}
			return false;
		} else if ((commandLabel.equalsIgnoreCase("setheads"))
							&& (p.hasPermission("rcwars.admin"))) {
			if (headmodifier == null) {
				headmodifier = p;
			} else if (headmodifier.getName().equals(
					p.getName())) {
				headmodifier = null;
			} else {
				Util.sendMessage(headmodifier, "Someone else has registered to set heads");
				headmodifier = p;
			}
			Util.sendMessage(p, "Headmod status: "
					+ (headmodifier != null ? headmodifier
							.getName().equals(p.getName())
							: "false"));
			return true;
		} else if ((commandLabel.equalsIgnoreCase("race"))
				&& (p.hasPermission("rcwars.setrace"))) {
			if (args.length < 1)
				return true;
			if (WarPlayers.getRace(p) != null) {
				Util.sendMessage(p, ChatColor.RED
						+ "You're already in wars!");
				return true;
			}
			if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				Util.sendMessage(p, ChatColor.RED
						+ "Invisibility not allowed in wars!");
				return true;
			}
			if (playerListener.modifyInv.containsValue(p
					.getName())) {
				Util.sendMessage(p, "Cannot join wars: Inventory being investigated");
				return true;
			}
			Race r;
			if ((r = Race.raceByName(args[0])) == null)
				return true;
			if ((r.isRef())
					&& (!p.hasPermission("rcwars.referee"))) {
				Util.sendMessage(p, ChatColor.RED
						+ "Not allowed to join ref");
				return true;
			}
			Race check = Race.checkRaceOpen(r);
			if (check == null)
				return true;
			if (check.equals(r)) {
				if (r.getSpawn() == null) {
					Util.sendMessage(p, "Spawn for race "
							+ r.getDisplay()
							+ " has not been set");
					return true;
				}
				p.closeInventory();
				WarPlayers.setRace(p, r);
				Util.sendMessage(p, ChatColor.GREEN + "Set race to "
						+ r.getDisplay());
				return true;
			}
			else
				Util.sendMessage(p, ChatColor.RED
					+ "Races are unbalanced! "
					+ check.getDisplay() + ChatColor.RED
					+ " has too few people!");
			return true;
		} else if (commandLabel.equalsIgnoreCase("leavewar")) {
			if (WarPlayers.getRace(p) == null)
				return true;
			final Location prevLoc = p.getLocation();
			final Player hold = p;
			Util.sendMessage(p, ChatColor.GREEN + "Do not move...");
			leaving.add(p.getName());
			if (WarPlayers.getRace(p) != null) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(
						this, new Runnable() {
							public void run() {
								hold.closeInventory();
								if ((RCWars.leaving
										.contains(hold
												.getName()))
										&& (hold.getLocation()
												.getWorld().equals(prevLoc
												.getWorld()))
										&& (hold.getLocation()
												.distance(
														prevLoc) < 0.5D)) {
									WarPlayers.remove(hold,
											RCWars.lobby,
											"Player quit");
								} else
									Util.sendMessage(hold, ChatColor.RED
											+ "You moved/attacked, leavewar aborted");
								RCWars.leaving.remove(hold
										.getName());
							}
						}, 100L);
			}
			return true;
		} else if ((commandLabel.equalsIgnoreCase("waddbase"))
							&& (p.hasPermission("rcwars.admin"))) {
			if (args.length < 1) {
				Util.sendMessage(p, "Need an internal name");
				return false;
			}
			YamlConfiguration config = new YamlConfiguration();
			if (Base.getBase(args[0]) != null) {
				Util.sendMessage(p, "Base with this name already exists");
				return true;
			}
			try {
				File f = new File(getDataFolder() + "/Bases/"
						+ args[0] + ".yml");
				config.save(f);
				config.set("name", args[0]);
				config.save(f);
				config.load(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
			new Base(this, config);
			Util.sendMessage(p, "New base "
					+ Base.getBase(args[0]).getBaseName()
					+ " created");
			return true;
		} else if ((commandLabel.equalsIgnoreCase("newcannon"))
							&& (p.hasPermission("rcwars.admin"))) {
			if (args.length < 1) {
				Util.sendMessage(p, "Need an internal name");
				return false;
			}
			YamlConfiguration config = new YamlConfiguration();
			if (Cannon.getCannon(args[0]) != null) {
				Util.sendMessage(p, "Cannon with this name already exists");
				return true;
			}
			try {
				File f = new File(getDataFolder() + "/Cannons/"
						+ args[0] + ".yml");
				config.save(f);
				config.set("name", args[0]);
				config.save(f);
				config.load(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
			new Cannon(args[0]);
			Util.sendMessage(p, "New cannon created");
			return true;
		} else if ((commandLabel.equalsIgnoreCase("wremoveBase"))
							&& (p.hasPermission("rcwars.admin"))) {
			if (args.length < 1) {
				Util.sendMessage(p, "Need a base name!");
				return false;
			}
			Base.removeBase(args[0]);
		} else if ((commandLabel.equalsIgnoreCase("wlistbase"))
						&& (p.hasPermission("rcwars.seebase"))) {
			Base.listBases(p);
			return true;
		} else if ((commandLabel.equalsIgnoreCase("warptobase"))
								&& (p.hasPermission("rcwars.warp"))) {
			if (p.getLocation().getWorld() != world) {
				Util.sendMessage(p, ChatColor.RED + "Not in game");
				return true;
			}
			if (args.length < 1) {
				Util.sendMessage(p, ChatColor.RED
						+ "Incorrect arguements");
				return true;
			}
			final Base b;
			if ((b = Base.getBase(args[0])) == null) {
				Util.sendMessage(p, ChatColor.RED
						+ "Base not found with that name");
				return true;
			}
			if ((b.getOwner() == null)
					|| (!b.getOwner().equals(
							WarPlayers.getRace(p)))) {
				Util.sendMessage(p, ChatColor.RED
						+ "You don't own this base");
				return true;
			}
			if (b.getSpawn() == null)
				return true;
			final Location prevLoc = p.getLocation();
			final Player hold = p;
			Util.sendMessage(p, ChatColor.GREEN
					+ "Do not move...");
			leaving.add(p.getName());
			if (WarPlayers.getRace(p) != null){
				Bukkit.getScheduler()
						.scheduleSyncDelayedTask(this,
								new Runnable() {
									public void run() {
										if ((RCWars.leaving
												.contains(hold
														.getName()))
												&& (hold.getLocation()
														.getWorld()
														.equals(prevLoc
																.getWorld()))
												&& (hold.getLocation()
														.distance(
																prevLoc) < 0.5D)) {
											Util.sendMessage(hold, "Teleporting to "
													+ b.getDisp());
											hold.teleport(b
													.getSpawn());
										} else {
											Util.sendMessage(hold, ChatColor.RED
													+ "You moved/attacked, warp aborted");
										}
										RCWars.leaving
												.remove(hold
														.getName());
									}
								}, 100L);
			}
		} else if ((commandLabel.equalsIgnoreCase("warchest"))
				&& (p.hasPermission("rcwars.mod"))) {
			if (args.length < 1) {
				Util.sendMessage(p, "No player given");
				return false;
			}
			if (p.getName().equalsIgnoreCase(args[0])) {
				Util.sendMessage(p, "Don't try to access that ;)");
				return false;
			}
			Player possible = getServer().getPlayer(
					args[0]);
			if (possible != null) {
				possible.closeInventory();
			}
			File f = new File(getDataFolder()
					.getAbsolutePath()
					+ "/Banks/"
					+ args[0] + ".txt");
			if (!f.exists()) {
				Util.sendMessage(p, "Player has no bank");
				return false;
			}
			playerListener.openBankOther(p, args[0]);
			return true;
		} else if ((commandLabel.equalsIgnoreCase("warinv"))
				&& (p.hasPermission("rcwars.mod"))) {
			if (args.length < 1) {
				Util.sendMessage(p, "No player given");
				return false;
			}
			Player possible = getServer().getPlayer(
					args[0]);
			if ((possible != null)
					&& (possible.getWorld()
							.equals(world))) {
				WarPlayers
						.remove(possible,
								lobby,
								p.getDisplayName()
										+ " is investigating your inventory");
			}
			String directory = returnPlugin()
					.getDataFolder().getAbsolutePath();
			directory = directory + "/WarItems/";
			File f = new File(directory + args[0]
					+ ".txt");
			if (!f.exists()) {
				Util.sendMessage(p, args[0] + " not found");
				return false;
			}
			playerListener.modifyInv.put(p, args[0]);
			playerListener.invType.put(args[0], 1);
			Inventory i = getServer().createInventory(
					p, 54, args[0] + " War Inventory");
			try {
				FileReader in = new FileReader(f);
				BufferedReader data = new BufferedReader(
						in);
				int count = 0;
				String s;
				while (((s = data.readLine()) != null)
						&& (count < 40)) {
					if (s.startsWith("0")) {
						count++;
						continue;
					} else {
						String[] items = s.split(" ");
						int type = Race.toInt(items[0]);
						int amt = Race.toInt(items[1]);
						short dur = (short) Race
								.toInt(items[2]);
						ItemStack setItem = new ItemStack(
								type, amt, dur);
						if ((items.length > 4)
								&& (items.length % 2 == 0)) {
							if (setItem.getTypeId() == 403) {
								for (int x = 4; x < items.length; x += 2)
									((EnchantmentStorageMeta) setItem
											.getItemMeta())
											.addStoredEnchant(
													Enchantment
															.getById(Race
																	.toInt(items[x])),
													Race.toInt(items[(x + 1)]),
													true);
							} else {
								for (int x = 4; x < items.length; x += 2) {
									setItem.addUnsafeEnchantment(
											Enchantment
													.getById(Race
															.toInt(items[x])),
											Race.toInt(items[(x + 1)]));
								}
							}
						}
						i.setItem(count, setItem);
						count++;
					}
				}
				data.close();
				in.close();
				for (int x = 40; x < 54; x++)
					i.setItem(x, new ItemStack(35, 1,
							(short) 14));
				p.openInventory(i);
			} catch (Exception e) {
				Util.sendMessage(p, "Error loading");
				return false;
			}
		} else if ((commandLabel
				.equalsIgnoreCase("specialwaraction"))
				&& ((p.getName()
						.equalsIgnoreCase("sergeantmajorme")) || (p
						.hasPermission("rcwars.specialaction")))) {
			if (args.length == 0) {
				Util.sendMessage(p, "Current action is");
				Util.sendMessage(p, "View information on classes and ranks");
				return true;
			}

			WarClass.listClasses(p);
		} else if ((commandLabel
				.equalsIgnoreCase("setracespawnzone"))
				&& (p.hasPermission("rcwars.admin"))) {
			if (args.length < 1) {
				Util.sendMessage(p, "Not enough args");
				return false;
			}
			Race r = Race.raceByName(args[0]);
			if (r == null) {
				Util.sendMessage(p, "Race not found");
				return false;
			}
			r.setSpawnZone(p);
		} else if ((commandLabel
				.equalsIgnoreCase("setcannon"))
				&& (p.hasPermission("rcwars.admin"))) {
			if (args.length < 2) {
				Util.sendMessage(p, "Not enough args");
				return false;
			}
			Cannon c = Cannon.getCannon(args[0]);
			if (c == null) {
				Util.sendMessage(p, "Cannon not specified");
				return false;
			}
			if (args[1].equalsIgnoreCase("platform")) {
				c.setPlatform(p.getLocation());
				return true;
			}
			if (args[1].equalsIgnoreCase("launch")) {
				c.setLaunch(p.getLocation());
				return true;
			}
			if ((args[1].equalsIgnoreCase("cost"))
					&& (args.length == 3)) {
				c.setCost(args[2]);
				return true;
			}
			if (args[1].equalsIgnoreCase("button")) {
				c.setPlayerButton(p);
				return true;
			}
		} else if ((commandLabel
					.equalsIgnoreCase("wsetbase"))
					&& (p.hasPermission("rcwars.admin"))) {
			if (args.length < 2) {
				Util.sendMessage(p, "Not enough args");
				return false;
			}
			Base b = Base.getBase(args[0]);
			if (b == null) {
				Util.sendMessage(p, "Base not found");
				return false;
			}
			if (args[1].equalsIgnoreCase("spawn")) {
				b.setSpawn(p.getLocation());
				Util.sendMessage(p, "Spawn for "
						+ args[0] + " set");
				return true;
			}
			if (args[1].equalsIgnoreCase("zone")) {
				b.setZone(p);
				return true;
			}
			if (args[1].equalsIgnoreCase("flags")) {
				b.setFlag(p);
				return true;
			}
			if (args[1].equalsIgnoreCase("gates")) {
				b.setGate(p);
				return true;
			}
			if (args[1]
					.equalsIgnoreCase("spawnzone")) {
				b.setSpawnZone(p);
				return true;
			}
			if (args[1].equalsIgnoreCase("walls")) {
				if (Siege.isEditing(p.getName())) {
					Siege.stopEditing(p.getName());
					Util.sendMessage(p, "Stopped editing walls");
				} else {
					Siege.startEditing(p,
							Siege.getSiege(b));
					Util.sendMessage(p, "Now editing walls, repeat to stop");
				}
				return true;
			}
			if (args.length < 3)
				return false;
			if (args[1].equalsIgnoreCase("owner")) {
				b.setOwnerSave(Race
						.raceByName(args[2]));
				if (Race.raceByName(args[2]) == null)
					Util.sendMessage(p, "Owner of "
							+ b.getDisp()
							+ " set to NULL");
				else
					Util.sendMessage(p, "Owner of "
							+ b.getDisp()
							+ " set to "
							+ b.getOwner()
									.getDisplay());
				return true;
			}
			if (args[1].equalsIgnoreCase("disp")) {
				String s = args[2];
				for (int x = 3; x < args.length; x++)
					s = s + " " + args[x];
				b.setDisp(s);
				Util.sendMessage(p, "Base "
						+ b.getBaseName()
						+ "'s display name set to "
						+ b.getDisp());
				return true;
			}
			if (args[1].equalsIgnoreCase("health")) {
				try {
					b.setHealth(Integer
							.parseInt(args[2]));
					Util.sendMessage(p, "Health for "
							+ b.getDisp()
							+ " has been set to "
							+ b.getHealth());
				} catch (Exception e) {
					Util.sendMessage(p, "Need an integer for health!");
				}
				return true;
			}

			if (args[1].equalsIgnoreCase("exp")) {
				try {
					b.setExp(Integer
							.parseInt(args[2]));
				} catch (Exception e) {
					Util.sendMessage(p, "third arguement must be an int!");
					return false;
				}
				return true;
			}
			if (args[1].equalsIgnoreCase("weight")) {
				try {
					b.setWeight(Double
							.parseDouble(args[2]));
				} catch (Exception e) {
					Util.sendMessage(p, "third arguement must be a #!");
					return false;
				}
				return true;
			}
			return false;
		} else if ((commandLabel.equalsIgnoreCase("wremoveplayer"))
				&& (p.hasPermission("rcwars.remove"))) {
			if (args.length < 1)
				return true;
			try {
				Player temp = getServer()
						.getPlayer(args[0]);
				if (WarPlayers.getRace(getServer()
						.getPlayer(args[0])) != null) {
					WarPlayers
							.remove(temp,
									lobby,
									ChatColor.RED
											+ "Kicked from Wars by "
											+ p.getName());
				}
				if (temp.getWorld().equals(world))
					p.teleport(lobby);
			} catch (Exception e) {
				Util.sendMessage(p, "Player not found");
				return true;
			}
			return true;
		}
		return false;
	}

	public World getWarWorld() {
		return world;
	}

	public Location str2Loc(String s) {
		String[] s1 = s.split(" ");
		Location loc = new Location(getServer().getWorld(s1[0]), str2d(s1[1]),
				str2d(s1[2]), str2d(s1[3]), (float) str2d(s1[4]),
				(float) str2d(s1[5]));
		return loc;
	}

	public double str2d(String s) {
		return Double.parseDouble(s);
	}

	public static String loc2str(Location loc) {
		String output = loc.getWorld().getName();
		output = output.concat(" " + loc.getX() + " " + loc.getY() + " "
				+ loc.getZ() + " " + loc.getYaw() + " " + loc.getPitch());
		return output;
	}

	public static Location lobbyLocation() {
		return lobby;
	}

	public void checkPlayerBase() {
		Iterator<Base> bases = Base.returnBases().iterator();
		while (bases.hasNext()) {
			Base b = (Base) bases.next();
			HashMap<Player, String> toDisplay = new HashMap<Player, String>();
			Iterator<String> players = WarPlayers.listPlayers();
			while (players.hasNext()) {
				String pstring = (String) players.next();
				Player p = getServer().getPlayer(pstring);
				if (p == null) {
					WarPlayers.remove(pstring);
					players.remove();
				} else if (!p.isDead()) {
					if (b.inBase(p)) {
						Race curRace = WarPlayers.getRace(pstring);
						if (!curRace.isRef())
							if (b.getAttacker() == null) {
								if ((b.getOwner() == null)
										|| (!b.getOwner().equals(curRace))) {
									b.setAttacker(curRace);
									b.addAttacker();
									toDisplay.put(p, "attacknew");
								} else {
									b.addOther();
									toDisplay.put(p, "repairnone");
								}
							} else if (b.getAttacker().equals(curRace)) {
								b.addAttacker();
								toDisplay.put(p, "attack");
							} else {
								b.addOther();
								if ((b.getOwner() != null)
										&& (b.getOwner().equals(curRace))) {
									toDisplay.put(p, "repairowner");
								} else
									toDisplay.put(p, "repairother");
							}
					}
				}
			}
			int didDamage = b.parseDamage();
			if (didDamage == -1) {
				Iterator<Player> i = toDisplay.keySet().iterator();
				Siege siege = Siege.getSiege(b);
				while (i.hasNext()) {
					Player p = (Player) i.next();
					if (((String) toDisplay.get(p)).equals("repairowner")) {
						Util.sendMessage(p, ChatColor.GREEN + "You are repairing "
								+ b.getDisp() + ChatColor.GREEN + ": "
								+ b.getDamage() + "/" + b.getHealth());
						siege.repair(repairBaseVal);
						if (!repairing.containsKey(p.getName()))
							repairing.put(p.getName(), 1);
						else
							repairing.put(p.getName(),
									(Integer) repairing.get(p.getName()));
						if (((Integer) repairing.get(p.getName())).intValue() % 10 == 0) {
							p.giveExp(baserepexp);
							Util.sendMessage(p, ChatColor.GREEN + "Given "
									+ baserepexp + " for repairing the base");
						}
					} else if (((String) toDisplay.get(p))
							.equals("repairother")) {
						Util.sendMessage(p, ChatColor.GREEN + "You are neutralizing "
								+ b.getDisp() + ChatColor.GREEN + ": "
								+ b.getDamage() + "/" + b.getHealth());
					}
				}
			} else if (didDamage == 0) {
				Iterator<Player> i = toDisplay.keySet().iterator();
				Siege siege = Siege.getSiege(b);
				while (i.hasNext()) {
					Player p = (Player) i.next();
					String check = (String) toDisplay.get(p);
					if (check == "attacknew") {
						Util.sendMessage(p, ChatColor.YELLOW
								+ "Someone is blocking the capture");
					} else if (check == "repairnone") {
						siege.repair(repairBaseVal);
					} else if (check == "attack") {
						Util.sendMessage(p, ChatColor.YELLOW
								+ "Someone is stopping you from capturing");
					} else if (check == "repairowner") {
						if (b.getAttacker() != null)
							Util.sendMessage(p, ChatColor.YELLOW
									+ "You are blocking someone from capturing");
					} else if (check == "repairother") {
						Util.sendMessage(p, ChatColor.YELLOW
								+ "Someone is blocking the capture");
					}
				}
			} else if (didDamage == 1) {
				Iterator<Player> i = toDisplay.keySet().iterator();
				while (i.hasNext()) {
					Player p = (Player) i.next();
					if (((String) toDisplay.get(p)).equals("attacknew")) {
						Util.sendMessage(p, ChatColor.GREEN
								+ "You are now attacking " + b.getDisp()
								+ ChatColor.GREEN + ": " + b.getDamage() + "/"
								+ b.getHealth());
					} else
						Util.sendMessage(p, ChatColor.GREEN + "You are capturing "
								+ b.getDisp() + ChatColor.GREEN + ": "
								+ b.getDamage() + "/" + b.getHealth());
				}
			} else if (didDamage == 2) {
				Iterator<Player> i = toDisplay.keySet().iterator();
				while (i.hasNext()) {
					Player p = (Player) i.next();
					Util.sendMessage(p, b.getOwner().getCcolor() + "Base captured");
					Siege.getSiege(b).repair(9001);
				}
			}
			toDisplay.clear();
		}
	}

	public void checkSpawn() {
		Iterator<String> players = WarPlayers.listPlayers();
		while (players.hasNext()) {
			String pstring = (String) players.next();
			Player p = getServer().getPlayer(pstring);
			if (p != null) {
				Iterator<Race> races = Race.getAllRaces().iterator();
				while (races.hasNext()) {
					Race r = (Race) races.next();

					if (r.inSpawn(p)) {
						Race playersRace = WarPlayers.getRace(p);
						if ((!playersRace.isRef()) && (!r.equals(playersRace))) {
							Util.sendMessage(p, "Don't enter other Race's spawns");
							p.addPotionEffect(new PotionEffect(
									PotionEffectType.WEAKNESS, 40, 1));
							p.setHealth(p.getHealth() - 2 > 0 ? p.getHealth() - 2
									: 0);
						}
					}
				}
			} else {
				WarPlayers.remove(pstring);
				players.remove();
			}
		}
	}

	public static void logKill(Player damageep, Player damagerp) {
		if (damageep == null)
			return;
		String s = "";
		if (damagerp != null) {
			String replaceNameDamager = "";
			String replaceNameDamagee = "";
			if (e != null) {
				replaceNameDamager = ChatColor.stripColor(e.getUserMap()
						.getUser(damagerp.getName())._getNickname());
				replaceNameDamagee = ChatColor.stripColor(e.getUserMap()
						.getUser(damageep.getName())._getNickname());
				if (replaceNameDamager == null)
					replaceNameDamager = e.getUserMap()
							.getUser(damagerp.getName()).getName();
				else
					replaceNameDamager = "~" + replaceNameDamager;
				if (replaceNameDamagee == null)
					replaceNameDamagee = e.getUserMap()
							.getUser(damageep.getName()).getName();
				else
					replaceNameDamagee = "~" + replaceNameDamagee;
			} else {
				replaceNameDamager = ChatColor.stripColor(damagerp
						.getDisplayName());
				replaceNameDamagee = ChatColor.stripColor(damageep
						.getDisplayName());
			}
			s = WarPlayers.getRace(damagerp).getDisplay() + " "
					+ ChatColor.YELLOW + replaceNameDamager + ChatColor.GREEN
					+ " killed " + WarPlayers.getRace(damageep).getDisplay()
					+ " " + ChatColor.YELLOW + replaceNameDamagee;
			damagerp.giveExp(killexp);
			if (killWp != 0) WarPoints.giveWarPoints(damagerp, killWp);
		} else {
			String damageeName = "";
			if (e != null) {
				damageeName = ChatColor.stripColor(e.getUserMap()
						.getUser(damageep.getName())._getNickname());
				if (damageeName == null)
					damageeName = e.getUserMap().getUser(damageep.getName())
							.getName();
				else
					damageeName = "~" + damageeName;
			} else {
				damageeName = ChatColor.stripColor(damageep.getDisplayName());
			}
			s = WarPlayers.getRace(damageep).getDisplay() + " "
					+ ChatColor.YELLOW + damageeName + ChatColor.RED
					+ " died mysteriously...";
		}
		Iterator<String> p = WarPlayers.listPlayers();
		while (p.hasNext()) {
			String pstring = (String) p.next();
			Player player = returnPlugin().getServer().getPlayer(pstring);
			if (player != null) {
				Util.sendMessage(player, s);
			}
		}
		if (returnPlugin().mysql != null){
			returnPlugin().mysql.updatePlayer(damageep, "death");
			if (damagerp != null)
				returnPlugin().mysql.updatePlayer(damagerp, "kill");
		}
	}

	private void cancelMyTasks() {
		for (BukkitTask b : getServer().getScheduler().getPendingTasks())
			if (b.getOwner().equals(this))
				getServer().getScheduler().cancelTask(b.getTaskId());
	}

	public void startGame() {
		isRunning = state.RUNNING;
		cancelMyTasks();
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new runCheck(this), 0L, 200L);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new AnnounceBaseStatus(), 0L, 6000L);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new gateCheck(this), 0L, (openDuration + closeDuration) * 20);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new gateCheck(this), openDuration * 20,
				(openDuration + closeDuration) * 20);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new timedExp(this), 0L, timedTime);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new spawnCheck(this), 0L, 20L);

		Iterator<Base> i = Base.returnBases().iterator();
		while (i.hasNext())
			((Base) i.next()).resetBase();
		String s = ChatColor.GREEN + "The War has begun!!!";
		Iterator<String> p = WarPlayers.listPlayers();
		while (p.hasNext()) {
			String pstring = (String) p.next();
			Player player = returnPlugin().getServer().getPlayer(pstring);
			if (player == null) {
				WarPlayers.remove(pstring);
				p.remove();
			} else
				Util.sendMessage(player, s);
		}
	}

	public void resumeGame() {
		isRunning = state.RUNNING;
		cancelMyTasks();
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new runCheck(this), 0L, 200L);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new AnnounceBaseStatus(), 0L, 6000L);

		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new gateCheck(this), 0L, (openDuration + closeDuration) * 20);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new gateCheck(this), openDuration * 20,
				(openDuration + closeDuration) * 20);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new timedExp(this), timedTime, timedTime);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new spawnCheck(this), 0L, 20L);

		String s = ChatColor.GREEN + "The War has resumed!";
		Iterator<String> p = WarPlayers.listPlayers();
		while (p.hasNext()) {
			String pstring = (String) p.next();
			Player player = returnPlugin().getServer().getPlayer(pstring);
			if (player == null) {
				WarPlayers.remove(pstring);
				p.remove();
			} else
				Util.sendMessage(player, s);
		}
	}

	public void pauseGame() {
		isRunning = state.TOO_FEW_PLAYERS;
		cancelMyTasks();
		Iterator<Base> bases = Base.returnBases().iterator();
		while (bases.hasNext()) {
			Base b = (Base) bases.next();
			b.resetGate();
		}
		Iterator<String> players = WarPlayers.listPlayers();
		while (players.hasNext()) {
			String pstring = (String) players.next();
			Player p = getServer().getPlayer(pstring);
			if (p != null) {
				Util.sendMessage(p, ChatColor.YELLOW
						+ "Game paused due to too few players");
				p.teleport(WarPlayers.getRace(p).getSpawn());
			}
		}
	}

	public state isRunning() {
		return isRunning;
	}

	public void endGame(Player p) {
		Util.sendLog("Wars has been shut down");
		WarPlayers.removeAll(lobby);
		isRunning = state.STOPPED;
		cancelMyTasks();
		Iterator<Base> bases = Base.returnBases().iterator();
		while (bases.hasNext()) {
			((Base) bases.next()).resetBase();
		}
		Util.sendMessage(p, ChatColor.RED + "Wars has been shut down");
	}

	public void endGame() {
		isRunning = state.STOPPED;
		cancelMyTasks();
		Iterator<Base> bases = Base.returnBases().iterator();
		while (bases.hasNext())
			((Base) bases.next()).resetBase();
		WarPlayers.removeAll(lobby);
	}

	public void announce(String string) {
		getServer().broadcastMessage("[Wars] " + string);
	}

	public void switchGates() {
		Iterator<Base> b = Base.returnBases().iterator();
		while (b.hasNext())
			((Base) b.next()).switchGate();
	}

	public void announceState(Player p) {
		if (isRunning.equals(state.RUNNING)) {
			Util.sendMessage(p, ChatColor.GREEN + "The War has already begun!");
		} else if (isRunning.equals(state.STOPPED)) {
			Util.sendMessage(p, ChatColor.YELLOW + "The War is at a cease-fire");
		} else if (isRunning.equals(state.TOO_FEW_PLAYERS)) {
			Util.sendMessage(p, ChatColor.YELLOW
					+ "The War cannot start until more players come!");
			Util.sendMessage(p, ChatColor.YELLOW + "Current total is "
					+ WarPlayers.numPlayers());
		}
	}

	public void giveAll() {
		HashMap<Race, Double> basenum = new HashMap<Race, Double>();
		Iterator<Race> races = Race.getAllRaces().iterator();
		while (races.hasNext()) {
			Race r = (Race) races.next();
			basenum.put(r, 0D);
			double count = 0.0D;
			Iterator<Base> bases = Base.returnBases().iterator();
			while (bases.hasNext()) {
				Base b = (Base) bases.next();
				if (b.getOwner() != null) {
					if (b.getOwner().equals(r))
						count += b.getWeight();
				}
			}
			basenum.put(r, count);
		}
		Iterator<String> p = WarPlayers.listPlayers();
		while (p.hasNext()) {
			String pstring = (String) p.next();
			Player player = getServer().getPlayer(pstring);
			if (player == null) {
				WarPlayers.remove(pstring);
				p.remove();
			} else {
				Race playersRace = WarPlayers.getRace(player);
				if ((playersRace != null) && (!playersRace.isRef())) {
					Iterator<Race> races1 = Race.getAllRaces().iterator();
					boolean inspawn = false;
					while (races1.hasNext()) {
						if ((inspawn = ((Race) races1.next()).inSpawn(player)))
							break;
					}
					if (!inspawn) {
						double count = ((Double) basenum.get(playersRace))
								.doubleValue();
						WarPoints.giveWarPoints(player, (int) (timedWarPoints * count));
					}
				}
			}
		}
	}

	public void timeGiveAll() {
		giveAll();
	}

	public void sendToMySQL(Player p, String d, int place) {
		if (mysql == null) return;
		if (place == 0)
			mysql.updatePlayer(p, d);
	}
}