package me.SgtMjrME.Tasks;

import me.SgtMjrME.RCWars;

public class spawnCheck implements Runnable {
	RCWars r;

	public spawnCheck(RCWars r) {
		this.r = r;
	}

	public void run() {
		r.checkSpawn();
	}
}