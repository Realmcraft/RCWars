package me.SgtMjrME.ClassUpdate.Abilities;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class Fireball extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;

	public Fireball(long long1, String string, int i, String string2) {
		delay = long1;
		disp = string;
		cost = i;
		desc = string2;
	}

	public boolean onAttack(Player p, EntityDamageByEntityEvent e) {
		return false;
	}

	public boolean OverrideAtt(Player p) {
		return true;
	}

	public boolean onDefend(Player p, EntityDamageByEntityEvent e) {
		if ((e.getEntity() instanceof org.bukkit.entity.Fireball)) {
			e.setCancelled(true);
		}
		return false;
	}

	public boolean OverrideDef(Player p) {
		return true;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		org.bukkit.entity.Fireball f = (org.bukkit.entity.Fireball) p
				.getWorld()
				.spawnEntity(p.getEyeLocation(), EntityType.FIREBALL);
		Location end = p.getTargetBlock(null, 50).getLocation();
		Location begin = p.getEyeLocation();
		Vector dir = end.toVector().subtract(begin.toVector());
		f.setDirection(dir);
		f.setShooter(p);
		f.setYield(2.0F);
		f.setIsIncendiary(true);
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