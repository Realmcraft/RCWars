package me.SgtMjrME.Object;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.ClassUpdate.WarClass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;

public class Race {
	private final String name;
	private final String display;
	private final DyeColor color;
	private final ChatColor ccolor;
	private Location spawn;
	private Location spawnZll;
	private Location spawnZur;
	public final Material portalType;
	public final byte portaldmg;
	public final MaterialData matdat;
	private final YamlConfiguration rcs;
	public final int swordtype;
	boolean ref = false;

	private static ConcurrentHashMap<Player, Race> setItemRace = new ConcurrentHashMap<Player, Race>();

	private static HashMap<String, Race> n2r = new HashMap<String, Race>();
	private ConcurrentHashMap<String, WarClass> p2c = new ConcurrentHashMap<String, WarClass>();

	public Race(YamlConfiguration cs) {
		rcs = cs;
		name = cs.getString("name").toLowerCase();
		color = DyeColor.valueOf(cs.getString("color"));
		ccolor = ChatColor.valueOf(cs.getString("ccolor"));
		display = cs.getString("name");
		portalType = Material.getMaterial(cs.getInt("portaltype"));
		portaldmg = ((byte) cs.getInt("portaldmg", 0));
		matdat = portalType.getNewData(portaldmg);
		String temp = cs.getString("spawn");
		swordtype = cs.getInt("swordtype", 0);
		if (temp == null)
			spawn = null;
		else
			spawn = str2Loc(temp);
		if (cs.getBoolean("referee", false))
			openReferee(cs);
		else
			try {
				spawnZll = str2Loc(cs.getString("spawnzonel"));
				spawnZur = str2Loc(cs.getString("spawnzoner"));
			} catch (Exception e) {
				spawnZll = null;
				spawnZur = null;
			}
		n2r.put(name, this);
	}

	private void openReferee(YamlConfiguration cs) {
		spawnZll = (this.spawnZur = null);
		ref = true;
	}

	static public void clear() {
		n2r.clear();
		setItemRace.clear();
		for (Race r : Race.getAllRaces())
			r.clearPlayers();
	}

	public void clearPlayers() {
		p2c.clear();
	}

	public static Race checkRaceOpen(Race r) {
		if (r.isRef())
			return r;
		HashMap<Race, Integer> numPerRace = new HashMap<Race, Integer>();
		Iterator<Race> tempRace = n2r.values().iterator();
		while (tempRace.hasNext()) {
			Race tr = (Race) tempRace.next();
			if (!tr.isRef())
				numPerRace.put(tr, 0);
		}
		Iterator<String> players = WarPlayers.listPlayers();
		Player p;
		while (players.hasNext()) {
			String pstring = (String) players.next();
			p = RCWars.returnPlugin().getServer().getPlayer(pstring);
			if (p == null) {
				players.remove();
			} else {
				Race temp = WarPlayers.getRace(p);
				if (temp != null) {
					if (!temp.isRef())
						if (numPerRace.get(temp) == null)
							numPerRace.put(temp, Integer.valueOf(1));
						else
							numPerRace.put(temp, Integer
									.valueOf(((Integer) numPerRace.get(temp))
											.intValue() + 1));
				}
			}
		}
		if (numPerRace.isEmpty())
			return null;
		int trying = ((Integer) numPerRace.get(r)).intValue();
		Iterator<Race> i = numPerRace.keySet().iterator();
		while (i.hasNext()) {
			Race race = (Race) i.next();
			int thisrace = ((Integer) numPerRace.get(race)).intValue();
			if (thisrace + 1 < trying)
				return race;
		}
		if ((((Integer) numPerRace.get(r)).intValue() > 2)
				&& (RCWars.returnPlugin().isRunning().equals(state.STOPPED)))
			RCWars.returnPlugin().startGame();
		return r;
	}

	public void setSpawn(Location l) {
		spawn = l;
		rcs.set("spawn", RCWars.loc2str(l));
		try {
			rcs.save(RCWars.returnPlugin().getDataFolder() + "/Races/" + name
					+ ".yml");
		} catch (IOException e) {
			RCWars.sendLogs("Crashed");
			e.printStackTrace();
		}
	}

	public Byte getColor() {
		return color.getWoolData();
	}

	public String getName() {
		return name;
	}

	public Location getSpawn() {
		return spawn;
	}

	public static Race raceByName(String s) {
		return (Race) n2r.get(s.toLowerCase());
	}

	public static Collection<Race> getAllRaces() {
		return n2r.values();
	}

	private Location str2Loc(String s) {
		String[] s1 = s.split(" ");
		Location loc = new Location(Bukkit.getServer().getWorld(s1[0]),
				str2d(s1[1]), str2d(s1[2]), str2d(s1[3]));
		return loc;
	}

