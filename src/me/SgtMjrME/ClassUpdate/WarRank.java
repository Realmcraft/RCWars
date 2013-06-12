package me.SgtMjrME.ClassUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Util;
import me.SgtMjrME.ClassUpdate.Abilities.AbilityTimer;
import me.SgtMjrME.ClassUpdate.Abilities.BaseAbility;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;
import me.SgtMjrME.Tasks.SetHelmetColor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WarRank {
	ArrayList<BaseAbility> commands = new ArrayList<BaseAbility>();
	ItemStack[] armor;
	public final Vector<ItemStack> otherItems = new Vector<ItemStack>();
	public WarClass c;
	String WCName;
	String name;
	String display;
	int curRank = -1;
	int levelRequirement;
	String reqPermission;
	String defaultStart;
	public int power;
	public double attswdpwr;
	public double defpwr;
	public double attbowpwr;
	float spdbst;
	public boolean valid;
	public static HashMap<String, WarRank> pRank = new HashMap<String, WarRank>();
//	public static HashMap<String, BaseAbility> pAbility = new HashMap<String, BaseAbility>();

	public WarRank(ConfigurationSection cs, String name, int i, WarClass inclass) {
		try {
			WCName = name;
			curRank = i;
			c = inclass;
			this.name = cs.getString("name");
			display = cs.getString("display");
			levelRequirement = cs.getInt("requiredLevel", 0);
			reqPermission = cs.getString("reqperm", null);
			defaultStart = cs.getString("startplayerperm", null);
			attswdpwr = cs.getDouble("attackboostsword", 1.0D);
			defpwr = cs.getDouble("defboost", 1.0D);
			attbowpwr = cs.getDouble("attackboostbow", 1.0D);
			spdbst = ((float) cs.getDouble("speedboost", 0.2D));
			power = cs.getInt("power", 1);
			getCommands(cs);
			getArmor(cs);
			getOtherItems(cs);
			valid = true;
		} catch (Exception e) {
			valid = false;
		}
	}

	private void getOtherItems(ConfigurationSection cs) {
		try{
		String[] o = cs.getString("otheritems").split(";");
		for (String s : o)
			otherItems.add(Util.str2Item(s));
		}
		catch(Exception e){
			Bukkit.getLogger().warning("RCWars: Problem reading other items " + cs.toString());
		}
	}

	private void getArmor(ConfigurationSection cs) {
		try{
			ItemStack[] hold = new ItemStack[4];
			for (int i = 0; i < 4; i++) {
				String in = cs.getString("slot" + (i + 1));
				ItemStack item = Util.str2Item(in);
				hold[i] = item;
			}
			armor = hold;
		}
		catch(Exception e){
			Bukkit.getLogger().warning("RCWars: Problem reading armor items " + cs.toString());
		}
	}

	private void getCommands(ConfigurationSection cs) {
		try{
		String[] in = cs.getString("commands").split(";");
		for (String s : in) {
			BaseAbility b = AbilityTimer.str2abil.get(s);
			if (b != null)
				commands.add(b);
		}
//		commands.add(new None());
		}
		catch(Exception e){
			Bukkit.getLogger().warning("RCWars: Problem reading commands " + cs.toString());
		}
	}

	public boolean hasCommand(String s) {
		return commands.contains(s);
	}

	public void addOther(final Player p) {
		for (int i = 0; i < otherItems.size(); i++)
			p.getInventory().clear(i);
		Bukkit.getScheduler().runTaskLater(RCWars.returnPlugin(),
				new Runnable() {
					public void run() {
						for (int i = 0; i < otherItems.size(); i++) {
							p.getInventory().setItem(i,
									(ItemStack) otherItems.get(i));
						}
						int slot = p.getInventory().first(119);
						if (slot == -1)
							return;
						p.getInventory().remove(slot);
						int swordtype = 0;
						Race r = WarPlayers.getRace(p);
						if (r != null)
							swordtype = r.swordtype;
						p.getInventory().setItem(slot,
								new ItemStack(swordtype, 1, (short) 0));
					}
				}, 1L);
	}

	public void addArmor(Player p) {
		p.getInventory().setHelmet(armor[0]);
		p.getInventory().setChestplate(armor[1]);
		p.getInventory().setLeggings(armor[2]);
		p.getInventory().setBoots(armor[3]);
		Bukkit.getScheduler().runTask(RCWars.returnPlugin(),
				new SetHelmetColor(WarPlayers.getRace(p), p));
		if (RCWars.returnPlugin().kitOnSpawn != null)
			RCWars.returnPlugin().kitOnSpawn.addKit(p);
	}

	public static WarRank getPlayer(Player player) {
		return getPlayer(player.getName());
	}

	public static WarRank getPlayer(String p) {
		return (WarRank) pRank.get(p);
	}

	public WarRank nextRankLevel() {
		WarRank nextRank = c.getNextRank(this);
		if (nextRank == null)
			return null;
		return nextRank;
	}

	public int getLevel() {
		return levelRequirement;
	}

	public void rankUp(Player p) {
		leave(p);
		pRank.put(p.getName(), this);
		addArmor(p);
		addOther(p);
		addSkills(p);
		p.setWalkSpeed(spdbst);
		p.sendMessage(ChatColor.GREEN + "You are now a(n) " + display + " "
				+ c.displayName);
	}

	private void addSkills(final Player p) {
		Bukkit.getScheduler().runTaskLater(RCWars.returnPlugin(),
				new Runnable() {
					@Override
					public void run() {
						int i = 0;
						while (p.getInventory().getItem(i) != null)
							i++;
						for (int j = 0; j < commands.size(); j++)
							if (commands.get(j).getItem() != null)
								p.getInventory().setItem(i + j,
									commands.get(j).getItem());
					}
				}, 2L);
	}

	private void removeSkills(Player p) {
		// Fuck it, I'm going to remove everything in the hotbar, they can deal
		// with it later
		for (int i = 0; i < 36; i++) {
			if (p.getInventory().getItem(i) != null
					&& !RCWars.allowedItems.contains(p.getInventory()
							.getItem(i).getTypeId()))
				p.getInventory().setItem(i, null);
		}
	}

	public void addPlayer(Player p) {
		rankUp(p);
	}

	public void leave(Player p) {
		p.getInventory().setArmorContents(null);
		WarRank wr = (WarRank) pRank.remove(p.getName());
		if (wr != null)
			wr.removeOther(p);
		p.setWalkSpeed(0.2F);
	}

//	public static BaseAbility getAbility(Player p) {
//		return getAbility(p.getName());
//	}
//
//	public static BaseAbility getAbility(String s) {
//		return (BaseAbility) pAbility.get(s);
//	}
//
//	public static void removeAbility(Player p) {
//		pAbility.remove(p.getName());
//	}
//
//	public static void setAbility(String s, BaseAbility none) {
//		pAbility.put(s, none);
//	}
//
//	public void cycleAbility(Player player) {
//		if (pAbility.containsKey(player.getName())) {
//			BaseAbility curAb = (BaseAbility) pAbility.get(player.getName());
//			if (curAb == null) {
//				setAbility(player.getName(), new None());
//				player.sendMessage("Ability: None");
//				return;
//			}
//			BaseAbility curCommand = curAb;
//			for (int i = 0; i < commands.size(); i++) {
//				if (commands.get(i).getDisplay()
//						.equals(curCommand.getDisplay())) {
//					if (i >= commands.size() - 1)
//						i = 0;
//					else {
//						i++;
//					}
//					curAb.clearAffects(player);
//					BaseAbility b = (BaseAbility) AbilityTimer.str2abil
//							.get(commands.get(i));
//					setAbility(player.getName(), b);
//					if (b == null) {
//						player.sendMessage(ChatColor.GREEN
//								+ "Ability: None (ERR)");
//					} else {
//						String disp = b.getDisplay() != null ? b.getDisplay()
//								: "???";
//						String desc = b.getDesc() != null ? b.getDesc() : "???";
//						player.sendMessage(ChatColor.GREEN + "Ability: " + disp
//								+ ChatColor.GRAY + " (" + desc + ")");
//					}
//					return;
//				}
//			}
//		}
//		setAbility(player.getName(), new None());
//		player.sendMessage("Ability: None");
//	}

	public void removeOther(Player player) {
		removeSkills(player);// For now
		// for (int i = 0; i < otherItems.size(); i++)
		// player.getInventory().setItem(i, null);
	}

	public String display() {
		return c.color + "[" + c.displayName + ' ' + curRank + "]";
	}
}