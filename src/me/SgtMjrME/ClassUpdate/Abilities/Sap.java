package me.SgtMjrME.ClassUpdate.Abilities;

import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Sap extends BaseAbility {
	PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 200, 1, true);
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;

	Sap(long l, String string, int i, String string2) {
		delay = l;
		disp = string;
		cost = i;
		desc = string2;
	}

	public boolean onAttack(Player p, EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player))
			return false;
		Race dmgeer = WarPlayers.getRace((Player) e.getEntity());
		Race dmgerr = WarPlayers.getRace(p);
		if ((dmgeer == null) || (dmgerr == null) || (dmgeer.equals(dmgerr)))
			return false;
		((Player) e.getEntity()).addPotionEffect(slow);
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