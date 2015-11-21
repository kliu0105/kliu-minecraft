//General idea of the game-
//Players compete to destroy the other team's "core" (redstone/lapiz block)
//To get supplies, they must create a "pipeline" to a beacon
//The pipeline blocks/core are broken by arrows
//Arrows are the only way to break blocks
//This isn't even 60% done

package tk.moocrafttowny.original;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BlockIterator;


/**
 * @author $wagg 31337 kl00
 *
 */
public class Main extends JavaPlugin implements Listener {
	//	event.getProjectile().getWorld().getBlockAt(event.getProjectile().getLocation()).setType(Material.AIR);
	//if (getBlockAt(event.getProjectile()))

	public Location blueCoreLocation;
	public Location redCoreLocation;
	private boolean gamestarted;

	@Override
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		//this.saveDefaultConfig();
		reloadConfig();
		this.saveDefaultConfig();
		getLogger().info("Plugin has loaded!");

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getMainScoreboard();
		Team team = board.getTeam("Blue");
		if (team == null) {
			team = board.registerNewTeam("Blue");
			team.setAllowFriendlyFire(false);
		}
		team.setPrefix(ChatColor.AQUA+"");
		team = board.getTeam("Red");
		if (team == null) {
			team = board.registerNewTeam("Red");
			team.setAllowFriendlyFire(false); 			
		}
		team.setPrefix(ChatColor.RED+"");

