package me.SgtMjrME.Listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import me.SgtMjrME.ClassUpdate.Abilities.AbilityTimer;
import me.SgtMjrME.ClassUpdate.Abilities.BaseAbility;
import me.SgtMjrME.ClassUpdate.WarRank;
import me.SgtMjrME.Object.Race;
import me.SgtMjrME.Object.WarPlayers;
import me.SgtMjrME.RCWars;
import me.SgtMjrME.SiegeUpdate.TntMeta;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EntityListener
  implements Listener
{
  private final RCWars pl;
  private HashMap<String, Long> delay = new HashMap<String, Long>();
  public static HashMap<String, String> explDmg = new HashMap<String, String>();

  public EntityListener(RCWars plugin) {
    pl = plugin;
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void EndDmg(EntityDamageEvent e) {
    if (!(e.getEntity() instanceof Player)) return;
    Race r = WarPlayers.getRace((Player)e.getEntity());
    if ((r != null) && (r.isRef())) e.setCancelled(true); 
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void EntDamEnt(EntityDamageByEntityEvent e)
  {
    if (e.isCancelled())
      return;
    if (!e.getEntity().getWorld().equals(pl.getWarWorld()))
      return;
    Player damageep;
    if (!(e.getEntity() instanceof Player)) {
      if ((e.getEntity() instanceof Projectile)) {
        damageep = (Player)((Projectile)e.getEntity()).getShooter();
        BaseAbility b = null;
        if (damageep != null)
          b = AbilityTimer.getAbility(damageep);
        if (b != null) b.onDefend(damageep, e);
        //Not sure what this was supposed to do, but that's what it decompiled to
//        if (!e.isCancelled())
//          break;
      }
      else
    	  return;
    }
    else {
      damageep = (Player)e.getEntity();
    }
    Race damagee = WarPlayers.getRace(damageep);
    if (damagee == null)
      return;
    if (damagee.isRef()) {
      e.setCancelled(true);
      return;
    }
    boolean setFire = false;
    Player damagerp = null;
    if ((e.getDamager() instanceof Projectile)) {
      if (!(((Projectile)e.getDamager()).getShooter() instanceof Player))
        return;
      damagerp = (Player)((Projectile)e.getDamager()).getShooter();
      if (((e.getDamager() instanceof Explosive)) && 
        (((Explosive)e.getDamager()).isIncendiary())) setFire = true;
    }
    else if ((e.getDamager() instanceof TNTPrimed)) {
      List<MetadataValue> mdvlist = ((TNTPrimed)e.getDamager()).getMetadata("shooter");
      if (mdvlist.size() == 1) {
        MetadataValue mdv = (MetadataValue)mdvlist.get(0);
        if ((mdv instanceof TntMeta)) {
          Player p = (Player)((TntMeta)mdv).value();
          if (p != null) {
            if (WarPlayers.getRace(p).equals(damagee))
            {
              e.setCancelled(true);
              return;
            }

            damagerp = p;
          }
        }

      }

    }
    else
    {
      if (!(e.getDamager() instanceof Player))
        return;
      damagerp = (Player)e.getDamager();
    }
    Race damager = WarPlayers.getRace(damagerp);
    if (damager == null)
      return;
    if (damager.isRef()) { e.setCancelled(true); return; }
    if (damagee.equals(damager)) {
      e.setCancelled(true);

      return;
    }
    damageep.getInventory().getChestplate().setDurability((short)0);
    damageep.getInventory().getLeggings().setDurability((short)0);
    damageep.getInventory().getBoots().setDurability((short)0);
    damagerp.getItemInHand().setDurability((short)0);
    AbilityTimer.onAttack(damagerp, e);
    AbilityTimer.onDefend(damageep, e);

    WarRank ofDead = WarRank.getPlayer(damageep);
    WarRank ofKiller = WarRank.getPlayer(damagerp);
    if ((e.getDamager() instanceof Projectile)) e.setDamage((int)(e.getDamage() * ofKiller.attbowpwr / ofDead.defpwr)); else
      e.setDamage((int)(e.getDamage() * ofKiller.attswdpwr / ofDead.defpwr));
    if (!damager.equals(damagee)) {
      if (e.getDamage() > 0) damagerp.giveExp(pl.hitexp);
      if (setFire) damageep.setFireTicks(100); 
    }
    else { if (damagee.equals(damager)) {
        e.setCancelled(true);

        return;
      }

      explDmg.put(damageep.getName(), damagerp.getName());
      if (damageep.getHealth() - e.getDamage() > 0)
        WarPlayers.setDamageTime(damageep, damagerp.getName()); }
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void onSplashEvent(PotionSplashEvent e)
  {
    if (e.isCancelled())
      return;
    if (!e.getEntity().getWorld().equals(pl.getWarWorld()))
      return;
    Iterator<PotionEffect> types = e.getPotion().getEffects().iterator();
    boolean harm = false;
    while (types.hasNext()) {
      PotionEffectType hold = ((PotionEffect)types.next()).getType();
      if ((hold.equals(PotionEffectType.HARM)) || (hold.equals(PotionEffectType.POISON)) || 
        (hold.equals(PotionEffectType.SLOW)) || (hold.equals(PotionEffectType.WEAKNESS))) {
        harm = true;
        break;
      }
    }
    if (!harm)
      return;
    if (!(e.getEntity().getShooter() instanceof Player))
      return;
    Race damager = WarPlayers.getRace((Player)e.getEntity().getShooter());
    if (damager == null)
      return;
    Iterator<LivingEntity> i = e.getAffectedEntities().iterator();
    while (i.hasNext()) {
      LivingEntity temp = (LivingEntity)i.next();
      if ((temp instanceof Player))
      {
        Race damagee = WarPlayers.getRace((Player)temp);
        if (damagee != null)
        {
          if (damagee.isRef()) e.setIntensity(temp, 0.0D);
          if (damagee.getName().equals(damager.getName()))
            e.setIntensity(temp, 0.0D);  } 
      }
    }
  }

  @EventHandler(priority=EventPriority.MONITOR)
  public void ondeath(PlayerDeathEvent e) { Player dead = e.getEntity();
    Player killer = e.getEntity().getKiller();
    if ((killer == null) && 
      (explDmg.containsKey(dead.getName()))) {
      killer = Bukkit.getPlayer((String)explDmg.remove(dead.getName()));
    }

    ExperienceOrb o = (ExperienceOrb)dead.getWorld().spawnEntity(dead.getLocation(), EntityType.EXPERIENCE_ORB);
    o.setExperience(e.getDroppedExp());
    e.setDroppedExp(0);

    if (WarPlayers.isPlaying(dead.getName())) {
      Iterator<ItemStack> i = e.getDrops().iterator();
      while (i.hasNext()) {
        ItemStack itemStack = (ItemStack)i.next();
        if (!RCWars.dropItems.contains(Integer.valueOf(itemStack.getTypeId())))
          i.remove();
      }
      ItemStack is = new ItemStack(Material.SKULL_ITEM, 1);
      is.setDurability((short)3);
      SkullMeta meta = (SkullMeta)is.getItemMeta();
      meta.setOwner(dead.getName());
      is.setItemMeta(meta);
      e.getDrops().add(is);
      RCWars.logKill(dead, killer);
    }
  }

  @EventHandler(priority=EventPriority.NORMAL)
  public void onPortal(PlayerPortalEvent e)
  {
    Player p = e.getPlayer();
    if ((delay.containsKey(p.getName())) && 
      (System.currentTimeMillis() - ((Long)delay.get(p.getName())).longValue() < 3000L)) return;

    Location ploc = e.getFrom().getBlock().getLocation();
    if (e.getFrom().getWorld().equals(RCWars.returnPlugin().getWarWorld())) return;
    Location temp = ploc.clone();
    temp.setX(temp.getX() - 1.0D);
    int type = temp.getBlock().getTypeId();

    if (type == 90) {
      Block b = checkFour(temp);
      activatePortal(p, b, e);
      return;
    }
    temp.setX(temp.getX() + 2.0D);
    type = temp.getBlock().getTypeId();
    if (type == 90) {
      Block b = checkFour(temp);
      activatePortal(p, b, e);
      return;
    }
    temp.setX(ploc.getX());
    temp.setZ(temp.getZ() - 1.0D);
    type = temp.getBlock().getTypeId();
    if (type == 90) {
      Block b = checkFour(temp);
      activatePortal(p, b, e);
      return;
    }
    temp.setZ(temp.getZ() + 2.0D);
    type = temp.getBlock().getTypeId();
    if (type == 90) {
      Block b = checkFour(temp);
      activatePortal(p, b, e);
      return;
    }
    p.sendMessage("No data found");
  }

  private Block checkFour(Location temp) {
    temp.setX(temp.getX() - 1.0D);
    int type = temp.getBlock().getTypeId();
    if ((type != 0) && (type != 90)) {
      return temp.getBlock();
    }
    if (type == 90) {
      return checkFour(temp);
    }
    temp.setX(temp.getX() + 2.0D);
    type = temp.getBlock().getTypeId();
    if ((type != 0) && (type != 90)) {
      return temp.getBlock();
    }
    if (type == 90) {
      return checkFour(temp);
    }
    temp.setX(temp.getX() - 1.0D);
    temp.setZ(temp.getZ() - 1.0D);
    type = temp.getBlock().getTypeId();
    if ((type != 0) && (type != 90)) {
      return temp.getBlock();
    }
    if (type == 90) {
      return checkFour(temp);
    }
    temp.setZ(temp.getZ() + 2.0D);
    type = temp.getBlock().getTypeId();
    if ((type != 0) && (type != 90)) {
      return temp.getBlock();
    }
    if (type == 90) {
      return checkFour(temp);
    }
    return null;
  }

  private void activatePortal(final Player p, Block b, PlayerPortalEvent e) {
    final Race r = Race.getRacePortal(b);
    e.setCancelled(true);
    if (r == null) return;
    if ((r.isRef()) && (!p.hasPermission("rcwars.ref"))) {
      p.sendMessage(ChatColor.RED + "Not allowed to join ref");
      delay.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
      return;
    }
    Race check = Race.checkRaceOpen(r);
    if (check == null)
      return;
    if (check.equals(r)) {
      if (r.getSpawn() == null) {
        p.sendMessage("Spawn for race " + r.getDisplay() + " has not been set");
        delay.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
        return;
      }
      Bukkit.getScheduler().runTaskLater(pl, new Runnable()
      {
        public void run()
        {
          p.closeInventory();
          WarPlayers.setRace(p, r);
          p.sendMessage(ChatColor.GREEN + "Set race to " + r.getDisplay());
          delay.remove(p.getName());
        }
      }
      , 1L);

      return;
    }

    p.sendMessage(ChatColor.RED + "Races are unbalanced! " + check.getDisplay() + 
      ChatColor.RED + " has too few people!");
    delay.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
  }

  public static void removeDmg(String p)
  {
    explDmg.remove(p);
  }
}