package me.simplicitee.project.arenas.arena;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.util.ActionBar;

import me.simplicitee.project.arenas.ProjectArenas;
import me.simplicitee.project.arenas.util.BlockInfo;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntity;

public class ArenaEditor implements Listener {

	private enum EditStep {
		SELECT_ONE, SELECT_TWO;
	}
	
	private ProjectArenas plugin;
	private Map<Player, EditStep> editors;
	private Map<Player, Block> first;
	private Map<Player, Block> second;
	private Map<Player, String> names;
	
	public ArenaEditor(ProjectArenas plugin) {
		this.plugin = plugin;
		this.editors = new HashMap<>();
		this.first = new HashMap<>();
		this.second = new HashMap<>();
		this.names = new HashMap<>();
		new BukkitRunnable() {

			@Override
			public void run() {
				for (Player player : editors.keySet()) {
					if (editors.get(player) == EditStep.SELECT_ONE) {
						if (first.containsKey(player)) {
							editors.put(player, EditStep.SELECT_TWO);
						}
						
						ActionBar.sendActionBar("Right Click a corner block", player);
					} else if (editors.get(player) == EditStep.SELECT_TWO) {
						ActionBar.sendActionBar("Right Click opposite corner block", player);
					}
				}
			}
			
		}.runTaskTimer(plugin, 0, 2);
		plugin.registerEvents(this);
	}
	
	public boolean isEditting(Player player) {
		return editors.containsKey(player);
	}
	
	public void startEditing(Player player, String name) {
		if (!editors.containsKey(player)) {
			editors.put(player, EditStep.SELECT_ONE);
			names.put(player, name);
		}
	}
	
	public void exit(Player player) {
		if (editors.containsKey(player)) {
			editors.remove(player);
			first.remove(player);
			second.remove(player);
			names.remove(player);
		}
	}
	
	public void createArena(Player player) {
		Block a = first.get(player);
		Block b = second.get(player);
		String name = names.get(player);
		World w = a.getWorld();	
		
		int xMax = Math.max(a.getX(), b.getX());
		int xMin = Math.min(a.getX(), b.getX());
		int yMax = Math.max(a.getY(), b.getY());
		int yMin = Math.min(a.getY(), b.getY());
		int zMax = Math.max(a.getZ(), b.getZ());
		int zMin = Math.min(a.getZ(), b.getZ());
		Map<Location, BlockInfo> data = new HashMap<>();
		
		for (int i = xMin; i <= xMax; i++) {
			for (int j = yMin; j <= yMax; j++) {
				for (int k = zMin; k <= zMax; k++) {
					Location loc = new Location(w, i, j, k);
					BlockInfo info;
					BlockPosition bp = new BlockPosition(i, j, k);
					TileEntity tile = ((CraftWorld) w).getHandle().getTileEntity(bp);
					
					if (tile != null) {
						NBTTagCompound nbt = tile.save(new NBTTagCompound());
						info = new BlockInfo(i, j, k, loc.getBlock().getBlockData(), nbt);
					} else {
						info = new BlockInfo(i, j, k, loc.getBlock().getBlockData());
					}
					
					data.put(loc, info);
				}
			}
		}
		
		ArenaRegion arena = new ArenaRegion(name, w, data);
		plugin.getManager().registerArena(arena);
		plugin.getManager().saveArena(arena);
		player.sendMessage(plugin.prefix() + ChatColor.GREEN + " Created arena " + name);
		exit(player);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		
		if (!editors.containsKey(player)) {
			return;
		}
		
		if (editors.get(player) == EditStep.SELECT_ONE) {
			first.put(player, event.getClickedBlock());
		} else if (editors.get(player) == EditStep.SELECT_TWO) {
			second.put(player, event.getClickedBlock());
			createArena(player);
		}
		event.setCancelled(true);
	}
}
