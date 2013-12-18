package me.SgtMjrME.ClassUpdate.Abilities;

import java.util.ArrayList;
import java.util.List;

import me.SgtMjrME.RCWars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Cobweb extends BaseAbility {

	public final String disp;
	public final long delay;
	public final int cost;
	public final String desc;
	public final ItemStack item;
	static BlockFace[] bf;

	public Cobweb(ConfigurationSection cs) {
		disp = ChatColor.translateAlternateColorCodes('&', cs.getString("display", "Cobweb"));
		cost = cs.getInt("cost", 0);
		delay = cs.getLong("delay", 30000);
		desc = ChatColor.translateAlternateColorCodes('&', cs.getString("description", "Throws a cobweb"));
		item = new ItemStack(cs.getInt("item"), 1,
				(short) cs.getInt("data"));
		bf = new BlockFace[4];
		bf[0] = BlockFace.NORTH;
		bf[1] = BlockFace.EAST;
		bf[2] = BlockFace.SOUTH;
		bf[3] = BlockFace.WEST;
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
		p.launchProjectile(Snowball.class);
		return true;
	}
	
	public static void createWeb(Location l){
		Location[] toRemove = new Location[bf.length + 1];
		for(int i = 0; i < toRemove.length; i++) toRemove[i] = null;
		if (l.getBlock().isEmpty()){
			l.getBlock().setType(Material.WEB);
			toRemove[toRemove.length - 1] = l;//Last slot
		}
		for(int i = 0; i < bf.length; i++){
			Block rel = l.getBlock().getRelative(bf[i]);
			if (rel.isEmpty()){
				rel.setType(Material.WEB);
				toRemove[i] = rel.getLocation();
			}
		}
		final Location[] out = toRemove;
		Bukkit.getScheduler().runTaskLater(RCWars.returnPlugin(), new Runnable(){
			@Override
			public void run() {
				for(int i = 0; i < out.length; i++){
					if (out[i] != null) out[i].getBlock().setType(null);
				}
			}
		}, 100);
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