	private double str2d(String s) {
		return Double.parseDouble(s);
	}

	public void sendToSpawn(Player p) {
		if (spawn != null)
			p.teleport(spawn);
		else
			p.teleport(RCWars.lobbyLocation());
	}

	public String getDisplay() {
		return ccolor + display;
	}

	public ChatColor getCcolor() {
		return ccolor;
	}

	public boolean spawnZonesSet() {
		return (spawnZll != null) && (spawnZur != null);
	}

	private void save(String path, String item) {
		rcs.set(path, item);
		try {
			rcs.save(RCWars.returnPlugin().getDataFolder() + "/Races/" + name
					+ ".yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int setSpawnProtect(Location protect, Player p) {
		if (spawnZll == null) {
			spawnZll = protect;
			return 1;
		}
		if (spawnZur == null) {
			spawnZur = protect;
			double xll = Math.min(spawnZll.getX(), spawnZur.getX());
			double xur = Math.max(spawnZll.getX(), spawnZur.getX());
			double yll = Math.min(spawnZll.getY(), spawnZur.getY());
			double yur = Math.max(spawnZll.getY(), spawnZur.getY());
			double zll = Math.min(spawnZll.getZ(), spawnZur.getZ());
			double zur = Math.max(spawnZll.getZ(), spawnZur.getZ());
			spawnZll = new Location(spawnZll.getWorld(), xll, yll, zll);
			spawnZur = new Location(spawnZll.getWorld(), xur, yur, zur);
			save("spawnzonel", RCWars.loc2str(spawnZll));
			save("spawnzoner", RCWars.loc2str(spawnZur));
			setItemRace.remove(p);
			return 2;
		}

		spawnZll = null;
		spawnZur = null;
		return 0;
	}

	public boolean inSpawn(Player p) {
		Location player = p.getLocation();
		return checkSpawnLocation(player);
	}

	private boolean checkSpawnLocation(Location p) {
		if ((spawnZll == null) || (spawnZur == null)){
			System.out.println("Check failed for spawn location on race " + getDisplay());
			return false;
		}
		if ((spawnZll.getX() > p.getX() || spawnZll.getZ() > p.getZ()
				|| spawnZll.getY() > p.getY()))
			return false;
		if ((spawnZur.getX() < p.getX() || spawnZur.getZ() < p.getZ()
				|| spawnZur.getY() < p.getY())) {
			return false;
		}
		return true;
	}

	public static boolean isOperating(Player player) {
		return setItemRace.containsKey(player);
	}

	public static void distributeAction(Player p, PlayerInteractEvent e) {
		int val = ((Race) setItemRace.get(p)).setSpawnProtect(e
				.getClickedBlock().getLocation(), p);
		if (val == 1)
			e.getPlayer().sendMessage("First marker hit");
		else if (val == 2)
			e.getPlayer().sendMessage("Second marker hit");
		else if (val == 3)
			e.getPlayer().sendMessage("Zone removed");
	}

	public void setSpawnZone(Player p) {
		if (setItemRace.containsKey(p)) {
			setItemRace.remove(p);
			p.sendMessage("No longer setting zones");
			return;
		}
		if (setItemRace.containsValue(this)) {
			p.sendMessage(ChatColor.DARK_RED
					+ "Someone is already editing this zone");
			return;
		}
		setItemRace.put(p, this);
		p.sendMessage(ChatColor.DARK_GREEN + "Punch 2 corners of the zone");
	}

	public static int toInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
		}
		return 9001;
	}

	public void sendMessage(String mes) {
		Iterator<String> players = WarPlayers.listPlayers();
		while (players.hasNext()) {
			String s = (String) players.next();
			Player send = Bukkit.getServer().getPlayer(s);
			if ((send != null) && (WarPlayers.getRace(s).equals(this)))
				send.sendMessage(mes);
		}
	}

	public void addPlayer(Player p, WarClass class1) {
		p2c.put(p.getName(), class1);
	}

	public void removePlayer(Player p) {
		removePlayer(p.getName());
	}

	public void removePlayer(String name) {
		p2c.remove(name);
	}

	public int getPlayersInRace() {
		return p2c.size();
	}

	public ConcurrentHashMap<String, WarClass> returnPlayers() {
		return p2c;
	}

	public boolean hasPlayer(Player p) {
		return hasPlayer(p.getName());
	}

	public boolean hasPlayer(String p) {
		return p2c.containsKey(p);
	}

	public boolean isRef() {
		return ref;
	}

	public static Race getRacePortal(Block b) {
		for (Race r : n2r.values()) {
			if ((r.matdat.getItemTypeId() == b.getTypeId())
					&& (r.matdat.getData() == b.getData()))
				return r;
		}
		return null;
	}
}