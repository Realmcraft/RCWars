package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Sap extends BaseAbility {
	PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 200, 1, true);
	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final ItemStack item;

	public Sap(ConfigurationSection cs) {
		disp = ChatColor.translateAlternateColorCodes('&', cs.getString("display", "sap"));
		cost = cs.getInt("cost", 0);
		delay = cs.getLong("delay", 15000);
		desc = ChatColor.translateAlternateColorCodes('&', cs.getString("description", "Slows down your target"));
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
		if (!(e.getEntity() instanceof Player))
			return false;
		Race dmgeer = WarPlayers.getRace((Player) e.getEntity());
		Race dmgerr = WarPlayers.getRace(p);
		if ((dmgeer == null) || (dmgerr == null) || (dmgeer.equals(dmgerr)))
			return false;
		((Player) e.getEntity()).addPotionEffect(slow);
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