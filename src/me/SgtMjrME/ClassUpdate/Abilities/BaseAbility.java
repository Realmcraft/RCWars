package me.SgtMjrME.ClassUpdate.Abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public abstract class BaseAbility {
	public boolean onAttack(Player p, EntityDamageByEntityEvent e) {
		return false;
	}

	public boolean onDefend(Player p, EntityDamageByEntityEvent e) {
		return false;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		return false;
	}

	public boolean onJoin(Player p, PlayerJoinEvent e) {
		return false;
	}

	public boolean onLeave(Player p, PlayerQuitEvent e) {
		return false;
	}

	public boolean onLaunch(ProjectileLaunchEvent e) {
		return false;
	}

	public boolean onTeleport(PlayerTeleportEvent e) {
		if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL))
			e.setCancelled(true);
		return false;
	}

	public boolean onExplode(EntityExplodeEvent e) {
		return false;
	}

	public abstract String getDisplay();

	public abstract long getDelay();

	public abstract int getCost();

	public abstract String getDesc();

	public boolean OverrideAtt(Player p) {
		return false;
	}

	public boolean OverrideDef(Player p) {
		return false;
	}

	public boolean OverrideInt(Player p) {
		return false;
	}

	public boolean OverrideTpt(Player p) {
		return false;
	}

	public boolean OverrideTnt(Player p) {
		return false;
	}

	public void clearAffects(Player player) {
	}
}