package me.SgtMjrME.ClassUpdate.Abilities;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class FireArrow extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;

	public FireArrow(long long1, String string, int i, String string2) {
		delay = long1;
		disp = string;
		cost = i;
		desc = string2;
	}

	public boolean onAttack(Player p, EntityDamageByEntityEvent e) {
		if (((e.getDamager() instanceof Arrow))
				&& (e.getDamager().getFireTicks() > 0)) {
			e.getEntity().setFireTicks(100);
		}
		return false;
	}

	public boolean onLaunch(ProjectileLaunchEvent e) {
		if ((e.getEntity() instanceof Arrow)) {
			((Arrow) e.getEntity()).setFireTicks(600);
		}
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