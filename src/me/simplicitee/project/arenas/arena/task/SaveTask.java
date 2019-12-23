package me.simplicitee.project.arenas.arena.task;

import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;

import me.simplicitee.project.arenas.ProjectArenas;
import me.simplicitee.project.arenas.arena.ArenaRegion;
import me.simplicitee.project.arenas.storage.NBTStorageFile;
import me.simplicitee.project.arenas.util.BlockInfo;

public class SaveTask {
	
	private static Map<ArenaRegion, SaveTask> tasks = new HashMap<>();
	
	private ProjectArenas plugin;
	private ArenaRegion arena;
	private NBTStorageFile file;
	private Iterator<Location> steps;
	private int current;
	
	public SaveTask(ArenaRegion arena) {
		this.plugin = ProjectArenas.getInstance();
		this.arena = arena;
		
		file = new NBTStorageFile(plugin.getArenasFolder(), arena.getName().toLowerCase()).read();
		
		file.setString("name", arena.getName());
		file.setInt("size", arena.getLocations().size());
		file.setString("world", arena.getWorld().getName());
		file.setBoolean("auto", plugin.getManager().isAutoReloading(arena));
		file.setBoolean("reloading", plugin.getManager().isReloadQueued(arena));
		
		this.steps = arena.getLocations().iterator();
		tasks.put(arena, this);
	}
	
	public boolean step() {
		if (steps.hasNext()) {
			Location loc = steps.next();
			BlockInfo info = arena.getBlockInfo(loc);
			
			file.setInt("locations." + current + ".x", info.getX());
			file.setInt("locations." + current + ".y", info.getY());
			file.setInt("locations." + current + ".z", info.getZ());
			file.setString("locations." + current + ".data", Base64.getEncoder().encodeToString(info.getData().getAsString().getBytes()));
			
			String nbt = "empty";
			if (info.getNBT() != null) {
				nbt = info.getNBT().asString();
			}
			
			file.setString("locations." + current + ".nbt", Base64.getEncoder().encodeToString(nbt.getBytes()));
			
			current++;
			return false;
		}
		
		file.write();
		tasks.remove(arena);
		return true;
	}
	
	public String getArenaName() {
		return arena.getName();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SaveTask)) {
			return false;
		}
		
		SaveTask task = (SaveTask) other;
		return this.arena.equals(task.arena);
	}
	
	public static SaveTask from(ArenaRegion arena) {
		if (tasks.containsKey(arena)) {
			return tasks.get(arena);
		}
		
		return new SaveTask(arena);
	}
}
