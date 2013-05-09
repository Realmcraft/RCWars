package me.SgtMjrME.Tasks;

import me.SgtMjrME.RCWars;

public class gateCheck implements Runnable {
	private final RCWars p;

	public gateCheck(RCWars plugin) {
		p = plugin;
	}

	public void run() {
		p.switchGates();
	}
}