		blueCoreLocation = new Location(getServer().getWorld("plotworld"), getConfig().getInt("bluecorelocation.x"), getConfig().getInt("bluecorelocation.y"), getConfig().getInt("bluecorelocation.z"));
		redCoreLocation = new Location(getServer().getWorld("plotworld"), getConfig().getInt("redcorelocation.x"), getConfig().getInt("redcorelocation.y"), getConfig().getInt("redcorelocation.z"));
		BukkitTask task = new ScheduleTask(this).runTaskTimer(this, 0, 2);
		gamestarted = getConfig().getBoolean("gamestartedstatus",false);
	}

	@Override
	public void onDisable(){
		getServer().getScheduler().cancelTasks(this); // not needed anyway
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile entity = event.getEntity();
		if(!(entity.getType() == EntityType.ARROW)) 
			// Remove to check all projectiles
			return;

		Arrow arrow = (Arrow)entity;	 
		World world = arrow.getWorld();
		BlockIterator iterator = new BlockIterator(world, arrow.getLocation().toVector(), arrow.getVelocity().normalize(), 0, 4);
		Block hitBlock = null;

		while(iterator.hasNext()) {
			hitBlock = iterator.next();
			if(hitBlock.getType() != Material.AIR) {
				//Check all non-solid blockid's here.
				if(hitBlock.getType() == Material.REDSTONE_BLOCK 
						|| hitBlock.getType() == Material.LAPIS_BLOCK 
						|| hitBlock.getType() == Material.SEA_LANTERN 
						|| hitBlock.getType() == Material.GLOWSTONE){
					hitBlock.setType(Material.AIR);
					if (hitBlock.getLocation().equals(blueCoreLocation)) {
						getServer().dispatchCommand(getServer().getConsoleSender(), "say RED WON!");
						//does this work?
						getServer().dispatchCommand(getServer().getConsoleSender(), "title title @a RED WON!");
						stopgame();
					} else if (hitBlock.getLocation().equals(redCoreLocation)) {
						getServer().dispatchCommand(getServer().getConsoleSender(), "say BLUE WON!");
						getServer().dispatchCommand(getServer().getConsoleSender(), "title title @a BLUE WON!");
						stopgame();
					}
					arrow.remove();
					break;

				}
			}
		}
		getLogger().info(entity.getType().toString());

	}
	private void stopgame() {
		gamestarted = false;
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getMainScoreboard();

		Team teamblue = board.getTeam("Blue");
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			if (teamblue.hasEntry(player.getName())) {
				getServer().dispatchCommand(getServer().getConsoleSender(), "tp "+player.getName()+ " " +getConfig().getString("bluelobby.x")+" "+getConfig().getString("bluelobby.y")+ " "+ getConfig().getString("bluelobby.z"));
			} else {
				getServer().dispatchCommand(getServer().getConsoleSender(), "tp "+player.getName()+ " " +getConfig().getString("redlobby.x")+" "+getConfig().getString("redlobby.y")+ " "+ getConfig().getString("redlobby.z"));
			}
		}
	}

	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		getLogger().log(Level.INFO, "Player " + event.getPlayer().getName() + " is logging in!");
		Player player = event.getPlayer();
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getMainScoreboard();
		Team teamblue = board.getTeam("Blue");
		Team teamred = board.getTeam("Red");
		if (teamblue.getSize() < teamred.getSize()){
			teamblue.addEntry(player.getName());
			getServer().dispatchCommand(getServer().getConsoleSender(), "pex user "+player.getName()+" group set Blue");
			player.sendMessage(ChatColor.AQUA+"You are now on team blue");
		}else{
			teamred.addEntry(player.getName());
			getServer().dispatchCommand(getServer().getConsoleSender(), "pex user "+player.getName()+" group set Red"); 
			player.sendMessage(ChatColor.RED+"You are now on team red");
		}
		getServer().dispatchCommand(getServer().getConsoleSender(), "clear "+player.getName()); 
		if(gamestarted == false) {
			Set<String> entries = teamblue.getEntries();
			if (teamblue.hasEntry(player.getName())){
				getServer().dispatchCommand(getServer().getConsoleSender(), "tp "+player.getName()+ " " +getConfig().getString("bluelobby.x")+" "+getConfig().getString("bluelobby.y")+ " "+ getConfig().getString("bluelobby.z"));
			} else {
				getServer().dispatchCommand(getServer().getConsoleSender(), "tp "+player.getName()+ " " +getConfig().getString("redlobby.x")+" "+getConfig().getString("redlobby.y")+ " "+ getConfig().getString("redlobby.z"));
			}
		} else {
			getServer().dispatchCommand(getServer().getConsoleSender(), "kill "+player.getName());
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getMainScoreboard();
		final Player player = event.getPlayer();
		Team teamblue = board.getTeam("Blue");
		if (teamblue.hasEntry(player.getName())) {
			event.setRespawnLocation(new Location(getServer().getWorld("plotworld"), getConfig().getInt("bluespawn.x"), getConfig().getInt("bluespawn.y"), getConfig().getInt("bluespawn.z")));
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" lapis_block "+ getConfig().getString("blocksOnRespawn"));
					getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" bow 1");
					getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" arrow "+ getConfig().getString("arrowsOnRespawn"));

				}
			}, 60L);
		} else {
			event.setRespawnLocation(new Location(getServer().getWorld("plotworld"), getConfig().getInt("redspawn.x"), getConfig().getInt("redspawn.y"), getConfig().getInt("redspawn.z")));
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" redstone_block "+ getConfig().getString("blocksOnRespawn"));
					getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" bow 1");
					getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" arrow "+ getConfig().getString("arrowsOnRespawn"));

				}
			}, 60L);
		}
	}

	@EventHandler
	public void onPlayerLogOut(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getMainScoreboard();
		Team teamblue = board.getTeam("Blue");
		Team teamred = board.getTeam("Red");
		teamblue.removeEntry(event.getPlayer().getName());
		teamred.removeEntry(event.getPlayer().getName());
	}
