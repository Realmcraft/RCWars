package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import me.SgtMjrME.RCWars;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Volley extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final ItemStack item;

	public Volley(ConfigurationSection cs) {
		disp = ChatColor.translateAlternateColorCodes('&', cs.getString("display", "volley"));
		cost = cs.getInt("cost", 3);
		delay = cs.getLong("delay", 10000);
		desc = ChatColor.translateAlternateColorCodes('&', cs.getString("description", "(3 wp) Launches a volley of arrows"));
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

	public boolean onInteract(Player p, PlayerInteractEvent e) {
		Vector direction = p.getLocation().getDirection();
		Location spawn = p.getEyeLocation().toVector().add(direction)
				.toLocation(p.getWorld());
		for (int i = 0; i < 10; i++) {
			Arrow a = RCWars.returnPlugin().getWarWorld()
					.spawnArrow(spawn, direction, 1.5F, 16.0F);
			a.setShooter(p);
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