package me.SgtMjrME;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Iterator;

import me.SgtMjrME.Object.Race;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
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
	
	public static ItemStack addEnchant(ItemStack i, String s) {
		String[] split = s.split(":");
		if (split.length != 2)
			return null;
		ItemMeta item = i.getItemMeta();
		String value = split[0];
		Integer force = Integer.valueOf(Integer.parseInt(split[1]));
		try {
			if (value.equals("WATER_WORKER"))
				item.addEnchant(Enchantment.WATER_WORKER, force.intValue(), false);
			else if (value.equals("OXYGEN"))
				item.addEnchant(Enchantment.OXYGEN, force.intValue(), false);
			else if (value.equals("DIG_SPEED"))
				item.addEnchant(Enchantment.DIG_SPEED, force.intValue(), false);
			else if (value.equals("LOOT_BLOCK"))
				item.addEnchant(Enchantment.LOOT_BONUS_BLOCKS,
						force.intValue(), false);
			else if (value.equals("SILK_TOUCH"))
				item.addEnchant(Enchantment.SILK_TOUCH, force.intValue(), false);
			else if (value.equals("SHARP"))
				item.addEnchant(Enchantment.DAMAGE_ALL, force.intValue(), false);
			else if (value.equals("DMG_SPIDER"))
				item.addEnchant(Enchantment.DAMAGE_ARTHROPODS,
						force.intValue(), false);
			else if (value.equals("SMITE"))
				item.addEnchant(Enchantment.DAMAGE_UNDEAD, force.intValue(), false);
			else if (value.equals("DURABILITY"))
				item.addEnchant(Enchantment.DURABILITY, force.intValue(), false);
			else if (value.equals("FIRE_ASPECT"))
				item.addEnchant(Enchantment.FIRE_ASPECT, force.intValue(), false);
			else if (value.equals("KNOCKBACK"))
				item.addEnchant(Enchantment.KNOCKBACK, force.intValue(), false);
			else if (value.equals("LOOTING"))
				item.addEnchant(Enchantment.LOOT_BONUS_MOBS,
						force.intValue(), false);
			else if (value.equals("PROT_ALL"))
				item.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,
						force.intValue(), false);
			else if (value.equals("PROT_EXPL"))
				item.addEnchant(Enchantment.PROTECTION_EXPLOSIONS,
						force.intValue(), false);
			else if (value.equals("FEATHER_FALL"))
				item.addEnchant(Enchantment.PROTECTION_FALL,
						force.intValue(), false);
			else if (value.equals("PROT_FIRE"))
				item.addEnchant(Enchantment.PROTECTION_FIRE,
						force.intValue(), false);
			else if (value.equals("PROT_PROJ"))
				item.addEnchant(Enchantment.PROTECTION_PROJECTILE,
						force.intValue(), false);
			else if (value.equals("PROT_THORNS"))
				item.addEnchant(Enchantment.THORNS, force.intValue(), false);
			else if (value.equals("ARR_DMG"))
				item.addEnchant(Enchantment.ARROW_DAMAGE, force.intValue(), false);
			else if (value.equals("FIRE_ARROW"))
				item.addEnchant(Enchantment.ARROW_FIRE, force.intValue(), false);
			else if (value.equals("INF_ARROW"))
				item.addEnchant(Enchantment.ARROW_INFINITE,
						force.intValue(), false);
			else if (value.equals("ARR_KNOCK"))
				item.addEnchant(Enchantment.ARROW_KNOCKBACK,
						force.intValue(), false);
			i.setItemMeta(item);
		} catch (Exception e) {
			return null;
		}
		return i;
	}
	
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	public static boolean readInv(Player p, boolean inWars) throws Exception {
		int count = 0;
		try {
			if (p == null)
				return false;
			ItemStack[] allItemsBack = (ItemStack[]) p.getInventory()
					.getContents().clone();
			allItemsBack = (ItemStack[]) concat(allItemsBack, (ItemStack[]) p
					.getInventory().getArmorContents().clone());
			if (!inWars)
				savePlayer(p.getName(), "Backup", allItemsBack);
			String directory = RCWars.returnPlugin().getDataFolder()
					.getAbsolutePath();
			if (inWars)
				directory = directory + "/Items/";
			else
				directory = directory + "/WarItems/";
			File f = new File(directory + p.getName().toLowerCase() + ".txt");
			if (!f.exists()) {
				p.sendMessage("No previous war data found");
				if (!inWars)
					savePlayer(
							p.getName(),
							"Items",
							(ItemStack[]) concat(
									p.getInventory().getContents(), p
											.getInventory().getArmorContents()));
				else
					savePlayer(
							p.getName(),
							"WarItems",
							(ItemStack[]) concat(
									p.getInventory().getContents(), p
											.getInventory().getArmorContents()));
				Iterator<ItemStack> i = p.getInventory().iterator();
				while(i.hasNext()){
					if (!RCWars.allowedItems.contains(i.next().getTypeId())) i.remove(); 
				}
//				for (int i = 0; i < p.getInventory().getArmorContents().length; i++) {
					p.getInventory().setArmorContents(null);
//				}
				return false;
			}
			FileReader in = new FileReader(f);
			BufferedReader data = new BufferedReader(in);
			String s;
			while (((s = data.readLine()) != null) && (count < 40)) {
				ItemStack setItem = Util.str2Item(s);
				if (count < 36){
					if (p.getInventory().getItem(count) != null &&
							!RCWars.allowedItems.contains(p.getInventory().getItem(count).getTypeId()))
							p.getInventory().setItem(count, setItem);
				}
				else if (count == 39)
					p.getInventory().setHelmet(setItem);
				else if (count == 38)
					p.getInventory().setChestplate(setItem);
				else if (count == 37)
					p.getInventory().setLeggings(setItem);
				else
					p.getInventory().setBoots(setItem);
				count++;
			}
			data.close();
			in.close();
			if (!inWars)
				savePlayer(p.getName(), "Items", allItemsBack);
			else
				savePlayer(p.getName(), "WarItems", allItemsBack);
			return true;
		} catch (Exception e) {
			Bukkit.getLogger().info(
					"Error saving player " + p.getName() + " at " + count);
		}
		return false;
	}
	
	public static void savePlayer(String p, String subDir, ItemStack[] items) {
		try {
			String directory = RCWars.returnPlugin().getDataFolder()
					.getAbsolutePath()
					+ "/" + subDir + "/";
			if (p == null)
				return;
			FileWriter fstream = new FileWriter(directory + p.toLowerCase()
					+ ".txt");
			BufferedWriter out = new BufferedWriter(fstream);
			if (items == null) {
				for (int i = 0; i < 40; i++)
					out.write("0\n");
				out.close();
				fstream.close();
				return;
			}
			for (ItemStack tempItem : items) {
				if (tempItem == null) {
					out.write("0");
					out.newLine();
				} else {
					out.write(tempItem.getTypeId() + " " + tempItem.getAmount()
							+ " " + tempItem.getDurability() + " "
							+ tempItem.getData().getData());
					for (Enchantment e : tempItem.getEnchantments().keySet()) {
						out.write(" " + e.getId() + ' '
								+ tempItem.getEnchantmentLevel(e));
					}

					if ((tempItem.getItemMeta() instanceof EnchantmentStorageMeta)) {
						for (Enchantment tempEnchant : ((EnchantmentStorageMeta) tempItem
								.getItemMeta()).getStoredEnchants().keySet())
							out.write(" "
									+ tempEnchant.getId()
									+ ' '
									+ ((EnchantmentStorageMeta) tempItem
											.getItemMeta())
											.getStoredEnchantLevel(tempEnchant));
					}
					out.newLine();
				}
			}
			out.close();
			fstream.close();
		} catch (Exception e1) {
			if (p == null)
				return;
			Player player = Bukkit.getServer().getPlayer(p);
			if (player == null)
				return;
			player.sendMessage(ChatColor.RED
					+ "Error: Please tell a mod and refrain from re-entering wars (savePlayer)");
			System.err.println("Error: " + e1.getMessage());
		}
	}
}