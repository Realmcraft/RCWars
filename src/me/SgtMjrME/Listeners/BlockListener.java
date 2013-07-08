package me.SgtMjrME.Listeners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import me.SgtMjrME.RCWars;
import me.SgtMjrME.Util;
import me.SgtMjrME.ClassUpdate.Abilities.AbilityTimer;
import me.SgtMjrME.Object.Base;
import me.SgtMjrME.Object.WarPlayers;
import me.SgtMjrME.SiegeUpdate.Siege;
import me.SgtMjrME.Tasks.ScoreboardHandler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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
  private RCWars pl;
  final int size = 2;
  static public ArrayList<Player> setLeaderboard = new ArrayList<Player>();

  public BlockListener(RCWars plugin)
  {
    pl = plugin;
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void onBlockBreak(BlockBreakEvent e)
  {
    if (!e.getBlock().getWorld().equals(pl.getWarWorld()))
      return;
    if (e.getBlock().getTypeId() == 46)
      return;
    if (!e.getPlayer().hasPermission("rcwars.admin"))
      e.setCancelled(true);
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void onBlockPlace(BlockPlaceEvent e) throws FileNotFoundException, IOException, InvalidConfigurationException
  {
	  if (setLeaderboard.contains(e.getPlayer())){
		YamlConfiguration cfg = new YamlConfiguration();
		File f = new File(pl.getDataFolder().getAbsolutePath() + "/leaderboardSkull.yml");
		f.delete(); f.createNewFile();
		cfg.load(new File(pl.getDataFolder().getAbsolutePath() + "/leaderboardSkull.yml"));
  		Block block = e.getBlock();
  		Location playerLoc = e.getPlayer().getLocation();
  		int xdif = block.getX() - playerLoc.getBlockX();
  		int xdifabs = Math.abs(block.getX() - playerLoc.getBlockX());
  		int zdif = block.getZ() - playerLoc.getBlockZ();
  		int zdifabs = Math.abs(block.getZ() - playerLoc.getBlockZ());
  		Location blockLoc = block.getLocation();
  		Skull temp;
  		if (xdifabs > zdifabs){
  			//Ok, so we'll do the blocks in the X direction
  			BlockFace rot;
  			if (xdif > 0) rot = BlockFace.EAST;
  			else rot = BlockFace.WEST;
  			blockLoc.add(0,0,1);
  			temp = setBlock(Material.GOLD_BLOCK, blockLoc, e, rot);
  			if (temp != null){
  				ScoreboardHandler.goldPlayer = temp;
  				cfg.set("gold", RCWars.loc2str(blockLoc.clone().add(0,1,0)));
  			}
  			blockLoc.add(0,0,-2);
  			temp = setBlock(Material.IRON_BLOCK, blockLoc, e, rot);
  			if (temp != null){
  				ScoreboardHandler.ironPlayer = temp;
  				cfg.set("iron", RCWars.loc2str(blockLoc.clone().add(0,1,0)));
  			}
  			blockLoc.add(0,1,1);
  			temp = setBlock(Material.DIAMOND_BLOCK, blockLoc, e, rot);
  			if (temp != null){
  				ScoreboardHandler.diamondPlayer = temp;
  				cfg.set("diamond", RCWars.loc2str(blockLoc.clone().add(0,1,0)));
  			}
  			setLeaderboard.remove(e.getPlayer());
  		}
  		else if (zdifabs > xdifabs){
  			//Ok, so we'll do the blocks in the Z direction
  			BlockFace rot;
  			if (zdif > 0) rot = BlockFace.NORTH;
  			else rot = BlockFace.SOUTH;
  			blockLoc.add(1,0,0);
  			temp = setBlock(Material.GOLD_BLOCK, blockLoc, e, rot);
  			if (temp != null){
  				ScoreboardHandler.goldPlayer = temp;
  				cfg.set("gold", RCWars.loc2str(blockLoc.clone().add(0,1,0)));
  			}
  			blockLoc.add(-2,0,0);
  			temp = setBlock(Material.IRON_BLOCK, blockLoc, e, rot);
  			if (temp != null){
  				ScoreboardHandler.ironPlayer = temp;
  				cfg.set("iron", RCWars.loc2str(blockLoc.clone().add(0,1,0)));
  			}
  			blockLoc.add(1,1,0);
  			temp = setBlock(Material.DIAMOND_BLOCK, blockLoc, e, rot);
  			if (temp != null){
  				ScoreboardHandler.diamondPlayer = temp;
  				cfg.set("diamond", RCWars.loc2str(blockLoc.clone().add(0,1,0)));
  			}
  			setLeaderboard.remove(e.getPlayer());
  		}
  		else if (zdifabs == xdifabs){
  			Util.sendMessage(e.getPlayer(), 
  					"Please make the direction more noticeable (move farther away in one direction)");
  		}
  		cfg.save(new File(pl.getDataFolder().getAbsolutePath() + "/leaderboardSkull.yml"));
  	}
    if (!e.getBlock().getWorld().equals(this.pl.getWarWorld())) return;
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

        Bukkit.getScheduler().runTask(this.pl, new Runnable()
        {
          public void run()
          {
            place.getBlock().setType(Material.TNT);
          }
        });
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.pl, new Runnable()
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
  
  private Skull setBlock(Material mat, Location blockLoc, BlockPlaceEvent e, BlockFace rotation){
		if (!blockLoc.getBlock().getType().equals(Material.AIR)){
			Util.sendMessage(e.getPlayer(), "Non-air block detected, please retry");
			return null;
		}
		blockLoc.getBlock().setType(mat);
		Block b = blockLoc.clone().add(0,1,0).getBlock();
		b.setType(Material.SKULL);
		Skull skull = (Skull) b.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setRotation(rotation);
		org.bukkit.material.Skull md = (org.bukkit.material.Skull) skull.getData();
		md.setFacingDirection(rotation);
		skull.setData(md);
		skull.update(true);
		return skull;
  }

  protected void createExplosion(Location place)
  {
    place.getBlock().setTypeId(0);
    pl.getWarWorld().createExplosion(place, 4.0F, false);
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
                Siege s = Siege.checkWall(temp);
                if (s != null)
                  s.wallDestroyed(temp);
              }
            }
          }
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void onBucket(PlayerBucketEmptyEvent e)
  {
    if ((!e.getPlayer().hasPermission("rcwars.admin")) && (e.getBlockClicked().getWorld().equals(pl.getWarWorld())))
      e.setCancelled(true);
  }

  @EventHandler(priority=EventPriority.LOWEST)
  public void onBucketFill(PlayerBucketFillEvent e)
  {
    if ((!e.getPlayer().hasPermission("rcwars.admin")) && (e.getBlockClicked().getWorld().equals(pl.getWarWorld())))
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