package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Rally extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final ItemStack item;

	public Rally(ConfigurationSection cs) {
		disp = cs.getString("display", "rally");
		cost = cs.getInt("cost", 9);
		delay = cs.getLong("delay", 180000);
		desc = cs.getString("description", "(9 wp) Sends a rally teleport request to your team");
		item = new ItemStack(cs.getInt("item"), 1, (short) cs.getInt("data"));
		String s = cs.getString("lore", "");
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(disp);
		if (s != null && s != ""){
			List<String> lore = new ArrayList<String>();
			lore.add(s);
			im.setLore(lore);
		}
		item.setItemMeta(im);
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		Race r = WarPlayers.getRace(p);
		if (r == null)
			return false;
		RCWars.rallyDat.remove(r);
		RCWars.rallyDat.put(r, new me.SgtMjrME.Object.Rally(p));
		r.sendMessage(r.getCcolor() + p.getName() + r.getCcolor()
				+ " has asked for help! /Rally to reply to the call!");
		return true;
	}

	public String getDisplay() {
		return disp;
	}

	public long getDelay() {
		return delay;
	}

	public int getCost() {
		return cost;
	}

	public String getDesc() {
		return desc;
	}

	@Override
	public ItemStack getItem() {
		return item;
	}
}