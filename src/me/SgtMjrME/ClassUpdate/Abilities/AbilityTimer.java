package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.ClassUpdate.WarRank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class AbilityTimer {
	private static HashMap<String, HashSet<cooldown>> times = new HashMap<String, HashSet<cooldown>>();

	public static final HashMap<String, BaseAbility> str2abil = new HashMap<String, BaseAbility>();

	public AbilityTimer(Configuration cs) {
		str2abil.put(
				cs.getString("boost.display"),
				new Boost(cs.getLong("boost.delay"), cs
						.getString("boost.display"), cs.getInt("boost.cost"),
						cs.getString("boost.description")));
		str2abil.put(
				cs.getString("cloak.display"),
				new Cloak(cs.getLong("cloak.delay"), cs
						.getString("cloak.display"), cs.getInt("cloak.cost"),
						cs.getString("cloak.description")));
		str2abil.put(
				cs.getString("drainlife.display"),
				new DrainLife(cs.getLong("drainlife.delay"), cs
						.getString("drainlife.display"), cs
						.getInt("drainlife.cost"), cs
						.getString("drainlife.description")));
		str2abil.put(
				cs.getString("feedme.display"),
				new Feedme(cs.getLong("feedme.delay"), cs
						.getString("feedme.display"), cs.getInt("feedme.cost"),
						cs.getString("feedme.description")));
		str2abil.put(
				cs.getString("firearrow.display"),
				new FireArrow(cs.getLong("firearrow.delay"), cs
						.getString("firearrow.display"), cs
						.getInt("firearrow.cost"), cs
						.getString("firearrow.description")));
		str2abil.put(
				cs.getString("fireball.display"),
				new Fireball(cs.getLong("fireball.delay"), cs
						.getString("fireball.display"), cs
						.getInt("fireball.cost"), cs
						.getString("fireball.description")));
		str2abil.put(
				cs.getString("firenova.display"),
				new Firenova(cs.getLong("firenova.delay"), cs
						.getString("firenova.display"), cs
						.getInt("firenova.cost"), cs
						.getString("firenova.description")));
		str2abil.put(
				cs.getString("firestorm.display"),
				new FireStorm(cs.getLong("firestorm.delay"), cs
						.getString("firestorm.display"), cs
						.getInt("firestorm.cost"), cs
						.getString("firestorm.description")));
		str2abil.put(
				cs.getString("healgroup.display"),
				new HealGroup(cs.getLong("healgroup.delay"), cs
						.getInt("healgroup.pow"), cs
						.getString("healgroup.display"), cs
						.getString("healgroup.description"), cs
						.getInt("healgroup.cost"), cs
						.getInt("healgroup.exp", 2)));
		str2abil.put(
				cs.getString("healme.display"),
				new Healme(cs.getLong("healme.delay"), cs
						.getString("healme.display"), cs.getInt("healme.cost"),
						cs.getString("healme.description")));
		str2abil.put(
				cs.getString("healplayer.display"),
				new HealPlayer(cs.getLong("healplayer.delay"), cs
						.getString("healplayer.display"), cs
						.getInt("healplayer.cost"), cs
						.getString("healplayer.description"), cs.getInt(
						"healplayer.exp", 5)));
		str2abil.put("none", new None());
		str2abil.put(
				cs.getString("rally.display"),
				new Rally(cs.getLong("rally.delay"), cs
						.getString("rally.display"), cs.getInt("rally.cost"),
						cs.getString("rally.description")));
		str2abil.put(cs.getString("sap.display"),
				new Sap(cs.getLong("sap.delay"), cs.getString("sap.display"),
						cs.getInt("sap.cost"), cs.getString("sap.description")));
		str2abil.put(
				cs.getString("strike.display"),
				new Strike(cs.getLong("strike.delay"), cs
						.getString("strike.display"), cs.getInt("strike.cost"),
						cs.getString("strike.description")));
		str2abil.put(
				cs.getString("volley.display"),
				new Volley(cs.getLong("volley.delay"), cs
						.getString("volley.display"), cs.getInt("volley.cost"),
						cs.getString("volley.description")));
	}

	public static void addCooldown(Player p, BaseAbility b) {
		HashSet<cooldown> cdtimer = times.get(p.getName());
		if (cdtimer == null) {
			times.put(p.getName(), new HashSet<cooldown>());
			cdtimer = times.get(p.getName());
		}
		Iterator<cooldown> cd = cdtimer.iterator();
		while (cd.hasNext())
			if (((cooldown) cd.next()).a.equals(b))
				cd.remove();
		cdtimer.add(new cooldown(b));
		times.put(p.getName(), cdtimer);
		final Player pl = p;
		final BaseAbility ba = b;
		Bukkit.getScheduler().runTaskLater(RCWars.returnPlugin(),
				new Runnable() {
					public void run() {
						pl.sendMessage(ChatColor.GREEN + "Ability "
								+ ba.getDisplay() + " is ready to be used");
					}
				}, b.getDelay() / 1000L * 20L);
	}

	public static void removeAllCooldown(Player p) {
		times.remove(p.getName());
	}

	public static boolean checkTime(Player p, BaseAbility b) {
		HashSet<cooldown> cdtimer = times.get(p.getName());
		if (cdtimer == null) {
			addCooldown(p, b);
			return true;
		}

		Iterator<cooldown> i = cdtimer.iterator();
		while (i.hasNext()) {
			cooldown c = (cooldown) i.next();

			if (c.a.equals(b)) {
				boolean go = System.currentTimeMillis() - c.time.longValue() > b
						.getDelay();
				if (!go)
					p.sendMessage(ChatColor.GRAY
							+ "Ability is not ready yet ("
							+ (b.getDelay() - (System.currentTimeMillis() - c.time
									.longValue())) / 1000L + "s)");
				return System.currentTimeMillis() - c.time.longValue() > b
						.getDelay();
			}
		}

		return true;
	}

	public static void onAttack(Player p, EntityDamageByEntityEvent e) {
		BaseAbility b = WarRank.getAbility(p);

		if (b == null)
			return;
		if (!RCWars.warPointSave.containsKey(p)) {
			return;
		}
		if ((!b.OverrideAtt(p)) && (!checkTime(p, b)))
			return;

		if (b.getCost() > RCWars.getWarPoints(p).intValue()) {
			p.sendMessage(ChatColor.RED + "Not enough warpoints");
			return;
		}

		if (b.onAttack(p, e)) {
			if (b.getCost() != 0)
				RCWars.spendWarPoints(p, b.getCost());
			p.sendMessage(ChatColor.GREEN + "You have used ability "
					+ b.getDisplay());
			addCooldown(p, b);
		}
	}

	public static void onDefend(Player p, EntityDamageByEntityEvent e) {
		BaseAbility b = WarRank.getAbility(p);
		if (b == null)
			return;
		if (!RCWars.warPointSave.containsKey(p)) {
			return;
		}
		if ((!b.OverrideDef(p)) && (!checkTime(p, b)))
			return;
		if (b.getCost() > RCWars.getWarPoints(p).intValue()) {
			p.sendMessage(ChatColor.RED + "Not enough warpoints");
			return;
		}
		if (b.onDefend(p, e)) {
			if (b.getCost() != 0)
				RCWars.spendWarPoints(p, b.getCost());
			p.sendMessage(ChatColor.GREEN + "You have used ability "
					+ b.getDisplay());
			addCooldown(p, b);
		}
	}

	public static void onInteract(Player p, PlayerInteractEvent e) {
		BaseAbility b = WarRank.getAbility(p);
		if (b == null)
			return;
		if (!RCWars.warPointSave.containsKey(p)) {
			return;
		}
		if ((!b.OverrideInt(p)) && (!checkTime(p, b)))
			return;
		if (b.getCost() > RCWars.getWarPoints(p).intValue()) {
			p.sendMessage(ChatColor.RED + "Not enough warpoints");
			return;
		}
		if (b.onInteract(p, e)) {
			if (b.getCost() != 0)
				RCWars.spendWarPoints(p, b.getCost());
			p.sendMessage(ChatColor.GREEN + "You have used ability "
					+ b.getDisplay());
			addCooldown(p, b);
		}
	}

	public static void onJoin(Player p, PlayerJoinEvent e) {
		BaseAbility b = WarRank.getAbility(p);
		if (b == null)
			return;
		if (!RCWars.warPointSave.containsKey(p)) {
			return;
		}
		if (!checkTime(p, b))
			return;
		if (b.getCost() > RCWars.getWarPoints(p).intValue()) {
			p.sendMessage(ChatColor.RED + "Not enough warpoints");
			return;
		}
		if (b.onJoin(p, e))
			RCWars.spendWarPoints(p, b.getCost());
		addCooldown(p, b);
	}

	public static void onLeave(Player p, PlayerQuitEvent e) {
		BaseAbility b = WarRank.getAbility(p);
		if (b == null)
			return;
		b.onLeave(p, e);
		removeAllCooldown(p);
	}

	public static void onLaunch(ProjectileLaunchEvent e) {
		if (!(e.getEntity().getShooter() instanceof Player))
			return;
		Player p = (Player) e.getEntity().getShooter();
		BaseAbility b = WarRank.getAbility(p);
		if (b == null)
			return;
		if (!RCWars.warPointSave.containsKey(p)) {
			return;
		}
		if (!checkTime(p, b))
			return;
		if (b.getCost() > RCWars.getWarPoints(p).intValue()) {
			p.sendMessage(ChatColor.RED + "Not enough warpoints");
			return;
		}
		if (b.onLaunch(e)) {
			if (b.getCost() != 0)
				RCWars.spendWarPoints(p, b.getCost());
			p.sendMessage(ChatColor.GREEN + "You have used ability "
					+ b.getDisplay());
			addCooldown(p, b);
		}
	}

	public static void onTeleport(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		BaseAbility b = WarRank.getAbility(p);
		if (b == null)
			return;
		if (!RCWars.warPointSave.containsKey(p)) {
			return;
		}
		if ((!b.OverrideTpt(p)) && (!checkTime(p, b)))
			return;
		if (b.getCost() > RCWars.getWarPoints(p).intValue()) {
			p.sendMessage(ChatColor.RED + "Not enough warpoints");
			return;
		}
		if (b.onTeleport(e)) {
			if (b.getCost() != 0)
				RCWars.spendWarPoints(p, b.getCost());
			e.getPlayer()
					.sendMessage(
							ChatColor.GREEN + "You have used ability "
									+ b.getDisplay());
			addCooldown(p, b);
		}
	}

	public static void onExplode(EntityExplodeEvent e) {
		if (!(e.getEntity() instanceof Projectile))
			return;
		Projectile proj = (Projectile) e.getEntity();
		if (!(proj.getShooter() instanceof Player))
			return;
		Player p = (Player) proj.getShooter();
		if (p == null)
			return;
		BaseAbility b = WarRank.getAbility(p);
		if (b == null)
			return;
		if (!RCWars.warPointSave.containsKey(p)) {
			return;
		}
		if ((!b.OverrideTnt(p)) && (!checkTime(p, b)))
			return;
		if (b.getCost() > RCWars.getWarPoints(p).intValue()) {
			p.sendMessage(ChatColor.RED + "Not enough warpoints");
			return;
		}
		if (b.onExplode(e)) {
			if (b.getCost() != 0)
				RCWars.spendWarPoints(p, b.getCost());
			p.sendMessage(ChatColor.GREEN + "You have used ability "
					+ b.getDisplay());
			addCooldown(p, b);
		}
	}
}