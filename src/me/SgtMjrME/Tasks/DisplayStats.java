package me.SgtMjrME.Tasks;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DisplayStats implements Runnable{
	
	private Player p;

	public DisplayStats(Player p){
		this.p = p;
	}
	
	private void display(int[] out){
		if (out == null) {
			Util.sendMessage(p, ChatColor.RED
					+ "Data not found in database");
			return;
		}
		Util.sendMessage(p, ChatColor.GOLD
				+ "War Stats for this month", false);
		Util.sendMessage(p, ChatColor.GREEN + "Kills: " + out[0]
				+ "  Deaths: " + out[1], false);
		if (out[1] != 0) {
			Util.sendMessage(p, ChatColor.GREEN + "K/D: " + out[0]
					/ out[1], false);
		}
		Util.sendMessage(p, ChatColor.GREEN + "WarPoints: " + out[2], false);
	}
	
	@Override
	public void run() {
		final int[] out = RCWars.returnPlugin().mysql.getStats(p.getName());
		Bukkit.getScheduler().runTask(RCWars.returnPlugin(), new Runnable(){
			@Override
			public void run() {
				display(out);
			}
		});
	}
}
