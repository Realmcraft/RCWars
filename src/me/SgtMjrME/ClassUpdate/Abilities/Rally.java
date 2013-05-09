package me.SgtMjrME.ClassUpdate.Abilities;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class Rally extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;

	public Rally(long long1, String string, int i, String string2) {
		delay = long1;
		disp = string;
		cost = i;
		desc = string2;
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
}