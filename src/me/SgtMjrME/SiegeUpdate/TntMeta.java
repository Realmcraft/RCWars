package me.SgtMjrME.SiegeUpdate;

import me.SgtMjrME.RCWars;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class TntMeta implements MetadataValue {
	Player r;

	public TntMeta(Player race) {
		r = race;
	}

	public boolean asBoolean() {
		return false;
	}

	public byte asByte() {
		return 0;
	}

	public double asDouble() {
		return 0.0D;
	}

	public float asFloat() {
		return 0.0F;
	}

	public int asInt() {
		return 0;
	}

	public long asLong() {
		return 0L;
	}

	public short asShort() {
		return 0;
	}

	public String asString() {
		return null;
	}

	public Plugin getOwningPlugin() {
		return RCWars.returnPlugin();
	}

	public void invalidate() {
		r = null;
	}

	public Object value() {
		return r;
	}
}