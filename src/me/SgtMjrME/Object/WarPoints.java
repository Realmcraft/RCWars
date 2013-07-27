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
	private static ConcurrentHashMap<String, Integer> warPointSave = new ConcurrentHashMap<String, Integer>();
	private static int warPointMax;
	private static mysqlLink mysql;
	private static RCWars rc;
	
	public WarPoints(int wpm, mysqlLink m, RCWars r){
		warPointMax = wpm;
		mysql = m;
		rc = r;
	}
	
	public static Boolean spendWarPoints(Player p, int cost) {
		if (warPointSave.containsKey(p.getName())) {
			int points = warPointSave.get(p.getName());
			if (points < cost) {
				Util.sendMessage(p, ChatColor.RED + "Not enough War Points");
				return false;
			}
			Util.sendMessage(p, ChatColor.GREEN + "You have been charged " + cost
					+ " warpoints");
			warPointSave.put(p.getName(), points - cost);
			saveWPnoRemove(p);
			return true;
		}
		Util.sendMessage(p, ChatColor.RED + "War data not loaded");
		return false;
	}

	public static void giveWarPoints(Player player, int warPoints) {
		if ((warPointSave.containsKey(player.getName()))
				&& (((Integer) warPointSave.get(player.getName())) + warPoints > warPointMax)) {
			Util.sendMessage(player, "You have hit the max of " + warPointMax);
			warPointSave.put(player.getName(), warPointMax);
			if (mysql != null)
				mysql.updatePlayer(player, "wp", warPoints);
			return;
		}
		else if (!warPointSave.containsKey(player.getName())) {
			warPointSave.put(player.getName(), warPoints);
			if (mysql != null) mysql.updatePlayer(player, "wp", warPoints);
		} else { //how would this...
			warPointSave.put(player.getName(), warPointSave.get(player.getName()) + warPoints);
			if (mysql != null) mysql.updatePlayer(player, "wp", warPoints);
		}
	}
	
	public static Integer getWarPoints(String s){
		if (!isLoaded(s)){
			loadWarPoints(s);
			return -1;//Hasn't loaded yet, they'll deal with it.
		}
		return (Integer) warPointSave.get(s);
	}

	public static Integer getWarPoints(Player p) {
		return (Integer) warPointSave.get(p.getName());
	}

	public static void loadWarPoints(final String p) {
		Bukkit.getScheduler().runTaskAsynchronously(rc, new Runnable(){
			@Override
			public void run() {
				int points = 0;
				try {
					BufferedReader b = new BufferedReader(new FileReader(new File(
							rc.getDataFolder() + "/WarPoints/"
									+ p + ".txt")));
					String temp = b.readLine();
					points = Integer.parseInt(temp);
					b.close();
				} catch (FileNotFoundException e) {
					Util.sendLog("File not found for player " + p);
				} catch (IOException e) {
					Util.sendLog("Error reading player " + p);
				} catch (Exception e) {
					Util.sendLog("Other Error with " + p);
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
		if (warPointSave.containsKey(p.getName())) {
			Util.sendMessage(p, "You have " + warPointSave.get(p.getName())
					+ " warpoints");
		} else {
			Util.sendMessage(p, "Your war data is not loaded, attempting load");
			if (!isLoaded(p.getName())) Util.sendMessage(p, "Could not load)");
			else dispWP(p);
		}
	}
	
	public static boolean isLoaded(String s){ //Will return true if it loads, false otherwise (this will load the wp's)
		if (warPointSave.containsKey(s)) return true;
		loadWarPoints(s);
		return warPointSave.containsKey(s);//False if not contained, we have an issue.
	}
	
	public static boolean has(String name, double amt){
		if (isLoaded(name)){
			if (warPointSave.get(name) > amt) return true;
			return false;
		}
		return false;
	}
}
