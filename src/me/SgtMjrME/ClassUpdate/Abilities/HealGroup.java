package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.Iterator;

import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HealGroup extends BaseAbility {
	PotionEffect pot;
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final int exp;

	HealGroup(long delay, int dur, String string, String string2, int i, int e) {
		this.delay = delay;
		pot = new PotionEffect(PotionEffectType.REGENERATION, dur, 2);
		disp = string;
		cost = i;
		desc = string2;
		exp = e;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		Iterator<Entity> players = p.getNearbyEntities(4.0D, 4.0D, 3.0D)
				.iterator();
		Race cmp = WarPlayers.getRace(p);
		if (cmp == null)
			return false;
		while (players.hasNext()) {
			Entity ent = (Entity) players.next();
			if ((ent instanceof Player)) {
				Race r = WarPlayers.getRace((Player) ent);
				if (cmp.equals(r)) {
					((Player) ent).addPotionEffect(pot);
					p.giveExp(exp);
				}
			}
		}
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