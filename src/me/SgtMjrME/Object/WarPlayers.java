package me.SgtMjrME.Object;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.ClassUpdate.WarClass;
import me.SgtMjrME.ClassUpdate.WarRank;
import me.SgtMjrME.Listeners.EntityListener;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WarPlayers {
	private static HashMap<String, dmgHold> lastDamage = new HashMap<String, dmgHold>();
	private static HashSet<String> allPlayers = new HashSet<String>();

	public static void clear() {
		for (Race r : Race.getAllRaces()) {
			r.clearPlayers();
		}
		lastDamage.clear();
	}

	public static void add(Player p, Race r) {
		allPlayers.add(p.getName());
		r.addPlayer(p, WarClass.defaultClass);
		RCWars.returnPlugin().announceState(p);
		if ((numPlayers() > 7)
				&& (RCWars.returnPlugin().isRunning()
						.equals(state.TOO_FEW_PLAYERS)))
			RCWars.returnPlugin().resumeGame();
	}

	public static void setRace(Player p, Race r) {
		int points = 0;
		try {
			BufferedReader b = new BufferedReader(new FileReader(new File(
					RCWars.returnPlugin().getDataFolder() + "/WarPoints/"
							+ p.getName() + ".txt")));
			String temp = b.readLine();
			points = Integer.parseInt(temp);
			b.close();
		} catch (FileNotFoundException e) {
			RCWars.sendLogs("File not found for player " + p.getName());
		} catch (IOException e) {
			RCWars.sendLogs("Error reading player " + p.getName());
		} catch (Exception e) {
			RCWars.sendLogs("Other Error with " + p.getName());
		}
		RCWars.warPointSave.put(p, Integer.valueOf(points));

		add(p, r);
		p.teleport(r.getSpawn());
		WarClass.defaultClass.enterClass(p);
	}

	public static void removeAll(Location l) {
		for (Race r : Race.getAllRaces()) {
			for (String s : r.returnPlayers().keySet()) {
				Player p = RCWars.returnPlugin().getServer().getPlayer(s);
				if (p != null) {
					remove(p, l, "Removing all players");
				}
			}
			r.returnPlayers().clear();
		}
	}

	public static void remove(Player p, Location l, String reason) {
		if (p == null)
			return;
		remove(p, reason);
		p.teleport(l);
	}

	public static void remove(String p) {
		allPlayers.remove(p);
		getRace(p).removePlayer(p);
		lastDamage.remove(p);
		WarRank.pRank.remove(p);
		RCWars.repairing.remove(p);
		EntityListener.removeDmg(p);
	}

	public static void remove(Player p, String reason) {
		Race temp = getRace(p);
		if (temp == null) {
			return;
		}

		RCWars.returnPlugin().saveWarPoints(p);

		if ((numPlayers() < 8)
				&& (RCWars.returnPlugin().isRunning().equals(state.RUNNING)))
			RCWars.returnPlugin().pauseGame();
		getRace(p).removePlayer(p);
		allPlayers.remove(p.getName());
		lastDamage.remove(p.getName());
		RCWars.repairing.remove(p.getName());
		EntityListener.removeDmg(p.getName());
		if ((p == null) || (!p.isValid())) {
			return;
		}

		WarRank wr = WarRank.getPlayer(p);
		if (wr != null)
			wr.leave(p);
		p.sendMessage("You have been removed from Wars: " + reason);
	}

	public void leave(Player p) {
		remove(p, RCWars.lobbyLocation(), "Player Quit");
	}

	public static Race getRace(Player p) {
		return getRace(p.getName());
	}

	public static Race getRace(String s) {
		for (Race race : Race.getAllRaces()) {
			if (race.hasPlayer(s))
				return race;
		}
		return null;
	}

	public static Iterator<String> listPlayers() {
		return allPlayers.iterator();
	}

	public static void setDamageTime(Player p, String prev) {
		setDamageTime(p.getName(), prev);
	}

	public static void setDamageTime(String name, String prev) {
		lastDamage.put(name, new dmgHold(System.currentTimeMillis(), prev));
	}

	public static boolean gotDamaged(Player p) {
		return gotDamaged(p.getName());
	}

	public static boolean gotDamaged(String name) {
		if (!lastDamage.containsKey(name)) {
			return false;
		}
		long old = ((dmgHold) lastDamage.get(name)).time.longValue();
		if (System.currentTimeMillis() - old > 5000L)
			return false;
		return true;
	}

	public static void removeDamaged(String name) {
		lastDamage.remove(name);
	}

	public static int numPlayers() {
		return allPlayers.size();
	}

	public static boolean isPlaying(String p) {
		return allPlayers.contains(p);
	}
}