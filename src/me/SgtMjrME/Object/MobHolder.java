package me.SgtMjrME.Object;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class MobHolder {
	public MobHolder(LivingEntity entity, Player player, Race race, Location loc) {
		mob = entity;
		p = player;
		r = race;
		l = loc;
	}
	public LivingEntity mob;
	public Player p;
	public Race r;
	public Location l;
}
