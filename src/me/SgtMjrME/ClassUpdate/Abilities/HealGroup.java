package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HealGroup extends BaseAbility {
	PotionEffect pot;
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final int exp;
	public final ItemStack item;

	public HealGroup(ConfigurationSection cs) {
		pot = new PotionEffect(PotionEffectType.REGENERATION, 
				cs.getInt("pow", 3), 2);
		exp = cs.getInt("exp", 2);
		
		disp = cs.getString("display", "healgroup");
		cost = cs.getInt("cost", 3);
		delay = cs.getLong("delay", 20000);
		desc = cs.getString("description", "(3 wp) Heals your surrounding allies");
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
		Iterator<Entity> players = p.getNearbyEntities(4.0D, 4.0D, 3.0D)
				.iterator();
		Race cmp = WarPlayers.getRace(p);
		if (cmp == null)
			return false;
		while (players.hasNext()) {
			Entity ent = (Entity) players.next();
			if ((ent instanceof Player)) {
				Race r = WarPlayers.getRace((Player) ent);
				if (cmp.equals(r)) {
					((Player) ent).addPotionEffect(pot);
					p.giveExp(exp);
				}
			}
		}
		p.addPotionEffect(pot);
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