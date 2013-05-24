package me.SgtMjrME.ClassUpdate.Abilities;

import org.bukkit.inventory.ItemStack;

public class None extends BaseAbility {
	public final String disp = "none";
	public final long delay = 0L;
	public final int cost = 0;
	public final String desc = "No ability selected";

	public String getDisplay() {
		return "none";
	}

	public long getDelay() {
		return 0L;
	}

	public int getCost() {
		return 0;
	}

	public String getDesc() {
		return "No ability selected";
	}

	@Override
	public ItemStack getItem() {
		return null;
	}
}