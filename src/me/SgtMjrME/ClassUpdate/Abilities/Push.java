package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
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
	
	public Push(ConfigurationSection cs) {
		disp = ChatColor.translateAlternateColorCodes('&', cs.getString("display", "Push"));
		cost = cs.getInt("cost", 0);
		delay = cs.getLong("delay", 30000);
		desc = ChatColor.translateAlternateColorCodes('&', cs.getString("description", "Pushes the opponent"));
		item = new ItemStack(cs.getInt("item"), 1,
				(short) cs.getInt("data"));
		strength = cs.getDouble("power",1);
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
		List<Entity> enemy = p.getNearbyEntities(3, 3, 3);
		for(Entity ent : enemy){
			Vector base = ent.getLocation().subtract(p.getLocation()).toVector().normalize();
			base.setY((base.getX() + base.getZ())/2);//Average of x and y? Sure, why not.
			ent.setVelocity(base.multiply(strength));
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

	@Override
	public ItemStack getItem() {
		return item;
	}

}
