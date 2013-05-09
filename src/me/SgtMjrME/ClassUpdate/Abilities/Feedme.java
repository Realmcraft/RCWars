package me.SgtMjrME.ClassUpdate.Abilities;

import me.SgtMjrME.ClassUpdate.WarRank;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class Feedme extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	private final String desc;

	public Feedme(long long1, String string, int i, String string2) {
		disp = string;
		delay = long1;
		cost = i;
		desc = string2;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		int pow = 1;
		WarRank wr = WarRank.getPlayer(p);
		if (wr != null)
			pow = wr.power;
		p.setFoodLevel(p.getFoodLevel() + pow);

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