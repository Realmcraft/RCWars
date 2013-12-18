package me.SgtMjrME.Tasks;

import java.util.Collection;

import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;

import me.SgtMjrME.Listeners.MobHandler;
import me.SgtMjrME.Object.MobHolder;

public class SpiderAgressor implements Runnable{

//	@Override
//	public void run() {
//		Collection<MobHolder> mobs = MobHandler.getMobs();
//		for (MobHolder m : mobs){
//			try{
//				if (m.mob instanceof CaveSpider || m.mob instanceof Spider){
//					Creature c = (Creature) m.mob;
//					if (c.getTarget() != null) continue;
//					for (Entity e : c.getNearbyEntities(16, 16, 16)){
//						if (!(e instanceof LivingEntity)) continue;
//						if (MobHandler.targetEntity(c, e)){
//							c.setTarget((LivingEntity) e);
//							break;
//						}
//					}
//				}
//			} catch (Exception e){
//				e.printStackTrace();
//			}
//		}
//	}

}
