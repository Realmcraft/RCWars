package me.SgtMjrME.Tasks;

import me.SgtMjrME.RCWars;

public class runCheck implements Runnable {
	private final RCWars p;

	public runCheck(RCWars plugin) {
		p = plugin;
	}

	public void run() {
		p.checkPlayerBase();
	}
}