package me.SgtMjrME.Object;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Util;
import me.SgtMjrME.mysqlLink;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class WarPoints {
	private static ConcurrentHashMap<Player, Integer> warPointSave = new ConcurrentHashMap<Player, Integer>();
	private static int warPointMax;
	private static mysqlLink mysql;
	private static RCWars rc;
	
	public WarPoints(int wpm, mysqlLink m, RCWars r){
		warPointMax = wpm;
		mysql = m;
		rc = r;
	}
	
	public static Boolean spendWarPoints(Player p, int cost) {
		if (warPointSave.containsKey(p)) {
			int points = warPointSave.get(p);
			if (points < cost) {
				Util.sendMessage(p, ChatColor.RED + "Not enough War Points");
				return false;
			}
			Util.sendMessage(p, ChatColor.GREEN + "You have been charged " + cost
					+ " warpoints");
			warPointSave.put(p, points - cost);
			saveWPnoRemove(p);
			return true;
		}
		Util.sendMessage(p, ChatColor.RED + "War data not loaded");
		return false;
	}

	public static void giveWarPoints(Player player, int warPoints) {
		if ((warPointSave.containsKey(player))
				&& (((Integer) warPointSave.get(player)).intValue() + warPoints > warPointMax)) {
			Util.sendMessage(player, "You have hit the max of " + warPointMax);
			warPointSave.put(player, warPointMax);
			if (mysql != null)
				mysql.updatePlayer(player, "wp", warPoints);
			return;
		}
		else if (!warPointSave.containsKey(player)) {
			warPointSave.put(player, warPoints);
			if (mysql != null) mysql.updatePlayer(player, "wp", warPoints);
		} else { //how would this...
			warPointSave.put(player, warPointSave.get(player) + warPoints);
			if (mysql != null) mysql.updatePlayer(player, "wp", warPoints);
		}
	}

	public static Integer getWarPoints(Player p) {
		return (Integer) warPointSave.get(p);
	}

	public static void loadWarPoints(final Player p) {
		Bukkit.getScheduler().runTaskAsynchronously(rc, new Runnable(){
			@Override
			public void run() {
				int points = 0;
				try {
					BufferedReader b = new BufferedReader(new FileReader(new File(
							rc.getDataFolder() + "/WarPoints/"
									+ p.getName() + ".txt")));
					String temp = b.readLine();
					points = Integer.parseInt(temp);
					b.close();
				} catch (FileNotFoundException e) {
					Util.sendLog("File not found for player " + p.getName());
				} catch (IOException e) {
					Util.sendLog("Error reading player " + p.getName());
				} catch (Exception e) {
					Util.sendLog("Other Error with " + p.getName());
				}
				warPointSave.put(p, points);
			}
		});
	}

	public static void saveWPnoRemove(final Player p) {
		Bukkit.getScheduler().runTaskAsynchronously(rc, new Runnable(){
			@Override
			public void run() {
				if (warPointSave.containsKey(p)) {
					int points = warPointSave.get(p);
					try {
						File f = new File(rc.getDataFolder() + "/WarPoints");
						if (!f.exists())
							f.mkdir();
						BufferedWriter b = new BufferedWriter(
								new FileWriter(new File(rc.getDataFolder() + "/WarPoints/"
										+ p.getName() + ".txt")));
						b.write("" + points);
						b.close();
					} catch (IOException e) {
						Util.sendLog("Could not save player");
					}
				}
			}
		});
		
	}

	public static void saveWarPoints(final Player p) {
		if (warPointSave.containsKey(p)) {
			saveWPnoRemove(p);
			Bukkit.getScheduler().runTaskLater(rc, new Runnable(){
				@Override
				public void run(){
					warPointSave.remove(p);
				}
			}, 400);//Give it 20 seconds, should be done saving by then.
		}
	}

	public static void dispWP(Player p) {
		if (warPointSave.containsKey(p)) {
			Util.sendMessage(p, "You have " + warPointSave.get(p)
					+ " warpoints");
		} else {
			Util.sendMessage(p, "Your war data is not loaded, attempting load");
			if (!isLoaded(p)) Util.sendMessage(p, "Could not load)");
			else dispWP(p);
		}
	}
	
	public static boolean isLoaded(Player p){ //Will return true if it loads, false otherwise (this will load the wp's)
		if (warPointSave.containsKey(p)) return true;
		loadWarPoints(p);
		return warPointSave.containsKey(p);//False if not contained, we have an issue.
	}
}
