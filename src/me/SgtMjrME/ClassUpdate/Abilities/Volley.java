package me.SgtMjrME.ClassUpdate.Abilities;

import me.SgtMjrME.RCWars;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class Volley extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;

	public Volley(long long1, String string, int i, String string2) {
		delay = long1;
		disp = string;
		cost = i;
		desc = string2;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		Vector direction = p.getLocation().getDirection();
		Location spawn = p.getEyeLocation().toVector().add(direction)
				.toLocation(p.getWorld());
		for (int i = 0; i < 10; i++) {
			Arrow a = RCWars.returnPlugin().getWarWorld()
					.spawnArrow(spawn, direction, 1.5F, 16.0F);
			a.setShooter(p);
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