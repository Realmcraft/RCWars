package me.SgtMjrME.Object;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import me.SgtMjrME.RCWars;

public class CitizensHandler implements Listener,Runnable{
	
	RCWars rcwars;
	
	ArrayList<NPCHolder> npcrace = new ArrayList<NPCHolder>();

	public CitizensHandler(RCWars rcWars){
		rcwars = rcWars;
		rcwars.getServer().getPluginManager().registerEvents(this, rcwars);
		rcwars.getServer().getScheduler().runTaskTimer(rcwars, this, 0, 20L);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onSpawn(NPCSpawnEvent e){
		if (e.isCancelled()) return;
		if (!e.getNPC().getStoredLocation().getWorld().equals(rcwars.getWarWorld())) return;
		Base b = getClosestBase(e.getLocation());
		e.getNPC().getDefaultGoalController().addGoal(WanderGoal.createWithNPCAndRange(e.getNPC(),5,5), 1);
		npcrace.add(new NPCHolder(e.getNPC().getBukkitEntity().getUniqueId(),b.getOwner(), b, e.getNPC()));
		Bukkit.getLogger().info("[RCWars] Currently " +npcrace.size() + " mobs spawned");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onNPCDeath(NPCDeathEvent e){
		UUID id = e.getNPC().getBukkitEntity().getUniqueId();
		for(NPCHolder n : npcrace){
			if (id.equals(n.id)){
				npcrace.remove(id);
				return;
			}
		}
	}

	private Base getClosestBase(Location location) {
		int i = Integer.MAX_VALUE;
		Base out = null;
		int dist = 0;
		for(Base b : Base.returnBases()){
			if ((dist = (int) b.getSpawn().distanceSquared(location)) < i){
				i = dist;
				out = b;
			}
		}
		return out;
	}

	@Override
	public void run() {
		Iterator<NPCHolder> i = npcrace.iterator();
		while(i.hasNext()){
			findTarget(i.next());
		}
	}

	private void findTarget(NPCHolder next) {
		if (next.npc.getNavigator().getTargetType().equals(TargetType.ENTITY)) return;
		List<Entity> l = next.npc.getBukkitEntity().getNearbyEntities(10, 3, 10);
		for(Entity e : l){
			if (!(e instanceof Player)) continue;
			Race r = WarPlayers.getRace((Player) e);
			if (r == null || r.isRef()) continue;
			if (!r.equals(next.r)){
				next.npc.getNavigator().setTarget(e, true);
			}
		}
	}
	
}
