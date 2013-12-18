package me.SgtMjrME.Object;

import java.util.UUID;

import net.citizensnpcs.api.npc.NPC;

public class NPCHolder {
	
	public NPCHolder(UUID i, Race owner, Base b2, NPC n) {
		id = i;
		r = owner;
		b = b2;
		npc = n;
	}
	
	public UUID id;
	public Race r;
	public Base b;
	public NPC npc;

}
