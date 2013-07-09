package me.SgtMjrME.Tasks;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Object.DatabaseObject;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.ScoreboardType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
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
	static public Skull goldPlayer;
	static public Sign goldSign;
	static public Skull ironPlayer;
	static public Sign ironSign;
	static public Skull diamondPlayer;
	static public Sign diamondSign;
	
	static HashMap<String, Scoreboard> pscoreboard = new HashMap<String, Scoreboard>();
	
	public ScoreboardHandler(){
		sbm = RCWars.returnPlugin().getServer().getScoreboardManager();
		maxKills = sbm.getNewScoreboard();
		maxDeaths = sbm.getNewScoreboard();
		maxWp = sbm.getNewScoreboard();
		resetObjectives();
		setupMaxSkills();
		isPlayer = false;
		nextSb = ScoreboardType.KILLS;
	}
	
	private void resetObjectives(){
		maxKillsObj = maxKills.registerNewObjective("leaderboard", "dummy");
		maxKillsObj.setDisplayName("Top Killers");
		maxKillsObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		maxDeathsObj = maxDeaths.registerNewObjective("leaderboard", "dummy");
		maxDeathsObj.setDisplayName("Top Deaths");
		maxDeathsObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		maxWpObj = maxWp.registerNewObjective("leaderboard", "dummy");
		maxWpObj.setDisplayName("Top WarPoints");
		maxWpObj.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	private void setupMaxSkills() {
		Bukkit.getScheduler().runTaskAsynchronously(RCWars.returnPlugin(), new Runnable(){
			@Override
			public void run() {
				final DatabaseObject[][] db = RCWars.returnPlugin().mysql.getMaxStats();
				Bukkit.getScheduler().runTask(RCWars.returnPlugin(), new Runnable(){
					@Override
					public void run(){
						maxKillsObj.unregister();
						maxDeathsObj.unregister();
						maxWpObj.unregister();
						resetObjectives();						
						setObjective(0, maxKillsObj, db);
						setObjective(1, maxDeathsObj, db);
						setObjective(2, maxWpObj, db);
						if (diamondPlayer != null) diamondPlayer.setOwner(Bukkit.getPlayer(db[0][0].s).getName());
						if (diamondSign != null){
							diamondSign.setLine(2, db[0][0].s);
							diamondSign.update();
						}
						else System.out.println("Diamond sign null");
						if (goldPlayer != null) goldPlayer.setOwner(Bukkit.getPlayer(db[0][1].s).getName());
						if (goldSign != null){
							goldSign.setLine(2, db[0][1].s);
							goldSign.update();
						}
						if (ironPlayer != null) ironPlayer.setOwner(Bukkit.getPlayer(db[0][2].s).getName());
						if (ironSign != null){
							ironSign.setLine(2, db[0][2].s);
							ironSign.update();
						}
						if (diamondPlayer != null)diamondPlayer.update();
						if (goldPlayer != null) goldPlayer.update();
						if (ironPlayer != null) ironPlayer.update();
					}

					private void setObjective(int place, Objective obj,
							DatabaseObject[][] db) {
						Score s;
						for(int i = 0; i < 3; i++){
							s = obj.getScore(Bukkit.getOfflinePlayer(db[place][i].s));
							s.setScore(db[place][i].get(place));
						}
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
						Score s;
						s = o.getScore(Bukkit.getOfflinePlayer("Kills"));
						s.setScore(stats[0]);
						s = o.getScore(Bukkit.getOfflinePlayer("Deaths"));
						s.setScore(stats[1]);
						s = o.getScore(Bukkit.getOfflinePlayer("WarPoints"));
						s.setScore(stats[2]);
						pscoreboard.put(p.getName(), psb);
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
		if (r.getTeam() != null) r.getTeam().addPlayer(p);
	}

	public static void registerTeam(String name, ChatColor ccolor) {
		setupScoreboardTeam(name, maxKills, ccolor);
		setupScoreboardTeam(name, maxDeaths, ccolor);
		setupScoreboardTeam(name, maxWp, ccolor);
	}
	
	private static void setupScoreboardTeam(String name, Scoreboard sb, ChatColor ccolor){
		Team t = sb.getTeam(name);
		if (t == null) t = sb.registerNewTeam(name);
		t.setAllowFriendlyFire(false);
		t.setCanSeeFriendlyInvisibles(true);
		t.setPrefix("" + ccolor);
	}

	public static void setupSkulls() {
		YamlConfiguration cfg = new YamlConfiguration();
		try {
			cfg.load(new File(RCWars.returnPlugin().getDataFolder().getAbsolutePath() + "/leaderboardSkull.yml"));
			goldPlayer = handleSkullCheck(cfg, "gold");
			goldSign = handleSign(cfg, "goldsign");
			ironPlayer = handleSkullCheck(cfg, "iron");
			ironSign = handleSign(cfg, "ironsign");
			diamondPlayer = handleSkullCheck(cfg, "diamond");
			diamondSign = handleSign(cfg, "diamondsign");
		} catch (IOException
				| InvalidConfigurationException e) {
			Bukkit.getLogger().severe("RCWars could not read leaderboard skulls.  Are they set up?");
		}
	}

	private static Sign handleSign(YamlConfiguration cfg, String s) {
		try{
			String str = cfg.getString(s);
			if (str == null) return null;
			Location l = RCWars.returnPlugin().str2Loc(str);
			if (l == null) return null;
			Block b = l.getBlock();
			if (b != null && b.getType().equals(Material.WALL_SIGN)){
				return (Sign) b.getState();
			}
			System.out.println(l.toString());
			return null;
			}
			catch(Exception e){
				//Well, something went wrong, but there is no skull here
				e.printStackTrace();
				System.err.println("Error handling " + s);
				return null;
			}
	}

	private static Skull handleSkullCheck(YamlConfiguration cfg, String s){
		try{
		String str = cfg.getString(s);
		if (str == null) return null;
		Location l = RCWars.returnPlugin().str2Loc(str);
		if (l == null) return null;
		Block b = l.getBlock();
		if (b != null && b.getType() == Material.SKULL){
			Skull skull = (Skull) b.getState();
			return skull;
		}
		return null;
		}
		catch(Exception e){
			//Well, something went wrong, but there is no skull here
			return null;
		}
	}

}
