package me.SgtMjrME.SiegeUpdate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import me.SgtMjrME.RCWars;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class Cannon {
	static RCWars rcwars;
	boolean occupied;
	String name;
	Location fire;
	Player player;
	public double velocity;
	public Location launch;
	public static HashMap<Cannon, Long> times = new HashMap<Cannon, Long>();
	static final int kickTime = 60;
	private int cannonCost;
	public static HashMap<Cannon, Location> cannons = new HashMap<Cannon, Location>();

	public static HashMap<Location, Cannon> btn2can = new HashMap<Location, Cannon>();
	public static HashMap<Player, Cannon> setItem = new HashMap<Player, Cannon>();
	public static HashMap<Player, Cannon> PCannon = new HashMap<Player, Cannon>();
	private HashMap<String, Long> delay = new HashMap<String, Long>();

	public Cannon(String n) {
		name = n;
		occupied = false;
		fire = null;
		player = null;
		velocity = 4.0D;
		launch = null;
		cannonCost = 25;
		cannons.put(this, null);
	}

	public Cannon(Location l, Location launch, String n, double v, int cost,
			Location button) {
		occupied = false;
		name = n;
		player = null;
		fire = null;
		this.launch = launch;
		velocity = v;
		cannonCost = cost;
		cannons.put(this, l);
		btn2can.put(button, this);
	}

	public static Cannon getCannon(String s) {
		for (Cannon c : cannons.keySet()) {
			if (c.name.equals(s))
				return c;
		}
		return null;
	}

	public static Cannon getCannon(Player p) {
		return (Cannon) PCannon.get(p);
	}

	public static void enterCannon(Player p, String s) {
		Cannon c = getCannon(s);
		if (c == null) {
			p.sendMessage("Cannon not found");
			return;
		}
		if (c.occupied) {
			p.sendMessage("Cannon already occupied");
			if ((times.get(c) != null)
					&& ((System.currentTimeMillis() - ((Long) times.get(c))
							.longValue()) / 1000L < 60L)) {
				return;
			}
			leaveCannon(c);
			p.sendMessage("Person is overtime, entering cannon");
			enterCannon(p, s);
			return;
		}
		if (cannons.get(c) == null) {
			p.sendMessage("Cannon has no seat");
			return;
		}

		PCannon.put(p, c);

		c.player = p;
		c.occupied = true;
		c.player.sendMessage(ChatColor.GREEN + "You have entered the cannon");
		times.put(c, Long.valueOf(System.currentTimeMillis()));
	}

	public static void leaveCannon(Cannon c) {
		c.occupied = false;
		PCannon.remove(c.player);
		c.delay.remove(c.player.getName());
		c.player = null;
	}

	public void fireTNT() {
		Player p = player;
		if ((delay.containsKey(p.getName()))
				&& ((System.currentTimeMillis() - ((Long) delay
						.get(p.getName())).longValue()) / 1000L < 3L)) {
			return;
		}
		delay.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
		Cannon c = getCannon(p);
		if (c == null)
			return;
		if (!c.occupied)
			return;
		if (c.launch == null)
			return;
		if (c.player.equals(p))
			launchTNT(p);
	}

	private void launchTNT(Player p) {
		if (!RCWars.spendWarPoints(p, cannonCost).booleanValue()) {
			p.sendMessage(ChatColor.RED
					+ "Not enough War Points, removing from cannon");
			leaveCannon(getCannon(p));
			return;
		}
		Vector v = p.getLocation().getDirection().clone();
		TNTPrimed e = (TNTPrimed) RCWars.returnPlugin().getWarWorld()
				.spawnEntity(launch, EntityType.PRIMED_TNT);
		e.setYield(2.0F);
		e.setVelocity(v.multiply(velocity));
		e.setFuseTicks(e.getFuseTicks() / 2);
		e.setMetadata("shooter", new TntMeta(p));
	}

	public static void loadCannons(RCWars rcWars) {
		rcwars = rcWars;
		File f = new File(rcWars.getDataFolder() + "/Cannons");
		if (!f.exists()) {
			RCWars.sendLogs("Cannon folder not found");
			f.mkdir();
		}
		String[] files = f.list();
		World w = RCWars.returnPlugin().getWarWorld();
		for (String s : files) {
			if (s.endsWith(".yml")) {
				f = new File(rcWars.getDataFolder() + "/Cannons/" + s);
				YamlConfiguration temp = new YamlConfiguration();
				try {
					temp.load(f);
				} catch (Exception e) {
					continue;
				}

				int x = temp.getInt("x", 0);
				int y = temp.getInt("y", 0);
				int z = temp.getInt("z", 0);
				float yaw = (float) temp.getDouble("yaw", 0.0D);
				float pitch = (float) temp.getDouble("pitch", 0.0D);
				int lx = temp.getInt("lx", 0);
				int ly = temp.getInt("ly", 0);
				int lz = temp.getInt("lz", 0);
				int bx = temp.getInt("bx", 0);
				int by = temp.getInt("by", 0);
				int bz = temp.getInt("bz", 0);
				String name = temp.getString("name", "");
				int cost = temp.getInt("cost", 25);
				new Cannon(new Location(w, x, y, z, yaw, pitch), new Location(
						w, lx, ly, lz), name, 4.0D, cost, new Location(w, bx,
						by, bz));
			}
		}
	}

	public void setPlatform(Location location) {
		cannons.put(this, location);
		YamlConfiguration config = new YamlConfiguration();
		File f = new File(rcwars.getDataFolder() + "/Cannons/" + name + ".yml");
		if (!f.exists())
			return;
		try {
			config.load(f);
			config.set("x", Double.valueOf(location.getX()));
			config.set("y", Double.valueOf(location.getY()));
			config.set("z", Double.valueOf(location.getZ()));
			config.set("yaw", Float.valueOf(location.getYaw()));
			config.set("pitch", Float.valueOf(location.getPitch()));
			config.save(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void setLaunch(Location location) {
		launch = location;
		YamlConfiguration config = new YamlConfiguration();
		File f = new File(rcwars.getDataFolder() + "/Cannons/" + name + ".yml");
		if (!f.exists())
			return;
		try {
			config.load(f);
			config.set("lx", Integer.valueOf(location.getBlockX()));
			config.set("ly", Integer.valueOf(location.getBlockY()));
			config.set("lz", Integer.valueOf(location.getBlockZ()));
			config.save(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void setButton(Player p, Location location) {
		btn2can.put(location, this);
		YamlConfiguration config = new YamlConfiguration();
		File f = new File(rcwars.getDataFolder() + "/Cannons/" + name + ".yml");
		if (!f.exists())
			return;
		try {
			config.load(f);
			config.set("bx", Integer.valueOf(location.getBlockX()));
			config.set("by", Integer.valueOf(location.getBlockY()));
			config.set("bz", Integer.valueOf(location.getBlockZ()));
			config.save(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		setItem.remove(p);
	}

	public void setCost(String string) {
		int cost;
		try {
			cost = Integer.parseInt(string);
		} catch (Exception e) {
			RCWars.sendLogs("Error, not an int");
			return;
		}
		cannonCost = cost;
		YamlConfiguration config = new YamlConfiguration();
		File f = new File(rcwars.getDataFolder() + "/Cannons/" + name + ".yml");
		if (!f.exists())
			return;
		try {
			config.load(f);
			config.set("cost", Integer.valueOf(cost));
			config.save(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static Cannon isCannonButton(Location l) {
		return (Cannon) btn2can.get(l.getBlock().getLocation());
	}

	public void setPlayerButton(Player p) {
		if (setItem.containsKey(p)) {
			p.sendMessage("Not setting button");
			setItem.remove(p);
			return;
		}
		setItem.put(p, this);
		p.sendMessage("Setting button for " + getName());
	}

	public String getName() {
		return name;
	}

	public static void distributeAction(Player p, String s,
			PlayerInteractEvent e) {
		Cannon c = (Cannon) setItem.get(p);
		if (s == "button") {
			c.setButton(e.getPlayer(), e.getClickedBlock().getLocation());
			return;
		}
	}
}