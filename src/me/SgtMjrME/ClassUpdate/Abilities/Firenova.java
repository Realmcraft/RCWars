package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Firenova extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final ItemStack item;

	public Firenova(ConfigurationSection cs) {
		disp = ChatColor.translateAlternateColorCodes('&', cs.getString("display", "firenova"));
		cost = cs.getInt("cost", 1);
		delay = cs.getLong("delay", 20000);
		desc = ChatColor.translateAlternateColorCodes('&', cs.getString("description", "(1 WP) Launches a ring of fire around you"));
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
		Race cmp = WarPlayers.getRace(p);
		Iterator<Entity> i = p.getNearbyEntities(5.0D, 5.0D, 3.0D).iterator();
		while (i.hasNext()) {
			Entity ent = (Entity) i.next();
			if ((ent instanceof Player)) {
				Race r = WarPlayers.getRace((Player) ent);
				if (!cmp.equals(r))
					ent.setFireTicks(300);
			}
		}
		p.getWorld()
				.playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 5);
		p.getWorld().playSound(p.getLocation(), Sound.FUSE, 1.0F, 1.0F);
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