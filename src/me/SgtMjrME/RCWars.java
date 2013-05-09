package me.SgtMjrME;

import com.earth2me.essentials.Essentials;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import me.SgtMjrME.Listeners.PlayerListenerNew;
import me.SgtMjrME.Object.Base;
import me.SgtMjrME.Object.Kit;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.Rally;
import me.SgtMjrME.Object.WarPlayers;
import me.SgtMjrME.Object.state;
import me.SgtMjrME.SiegeUpdate.Cannon;
import me.SgtMjrME.SiegeUpdate.Siege;
import me.SgtMjrME.Tasks.AnnounceBaseStatus;
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
	private World world;
	private static Location lobby;
	private YamlConfiguration config;
	private File configfile;
	private int openDuration;
	private int closeDuration;
	private int warPoints;
	private int warPointMax;
	public Kit kitOnSpawn;
	private int repairBaseVal;
	private mysqlLink mysql;
	public int hitexp;
	public static int killexp;
	public static int basecapexp;
	public int baserepexp;
	public static HashMap<Player, Integer> warPointSave = new HashMap<Player, Integer>();
	private long timedTime;
	public static Player headmodifier;
	public static HashSet<String> leaving = new HashSet<String>();

	public static HashSet<Integer> blockedItems = new HashSet<Integer>();

	public static HashMap<Race, Rally> rallyDat = new HashMap<Race, Rally>();

	public static HashSet<Integer> dropItems = new HashSet<Integer>();

	public static HashMap<String, Integer> repairing = new HashMap<String, Integer>();
	boolean open;

	public void onEnable() {
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
		pm.registerEvents(playerListener, this);
		pm.registerEvents(blockListener, this);
		pm.registerEvents(entityListener, this);

		config = new YamlConfiguration();
		try {
			configfile = new File("plugins/RCWars/config.yml");
			config.load(configfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		new AbilityTimer(config);
		String temp = config.getString("world", null);
		hitexp = config.getInt("exp.hit", 1);
		killexp = config.getInt("exp.kill", 10);
		basecapexp = config.getInt("exp.basecap", 20);
		baserepexp = config.getInt("exp.baserepair", 10);
		mysql = new mysqlLink(config.getString("address", "localhost"),
				config.getString("port", "3306"), config.getString("username",
						"root"), config.getString("password", ""),
				config.getString("dbname", "rcwars"));
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
		warPoints = config.getInt("warpoints", 1);
		warPointMax = config.getInt("warpointmax", 150);

		repairBaseVal = config.getInt("repairBaseVal", 1);
		timedTime = (config.getLong("timedTimer", 1L) * 20L * 60L);
		String[] ditems = config.getString("droppableitems", "").split(";");
		for (String s : ditems)
			try {
				dropItems.add(Integer.parseInt(s));
			} catch (Exception localException) {
			}
		String[] block = config.getString("blockedItems", "").split(",");
		for (String bl : block) {
			blockedItems.add(Integer.parseInt(bl));
		}

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
				File f = new File(temp + name + ".txt");
				try {
					f.createNewFile();
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					for (int j = 0; j < 3; j++) {
						out.write(299 + i * 4 + j + " 1 0");
						out.newLine();
					}
					out.write(swordid + " 1 0");
					out.newLine();
					out.write("261 1 0");
					out.newLine();
					out.write("262 16 0");
					out.close();
				} catch (Exception localException) {
				}
			}
		}
	}

	public static Boolean spendWarPoints(Player p, int cost) {
		if (warPointSave.get(p) != null) {
			if (((Integer) warPointSave.get(p)).intValue() < cost) {
				p.sendMessage(ChatColor.RED + "Not enough War Points");
				return false;
			}
			p.sendMessage(ChatColor.GREEN + "You have been charged " + cost
					+ " warpoints");
			warPointSave.put(p, warPointSave.get(p) - cost);
			returnPlugin().saveWPnoRemove(p);
			return true;
		}

		p.sendMessage(ChatColor.RED + "War data not loaded");
		return false;
	}

	public void giveWarPoints(Player player, int warPoints) {
		if ((warPointSave.containsKey(player))
				&& (((Integer) warPointSave.get(player)).intValue() + warPoints > warPointMax)) {
			player.sendMessage("You have hit the max of " + warPointMax);
			warPointSave.put(player, warPointMax);
			mysql.updatePlayer(player, "wp", warPoints);
			return;
		}
		if (!warPointSave.containsKey(player)) {
			warPointSave.put(player, warPoints);
			mysql.updatePlayer(player, "wp", warPoints);
		} else {
			warPointSave.put(player, warPointSave.get(player) + warPoints);
			mysql.updatePlayer(player, "wp", warPoints);
		}
		player.sendMessage(ChatColor.GREEN + "You have been given " + warPoints
				+ " warpoints");
	}

	public static Integer getWarPoints(Player p) {
		return (Integer) warPointSave.get(p);
	}

	public static void loadWarPoints(Player p) {
		int points = 0;
		try {
			BufferedReader b = new BufferedReader(new FileReader(new File(
					returnPlugin().getDataFolder() + "/WarPoints/"
							+ p.getName() + ".txt")));
			String temp = b.readLine();
			points = Integer.parseInt(temp);
			b.close();
		} catch (FileNotFoundException e) {
			sendLogs("File not found for player " + p.getName());
		} catch (IOException e) {
			sendLogs("Error reading player " + p.getName());
		} catch (Exception e) {
			sendLogs("Other Error with " + p.getName());
		}
		warPointSave.put(p, points);
	}

	public void saveWPnoRemove(Player p) {
		if (warPointSave.containsKey(p)) {
			int points = ((Integer) warPointSave.get(p)).intValue();
			try {
				File f = new File(getDataFolder() + "/WarPoints");
				if (!f.exists())
					f.mkdir();
				BufferedWriter b = new BufferedWriter(
						new FileWriter(new File(getDataFolder() + "/WarPoints/"
								+ p.getName() + ".txt")));
				b.write(points);
				b.close();
			} catch (IOException e) {
				sendLogs("Could not save player");
			}
		}
	}

	public void saveWarPoints(Player p) {
		if (warPointSave.containsKey(p)) {
			saveWPnoRemove(p);
			warPointSave.remove(p);
		}
	}

	protected void playerLeave(String player) {
		Player p = getServer().getPlayer(player);
		if (p == null) {
			return;
		}

		if (WarPlayers.getRace(p) != null) {
			saveWarPoints(p);

			if (shouldDie(p)) {
				p.setHealth(0);
				mysql.updatePlayer(p, "death");
			}

			WarPlayers.remove(p, "Disconnect");
		}
	}

	private boolean shouldDie(Player p) {
		return WarPlayers.gotDamaged(p);
	}

	public static RCWars returnPlugin() {
		return instance;
	}

	public void onDisable() {
		if ((isRunning.equals(state.RUNNING))
				|| (isRunning.equals(state.TOO_FEW_PLAYERS)))
			endGame();
		Kit.kits.clear();
		mysql.close();
	}

	private void loadRaces() {
		File f = new File("plugins/RCWars/Races");
		String[] files = f.list();
		for (String s : files) {
			if (s.endsWith(".yml")) {
				f = new File("plugins/RCWars/Races/" + s);
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
			if (commandLabel.equalsIgnoreCase("startwar")) {
				startGame();
				return true;
			}
			if (commandLabel.equalsIgnoreCase("endwar")) {
				endGame();
				return true;
			}
			if (commandLabel.equalsIgnoreCase("repairwalls")) {
				Siege.repairAll();
				return true;
			}
			if (commandLabel.equalsIgnoreCase("testWarCommand")) {
				mysql.updatePlayer(getServer().getPlayer(args[0]), "kill");
				return true;
			}
			if (commandLabel.equalsIgnoreCase("resetwarstats")) {
				mysql.dropTable();
				mysql.createTable();
				log.info("[RCWars] New table created, stats reset");
				return true;
			}
			if (commandLabel.equalsIgnoreCase("warthanks")) {
				sender.sendMessage("Credits: Richard Johnson (SergeantMajorME)");
				sender.sendMessage("Produced exclusively for RealmCraft!");
				sender.sendMessage(getDescription().getFullName() + " "
						+ getDescription().getDescription());
				return true;
			}
			if (commandLabel.equalsIgnoreCase("wlistbase")) {
				Base.listBases(sender);
				return true;
			}
			if (commandLabel.equalsIgnoreCase("wremoveplayer")) {
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
					sender.sendMessage("Player not found");
					return true;
				}
				return true;
			}
			int x;
			if (commandLabel.equalsIgnoreCase("warptobase")) {
				if (args.length < 2) {
					sender.sendMessage("Not enough args");
					return false;
				}
				Base b;
				if ((b = Base.getBase(args[0])) == null) {
					sender.sendMessage("Base not found with that name");
					return true;
				}
				if (b.getOwner() == null) {
					sender.sendMessage("The base requested has no owner");
					return false;
				}
				if (b.getSpawn() == null) {
					sender.sendMessage("Base has no spawn");
					return false;
				}
				for (x = 1; x < args.length; x++) {
					Player p = getServer().getPlayer(args[x]);
					if (p == null) {
						sender.sendMessage("Player " + args[x] + " not found");
					} else if (b.getOwner().equals(WarPlayers.getRace(p))) {
						p.sendMessage("Teleporting to " + b.getDisp());
						p.teleport(b.getSpawn());
					} else {
						sender.sendMessage("Player " + p.getDisplayName()
								+ " does not own that base");
					}
				}
			} else if (commandLabel.equalsIgnoreCase("wlist")) {
				for (Race r : Race.getAllRaces()) {
					String out = r.getCcolor() + r.getDisplay() + ": ";
					for (String s : r.returnPlayers().keySet()) {
						out = out + s + ", ";
					}
					sender.sendMessage(out);
				}
			} else if (commandLabel.equalsIgnoreCase("specialwaraction")) {
				if (args.length == 0) {
					sender.sendMessage("Current action is");
					sender.sendMessage("View information on classes and ranks");
					return true;
				}

				WarClass.listClasses(sender);
			}
		}

		if (!(sender instanceof Player))
			return false;
		Player p = (Player) sender;

		if ((commandLabel.equalsIgnoreCase("startwar"))
				&& (p.hasPermission("rcwars.admin"))) {
			startGame();
			return true;
		}
		if ((commandLabel.equalsIgnoreCase("endwar"))
				&& (p.hasPermission("rcwars.mod"))) {
			endGame(p);
			return true;
		}
		if ((commandLabel.equalsIgnoreCase("repairwalls"))
				&& (p.hasPermission("rcwars.mod"))) {
			Siege.repairAll();
			return true;
		}
		if (commandLabel.equalsIgnoreCase("reloadwar")) {
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
		} else {
			if (commandLabel.equalsIgnoreCase("warthanks")) {
				p.sendMessage("Credits: " + ChatColor.GOLD
						+ "Richard Johnson (SergeantMajorME)");
				p.sendMessage("Produced exclusively for " + ChatColor.GOLD
						+ "RealmCraft!");
				p.sendMessage(getDescription().getFullName() + " "
						+ getDescription().getDescription());
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
			}
			if (commandLabel.equalsIgnoreCase("wlist")) {
				for (Race r : Race.getAllRaces()) {
					String out = r.getCcolor() + r.getDisplay() + ": ";
					for (String s : r.returnPlayers().keySet()) {
						out = out + s + ", ";
					}
					sender.sendMessage(out);
				}
			} else {
				if (commandLabel.equalsIgnoreCase("wp")) {
					if (warPointSave.containsKey(p)) {
						p.sendMessage("You have " + warPointSave.get(p)
								+ " warpoints");
					} else {
						p.sendMessage("Your war data is not loaded");
					}
					return true;
				}
				if ((commandLabel.equalsIgnoreCase("listkits"))
						&& (p.hasPermission("rcwars.mod"))) {
					Kit.listKits(p);
				} else {
					if (commandLabel.equalsIgnoreCase("rally")) {
						Race r = WarPlayers.getRace(p);
						if (r == null)
							return true;
						if (rallyDat.containsKey(r)) {
							if ((System.currentTimeMillis() - ((Rally) rallyDat
									.get(WarPlayers.getRace(p))).time) / 1000L < 30L) {
								p.teleport(((Rally) rallyDat.get(r)).p
										.getLocation());
								p.sendMessage("RALLY!");
							}

						}

						return true;
					}
					if ((commandLabel.equalsIgnoreCase("warstats"))
							&& (p.hasPermission("rcwars.stats"))) {
						int[] out = mysql.getStats(p.getName());
						if (out == null) {
							p.sendMessage(ChatColor.RED
									+ "Data not found in database");
							return true;
						}
						p.sendMessage(ChatColor.GOLD
								+ "War Stats for this month");
						p.sendMessage(ChatColor.GREEN + "Kills: " + out[0]
								+ "  Deaths: " + out[1]);
						if (out[1] != 0) {
							p.sendMessage(ChatColor.GREEN + "K/D: " + out[0]
									/ out[1]);
						}
						p.sendMessage(ChatColor.GREEN + "WarPoints: " + out[2]);
						return true;
					}
					if ((commandLabel.equalsIgnoreCase("wsetworld"))
							&& (p.hasPermission("rcwars.admin"))) {
						world = p.getWorld();
						this.config.set("world", world.getName());
						try {
							this.config.save(configfile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
					}
					if ((commandLabel.equalsIgnoreCase("wsetlobby"))
							&& (p.hasPermission("rcwars.admin"))) {
						lobby = p.getLocation();
						this.config.set("spawn", loc2str(p.getLocation()));
						try {
							this.config.save(configfile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
					}
					if ((commandLabel.equalsIgnoreCase("setRaceSpawn"))
							&& (p.hasPermission("rcwars.admin"))) {
						if (args.length < 1)
							return false;
						Race r = Race.raceByName(args[0]);
						if (r == null)
							return false;
						p.sendMessage(r.getDisplay() + " default spawn set");
						r.setSpawn(p.getLocation());
					} else if ((commandLabel.equalsIgnoreCase("class"))
							&& (p.hasPermission("rcwars.class"))) {
						if (args.length != 1) {
							p.sendMessage("No class specified");
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
							p.sendMessage("Class does not exist");
							return true;
						}
						if (w.enterClass(p)) {
							return true;
						}

					} else {
						if ((commandLabel.equalsIgnoreCase("setheads"))
								&& (p.hasPermission("rcwars.admin"))) {
							if (headmodifier == null) {
								headmodifier = p;
							} else if (headmodifier.getName().equals(
									p.getName())) {
								headmodifier = null;
							} else {
								headmodifier
										.sendMessage("Someone else has registered to set heads");
								headmodifier = p;
							}
							p.sendMessage("Headmod status: "
									+ (headmodifier != null ? headmodifier
											.getName().equals(p.getName())
											: "false"));
							return true;
						}
						if ((commandLabel.equalsIgnoreCase("race"))
								&& (p.hasPermission("rcwars.setrace"))) {
							if (args.length < 1)
								return true;
							if (WarPlayers.getRace(p) != null) {
								p.sendMessage(ChatColor.RED
										+ "You're already in wars!");
								return true;
							}

							if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
								p.sendMessage(ChatColor.RED
										+ "Invisibility not allowed in wars!");
								return true;
							}
							if (playerListener.modifyInv.containsValue(p
									.getName())) {
								p.sendMessage("Cannot join wars: Inventory being investigated");
								return true;
							}
							Race r;
							if ((r = Race.raceByName(args[0])) == null)
								return true;
							if ((r.isRef())
									&& (!p.hasPermission("rcwars.referee"))) {
								p.sendMessage(ChatColor.RED
										+ "Not allowed to join ref");
								return true;
							}
							Race check = Race.checkRaceOpen(r);
							if (check == null)
								return true;
							if (check.equals(r)) {
								if (r.getSpawn() == null) {
									p.sendMessage("Spawn for race "
											+ r.getDisplay()
											+ " has not been set");
									return true;
								}
								p.closeInventory();
								WarPlayers.setRace(p, r);
								p.sendMessage(ChatColor.GREEN + "Set race to "
										+ r.getDisplay());
								return true;
							}

							p.sendMessage(ChatColor.RED
									+ "Races are unbalanced! "
									+ check.getDisplay() + ChatColor.RED
									+ " has too few people!");
							return true;
						}

						if (commandLabel.equalsIgnoreCase("leavewar")) {
							if (WarPlayers.getRace(p) == null)
								return true;
							final Location prevLoc = p.getLocation();
							final Player hold = p;
							p.sendMessage(ChatColor.GREEN + "Do not move...");
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
													hold.sendMessage(ChatColor.RED
															+ "You moved/attacked, leavewar aborted");
												RCWars.leaving.remove(hold
														.getName());
											}
										}, 100L);
							}
							return true;
						}
						if ((commandLabel.equalsIgnoreCase("waddbase"))
								&& (p.hasPermission("rcwars.admin"))) {
							if (args.length < 1) {
								p.sendMessage("Need an internal name");
								return false;
							}
							YamlConfiguration config = new YamlConfiguration();
							if (Base.getBase(args[0]) != null) {
								p.sendMessage("Base with this name already exists");
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
							p.sendMessage("New base "
									+ Base.getBase(args[0]).getBaseName()
									+ " created");
							return true;
						}
						if ((commandLabel.equalsIgnoreCase("newcannon"))
								&& (p.hasPermission("rcwars.admin"))) {
							if (args.length < 1) {
								p.sendMessage("Need an internal name");
								return false;
							}
							YamlConfiguration config = new YamlConfiguration();
							if (Cannon.getCannon(args[0]) != null) {
								p.sendMessage("Base with this name already exists");
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
							p.sendMessage("New cannon created");
							return true;
						}
						if ((commandLabel.equalsIgnoreCase("wremoveBase"))
								&& (p.hasPermission("rcwars.admin"))) {
							if (args.length < 1) {
								p.sendMessage("Need a base name!");
								return false;
							}
							Base.removeBase(args[0]);
						} else {
							if ((commandLabel.equalsIgnoreCase("wlistbase"))
									&& (p.hasPermission("rcwars.seebase"))) {
								Base.listBases(p);
								return true;
							}
							if ((commandLabel.equalsIgnoreCase("warptobase"))
									&& (p.hasPermission("rcwars.warp"))) {
								if (p.getLocation().getWorld() != world) {
									p.sendMessage(ChatColor.RED + "Not in game");
									return true;
								}
								if (args.length < 1) {
									p.sendMessage(ChatColor.RED
											+ "Incorrect arguements");
									return true;
								}
								final Base b;
								if ((b = Base.getBase(args[0])) == null) {
									p.sendMessage(ChatColor.RED
											+ "Base not found with that name");
									return true;
								}
								if ((b.getOwner() == null)
										|| (!b.getOwner().equals(
												WarPlayers.getRace(p)))) {
									p.sendMessage(ChatColor.RED
											+ "You don't own this base");
									return true;
								}
								if (b.getSpawn() == null)
									return true;
								final Location prevLoc = p.getLocation();
								final Player hold = p;
								p.sendMessage(ChatColor.GREEN
										+ "Do not move...");
								leaving.add(p.getName());
								if (WarPlayers.getRace(p) != null)
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
																hold.sendMessage("Teleporting to "
																		+ b.getDisp());
																hold.teleport(b
																		.getSpawn());
															} else {
																hold.sendMessage(ChatColor.RED
																		+ "You moved/attacked, warp aborted");
															}
															RCWars.leaving
																	.remove(hold
																			.getName());
														}
													}, 100L);
							} else {
								if ((commandLabel.equalsIgnoreCase("warchest"))
										&& (p.hasPermission("rcwars.mod"))) {
									if (args.length < 1) {
										p.sendMessage("No player given");
										return false;
									}
									if (p.getName().equalsIgnoreCase(args[0])) {
										p.sendMessage("Don't try to access that ;)");
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
										p.sendMessage("Player has no bank");
										return false;
									}
									playerListener.openBankOther(p, args[0]);
									return true;
								}
								if ((commandLabel.equalsIgnoreCase("warinv"))
										&& (p.hasPermission("rcwars.mod"))) {
									if (args.length < 1) {
										p.sendMessage("No player given");
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
										p.sendMessage(args[0] + " not found");
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
										p.sendMessage("Error loading");
										return false;
									}
								} else if ((commandLabel
										.equalsIgnoreCase("specialwaraction"))
										&& ((p.getName()
												.equalsIgnoreCase("sergeantmajorme")) || (p
												.hasPermission("rcwars.specialaction")))) {
									if (args.length == 0) {
										p.sendMessage("Current action is");
										p.sendMessage("View information on classes and ranks");
										return true;
									}

									WarClass.listClasses(p);
								} else if ((commandLabel
										.equalsIgnoreCase("setracespawnzone"))
										&& (p.hasPermission("rcwars.admin"))) {
									if (args.length < 1) {
										p.sendMessage("Not enough args");
										return false;
									}
									Race r = Race.raceByName(args[0]);
									if (r == null) {
										p.sendMessage("Race not found");
										return false;
									}
									r.setSpawnZone(p);
								} else if ((commandLabel
										.equalsIgnoreCase("setcannon"))
										&& (p.hasPermission("rcwars.admin"))) {
									if (args.length < 2) {
										p.sendMessage("Not enough args");
										return false;
									}
									Cannon c = Cannon.getCannon(args[0]);
									if (c == null) {
										p.sendMessage("Cannon not specified");
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
								} else {
									if ((commandLabel
											.equalsIgnoreCase("wsetbase"))
											&& (p.hasPermission("rcwars.admin"))) {
										if (args.length < 2) {
											p.sendMessage("Not enough args");
											return false;
										}
										Base b = Base.getBase(args[0]);
										if (b == null) {
											p.sendMessage("Base not found");
											return false;
										}
										if (args[1].equalsIgnoreCase("spawn")) {
											b.setSpawn(p.getLocation());
											p.sendMessage("Spawn for "
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
												p.sendMessage("Stopped editing walls");
											} else {
												Siege.startEditing(p,
														Siege.getSiege(b));
												p.sendMessage("Now editing walls, repeat to stop");
											}
											return true;
										}
										if (args.length < 3)
											return false;
										if (args[1].equalsIgnoreCase("owner")) {
											b.setOwnerSave(Race
													.raceByName(args[2]));
											if (Race.raceByName(args[2]) == null)
												p.sendMessage("Owner of "
														+ b.getDisp()
														+ " set to NULL");
											else
												p.sendMessage("Owner of "
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
											p.sendMessage("Base "
													+ b.getBaseName()
													+ "'s display name set to "
													+ b.getDisp());
											return true;
										}
										if (args[1].equalsIgnoreCase("health")) {
											try {
												b.setHealth(Integer
														.parseInt(args[2]));
												p.sendMessage("Health for "
														+ b.getDisp()
														+ " has been set to "
														+ b.getHealth());
											} catch (Exception e) {
												p.sendMessage("Need an integer for health!");
											}
											return true;
										}

										if (args[1].equalsIgnoreCase("exp")) {
											try {
												b.setExp(Integer
														.parseInt(args[2]));
											} catch (Exception e) {
												p.sendMessage("third arguement must be an int!");
												return false;
											}
											return true;
										}
										if (args[1].equalsIgnoreCase("weight")) {
											try {
												b.setWeight(Double
														.parseDouble(args[2]));
											} catch (Exception e) {
												p.sendMessage("third arguement must be a #!");
												return false;
											}
											return true;
										}
										return false;
									}
									if ((commandLabel
											.equalsIgnoreCase("wremoveplayer"))
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
											p.sendMessage("Player not found");
											return true;
										}
										return true;
									}
								}
							}
						}
					}
				}
			}
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

	public void sendLog(String string) {
		sendLogs(string);
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
						p.sendMessage(ChatColor.GREEN + "You are repairing "
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
							p.sendMessage(ChatColor.GREEN + "Given "
									+ baserepexp + " for repairing the base");
						}
					} else if (((String) toDisplay.get(p))
							.equals("repairother")) {
						p.sendMessage(ChatColor.GREEN + "You are neutralizing "
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
						p.sendMessage(ChatColor.YELLOW
								+ "Someone is blocking the capture");
					} else if (check == "repairnone") {
						siege.repair(repairBaseVal);
					} else if (check == "attack") {
						p.sendMessage(ChatColor.YELLOW
								+ "Someone is stopping you from capturing");
					} else if (check == "repairowner") {
						if (b.getAttacker() != null)
							p.sendMessage(ChatColor.YELLOW
									+ "You are blocking someone from capturing");
					} else if (check == "repairother") {
						p.sendMessage(ChatColor.YELLOW
								+ "Someone is blocking the capture");
					}
				}
			} else if (didDamage == 1) {
				Iterator<Player> i = toDisplay.keySet().iterator();
				while (i.hasNext()) {
					Player p = (Player) i.next();
					if (((String) toDisplay.get(p)).equals("attacknew")) {
						p.sendMessage(ChatColor.GREEN
								+ "You are now attacking " + b.getDisp()
								+ ChatColor.GREEN + ": " + b.getDamage() + "/"
								+ b.getHealth());
					} else
						p.sendMessage(ChatColor.GREEN + "You are capturing "
								+ b.getDisp() + ChatColor.GREEN + ": "
								+ b.getDamage() + "/" + b.getHealth());
				}
			} else if (didDamage == 2) {
				Iterator<Player> i = toDisplay.keySet().iterator();
				while (i.hasNext()) {
					Player p = (Player) i.next();
					p.sendMessage(b.getOwner().getCcolor() + "Base captured");
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
							p.sendMessage("Don't enter other Race's spawns");
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

	public static void sendLogs(String currentPath) {
		log.info(currentPath);
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
				player.sendMessage(s);
			}
		}
		returnPlugin().mysql.updatePlayer(damageep, "death");
		if (damagerp != null)
			returnPlugin().mysql.updatePlayer(damagerp, "kill");
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
				player.sendMessage(s);
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
				player.sendMessage(s);
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
				p.sendMessage(ChatColor.YELLOW
						+ "Game paused due to too few players");
				p.teleport(WarPlayers.getRace(p).getSpawn());
			}
		}
	}

	public state isRunning() {
		return isRunning;
	}

	public void endGame(Player p) {
		sendLogs("Wars has been shut down");
		WarPlayers.removeAll(lobby);
		isRunning = state.STOPPED;
		cancelMyTasks();
		Iterator<Base> bases = Base.returnBases().iterator();
		while (bases.hasNext()) {
			((Base) bases.next()).resetBase();
		}
		p.sendMessage(ChatColor.RED + "Wars has been shut down");
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
			p.sendMessage(ChatColor.GREEN + "The War has already begun!");
		} else if (isRunning.equals(state.STOPPED)) {
			p.sendMessage(ChatColor.YELLOW + "The War is at a cease-fire");
		} else if (isRunning.equals(state.TOO_FEW_PLAYERS)) {
			p.sendMessage(ChatColor.YELLOW
					+ "The War cannot start until more players come!");
			p.sendMessage(ChatColor.YELLOW + "Current total is "
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

						player.sendMessage(ChatColor.GREEN
								+ "You have been given " + warPoints * count
								+ " warpoints for playing");
						giveWarPoints(player, (int) (warPoints * count));
					}
				}
			}
		}
	}

	public void timeGiveAll() {
		giveAll();
	}

	public void sendToMySQL(Player p, String d, int place) {
		if (place == 0)
			mysql.updatePlayer(p, d);
	}
}