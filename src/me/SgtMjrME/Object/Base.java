package me.SgtMjrME.Object;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.SiegeUpdate.Siege;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class Base {
	private final RCWars pl;
	private Location spawn;
	private Location protectll;
	private Location protectur;
	private Race owner;
	private int damage;
	private int health;
	private double weight;
	private Race attacker;
	private int attackerNum;
	private int otherNum;
	private final YamlConfiguration config;
	private final String basename;
	private String disp;
	private boolean gateOpen;
	private int exp;
	private boolean display;
	private ArrayList<Location> flags = new ArrayList<Location>();
	private ArrayList<Location> gates = new ArrayList<Location>();

	private static ConcurrentHashMap<String, Base> bases = new ConcurrentHashMap<String, Base>();
	private static ConcurrentHashMap<Player, Base> setItemBase = new ConcurrentHashMap<Player, Base>();

	private static ConcurrentHashMap<Player, String> setitem = new ConcurrentHashMap<Player, String>();

	public static Base getBase(String s) {
		return (Base) bases.get(s.toLowerCase());
	}

	public static Collection<Base> returnBases() {
		return bases.values();
	}

	public boolean zonesAreSet() {
		return (protectll != null) && (protectur != null);
	}

	public void resetBase() {
		try {
			config.load("plugins/RCWars/Bases/" + basename + ".yml");
			reloadBase(config);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static void listBases(CommandSender p) {
		Iterator<String> i = bases.keySet().iterator();
		while (i.hasNext())
			try {
				String baseName = i.next();
				Base b = getBase(baseName);
				p.sendMessage("Internal base name: " + baseName);
				if (b != null) {
					p.sendMessage("Display name: " + b.getDisp());
					p.sendMessage("Damage: " + b.getDamage());
					p.sendMessage("Health: " + b.getHealth());

					p.sendMessage("Exp: " + b.getExp());
					if (b.getOwner() != null)
						p.sendMessage("Owner: " + b.getOwner().getDisplay());
					else
						p.sendMessage("Owner: None");
					if (b.zonesAreSet())
						p.sendMessage("Capture zones are set");
				}
			} catch (Exception e) {
				p.sendMessage(ChatColor.DARK_RED
						+ "Error displaying base, moving to next one");
				RCWars.sendLogs("[RCWars] Error displaying bases");
			}
	}

	public Base(RCWars plugin, YamlConfiguration config) {
		pl = plugin;
		this.config = config;
		basename = config.getString("name").toLowerCase();
		reloadBase(config);
		bases.put(basename, this);
		new Siege(config, this);
	}

	private void reloadBase(YamlConfiguration config) {
		health = config.getInt("health", 100);
		setWeight(config.getDouble("weight", 1.0D));
		disp = config.getString("displayName", basename);
		display = config.getBoolean("display",false);
		try {
			spawn = pl.str2Loc(config.getString("spawn"));
		} catch (Exception e) {
			spawn = null;
		}
		try {
			protectll = pl.str2Loc(config.getString("protectllc"));
			protectur = pl.str2Loc(config.getString("protecturf"));
		} catch (Exception e) {
			protectll = null;
			protectur = null;
		}

		String s = config.getString("flagloc");
		if (s != null) {
			String[] blocks = s.split(",");
			for (String l : blocks)
				try {
					flags.add(str2Loc(l));
				} catch (Exception localException1) {
				}

		}
		String s2 = config.getString("gatesloc");
		if (s2 != null) {
			String[] blocks = s2.split(",");
			for (String l : blocks)
				try {
					gates.add(str2Loc(l));
				} catch (Exception localException2) {
				}

		}
		attacker = null;
		damage = 0;
		attackerNum = 0;
		otherNum = 0;
		String ownerString = config.getString("owner");
		if (ownerString != null)
			owner = Race.raceByName(ownerString);
		else {
			owner = null;
		}
		setExp(config.getInt("exp", 5));
		changeFlagColor();
		resetGate();
		RCWars.sendLogs("Base " + basename + " set up");
	}

	public void resetGate() {
		gateOpen = false;
		Iterator<Location> g = gates.iterator();
		while (g.hasNext())
			g.next().getBlock().setTypeId(101);
	}

	public static void loadBases(RCWars pl) {
		File f = new File("plugins/RCWars/Bases");
		String[] files = f.list();
		for (String s : files) {
			if (s.endsWith(".yml")) {
				f = new File("plugins/RCWars/Bases/" + s);
				YamlConfiguration temp = new YamlConfiguration();
				try {
					temp.load(f);
				} catch (Exception e) {
					continue;
				}
				bases.put(temp.getString("name"), new Base(pl, temp));
			}
		}
	}

	public RCWars getPl() {
		return pl;
	}

	public void setSpawn(Location l) {
		spawn = l;
		save("spawn", RCWars.loc2str(l));
	}

	public Location getSpawn() {
		return spawn;
	}

	public Race getOwner() {
		return owner;
	}

	public void setOwner(Race owner) {
		this.owner = owner;
	}

	public void setOwnerSave(Race owner) {
		setOwner(owner);
		if (owner == null)
			save("owner", null);
		else
			save("owner", owner.getName());
	}

	private void save(String path, String item) {
		config.set(path, item);
		try {
			config.save(pl.getDataFolder() + "/Bases/" + basename + ".yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveDouble(String path, double val) {
		config.set(path, Double.valueOf(val));
		try {
			config.save(pl.getDataFolder() + "/Bases/" + basename + ".yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveInt(String path, int val) {
		config.set(path, Integer.valueOf(val));
		try {
			config.save(pl.getDataFolder() + "/Bases/" + basename + ".yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getDamage() {
		return damage;
	}

	public int addDamage(int damage) {
		if (this.damage + damage != health) {
			String s = "";
			if ((damage > 0) && ((this.damage + damage) % 10 == 1)
					&& (attacker != null)) {
				s = getDisp() + " is being attacked by the "
						+ attacker.getDisplay() + "'s! " + this.damage + "/"
						+ health;
			} else if ((damage < 0) && ((this.damage + damage) % 10 == 0)) {
				s = disp + " is being repaired! " + this.damage + "/" + health;
			}
			if (s != "") {
				Iterator<String> players = WarPlayers.listPlayers();
				while (players.hasNext()) {
					String temp = (String) players.next();
					Player p = pl.getServer().getPlayer(temp);
					if (p == null) {
						WarPlayers.remove(temp);
						players = WarPlayers.listPlayers();
					} else {
						p.sendMessage(s);
					}
				}
			}
		}
		this.damage += damage;
		if (this.damage >= health) {
			capBase(owner, attacker);
			owner = attacker;
			changeFlagColor();
			resetDamage();
			return 2;
		}

		if (damage > 0)
			return 1;
		return -1;
	}

	private void changeFlagColor() {
		if (flags.isEmpty())
			return;
		Iterator<Location> i = flags.iterator();
		Byte tocolor = Byte.valueOf((byte) 0);
		if (owner != null)
			tocolor = owner.getColor();
		while (i.hasNext())
			i.next().getBlock().setData(tocolor.byteValue());
	}

	public void resetDamage() {
		damage = 0;
		setAttacker(null);
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int h) {
		health = h;
		saveInt("health", h);
	}

	public Race getAttacker() {
		return attacker;
	}

	public void setAttacker(Race attacker) {
		this.attacker = attacker;
	}

	public String getBaseName() {
		return basename;
	}

	public int parseDamage() {
		int out = 0;
		if (attackerNum == 0) {
			if ((otherNum > 0) && (damage > 0)) {
				out = addDamage(-otherNum);
			}
		} else if ((attackerNum > 0) && (otherNum == 0)) {
			out = addDamage(attackerNum);
		}
		if (damage <= 0)
			resetDamage();
		attackerNum = 0;
		otherNum = 0;
		return out;
	}

	public int setProtect(Location protect, Player p) {
		if (protectll == null) {
			protectll = protect;
			return 1;
		}
		if (protectur == null) {
			protectur = protect;
			double xll = Math.min(protectll.getX(), protectur.getX());
			double xur = Math.max(protectll.getX(), protectur.getX());
			double yll = Math.min(protectll.getY(), protectur.getY());
			double yur = Math.max(protectll.getY(), protectur.getY());
			double zll = Math.min(protectll.getZ(), protectur.getZ());
			double zur = Math.max(protectll.getZ(), protectur.getZ());
			protectll = new Location(protectll.getWorld(), xll, yll, zll);
			protectur = new Location(protectll.getWorld(), xur, yur, zur);
			save("protectllc", RCWars.loc2str(protectll));
			save("protecturf", RCWars.loc2str(protectur));
			setItemBase.remove(p);
			setitem.remove(p);
			return 2;
		}

		protectll = null;
		protectur = null;
		return 0;
	}

	public String getDisp() {
		if (owner == null)
			return disp;
		return owner.getCcolor() + disp;
	}

	public void setDisp(String s) {
		disp = s;
		save("displayName", s);
	}

	public boolean inBase(Player p) {
		Location player = p.getLocation();
		return checkLocation(player);
	}

	private boolean checkLocation(Location p) {
		if ((protectll == null) || (protectur == null))
			return false;
		if ((protectll.getX() > p.getX()) || (protectll.getZ() > p.getZ())
				|| (protectll.getY() > p.getY()))
			return false;
		if ((protectur.getX() < p.getX()) || (protectur.getZ() < p.getZ())
				|| (protectur.getY() < p.getY())) {
			return false;
		}
		return true;
	}

	public void addAttacker() {
		attackerNum += 1;
	}

	public void addOther() {
		otherNum += 1;
	}

	public static void removeBase(String string) {
		bases.remove(string);
		String f = RCWars.returnPlugin().getDataFolder() + "/Bases/" + string
				+ ".yml";
		File file = new File(f);
		if (file.exists())
			file.delete();
	}

	public Location str2Loc(String s) {
		String[] s1 = s.split(" ");
		Location loc = new Location(Bukkit.getServer().getWorld(s1[0]),
				str2d(s1[1]), str2d(s1[2]), str2d(s1[3]), (float) str2d(s1[4]),
				(float) str2d(s1[5]));
		return loc;
	}

	public double str2d(String s) {
		return Double.parseDouble(s);
	}

	public void setFlag(Player p) {
		if (setItemBase.containsKey(p)) {
			setItemBase.remove(p);
			setitem.remove(p);
			p.sendMessage(ChatColor.DARK_GREEN + "No longer setting flags");
			return;
		}
		if (setItemBase.containsValue(this)) {
			p.sendMessage(ChatColor.DARK_RED
					+ "Someone is already editing flags");
			return;
		}
		setItemBase.put(p, this);
		setitem.put(p, "flags");
		p.sendMessage(ChatColor.DARK_GREEN + "To end, re-type the command");
	}

	public void setZone(Player p) {
		if (setItemBase.containsKey(p)) {
			setItemBase.remove(p);
			setitem.remove(p);
			p.sendMessage("No longer setting zones");
			return;
		}
		if (setItemBase.containsValue(this)) {
			p.sendMessage(ChatColor.DARK_RED
					+ "Someone is already editing this zone");
			return;
		}
		setItemBase.put(p, this);
		setitem.put(p, "zones");
		p.sendMessage(ChatColor.DARK_GREEN + "Punch 2 corners of the zone");
	}

	public void setSpawnZone(Player p) {
		if (setItemBase.containsKey(p)) {
			setItemBase.remove(p);
			setitem.remove(p);
			p.sendMessage("No longer setting zones");
			return;
		}
		if (setItemBase.containsValue(this)) {
			p.sendMessage(ChatColor.DARK_RED
					+ "Someone is already editing this zone");
			return;
		}
		setItemBase.put(p, this);
		setitem.put(p, "spawn");
		p.sendMessage(ChatColor.DARK_GREEN + "Punch 2 corners of the zone");
	}

	public void setGate(Player p) {
		if (setItemBase.containsKey(p)) {
			setItemBase.remove(p);
			setitem.remove(p);
			p.sendMessage(ChatColor.DARK_GREEN + "No longer setting gates");
			return;
		}
		if (setItemBase.containsValue(this)) {
			p.sendMessage(ChatColor.DARK_RED
					+ "Someone is already editing gates");
			return;
		}
		setItemBase.put(p, this);
		setitem.put(p, "gates");
		p.sendMessage(ChatColor.DARK_GREEN + "To end, re-type the command");
	}

	public void addFlag(Block clickedBlock) {
		flags.add(clickedBlock.getLocation());
		String s;
		try {
			s = config.getString("flagloc");
		} catch (Exception e) {
			s = "";
		}
		s = s + "," + RCWars.loc2str(clickedBlock.getLocation());
		save("flagloc", s);
	}

	public static String isOperating(Player p) {
		return (String) setitem.get(p);
	}

	public static void distributeAction(Player p, String s,
			PlayerInteractEvent e) {
		Base b = (Base) setItemBase.get(p);
		if (s == "flags") {
			b.addFlag(e.getClickedBlock());
			p.sendMessage("Flag added");
		} else if (s == "zones") {
			int val = b.setProtect(e.getClickedBlock().getLocation(), p);
			if (val == 1)
				e.getPlayer().sendMessage("First marker hit");
			else if (val == 2)
				e.getPlayer().sendMessage("Second marker hit");
			else if (val == 3)
				e.getPlayer().sendMessage("Zone removed");
		} else if (s == "gates") {
			b.addGate(e.getClickedBlock());
			p.sendMessage("Gate added");
		}
	}

	private void addGate(Block clickedBlock) {
		if (gates.contains(clickedBlock.getLocation()))
			return;
		gates.add(clickedBlock.getLocation());
		String s;
		try {
			s = config.getString("gatesloc");
		} catch (Exception e) {
			s = "";
		}
		s = s + "," + RCWars.loc2str(clickedBlock.getLocation());
		save("gatesloc", s);
	}

	public boolean isOpen() {
		return gateOpen;
	}

	public void switchGate() {
		if (gateOpen) {
			gateOpen = false;
			Iterator<Location> l = gates.iterator();
			while (l.hasNext())
				l.next().getBlock().setTypeId(101);
		} else {
			gateOpen = true;
			Iterator<Location> l = gates.iterator();
			while (l.hasNext())
				l.next().getBlock().setTypeId(0);
		}
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
		saveInt("exp", exp);
	}

	public void capBase(Race from, Race to) {
		giveExp(to);

		Iterator<String> players = WarPlayers.listPlayers();
		ChatColor c = ChatColor.WHITE;
		if (from != null)
			c = from.getCcolor();
		while (players.hasNext()) {
			String pstring = (String) players.next();
			Player p = pl.getServer().getPlayer(pstring);
			if (p == null) {
				WarPlayers.remove(pstring);
			} else
				p.sendMessage(c + disp + to.getCcolor()
						+ " has been captured by the " + to.getDisplay()
						+ "'s!");
		}
	}

	private void giveExp(Race winner) {
		Iterator<String> i = WarPlayers.listPlayers();
		while (i.hasNext()) {
			String pstring = (String) i.next();
			Player p = pl.getServer().getPlayer(pstring);
			if (p == null) {
				i.remove();
			} else {
				Race r = WarPlayers.getRace(p);
				if (r == null) {
					i.remove();
				} else if (WarPlayers.getRace(p).equals(winner)) {
					p.giveExp(RCWars.basecapexp);
					p.sendMessage(ChatColor.GREEN + "You have been given "
							+ RCWars.basecapexp + " exp for capturing a base");
				}
			}
		}
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
		saveDouble("weight", weight);
	}

	public boolean willDisplay() {
		return display;
	}
}