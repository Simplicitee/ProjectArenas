package me.simplicitee.project.arenas.arena.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.project.arenas.arena.ArenaRegion;

public class ReloadTask extends ArenaTask {
	
	private static Map<ArenaRegion, ReloadTask> tasks = new HashMap<>();

	private ArenaRegion arena;
	private Iterator<Location> steps;
	private int currentLayer, completedSteps, totalSteps;
	private long startTime, endTime;
	private boolean firstStep;
	
	private ReloadTask(ArenaRegion arena) {
		super(arena.getName());
		this.arena = arena;
		this.currentLayer = arena.getMinLayerY();
		this.steps = arena.getLayer(currentLayer).iterator();
		this.completedSteps = 0;
		this.totalSteps = arena.getLocations().size();
		this.startTime = 0;
		this.endTime = -1;
		this.firstStep = true;
		tasks.put(arena, this);
	}
	
	public String getArenaName() {
		return arena.getName();
	}
	
	/**
	 * Progresses the reloading of the arena to the next step
	 * @return true if no more steps to complete, false otherwise
	 */
	public boolean step() {
		if (firstStep) {
			startTime = System.currentTimeMillis();
			firstStep = false;
		}
		
		if (steps.hasNext()) {
			Location next = steps.next();
			
			if (TempBlock.isTempBlock(next.getBlock())) {
				TempBlock.get(next.getBlock()).revertBlock();
			}
			
			arena.getBlockInfo(next).update(arena.getWorld());
			
			completedSteps++;
			return false;
		} else {
			currentLayer++;
			
			if (currentLayer <= arena.getMaxLayerY()) {
				steps = arena.getLayer(currentLayer).iterator();
				return false;
			}
		}
		
		endTime = System.currentTimeMillis();
		tasks.remove(arena);
		return true;
	}
	
	public int getCompletedSteps() {
		return completedSteps;
	}
	
	public int getTotalSteps() {
		return totalSteps;
	}
	
	public double getPercentCompletion() {
		return (double) completedSteps / totalSteps;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getEndTime() {
		return endTime;
	}
	
	public long getElapsedTime() {
		if (endTime != -1) {
			return endTime - startTime;
		}
		
		return System.currentTimeMillis() - startTime;
	}
	
	public void delete() {
		steps = null;
		tasks.remove(arena);
		arena = null;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ReloadTask)) {
			return false;
		}
		
		ReloadTask task = (ReloadTask) other;
		return this.arena.equals(task.arena);
	}
	
	public static ReloadTask from(ArenaRegion arena) {
		if (tasks.containsKey(arena)) {
			return tasks.get(arena);
		}
		
		return new ReloadTask(arena);
	}

	@Override
	public String getType() {
		return "Reloading";
	}

	@Override
	public String getFinishMessage() {
		return ChatColor.GREEN + "Reload of '" + ChatColor.WHITE + getArenaName() + ChatColor.GREEN + "' complete! (" + (getElapsedTime() / 1000) + "s)";
	}

	@Override
	public String getProgressMessage() {
		double percent = getPercentCompletion();
		
		return ChatColor.YELLOW + (Math.round(percent * 100) + "% of arena '" + ChatColor.WHITE + getArenaName() + ChatColor.YELLOW + "' reloaded. (" + (getElapsedTime() / 1000) + "s)");
	}

	@Override
	public String getStatus() {
		if (getPercentCompletion() == 0) {
			return ChatColor.RED + "reload queued";
		}
		
		return ChatColor.YELLOW + "reloading [" + ChatColor.WHITE + (Math.round(getPercentCompletion() * 100) + "%") + ChatColor.YELLOW + "]";
	}
}
