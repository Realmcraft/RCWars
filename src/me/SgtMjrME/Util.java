package me.SgtMjrME;

import me.SgtMjrME.Object.Race;

import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class Util {
	public static int toInt(String s) {
		return Integer.parseInt(s);
	}

	public static ItemStack str2Item(String s) {
		if (s.startsWith("0")) {
			return null;
		}

		String[] items = s.split(" ");
		int type = toInt(items[0]);
		int amt = toInt(items[1]);
		short dur = (short) toInt(items[2]);

		ItemStack setItem = new ItemStack(type, amt, dur);
		if ((type > 298) && (type < 302)) {
			LeatherArmorMeta meta = (LeatherArmorMeta) setItem.getItemMeta();
			String[] color = items[3].split(";");
			if (color.length == 3)
				try {
					meta.setColor(Color.fromRGB(toInt(color[0]),
							toInt(color[1]), toInt(color[2])));
					setItem.setItemMeta(meta);
				} catch (Exception localException) {
				}
		}
		if ((items.length > 4) && (items.length % 2 == 0)) {
			if (setItem.getTypeId() == 403) {
				for (int x = 4; x < items.length; x += 2)
					((EnchantmentStorageMeta) setItem.getItemMeta())
							.addStoredEnchant(
									Enchantment.getById(Race.toInt(items[x])),
									Race.toInt(items[(x + 1)]), true);
			} else {
				for (int x = 4; x < items.length; x += 2) {
					setItem.addUnsafeEnchantment(
							Enchantment.getById(Race.toInt(items[x])),
							Race.toInt(items[(x + 1)]));
				}
			}
		}
		return setItem;
	}
}