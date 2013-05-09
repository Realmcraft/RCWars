package me.SgtMjrME.Object;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import me.SgtMjrME.RCWars;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit {
	private String name;
	private ArrayList<ItemStack> items = new ArrayList<ItemStack>();
	public static HashMap<String, Kit> kits = new HashMap<String, Kit>();

	public static void loadKits(RCWars pl) {
		File f = new File(pl.getDataFolder().getAbsolutePath() + "/Kits");
		String[] files = f.list();
		for (String s : files) {
			if (s.endsWith(".txt")) {
				f = new File(pl.getDataFolder().getAbsolutePath() + "/Kits/"
						+ s);
				try {
					kits.put(s.substring(0, s.length() - 4).toLowerCase(),
							new Kit(f));
				} catch (Exception localException) {
				}
			}
		}
	}

	private int ti(String s) {
		return Integer.parseInt(s);
	}

	Kit(File f) throws IOException {
		name = f.getName().substring(0, f.getName().length() - 4);
		BufferedReader in = new BufferedReader(new FileReader(f));
		String input;
		while ((input = in.readLine()) != null) {
			String[] split = input.split(" ");
			int typeid = ti(split[0]);
			int amt = ti(split[1]);
			short dmg = (short) ti(split[2]);
			ItemStack i = new ItemStack(typeid, amt, dmg);
			items.add(i);
		}
		in.close();
	}

	public String getName() {
		return name;
	}

	public void addKit(Player p) {
		for (ItemStack item : items)
			p.getInventory().addItem(new ItemStack[] { item.clone() });
	}

	public static Kit getKit(String name) {
		if (name == null)
			return null;
		return (Kit) kits.get(name.toLowerCase());
	}

	public static void listKits(Player p) {
		for (Kit k : kits.values()) {
			p.sendMessage(k.getName());
			for (ItemStack item : k.items) {
				p.sendMessage(item.getType().toString() + " amt "
						+ item.getAmount());
			}
			p.sendMessage("~~~~~~~~~~~~~~~~~~~~~~~~");
		}
	}
}