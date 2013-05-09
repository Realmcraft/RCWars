package me.SgtMjrME.SiegeUpdate;

import org.bukkit.Location;

class BlockData {
	public Location l;
	public int id;
	public byte dat;

	BlockData(Location lo, int i, byte dat) {
		l = lo;
		id = i;
		this.dat = dat;
	}
}