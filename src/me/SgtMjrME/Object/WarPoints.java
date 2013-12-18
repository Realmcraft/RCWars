package me.SgtMjrME.Object;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Util;
import me.SgtMjrME.mysqlLink;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class WarPoints {
//	private static ConcurrentHashMap<String, Pair> warPointSave = new ConcurrentHashMap<String, Pair>();
	private static int warPointMax;
	private static mysqlLink mysql;
	private static RCWars rc;
	
	public WarPoints(int wpm, mysqlLink m, RCWars r){
		warPointMax = wpm;
		mysql = m;
		rc = r;
	}
	
	public static Boolean spendWarPoints(Player p, int cost) {
		int points = RCWars.returnPlugin().wpLink.getCoin(p.getName());
		if (points < cost){
			Util.sendMessage(p, ChatColor.RED + "Not enough Coins");
			return false;
		}
		Util.sendMessage(p, ChatColor.GREEN + "You have been charged " + cost + " coins");
		 RCWars.returnPlugin().wpLink.updatePlayerCoin(p, -cost);
		 return true;
//		if (warPointSave.containsKey(p.getName())) {
//			int points = warPointSave.get(p.getName()).cur;
//			if (points < cost) {
//				Util.sendMessage(p, ChatColor.RED + "Not enough War Points");
//				return false;
//			}
//			Util.sendMessage(p, ChatColor.GREEN + "You have been charged " + cost
//					+ " warpoints");
//			warPointSave.put(p.getName(), new Pair(warPointSave.get(p.getName()).start, points - cost));
//			saveWPnoRemove(p);
//			return true;
//		}
//		Util.sendMessage(p, ChatColor.RED + "War data not loaded");
//		return false;
	}

	public static void giveWarPoints(Player player, int warPoints) {
		if (player.hasPermission("rcwars.rank6")) warPoints *= rc.rank6;
		else if (player.hasPermission("rcwars.rank5")) warPoints *= rc.rank5;
		else if (player.hasPermission("rcwars.rank4")) warPoints *= rc.rank4;
		else if (player.hasPermission("rcwars.rank3")) warPoints *= rc.rank3;
		else if (player.hasPermission("rcwars.rank2")) warPoints *= rc.rank2;
		else if (player.hasPermission("rcwars.rank1")) warPoints *= rc.rank1;
		 RCWars.returnPlugin().wpLink.updatePlayerCoin(player, warPoints);
//		Pair cur = warPointSave.get(player.getName());
//		if ((warPointSave.containsKey(player.getName()))
//				&& (((Integer) cur.cur + warPoints > warPointMax))) {
//			Util.sendMessage(player, "You have hit the max of " + warPointMax);
//			cur.cur = warPointMax;
//			warPointSave.put(player.getName(), cur);
//			if (mysql != null)
//				mysql.updatePlayer(player, "wp", warPoints);
//			return;
//		}
//		else if (!warPointSave.containsKey(player.getName())) {
//			loadWarPoints(player.getName());
//			if (mysql != null) mysql.updatePlayer(player, "wp", warPoints);
//		} else { //how would this...
//			warPointSave.put(player.getName(), new Pair(0,warPointSave.get(player.getName()).cur + warPoints));
//			if (mysql != null) mysql.updatePlayer(player, "wp", warPoints);
//		}
	}
	
	public static Integer getWarPoints(String s){
//		if (!isLoaded(s)){
//			loadWarPoints(s);
//			return -1;//Hasn't loaded yet, they'll deal with it.
//		}
//		return (Integer) warPointSave.get(s).cur;
		return  RCWars.returnPlugin().wpLink.getCoin(s);
	}

	public static Integer getWarPoints(Player p) {
//		return (Integer) warPointSave.get(p.getName()).cur;
		 return RCWars.returnPlugin().wpLink.getCoin(p.getName());
	}

	public static void loadWarPoints(final String p) {//This function will do nothing until Stick wants me to cache again.
//		Bukkit.getScheduler().runTaskAsynchronously(rc, new Runnable(){
//			@Override
//			public void run() {
//				int both = RCWars.returnPlugin().wpLink.getCoin(p);
//				warPointSave.put(p, new Pair(both, both));
				//NOTHING BELOW HERE
//				try {
//					BufferedReader b = new BufferedReader(new FileReader(new File(
//							rc.getDataFolder() + "/WarPoints/"
//									+ p + ".txt")));
//					String temp = b.readLine();
//					points = Integer.parseInt(temp);
//					b.close();
//				} catch (FileNotFoundException e) {
//					Util.sendLog("File not found for player " + p);
//				} catch (IOException e) {
//					Util.sendLog("Error reading player " + p);
//				} catch (Exception e) {
//					Util.sendLog("Other Error with " + p);
//				}
//				warPointSave.put(p, points);
//			}
//		});
	}

	public static void saveWPnoRemove(final Player p) {
		//This function unused w/o cacheing
//		Bukkit.getScheduler().runTaskAsynchronously(rc, new Runnable(){
//			@Override
//			public void run() {
//				if (warPointSave.containsKey(p)) {
//					int points = warPointSave.get(p).cur;
//					try {
//						File f = new File(rc.getDataFolder() + "/WarPoints");
//						if (!f.exists())
//							f.mkdir();
//						BufferedWriter b = new BufferedWriter(
//								new FileWriter(new File(rc.getDataFolder() + "/WarPoints/"
//										+ p.getName() + ".txt")));
//						b.write("" + points);
//						b.close();
//					} catch (IOException e) {
//						Util.sendLog("Could not save player");
//					}
//				}
//			}
//		});
		
	}

	public static void saveWarPoints(final Player p) {
		//This function does nothing w/o cacheing.
//		if (warPointSave.containsKey(p)) {
//			saveWPnoRemove(p);
//			Pair cur = warPointSave.get(p.getName());
//			int diff = cur.cur - cur.start;
//			RCWars.returnPlugin().wpLink.updatePlayerCoin(p, diff);
//			Bukkit.getScheduler().runTaskLater(rc, new Runnable(){
//				@Override
//				public void run(){
//					warPointSave.remove(p);
//				}
//			}, 100);//Give it 5 seconds, should be done saving by then.
//		}
	}

	public static void dispWP(Player p) {
		Util.sendMessage(p, "You have " + RCWars.returnPlugin().wpLink.getCoin(p.getName())
					+ " coins");
//		if (warPointSave.containsKey(p.getName())) {
//			Util.sendMessage(p, "You have " + warPointSave.get(p.getName()).cur
//					+ " warpoints");
//		} else {
//			Util.sendMessage(p, "Your war data is not loaded, attempting load");
//			if (!isLoaded(p.getName())) Util.sendMessage(p, "Could not load)");
//			else dispWP(p);
//		}
	}
	
	public static boolean isLoaded(String s){ //Will return true if it loads, false otherwise (this will load the wp's)
		//Basically unneeded w/o cacheing
		return true;
//		if (warPointSave.containsKey(s)) return true;
//		loadWarPoints(s);
//		return warPointSave.containsKey(s);//False if not contained, we have an issue.
	}
	
	public static boolean has(String name, double amt){
		return true;
	}
//		if (isLoaded(name)){
//			if (warPointSave.get(name).cur > amt) return true;
//			return false;
//		}
//		return false;
//	}
}
