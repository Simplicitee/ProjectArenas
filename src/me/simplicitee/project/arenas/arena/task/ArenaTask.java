package me.simplicitee.project.arenas.arena.task;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.scheduler.BukkitRunnable;

import me.simplicitee.project.arenas.ProjectArenas;

public abstract class ArenaTask extends BukkitRunnable {
	
	private static final Map<String, ArenaTask> TASKS = new HashMap<>();
	
	protected String arena;
	protected boolean async = true;
	private boolean started = false;
	
	public ArenaTask(String arena) {
		this.arena = arena;
		TASKS.put(arena, this);
	}
	
	public String getArenaName() {
		return arena;
	}

	@Override
	public void run() {
		if (this.isCancelled()) {
			return;
		}
		
		for (int i = 0; i < ProjectArenas.getInstance().getTaskSpeed(); i++) {
			if (step()) {
				ProjectArenas.getInstance().getServer().broadcastMessage(ProjectArenas.getInstance().prefix() + " " + getFinishMessage());
				cancel();
				break;
			}
		}
	}
	
	public void startTask() {
		if (started) {
			return;
		}
		
		this.started = true;
		if (async) {
			this.runTaskTimerAsynchronously(ProjectArenas.getInstance(), 0, 1);
		} else {
			this.runTaskTimer(ProjectArenas.getInstance(), 0, 1);
		}
	}
	
	public boolean hasStarted() {
		return started;
	}

	public abstract boolean step();
	public abstract String getType();
	public abstract String getFinishMessage();
	public abstract String getProgressMessage();
	public abstract String getStatus();
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ArenaTask) {
			ArenaTask task = (ArenaTask) other;
			
			return this.getType().equals(task.getType()) && this.getArenaName().equals(task.getArenaName());
		}
		
		return false;
	}
	
	public static ArenaTask from(String name) {
		if (TASKS.containsKey(name)) {
			return TASKS.get(name);
		}
		
		return null;
	}
}
