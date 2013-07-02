package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Fireball extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final ItemStack item;

	public Fireball(ConfigurationSection cs) {
		disp = ChatColor.translateAlternateColorCodes('&', cs.getString("display", "fireball"));
		cost = cs.getInt("cost", 0);
		delay = cs.getLong("delay", 5000);
		desc = ChatColor.translateAlternateColorCodes('&', cs.getString("description", "Launches a fireball"));
		item = new ItemStack(cs.getInt("item"), 1, (short) cs.getInt("data"));
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


	public boolean onAttack(Player p, EntityDamageByEntityEvent e) {
		return false;
	}

	@Override
	public boolean OverrideAtt(Player p) {
		return true;
	}

	public boolean onDefend(Player p, EntityDamageByEntityEvent e) {
		if ((e.getEntity() instanceof org.bukkit.entity.Fireball)) {
			e.setCancelled(true);
		}
		return false;
	}

	@Override
	public boolean OverrideDef(Player p) {
		return true;
	}
	
	@Override
	public boolean OverrideTnt(Player p){
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


	@Override
	public ItemStack getItem() {
		return item;
	}
}