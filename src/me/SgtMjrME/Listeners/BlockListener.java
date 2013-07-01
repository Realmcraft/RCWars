package me.SgtMjrME.Listeners;

import java.util.Iterator;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Util;
import me.SgtMjrME.ClassUpdate.Abilities.AbilityTimer;
import me.SgtMjrME.Object.Base;
import me.SgtMjrME.Object.WarPlayers;
import me.SgtMjrME.SiegeUpdate.Siege;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class BlockListener
  implements Listener
{
  private RCWars p;
  final int size = 2;

  public BlockListener(RCWars plugin)
  {
    p = plugin;
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void onBlockBreak(BlockBreakEvent e)
  {
    if (!e.getBlock().getWorld().equals(p.getWarWorld()))
      return;
    if (e.getBlock().getTypeId() == 46)
      return;
    if (!e.getPlayer().hasPermission("rcwars.admin"))
      e.setCancelled(true);
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void onBlockPlace(BlockPlaceEvent e)
  {
    if (!e.getBlock().getWorld().equals(this.p.getWarWorld())) return;
    if (e.getPlayer().getItemInHand().getTypeId() == 46) {
      Siege s = Siege.isWall(e.getBlock().getLocation());
      if (s != null) {
        Base b = s.b;
        Iterator<String> i = WarPlayers.listPlayers();
        while (i.hasNext()) {
          Player p = Bukkit.getPlayer((String)i.next());
          if (p != null)
            Util.sendMessage(p, ChatColor.RED + "Base " + b.getDisp() + ChatColor.RED + " is being sieged!");
        }
        final Location place = e.getBlock().getLocation();

        Bukkit.getScheduler().runTask(this.p, new Runnable()
        {
          public void run()
          {
            place.getBlock().setType(Material.TNT);
          }
        });
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.p, new Runnable()
        {
          public void run() {
            if ((place.getBlock() != null) && (place.getBlock().getTypeId() == 46))
              createExplosion(place);
          }
        }
        , 30L);
        return;
      }
    }
    if (!e.getPlayer().hasPermission("rcwars.admin"))
      e.setCancelled(true);
  }

  protected void createExplosion(Location place)
  {
    place.getBlock().setTypeId(0);
    p.getWarWorld().createExplosion(place, 4.0F, false);
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void onExplode(EntityExplodeEvent event) {
    if (!event.getLocation().getWorld().equals(RCWars.returnPlugin().getWarWorld()))
      return;
    event.blockList().clear();
    AbilityTimer.onExplode(event);
    Location coord = event.getLocation();
    if ((event.getEntity() instanceof Fireball)) return;
    for (int y = -2; y < 3; y++)
      for (int x = -2; x < 3; x++)
        for (int z = -2; z < 3; z++)
          if ((Math.abs(y) != 2) || (
            (Math.abs(x) != 2) && (Math.abs(z) != 2)))
          {
            if ((Math.abs(x) != 2) || (Math.abs(z) != 2))
            {
              Location temp = new Location(coord.getWorld(), 
                coord.getBlockX() + x, coord.getBlockY() + y, coord.getBlockZ() + z);
              if (temp.getBlock().getTypeId() == 46) {
                createExplosion(temp);
              }
              else {
                Siege s = Siege.isWall(temp);
                if (s != null)
                  s.wallDestroyed(temp);
              }
            }
          }
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void onBucket(PlayerBucketEmptyEvent e)
  {
    if ((!e.getPlayer().hasPermission("rcwars.admin")) && (e.getBlockClicked().getWorld().equals(p.getWarWorld())))
      e.setCancelled(true);
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void onBucketFill(PlayerBucketFillEvent e)
  {
    if ((!e.getPlayer().hasPermission("rcwars.admin")) && (e.getBlockClicked().getWorld().equals(p.getWarWorld())))
      e.setCancelled(true);
  }

  @EventHandler(priority=EventPriority.NORMAL)
  public void onSignChange(SignChangeEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if ((event.getLine(0) != null) && 
      ((event.getLine(0).equals("[WarShop]")) || 
      (event.getLine(0).equals("[Cannon]")) || 
      (event.getLine(0).equals("[Class]"))) && 
      (!event.getPlayer().hasPermission("rcwars.admin"))) {
      event.setCancelled(true);
      Util.sendMessage(event.getPlayer(), ChatColor.RED + "Not allowed to place a " + event.getLine(0));
    }
  }
  
  @EventHandler(priority = EventPriority.NORMAL)
  public void onPainting(HangingBreakEvent e){
	  if (e.getEntity().getWorld().equals(RCWars.returnPlugin().getWarWorld())) e.setCancelled(true);
  }
  
  @EventHandler(priority = EventPriority.NORMAL)
  public void onRain(WeatherChangeEvent e){
	  if (e.toWeatherState()) e.setCancelled(true);
  }
}