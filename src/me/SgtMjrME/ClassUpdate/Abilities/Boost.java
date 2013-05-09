package me.SgtMjrME.ClassUpdate.Abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Boost extends BaseAbility {
	PotionEffect pot = new PotionEffect(PotionEffectType.SPEED, 200, 1);
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;

	public Boost(long long1, String string, int i, String d) {
		delay = long1;
		disp = string;
		cost = i;
		desc = d;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		e.getPlayer().addPotionEffect(pot);
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