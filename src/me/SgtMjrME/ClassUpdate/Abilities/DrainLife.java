package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.HashSet;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.Bukkit;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class DrainLife extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	private final String desc;
	private HashSet<String> fired = new HashSet<String>();

	public DrainLife(long long1, String string, int i, String string2) {
		delay = long1;
		disp = string;
		cost = i;
		desc = string2;
	}

	public boolean onAttack(Player p, EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player))
			return false;
		if (!(e.getDamager() instanceof Player)) {
			if (!(e.getDamager() instanceof EnderPearl))
				return false;

			e.setDamage(e.getDamage() + 4);
			LivingEntity pl = ((EnderPearl) e.getDamager()).getShooter();
			pl.setHealth(pl.getHealth() + 2 > 20 ? 20 : pl.getHealth() + 2);
			return false;
		}
		Race r = WarPlayers.getRace(p);
		Race w = WarPlayers.getRace((Player) e.getEntity());
		if ((r == null) || (w == null))
			return false;
		if (r.equals(w))
			return false;
		e.setDamage(e.getDamage() + 6);
		p.setHealth(p.getHealth() + 2 > 20 ? 20 : p.getHealth() + 2);
		return false;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		EnderPearl s = (EnderPearl) p.launchProjectile(EnderPearl.class);
		s.setShooter(p);

		fired.add(p.getName());
		return true;
	}

	public boolean OverrideAtt(Player p) {
		return fired.contains(p.getName());
	}

	public boolean OverrideTpt(Player p) {
		return fired.contains(p.getName());
	}

	public boolean onTeleport(PlayerTeleportEvent e) {
		if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL))
			return false;
		e.setCancelled(true);
		final PlayerTeleportEvent out = e;
		Bukkit.getScheduler().runTaskLater(RCWars.returnPlugin(),
				new Runnable() {
					public void run() {
						fired.remove(out.getPlayer().getName());
					}
				}, 1L);
		return false;
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