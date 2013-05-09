package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.Iterator;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Cloak extends BaseAbility {
	public static ArrayList<String> cloaked = new ArrayList<String>();
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;

	public Cloak(long long1, String string, int i, String string2) {
		delay = long1;
		disp = string;
		cost = i;
		desc = string2;
	}

	public boolean onAttack(Player p, EntityDamageByEntityEvent e) {
		removeCloak(p);
		e.setCancelled(true);
		return false;
	}

	public boolean onJoin(Player p, PlayerJoinEvent e) {
		return false;
	}

	public boolean onLeave(Player p, PlayerQuitEvent e) {
		seeAll(p);
		return true;
	}

	private static void seeAll(Player player) {
		Iterator<String> pl = WarPlayers.listPlayers();
		while (pl.hasNext())
			try {
				player.showPlayer(Bukkit.getPlayer((String) pl.next()));
			} catch (Exception localException) {
			}
	}

	private void cloakPlayer(final Player p) {
		Iterator<String> pl = WarPlayers.listPlayers();
		while (pl.hasNext())
			try {
				Player playa = Bukkit.getPlayer((String) pl.next());
				if (playa != null) {
					Race r = WarPlayers.getRace(playa);
					if (!r.isRef())
						playa.hidePlayer(p);
				}
			} catch (Exception localException) {
			}

		RCWars.returnPlugin().getServer().getScheduler()
				.runTaskLater(RCWars.returnPlugin(), new Runnable() {
					public void run() {
						Cloak.this.uncloakPlayer(p);
					}
				}, 400L);
		p.sendMessage(ChatColor.GRAY + "You have been cloaked");
	}

	private void uncloakPlayer(Player p) {
		if (!cloaked.contains(p.getName()))
			return;
		Iterator<String> pl = WarPlayers.listPlayers();
		while (pl.hasNext())
			try {
				Player playa = Bukkit.getPlayer((String) pl.next());
				if (playa != null)
					playa.showPlayer(p);
			} catch (Exception localException) {
			}
		cloaked.remove(p.getName());
		p.sendMessage(ChatColor.WHITE + "You have been uncloaked");
	}

	private void removeCloak(Player p) {
		uncloakPlayer(p);
	}

	public boolean onDefend(Player p, EntityDamageByEntityEvent e) {
		removeCloak(p);
		e.setCancelled(true);
		return false;
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		if (cloaked.contains(e.getPlayer().getName())) {
			removeCloak(p);
			e.setCancelled(true);
		} else {
			addCloak(p);
		}
		return true;
	}

	private void addCloak(Player p) {
		cloakPlayer(p);
		cloaked.add(p.getName());
	}

	public static void applyEffects(Player player) {
		Race r;
		if (cloaked.contains(player.getName())) {
			Iterator<String> pl = WarPlayers.listPlayers();
			while (pl.hasNext()) {
				Player playa = Bukkit.getPlayer((String) pl.next());
				if (playa == null)
					return;
				r = WarPlayers.getRace(playa);
				if ((r != null) && (!r.isRef()))
					playa.hidePlayer(player);
				else
					playa.showPlayer(player);
			}
		} else {
			Iterator<String> pl = WarPlayers.listPlayers();
			while (pl.hasNext()) {
				Player playa = Bukkit.getPlayer((String) pl.next());
				if (playa == null)
					return;
				r = WarPlayers.getRace(playa);
				if ((r != null) && (!r.isRef()))
					playa.showPlayer(player);
				else
					playa.showPlayer(player);
			}
		}
		r = WarPlayers.getRace(player);
		seeAll(player);
		if ((r != null) && (r.isRef()))
			return;
		for (String s : cloaked) {
			Player playa = Bukkit.getPlayer(s);
			if (playa != null)
				player.hidePlayer(playa);
		}
	}

	public boolean onTeleport(PlayerTeleportEvent e) {
		applyEffects(e.getPlayer());
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

	public void clearAffects(Player p) {
		uncloakPlayer(p);
	}

	public boolean OverrideAtt(Player p) {
		return cloaked.contains(p.getName());
	}

	public boolean OverrideDef(Player p) {
		return cloaked.contains(p.getName());
	}

	public boolean OverrideInt(Player p) {
		return cloaked.contains(p.getName());
	}

	public String getDesc() {
		return desc;
	}
}