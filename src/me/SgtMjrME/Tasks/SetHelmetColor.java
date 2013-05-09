package me.SgtMjrME.Tasks;

import me.SgtMjrME.Object.Race;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetHelmetColor implements Runnable {
	Race r;
	Player p;

	public SetHelmetColor(Race r, Player p) {
		this.r = r;
		this.p = p;
	}

	public void run() {
		byte color = 0;
		if (r != null)
			color = r.getColor().byteValue();
		ItemStack wool = new ItemStack(35, 1, color);
		p.getInventory().setHelmet(wool);
	}
}