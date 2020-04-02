package me.simplicitee.project.arenas.arena.task;

import java.util.HashMap;
import java.util.Map;

public abstract class ArenaTask {
	
	private static final Map<String, ArenaTask> TASKS = new HashMap<>();
	
	protected String arena;
	
	public ArenaTask(String arena) {
		this.arena = arena;
		TASKS.put(arena, this);
	}
	
	public String getArenaName() {
		return arena;
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
