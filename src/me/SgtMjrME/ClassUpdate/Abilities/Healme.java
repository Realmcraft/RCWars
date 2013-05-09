package me.SgtMjrME.ClassUpdate.Abilities;

import me.SgtMjrME.ClassUpdate.WarRank;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Healme extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;

	public Healme(long long1, String string, int i, String string2) {
		delay = long1;
		cost = i;
		disp = string;
		desc = string2;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		WarRank wr = WarRank.getPlayer(p);
		if (wr == null)
			return false;
		PotionEffect pot = new PotionEffect(PotionEffectType.REGENERATION, 10,
				wr.power, true);
		p.addPotionEffect(pot);
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