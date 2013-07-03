package me.SgtMjrME.Listeners;

import java.util.HashMap;

import me.SgtMjrME.Object.MobHolder;
import me.SgtMjrME.Object.Race;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MobHandler implements Listener{
	
	HashMap<Entity, MobHolder> mobs = new HashMap<Entity, MobHolder>();

	public static void resetMobs() {
		for (World w : Bukkit.getServer().getWorlds()){
			for (Entity e : w.getEntities()){
				if (e instanceof Player) continue;
				e.remove();//Remove all non-player entities
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onTarget(EntityTargetEvent e){
		
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	  public void onSpawn(CreatureSpawnEvent e){
		  //If not an egg, no spawning allowed.  This serves a dual purpose, stopping mobs, and only allowing my mobs.
		  if (!e.getSpawnReason().equals((CreatureSpawnEvent.SpawnReason.EGG))) e.setCancelled(true);
	  }
	
	public MobHolder getMH(Entity e){
		return mobs.get(e);
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
		if (e.getPlayer() == null) return;
		ItemStack item = e.getPlayer().getItemInHand();
		if (item == null) return;
		if (item.getTypeId() != 383) return;
		//Player was holding a spawn egg
		if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		//Player used on a block (mob should spawn, I think I need to force the spawn, I had hoped not)
		
	}

}
