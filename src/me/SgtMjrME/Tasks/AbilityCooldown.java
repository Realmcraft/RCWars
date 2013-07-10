package me.SgtMjrME.Tasks;

import me.SgtMjrME.RCWars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class AbilityCooldown extends BukkitRunnable {

	final Player p;
	final long time;
	int stage;
	final int slot;
	final ItemStack finalitem;
	final String finalItemDisp;

	public AbilityCooldown(Player p, int slot, long t) {
		this.p = p;
		this.slot = slot;
		time = t / 4 / 50;
		stage = 0;
		finalitem = p.getInventory().getItem(slot);
		ItemMeta im = finalitem.getItemMeta();
		if (im != null) finalItemDisp = ChatColor.stripColor(im.getDisplayName());
		else finalItemDisp = null;
	}
//
//	AbilityCooldown(Player p, int slot, long t, int s) {
//		this.p = p;
//		this.slot = slot;
//		time = t;
//		stage = s;
//	}

	@Override
	public void run() {
		ItemStack i = p.getInventory().getItem(slot);
		if (i == null)
			return;
		ItemMeta im = i.getItemMeta();
		if (im == null)
			return;
		String itemName = ChatColor.stripColor(im.getDisplayName());
		if (itemName == null)
			return;
		if (itemName.length() < 2) return;
		itemName = itemName.substring(1, itemName.length() - 1);
		if (stage != 0 && !itemName.equals(finalItemDisp))
			return;
		//Hopefully this means we have the correct item
		if (stage < 4) {
			if (stage == 0) {
				ItemStack newitem = new ItemStack(35,1,(short) 14);
				ItemMeta imn = newitem.getItemMeta();
				imn.setDisplayName('[' + finalitem.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.ITALIC + ']');
				newitem.setItemMeta(imn);
				p.getInventory().setItem(slot, newitem);
			} else if (stage == 1) {
				i.setDurability((short) 1);
			} else if (stage == 2) {
				i.setDurability((short) 4);
			} else if (stage == 3) {
				i.setDurability((short) 5);
			}
			stage += 1;
			Bukkit.getScheduler().runTaskLater(RCWars.returnPlugin(), this, time);
		} else {
			p.getInventory().setItem(slot, finalitem);
			return;
		}
	}

}
