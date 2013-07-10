package me.SgtMjrME.Listeners;

import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class TagAPIListener implements Listener{

	private void applyTag(PlayerReceiveNameTagEvent e, ChatColor color) {
		if (e.getNamedPlayer().getName().length() < 15)
			e.setTag(color + e.getNamedPlayer().getName());
		else
			e.setTag(color + e.getNamedPlayer().getName().substring(0, 14));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGetTag(PlayerReceiveNameTagEvent e) {
		Race r = WarPlayers.getRace(e.getNamedPlayer());
		if (r != null) {
			if ((e.getNamedPlayer().hasPermission("rcchat.m")) && (!r.isRef())) {
				ChatColor c = r.getCcolor();
				try {
					applyTag(e, ChatColor.valueOf("DARK_" + c.name()));
					return;
				} catch (Exception localException) {
				}
			}
			applyTag(e, r.getCcolor());
		}
	}
}
