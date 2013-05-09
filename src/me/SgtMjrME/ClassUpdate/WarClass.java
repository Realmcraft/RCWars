package me.SgtMjrME.ClassUpdate;

import java.io.File;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.ClassUpdate.Abilities.None;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WarClass {
	String className;
	String displayName;
	String permission;
	ChatColor color;
	Vector<WarRank> ranks = new Vector<WarRank>();
	RCWars pl;
	boolean valid = false;

	public static ConcurrentHashMap<String, WarClass> s2c = new ConcurrentHashMap<String, WarClass>();
	public static WarClass defaultClass;

	public WarClass(String string, RCWars rcWars) {
		pl = rcWars;
		File f = new File(string);
		YamlConfiguration cfg = new YamlConfiguration();
		try {
			cfg.load(f);
		} catch (Exception e) {
			rcWars.sendLog("Error loading " + string);
			valid = false;
		}
		className = cfg.getString("name");
		displayName = cfg.getString("display");
		permission = cfg.getString("permission", null);
		color = ChatColor.valueOf(cfg.getString("color", "WHITE"));
		if (cfg.getBoolean("defaultclass", false))
			defaultClass = this;

		int i = 0;
		ConfigurationSection cs;
		while ((cs = cfg.getConfigurationSection("rank" + ++i)) != null) {
			WarRank newRank = new WarRank(cs, className, i, this);
			if (!newRank.valid) {
				RCWars.sendLogs("Rank " + i + " in " + string
						+ " could not load");
				RCWars.sendLogs(cs.toString());
			} else {
				ranks.add(newRank);
			}
		}
		valid = true;
	}

	public static void loadClasses(RCWars rcWars) {
		File f = new File(rcWars.getDataFolder().getAbsolutePath() + "/Classes");
		if (!f.exists())
			f.mkdirs();
		for (String file : f.list())
			if (file.endsWith(".yml")) {
				WarClass c = new WarClass(f.getAbsoluteFile() + "/" + file,
						rcWars);
				if (!c.valid) {
					rcWars.sendLog("Error loading class data for " + file);
				} else
					s2c.put(c.className, c);
			}
		if (defaultClass == null)
			RCWars.sendLogs("Could not find default class");
	}

	public static WarClass getClass(String p) {
		return (WarClass) s2c.get(p);
	}

	public WarRank getNextRank(WarRank warRank) {
		if (ranks.size() <= warRank.curRank) {
			return null;
		}
		return (WarRank) ranks.get(warRank.curRank);
	}

	public boolean enterClass(final Player p) {
		if ((permission != null) && (!p.hasPermission(permission))) {
			p.sendMessage(ChatColor.RED
					+ "Do not have permission to join this class");
			return false;
		}
		for (int i = ranks.size() - 1; i > 0; i--) {
			if (p.hasPermission(((WarRank) ranks.get(i)).defaultStart)) {
				final int ranknum = i;
				Bukkit.getScheduler().runTaskLater(RCWars.returnPlugin(),
						new Runnable() {
							public void run() {
								((WarRank) ranks.get(ranknum)).addPlayer(p);
								p.setExp(0.0F);
								p.setLevel(((WarRank) ranks.get(ranknum)).levelRequirement);
							}
						}, 1L);
				WarRank.removeAbility(p);
				WarRank.setAbility(p.getName(), new None());
				return true;
			}
		}
		Bukkit.getScheduler().runTaskLater(RCWars.returnPlugin(),
				new Runnable() {
					public void run() {
						((WarRank) ranks.get(0)).addPlayer(p);
						p.setLevel(0);
						p.setExp(0.0F);
					}
				}, 1L);

		WarRank.removeAbility(p);
		WarRank.setAbility(p.getName(), new None());
		return true;
	}

	public void resetRank(Player p) {
		enterClass(p);
	}

	public static WarClass getClass(Player p) {
		return getClass(p.getName());
	}

	public static void dealWithSign(Player p, Sign state, PlayerInteractEvent e) {
		WarClass w = getClass(state.getLine(1));
		if (w == null) {
			p.sendMessage("Class not found");
			return;
		}
		w.enterClass(p);
	}

	public static void listClasses(CommandSender sender) {
		for (WarClass c : s2c.values()) {
			sender.sendMessage("internal " + c.className);
			sender.sendMessage("display " + c.displayName);
			sender.sendMessage("required perm " + c.permission);
			sender.sendMessage("valid " + c.valid);
			sender.sendMessage("Ranks: ");
			for (WarRank wr : c.ranks) {
				sender.sendMessage("rank num " + wr.curRank);
				sender.sendMessage("rank internal " + wr.name);
				sender.sendMessage("rank display " + wr.display);
				sender.sendMessage("Start perm " + wr.defaultStart);
				sender.sendMessage("Permission to join " + wr.reqPermission);
				for (ItemStack i : wr.armor)
					sender.sendMessage(i.toString());
				sender.sendMessage(wr.commands.toString());
				sender.sendMessage("is valid " + wr.valid);
			}
		}
	}
}