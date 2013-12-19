package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class Poison extends BaseAbility {

	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final ItemStack item;
	public int duration;
	public int amplifier;

	public Poison(ConfigurationSection cs) {
		disp = ChatColor.translateAlternateColorCodes('&', cs.getString("display", "Poison"));
		cost = cs.getInt("cost", 0);
		delay = cs.getLong("delay", 30000);
		desc = ChatColor.translateAlternateColorCodes('&', cs.getString("description", "Poison"));
		item = new ItemStack(cs.getInt("item"), 1,
				(short) cs.getInt("data"));
		duration = cs.getInt("duration", 5);
		amplifier = cs.getInt("power",1);
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
		ThrownPotion pot = e.getPlayer().launchProjectile(ThrownPotion.class);
		pot.getEffects().add(PotionEffectType.POISON.createEffect(duration, amplifier));
		Potion potion = new Potion(PotionType.INSTANT_DAMAGE, amplifier);
		potion.getEffects().add(PotionEffectType.POISON.createEffect(duration, amplifier));
		pot.setItem(potion.toItemStack(1));
//		pot.getEffects().add(PotionEffectType.POISON.createEffect(duration,  amplifier));
//		pot.setItem(new Potion(PotionType.INSTANT_DAMAGE, amplifier).toItemStack(1));
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
