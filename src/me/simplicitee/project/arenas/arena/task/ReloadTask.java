package me.simplicitee.project.arenas.arena.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;

import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.project.arenas.arena.ArenaRegion;

public class ReloadTask {
	
	private static Map<ArenaRegion, ReloadTask> tasks = new HashMap<>();

	private ArenaRegion arena;
	private Iterator<Location> steps;
	private int currentLayer, completedSteps, totalSteps;
	private long startTime, endTime;
	private boolean firstStep;
	
	private ReloadTask(ArenaRegion arena) {
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
}
