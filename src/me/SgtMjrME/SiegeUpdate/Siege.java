package me.SgtMjrME.SiegeUpdate;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Object.Base;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Siege {
	public static HashSet<Siege> sieges = new HashSet<Siege>();
	static HashMap<String, pair> editingPlayers = new HashMap<String, pair>();

	ArrayDeque<BlockData> blockLoc = new ArrayDeque<BlockData>();
	Random r;
	YamlConfiguration config;
	HashMap<Location, BlockData> walls = new HashMap<Location, BlockData>();
	public Base b;

	public Siege(YamlConfiguration config, Base b) {
		populateWalls(config);
		this.b = b;
		sieges.add(this);
	}

	public Siege() {
	}

	public static Siege getSiege(Base b) {
		for (Siege s : sieges)
			if (s.b.equals(b))
				return s;
		return null;
	}

	private void populateWalls(YamlConfiguration config) {
		this.config = config;
		String open = config.getString("siegewalls", "");
		if (open == "")
			return;
		String[] allLoc = open.split(";");
		if (allLoc.length == 0)
			return;
		World w = RCWars.returnPlugin().getWarWorld();
		for (String s : allLoc) {
			String[] split = s.split(",");
			if (split.length != 5) {
				RCWars.sendLogs("Siege did not properly grab data "
						+ b.getDisp());
			} else {
				int x = Integer.parseInt(split[0]);
				int y = Integer.parseInt(split[1]);
				int z = Integer.parseInt(split[2]);
				int t = Integer.parseInt(split[3]);
				byte d = Byte.parseByte(split[4]);
				Location temp = new Location(w, x, y, z);
				walls.put(temp, new BlockData(temp, t, d));
				w.getBlockAt(temp).setTypeIdAndData(t, d, true);
			}
		}
	}

	public static void startEditing(Player p, String b) {
		startEditing(p, getSiege(Base.getBase(b)));
	}

	public static void startEditing(Player p, Siege b) {
		if ((p == null) || (b == null))
			return;
		editingPlayers.put(p.getName(), new pair(b, null));
	}

	public static void stopEditing(String p) {
		editingPlayers.remove(p);
	}

	public static boolean isEditing(String p) {
		return editingPlayers.containsKey(p);
	}

	public static void setLocation(Player p, Location l2) {
		pair prev = (pair) editingPlayers.get(p.getName());
		if (prev == null)
			return;
		if (prev.s == null) {
			stopEditing(p.getName());
		} else if (prev.l == null) {
			prev.l = l2;
			p.sendMessage("First block set");
		} else {
			p.sendMessage("Second wall set: adding wall");
			p.sendMessage(prev.s.addWall(prev.l, l2) + " blocks added");
			prev.l = null;
		}
	}

	int addWall(Location l1, Location l2) {
		World w = RCWars.returnPlugin().getWarWorld();
		double xll = Math.min(l1.getX(), l2.getX());
		double xur = Math.max(l1.getX(), l2.getX());
		double yll = Math.min(l1.getY(), l2.getY());
		double yur = Math.max(l1.getY(), l2.getY());
		double zll = Math.min(l1.getZ(), l2.getZ());
		double zur = Math.max(l1.getZ(), l2.getZ());
		l1 = new Location(l1.getWorld(), xll, yll, zll);
		l2 = new Location(l1.getWorld(), xur, yur, zur);
		String newLocations = "";
		int count = 0;
		for (int x = (int) l1.getX(); x <= l2.getX(); x++) {
			for (int y = (int) l1.getY(); y <= l2.getY(); y++) {
				for (int z = (int) l1.getZ(); z <= l2.getZ(); z++) {
					Location temp = new Location(w, x, y, z);
					walls.put(temp, new BlockData(temp, temp.getBlock()
							.getTypeId(), (byte) 0));
					newLocations = newLocations + x + "," + y + "," + z + ","
							+ temp.getBlock().getTypeId() + ","
							+ temp.getBlock().getData() + ";";
					count++;
				}
			}
		}

		String prev = config.getString("siegewalls");
		if (prev != null)
			config.set("siegewalls", prev + newLocations);
		else
			config.set("siegewalls", newLocations);
		try {
			config.save(RCWars.returnPlugin().getDataFolder() + "/Bases/"
					+ b.getBaseName() + ".yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}

	public void repair(int i) {
		for (; i > 0; i--) {
			BlockData b = (BlockData) blockLoc.poll();
			if (b == null)
				return;
			b.l.getBlock().setTypeIdAndData(b.id, b.dat, true);
		}
	}

	public static Siege isWall(Location location) {
		for (Siege s : sieges) {
			if (s.walls.containsKey(location))
				return s;
		}
		return null;
	}

	public boolean wallDestroyed(Location l) {
		if (walls.containsKey(l)) {
			if (l.getBlock().getTypeId() != 0)
				blockLoc.push((BlockData) walls.get(l));
			l.getBlock().setTypeId(0);
			return true;
		}
		return false;
	}

	public static void repairAll() {
		for (Siege s : sieges) {
			s.blockLoc.clear();
			for (BlockData b : s.walls.values()) {
				b.l.getBlock().setTypeId(b.id);
				b.l.getBlock().setData(b.dat);
			}
		}
	}
}