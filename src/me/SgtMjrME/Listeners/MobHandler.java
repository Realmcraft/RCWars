package me.SgtMjrME.Listeners;

import java.util.HashMap;
import java.util.UUID;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Object.MobHolder;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SpawnEgg;
import org.bukkit.util.Vector;

public class MobHandler implements Listener{
	
	static HashMap<UUID, MobHolder> mobs = new HashMap<UUID, MobHolder>();

	public static void resetMobs() {
		for (World w : Bukkit.getServer().getWorlds()){
			for (Entity e : w.getEntities()){
				if (!(e instanceof Player)){
					if (e instanceof LivingEntity) mobs.remove(((LivingEntity) e).getUniqueId());
					e.remove();
				}
			}
		}
	}
	
	/*
	 * If the target was lost, return to the spawn location (Not implemented yet)
	 * if the target has the same race as entity, cancel it
	 * otherwise, ATTACK
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTarget(EntityTargetEvent e){
		if (!(e.getEntity() instanceof LivingEntity)) return;
//		LivingEntity ent = (LivingEntity) e.getEntity();
		if (e.getTarget() == null){ //No target, return to spawn (this will be fun...)
			//Not doing this yet TODO
			return;
		}
		MobHolder mh = mobs.get(((LivingEntity) e.getEntity()).getUniqueId());
		if (mh == null) return;
		Race targetRace = WarPlayers.getRace(mh.p);
		if (targetRace == null) return;
		if (targetRace.equals(mh.r) || targetRace.isRef()) e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	  public void onSpawn(CreatureSpawnEvent e){
		  //No spawning allowed (I hope it doesn't mess up anything)
		if (!e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM)) e.setCancelled(true);
	}
	
	public MobHolder getMH(LivingEntity e){
		return mobs.get(e.getUniqueId());
	}
	
	private MobHolder[] addArr(MobHolder[] arr, MobHolder m){
		//m must be valid, arr does not.
		int length = (arr == null)?0:arr.length;
		MobHolder[] mh = new MobHolder[length + 1];
		for(int i = 0; i < length; i++) mh[i] = arr[i];
		mh[length] = null;
		return mh;
	}
	
	public MobHolder[] getMH(Player p){
		//Allow multiple mobs per player
		//returns array of at least length 1 containing null, null terminating.
		MobHolder[] arrOut = new MobHolder[1];
		arrOut[0] = null;
		for (MobHolder mh : mobs.values()){
			if (mh.p.equals(p)) addArr(arrOut, mh);
		}
		return arrOut;
	}
	
	public MobHolder[] getMH(Race r){
		//Not sure why you would want this... but I'll leave it in here.
		//Allow multiple mobs per race
			MobHolder[] arrOut = new MobHolder[1];
			arrOut[0] = null;
			for (MobHolder mh : mobs.values()){
				if (mh.r.equals(r)) addArr(arrOut, mh);
			}
			return arrOut;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onSpawn(PlayerInteractEvent e){
		if (e.isCancelled()) return;
		if (e.getPlayer() == null) return;
		ItemStack item = e.getPlayer().getItemInHand();
		if (item == null) return;
		if (item.getTypeId() != 383) return;
		//Player was holding a spawn egg
		if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		//Player used on a block (mob should spawn, I think I need to force the spawn, I had hoped not)
		e.setCancelled(true);//Ok, cancel the event, spawn the mob
		MaterialData im = item.getData();
		if (!(im instanceof SpawnEgg)) return;//No idea when this will be triggered... hope never
		SpawnEgg egg = (SpawnEgg) im;
		item.setAmount(item.getAmount() - 1);
		//spawn entity (since i cancelled it before)
		LivingEntity entity = (LivingEntity) RCWars.returnPlugin().getWarWorld().spawnEntity(
				e.getClickedBlock().getLocation().add(new Vector(0,1,0)), egg.getSpawnedType());
		mobs.put(entity.getUniqueId(), new MobHolder(entity, e.getPlayer(), WarPlayers.getRace(e.getPlayer()), entity.getLocation()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntDie(EntityDeathEvent e){
		//If the entity dies, I really don't care what, let mobs decide how to remove it.
		if (e.getEntity() != null) mobs.remove(e.getEntity().getUniqueId());
	}

}
