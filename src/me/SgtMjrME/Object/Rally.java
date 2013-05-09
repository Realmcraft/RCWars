package me.SgtMjrME.Object;

import org.bukkit.entity.Player;

public class Rally {
	public Race race;
	public Player p;
	public long time;

	public Rally(Player p) {
		race = WarPlayers.getRace(p);
		time = System.currentTimeMillis();
		this.p = p;
	}
}