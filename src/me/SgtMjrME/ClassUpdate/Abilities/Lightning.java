package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Lightning extends BaseAbility {

	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final ItemStack item;
	public final int distance;
	
	public Lightning(ConfigurationSection cs) {
		disp = ChatColor.translateAlternateColorCodes('&', cs.getString("display", "Lightning"));
		cost = cs.getInt("cost", 0);
		delay = cs.getLong("delay", 30000);
		desc = ChatColor.translateAlternateColorCodes('&', cs.getString("description", "Strikes lightning where you shoot"));
		item = new ItemStack(cs.getInt("item"), 1,
				(short) cs.getInt("data"));
		distance = cs.getInt("distance", 50);
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
		Location start = p.getEyeLocation();
		Vector dir = p.getLocation().getDirection().normalize();
		for(int i = 0; i < distance; i++){
			if (!start.getBlock().isEmpty()){
				//We hit a block
				p.getWorld().strikeLightning(start);
				break;
			}
			//Air... damn
			start.add(dir);
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
