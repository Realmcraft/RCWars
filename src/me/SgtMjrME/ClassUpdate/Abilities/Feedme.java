package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import me.SgtMjrME.ClassUpdate.WarRank;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Feedme extends BaseAbility {
	public final String disp;
	public final long delay;
	public final int cost;
	private final String desc;
	public final ItemStack item;

	public Feedme(ConfigurationSection cs) {
		disp = cs.getString("display", "feedme");
		cost = cs.getInt("cost", 3);
		delay = cs.getLong("delay", 60000);
		desc = cs.getString("description", "(3 WP) Feeds the player");
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
		int pow = 1;
		WarRank wr = WarRank.getPlayer(p);
		if (wr != null)
			pow = wr.power;
		p.setFoodLevel(p.getFoodLevel() + pow);

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