package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import me.SgtMjrME.Util;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Push extends BaseAbility {
	
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final ItemStack item;
	public final double strength;
	boolean debug;
	
	public Push(ConfigurationSection cs) {
		disp = ChatColor.translateAlternateColorCodes('&', cs.getString("display", "Push"));
		cost = cs.getInt("cost", 0);
		delay = cs.getLong("delay", 30000);
		desc = ChatColor.translateAlternateColorCodes('&', cs.getString("description", "Pushes the opponent"));
		item = new ItemStack(cs.getInt("item"), 1,
				(short) cs.getInt("data"));
		debug = cs.getBoolean("debug");
		strength = cs.getDouble("power",5);
		String s = cs.getString("lore", "");
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(disp);
		if (s != null && s != ""){
			List<String> lore = new ArrayList<String>();
			lore.add(s);
			im.setLore(lore);
		}
		item.setItemMeta(im);
	}

	public boolean onInteract(Player p, PlayerInteractEvent e) {
//		List<Entity> enemy = p.getNearbyEntities(3, 3, 3);
//		for(Entity ent : enemy){
//			Vector base = ent.getLocation().subtract(p.getLocation()).toVector().normalize();
//			base.setY((base.getX() + base.getZ())/2);//Average of x and y? Sure, why not.
//			ent.setVelocity(base.multiply(strength));
//		}
		if (debug) Util.sendMessage(p, "Checking for player");
		List<Block> it = p.getLineOfSight(null, 40);
		List<Entity> ent = p.getNearbyEntities(40.0D, 20.0D, 40.0D);
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

		if (debug) Util.sendMessage(p, "Checking target " + ((target == null) ? "null":target.getPlayer().getName()));
		if (target == null)
			return false;
		Race dmgeer = WarPlayers.getRace(target);
		Race dmgerr = WarPlayers.getRace(p);

		if (debug) Util.sendMessage(p, "Checking race");
		if ((dmgeer == null) || (dmgerr == null) || (dmgeer.equals(dmgerr)))
			return false;
		//We hit someone
		Vector dir = p.getLocation().getDirection().normalize();
		dir.setY(1.5);
		dir.multiply(strength);
		if (debug) Util.sendMessage(p, "Applying effect of " + dir.toString());
		target.setVelocity(dir);
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

	@Override
	public ItemStack getItem() {
		return item;
	}

}
