package tk.moocrafttowny.original;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class ScheduleTask extends BukkitRunnable  {

	private final Main plugin;
	private boolean bluebrightwaslast = false;
	private boolean redbrightwaslast = false;
	Queue<Block> blueNeedTocheckBlocks = new LinkedList<Block>();
	Queue<Block> redNeedTocheckBlocks = new LinkedList<Block>();
	private Block bluestartblock;
	private Block redstartblock;
	int redtimes = 0;
	int bluetimes = 0;
	Set<Block> bluealreadyChecked = new HashSet<Block>();
	Set<Block> redalreadyChecked = new HashSet<Block>();
	
	public ScheduleTask(Main main) {
		this.plugin = (Main) main;
		this.plugin.getLogger().info("example "+String.valueOf(this));
		bluestartblock = plugin.getServer().getWorld("plotworld").getBlockAt(plugin.blueCoreLocation);
		redstartblock = plugin.getServer().getWorld("plotworld").getBlockAt(plugin.redCoreLocation);
	}

	@Override
	public void run() {
		// What you want to schedule goes here
		bluetimes += 1;
		if (blueNeedTocheckBlocks.isEmpty()) {
			if (bluetimes > 100){
				bluetimes = 0;
				bluebrightwaslast = !bluebrightwaslast;
				blueNeedTocheckBlocks.add(bluestartblock);
				bluealreadyChecked = new HashSet<Block>();
			}
		}
		if (bluebrightwaslast) {
			plugin.junk2(Material.LAPIS_BLOCK,Material.SEA_LANTERN,"Blue",blueNeedTocheckBlocks,5,bluealreadyChecked );
		} else {
			plugin.junk2(Material.SEA_LANTERN,Material.LAPIS_BLOCK,"Blue",blueNeedTocheckBlocks,5,bluealreadyChecked );
		}
		redtimes += 1;
		if (redNeedTocheckBlocks.isEmpty()) {
			if (redtimes > 100){
				redtimes = 0;
				redbrightwaslast = !redbrightwaslast;
				redNeedTocheckBlocks.add(redstartblock);
				redalreadyChecked = new HashSet<Block>();
			}
		}
		if (redbrightwaslast) {
			plugin.junk2(Material.REDSTONE_BLOCK,Material.GLOWSTONE,"Red",redNeedTocheckBlocks,5,redalreadyChecked );
		} else {
			plugin.junk2(Material.GLOWSTONE,Material.REDSTONE_BLOCK,"Red",redNeedTocheckBlocks,5,redalreadyChecked );
		}
	}

}