//does this even work...?
		if (cmd.getName().equalsIgnoreCase("start")){
			gamestarted = true;
			for(Player player : Bukkit.getServer().getOnlinePlayers()){
				getServer().dispatchCommand(sender, "/pos1 "+ getConfig().getInt("pos1.x") + "," + getConfig().getInt("pos1.y") + "," + getConfig().getInt("pos1.z"));
				getServer().dispatchCommand(sender, "/pos2 "+ getConfig().getInt("pos2.x") + "," + getConfig().getInt("pos2.y") + "," + getConfig().getInt("pos2.z"));
				getServer().dispatchCommand(sender, "/replace lapis_block air");
				getServer().dispatchCommand(sender, "/replace sea_lantern air");
				getServer().dispatchCommand(sender, "/replace redstone_block air");
				getServer().dispatchCommand(sender, "/replace glowstone air");
				getServer().dispatchCommand(getServer().getConsoleSender(), "clear "+player.getName());
				getServer().dispatchCommand(getServer().getConsoleSender(), "say Welcome to the minigame, coded by kliu0105!");
				getServer().dispatchCommand(getServer().getConsoleSender(), "say Instructions on how to play are on www.moocrafttowny.tk/minigame-rules");
				getServer().dispatchCommand(getServer().getConsoleSender(), "say Get ready to fight! Destroy the enemy core!");\
				//this part specifically
				ScoreboardManager manager = Bukkit.getScoreboardManager();
				Scoreboard board = manager.getMainScoreboard();
				Player player = event.getPlayer();	
				Team teamblue = board.getTeam("Blue");
				if (teamblue.hasEntry(player.getName())) {
					event.setRespawnLocation(new Location(getServer().getWorld("plotworld"), getConfig().getInt("bluespawn.x"), getConfig().getInt("bluespawn.y"), getConfig().getInt("bluespawn.z")));
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" lapis_block "+ getConfig().getString("blocksOnStart"));
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" bow 1");
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" arrow "+ getConfig().getString("arrowsOnStart"));

						}
					}, 60L);
				} else {
					event.setRespawnLocation(new Location(getServer().getWorld("plotworld"), getConfig().getInt("redspawn.x"), getConfig().getInt("redspawn.y"), getConfig().getInt("redspawn.z")));
				}}
						public void run() {
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" redstone_block "+ getConfig().getString("blocksOnStart"));
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" bow 1");
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+player.getName()+" arrow "+ getConfig().getString("arrowsOnStart"));

			}
		}
		if (cmd.getName().equalsIgnoreCase("stopgame")){
			getServer().dispatchCommand(getServer().getConsoleSender(), "say The game has ended!");
			stopgame();
		}
		return true;
	}

	BlockFace[] directions = new BlockFace[] {BlockFace.EAST,BlockFace.WEST,BlockFace.NORTH,BlockFace.SOUTH,BlockFace.UP,BlockFace.DOWN};
	public void junk(Location location, Material from,Material to,String teamname) {
		Block startblock = getServer().getWorld("plotworld").getBlockAt(location);
		Queue<Block> needTocheckBlocks = new LinkedList<Block>();
		needTocheckBlocks.add(startblock);
		Set<Block> alreadyChecked = new HashSet<Block>();
		junk2(from,to,teamname, needTocheckBlocks, 50, alreadyChecked);
	}

	public void junk2(Material from, Material to, String teamname, Queue<Block> needTocheckBlocks, int maxBlocksToChange, Set<Block> alreadyChecked) {
		int changedBlocks = 0;
		while (!needTocheckBlocks.isEmpty()) {
			Block block = needTocheckBlocks.remove();
			alreadyChecked.add(block);
			for (BlockFace direction : directions) {
				Block nextblock = block.getRelative(direction, 1);
				if (alreadyChecked.contains(nextblock)){
					//getLogger().info("alrady checked "+nextblock.getLocation().toString());
					continue;
				}
				if (nextblock.getType() == from || nextblock.getType() == to){
					//getLogger().info("changed "+nextblock.getLocation().toString());
					nextblock.setType(to);
					needTocheckBlocks.add(nextblock);
					alreadyChecked.add(nextblock);
					changedBlocks += 1;
					if (changedBlocks > maxBlocksToChange){
						return;
					}
				} else if (nextblock.getType() == Material.BEACON){
					getLogger().info("beecon at "+nextblock.getLocation().toString());
					ScoreboardManager manager = Bukkit.getScoreboardManager();
					Scoreboard board = manager.getMainScoreboard();
					Team team = board.getTeam(teamname);
					Set<String> entries = team.getEntries();
					if (teamname == "Blue"){
						for (String entry:entries){
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+entry+" arrow 1");\
							//eventually I should clean up the console
							getLogger().info("Gave an arow! w00t");
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+ entry + " lapis_block 1");
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+ entry + " potato 1");
						}
					}
					else{
						for (String entry:entries){
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+entry+" arrow 1");
							getLogger().info("Gave an arow! w00t");
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+ entry + " redstone_block 1");
							getServer().dispatchCommand(getServer().getConsoleSender(), "give "+ entry + " potato 1");
						}
					}
				}
			}
		}
	}
}
// Thanks for reading
