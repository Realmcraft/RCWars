package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.List;

import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class HealPlayer extends BaseAbility {
	PotionEffect pot = new PotionEffect(PotionEffectType.REGENERATION, 10, 4);
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final int exp;

	public HealPlayer(long long1, String string, int i, String string2, int e) {
		delay = long1;
		disp = string;
		cost = i;
		desc = string2;
		exp = e;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		List<Block> it = p.getLineOfSight(null, 20);
		List<Entity> ent = p.getNearbyEntities(20.0D, 20.0D, 20.0D);
		Player target = null;
		for (Block targetBlock : it) {
			Location blockLoc = targetBlock.getLocation();
			double bx = blockLoc.getX();
			double by = blockLoc.getY();
			double bz = blockLoc.getZ();
			for (Entity entity : ent) {
				Location loc = entity.getLocation();
				double ex = loc.getX();
				double ey = loc.getY();
				double ez = loc.getZ();
				if ((bx - 1.5D <= ex) && (ex <= bx + 2.0D) && (bz - 1.5D <= ez)
						&& (ez <= bz + 2.0D) && (by - 1.0D <= ey)
						&& (ey <= by + 2.5D) && ((entity instanceof Player)))
					target = (Player) entity;
			}
		}

		if (target == null)
			return false;
		Race dmgeer = WarPlayers.getRace(target);
		Race dmgerr = WarPlayers.getRace(p);

		if ((dmgeer == null) || (dmgerr == null) || (!dmgeer.equals(dmgerr)))
			return false;
		target.addPotionEffect(pot);
		p.giveExp(exp);
		p.sendMessage("Effect added");
		Vector diff = target.getLocation().toVector()
				.subtract(p.getLocation().toVector());
		diff.normalize();
		Location curLoc = p.getEyeLocation();
		for (int i = 0; i < p.getLocation().distance(target.getLocation()); i++) {
			curLoc.add(diff);
			p.getWorld().playEffect(curLoc, Effect.ENDER_SIGNAL, 0);
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