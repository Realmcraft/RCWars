package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Grenade extends BaseAbility {

	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final ItemStack item;
	public final int power;

	public Grenade(ConfigurationSection cs) {
		disp = ChatColor.translateAlternateColorCodes('&', cs.getString("display", "Grenade"));
		cost = cs.getInt("cost", 0);
		delay = cs.getLong("delay", 30000);
		desc = ChatColor.translateAlternateColorCodes('&', cs.getString("description", "Throws a grenade"));
		item = new ItemStack(cs.getInt("item"), 1,
				(short) cs.getInt("data"));
		power = cs.getInt("power", 1);
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
		//This is going to seem stupid, but f*** it
		Snowball pot = p.launchProjectile(Snowball.class);
		Vector vel = pot.getVelocity();
		pot.remove();
		TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.PRIMED_TNT);
		tnt.setVelocity(vel);
		tnt.setFuseTicks(60);
		tnt.setIsIncendiary(false);
		tnt.setYield(power);
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
