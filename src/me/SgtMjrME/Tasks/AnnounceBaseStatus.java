package me.SgtMjrME.Tasks;

import java.util.Iterator;

import org.bukkit.entity.Player;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Util;
import me.SgtMjrME.Object.WarPlayers;
import me.SgtMjrME.Object.Base;

public class AnnounceBaseStatus implements Runnable {

	@Override
	public void run() {
		Iterator<Base> bases = Base.returnBases().iterator();
		while (bases.hasNext()) {
			Base b = bases.next();
			if (!b.willDisplay()) continue;
			Iterator<String> players = WarPlayers.listPlayers();
			while (players.hasNext()) {
				String pstring = players.next();
				Player p = RCWars.returnPlugin().getServer().getPlayer(pstring);
				if (p == null) {
					WarPlayers.remove(pstring);
					continue;
				}
				Util.sendMessage(p, b.getDisp());
			}
		}
	}

}