package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.Iterator;

import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class Firenova extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;

	public Firenova(long long1, String string, int i, String string2) {
		delay = long1;
		disp = string;
		cost = i;
		desc = string2;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		Race cmp = WarPlayers.getRace(p);
		Iterator<Entity> i = p.getNearbyEntities(5.0D, 5.0D, 3.0D).iterator();
		while (i.hasNext()) {
			Entity ent = (Entity) i.next();
			if ((ent instanceof Player)) {
				Race r = WarPlayers.getRace((Player) ent);
				if (!cmp.equals(r))
					ent.setFireTicks(300);
			}
		}
		p.getWorld()
				.playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 5);
		p.getWorld().playSound(p.getLocation(), Sound.FUSE, 1.0F, 1.0F);
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