package me.SgtMjrME.Listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Object.MobHolder;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;
import me.SgtMjrME.Tasks.SpiderAgressor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
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
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class MobHandler implements Listener{
	
	static BukkitTask spiderTask = null;
	
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
		if (spiderTask != null) spiderTask.cancel();
		spiderTask = Bukkit.getScheduler().runTaskTimer(RCWars.returnPlugin(), new SpiderAgressor(), 20, 80);
	}
	
	/*
	 * If the target was lost, return to the spawn location (Not implemented yet)
	 * if the target has the same race as entity, cancel it
	 * otherwise, ATTACK
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTarget(EntityTargetEvent e){
		if (!targetEntity(e.getEntity(), e.getTarget())) e.setCancelled(true);
	}
	
	//returns true if the target should happen, false otherwise.
	public static boolean targetEntity(Entity ent, Entity target){
		if (!(ent instanceof LivingEntity)) return true;
//		LivingEntity ent = (LivingEntity) e.getEntity();
		if (target == null){ //No target, return to spawn (this will be fun...)
			//Not doing this yet TODO
			return true;
		}
		MobHolder mh = mobs.get(((LivingEntity) ent).getUniqueId());
		if (mh == null) return true;
		Race targetRace = null;
		if (target instanceof Player){
			targetRace = WarPlayers.getRace((Player) target);
		}
		else{
			MobHolder targetMH = mobs.get(((LivingEntity) ent).getUniqueId());
			if (targetMH != null){
				targetRace = targetMH.r;
			}
		}
		if (targetRace == null) return true;
		if (targetRace.equals(mh.r) || targetRace.isRef()) return false;
		return true;
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
		e.getPlayer().setItemInHand(item);
		//spawn entity (since i cancelled it before)
		LivingEntity entity = (LivingEntity) RCWars.returnPlugin().getWarWorld().spawnEntity(
				e.getClickedBlock().getLocation().add(new Vector(0,1,0)), egg.getSpawnedType());
		if (egg.getSpawnedType().equals(EntityType.HORSE)){
			Horse horse = (Horse) entity;
			horse.setTamed(true);
			horse.getInventory().setSaddle(new ItemStack(329,1));
			horse.getInventory().setArmor(new ItemStack(418,1));
		} else if (egg.getSpawnedType().equals(EntityType.SKELETON)){
			Skeleton skele = (Skeleton) entity;
			skele.getEquipment().setHelmet(new ItemStack(298,1));
			skele.getEquipment().setItemInHand(new ItemStack(261,1));
		} else if (egg.getSpawnedType().equals(EntityType.ZOMBIE)){
			Zombie zomb = (Zombie) entity;
			zomb.getEquipment().setHelmet(new ItemStack(298,1));
		} else if (egg.getSpawnedType().equals(EntityType.PIG_ZOMBIE)){
			PigZombie pig = (PigZombie) entity;
			pig.setAngry(true);
		}
		mobs.put(entity.getUniqueId(), new MobHolder(entity, e.getPlayer(), WarPlayers.getRace(e.getPlayer()), entity.getLocation()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntDie(EntityDeathEvent e){
		//If the entity dies, I really don't care what, let mobs decide how to remove it.
		if (e.getEntity() != null) mobs.remove(e.getEntity().getUniqueId());
	}
	
	public static Collection<MobHolder> getMobs(){
		return mobs.values();
	}

}
