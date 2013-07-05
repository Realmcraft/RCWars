package me.SgtMjrME.Tasks;

import java.util.HashMap;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Object.DatabaseObject;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.ScoreboardType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class ScoreboardHandler implements Runnable{

	static ScoreboardManager sbm;
	static Scoreboard maxKills;
	static Objective maxKillsObj;
	static Scoreboard maxDeaths;
	static Objective maxDeathsObj;
	static Scoreboard maxWp;
	static Objective maxWpObj;
	static ScoreboardType nextSb;
	static ScoreboardType current;
	static DatabaseObject[][] dbo;
	static boolean isPlayer;
	
	static HashMap<String, Scoreboard> pscoreboard = new HashMap<String, Scoreboard>();
	
	//TODO Need to set up in such a way that kills/deaths/wp can all be dispalyed < 16 characters.
	
	public ScoreboardHandler(){
		sbm = RCWars.returnPlugin().getServer().getScoreboardManager();
		maxKills = sbm.getNewScoreboard();
		maxDeaths = sbm.getNewScoreboard();
		maxWp = sbm.getNewScoreboard();
		maxKillsObj = maxKills.registerNewObjective("leaderboard", "dummy");
		maxKillsObj.setDisplayName("Top Killers");
		maxKillsObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		maxDeathsObj = maxKills.registerNewObjective("leaderboard", "dummy");
		maxDeathsObj.setDisplayName("Top Deaths");
		maxDeathsObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		maxWpObj = maxKills.registerNewObjective("leaderboard", "dummy");
		maxWpObj.setDisplayName("Top WarPoints");
		maxWpObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		setupMaxSkills();
		isPlayer = false;
		nextSb = ScoreboardType.KILLS;
	}

	private void setupMaxSkills() {
		Bukkit.getScheduler().runTaskAsynchronously(RCWars.returnPlugin(), new Runnable(){
			@Override
			public void run() {
				final DatabaseObject[][] db = RCWars.returnPlugin().mysql.getMaxStats();
				Bukkit.getScheduler().runTask(RCWars.returnPlugin(), new Runnable(){
					@Override
					public void run(){
						maxSkills.unregister();
						maxSkills = sb.registerNewObjective("leaderboard", "dummy");
						maxSkills.setDisplayName("Leaderboard");
						maxSkills.setDisplaySlot(DisplaySlot.SIDEBAR);
						maxSkills.getScore(Bukkit.getOfflinePlayer(db[0].s)).setScore(db[0].kills);
						maxSkills.getScore(Bukkit.getOfflinePlayer(db[1].s)).setScore(db[1].deaths);
						maxSkills.getScore(Bukkit.getOfflinePlayer(db[2].s)).setScore(db[2].wp);
					}
				});
			}
		});
	}
	
	public static void setupPlayerScoreboard(final Player p){
		Bukkit.getScheduler().runTaskAsynchronously(RCWars.returnPlugin(), new Runnable(){
			@Override
			public void run(){
				final int[] stats = RCWars.returnPlugin().mysql.getStats(p.getName());
				Bukkit.getScheduler().runTask(RCWars.returnPlugin(), new Runnable(){
					@Override
					public void run(){
						pscoreboard.remove(p.getName());
						Scoreboard psb = sbm.getNewScoreboard();
						Objective o = psb.registerNewObjective("Stats", "dummy");
						o.setDisplaySlot(DisplaySlot.SIDEBAR);
						o.getScore(Bukkit.getOfflinePlayer("Kills")).setScore(stats[0]);
						o.getScore(Bukkit.getOfflinePlayer("Deaths")).setScore(stats[1]);
						o.getScore(Bukkit.getOfflinePlayer("WarPoints")).setScore(stats[2]);
						pscoreboard.put(p.getName(), sbm.getNewScoreboard());
					}
				});
			}
		});
	}

	@Override
	public void run() {
		if (isPlayer){
			for(Player p : Bukkit.getOnlinePlayers()){
				setupPlayerScoreboard(p);
			}
		}
		updatePlayers();
		isPlayer = !isPlayer;
		if (isPlayer){
			if (nextSb.equals(ScoreboardType.KILLS)) nextSb = ScoreboardType.DEATHS;
			else if (nextSb.equals(ScoreboardType.DEATHS)) nextSb = ScoreboardType.WARPOINTS;
			else if (nextSb.equals(ScoreboardType.WARPOINTS)){
				nextSb = ScoreboardType.KILLS;
				setupMaxSkills();
			}
		}
	}

	private void updatePlayers() {
		if (!isPlayer){
			if (nextSb.equals(ScoreboardType.KILLS))
				for (Player p : Bukkit.getOnlinePlayers())
					p.setScoreboard(maxKills);
			else if (nextSb.equals(ScoreboardType.DEATHS))
				for (Player p : Bukkit.getOnlinePlayers())
					p.setScoreboard(maxDeaths);
			else if (nextSb.equals(ScoreboardType.WARPOINTS))
				for (Player p : Bukkit.getOnlinePlayers())
					p.setScoreboard(maxWp);
		}
		else{
			for(Player p : Bukkit.getOnlinePlayers()){
				Scoreboard temp = pscoreboard.get(p.getName());
				if (temp != null) 
					p.setScoreboard(temp);
			}
		}
	}

	public static void updateTeam(Player p, Race r) {
		Team old = sbm.getMainScoreboard().getPlayerTeam(p);
		if (old != null) old.removePlayer(p);
		r.getTeam().addPlayer(p);
	}

	public static Team registerTeam(String name, ChatColor ccolor) {
		Team t = sb.getTeam(name);
		if (t == null) t = sb.registerNewTeam(name);
		t.setAllowFriendlyFire(false);
		t.setCanSeeFriendlyInvisibles(true);
		t.setPrefix("" + ccolor);
		return sb.getTeam(name);
	}

}
