package me.simplicitee.project.arenas.arena;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import me.simplicitee.project.arenas.ProjectArenas;
import me.simplicitee.project.arenas.arena.task.ArenaTask;
import me.simplicitee.project.arenas.arena.task.LoadTask;
import me.simplicitee.project.arenas.arena.task.ReloadTask;
import me.simplicitee.project.arenas.storage.NBTStorageFile;

public class ArenaManager {

	private ProjectArenas plugin;
	private Queue<ArenaTask> tasks;
	private Map<String, ArenaRegion> regions;
	private Map<ArenaRegion, BukkitRunnable> autoreload;
	private boolean loading = false;
	
	public ArenaManager(ProjectArenas plugin) {
		this.plugin = plugin;
		this.tasks = new LinkedList<>();
		this.regions = new HashMap<>();
		this.autoreload = new HashMap<>();
		this.loadStored();
		
		new BukkitRunnable() {

			@Override
			public void run() {
				if (tasks.isEmpty()) {
					return;
				}
				
				ArenaTask current = tasks.peek();
				if (!current.hasStarted()) {
					current.startTask();
				} else if (current.isCancelled()) {
					tasks.poll();
				}
			}
			
		}.runTaskTimer(plugin, 1, 20);
	}
	
	public void disable() {
		this.saveSettings();
	}
	
	public Set<String> getArenas() {
		return new HashSet<>(regions.keySet());
	}
	
	public String getCurrentLoaderProgress() {
		if (loading) {
			return ChatColor.RED + "Loading arenas!";
		}
		
		if (tasks.isEmpty()) {
			return ChatColor.GREEN + "No loaders running!";
		}
		
		return tasks.peek().getProgressMessage();
	}
	
	public String getArenaStatus(ArenaRegion arena) {
		ArenaTask task = ArenaTask.from(arena.getName());
		String status = "";
		
		if (tasks.contains(task)) {
			status = task.getStatus();
		} else if (autoreload.containsKey(arena)) {
			status = ChatColor.AQUA + "auto reload";
		} else {
			status = ChatColor.GREEN + "idle";
		}
		return status;
	}
	
	public boolean isReloadQueued(ArenaRegion arena) {
		return tasks.contains(ReloadTask.from(arena));
	}
	
	public boolean isAutoReloading(ArenaRegion arena) {
		return autoreload.containsKey(arena);
	}
	
	public List<String> getDynamicArenaList() {
		List<String> list = new ArrayList<>();
		String adding = "";
		for (String name : regions.keySet()) {
			ArenaRegion arena = regions.get(name);
			
			adding = name + ": " + getArenaStatus(arena);
			
			list.add(adding);
		}
		
		return list;
	}
	
	public void delete(ArenaRegion arena) {
		File folder = new File(plugin.getDataFolder(), "/arenas/");
		
		if (!folder.exists()) {
			folder.mkdirs();
			return;
		}
		
		String fileName = arena.getName().toLowerCase() + ".dat";
		File file = new File(folder, fileName);
		
		if (file.exists()) {
			file.delete();
		}
		
		regions.remove(arena.getName().toLowerCase());
		if (autoreload.containsKey(arena)) {
			autoreload.get(arena).cancel();
		}
		autoreload.remove(arena);
		tasks.remove(ArenaTask.from(arena.getName()));
		ReloadTask.from(arena).delete();
	}
	
	public boolean toggleAutoReloader(ArenaRegion arena) {
		if (autoreload.containsKey(arena)) {
			autoreload.get(arena).cancel();
			autoreload.remove(arena);
			return false;
		}
		
		BukkitRunnable run = new BukkitRunnable() {

			@Override
			public void run() {
				ProjectArenas.getInstance().getManager().queueReload(arena);
			}
			
		};
		
		run.runTaskTimer(plugin, 1200 * autoreload.size() + plugin.getAutoInterval() * 20, plugin.getAutoInterval() * 20);
		autoreload.put(arena, run);
		return true;
	}
	
	public boolean queueTask(ArenaTask task) {
		if (tasks.contains(task)) {
			return false;
		}
		
		tasks.add(task);
		return true;
	}
	
	public boolean queueReload(ArenaRegion arena) {
		return queueTask(ReloadTask.from(arena));
	}
	
	public ArenaRegion getArena(String name) {
		name = name.toLowerCase();
		if (regions.containsKey(name)) {
			return regions.get(name);
		}
		
		return null;
	}
	
	public void registerArena(ArenaRegion arena) {
		String name = arena.getName().toLowerCase();
		if (!regions.containsKey(name)) {
			regions.put(name, arena);
		}
	}
	
	private void loadStored() {
		File folder = new File(plugin.getDataFolder(), "/arenas/");
		
		if (!folder.exists()) {
			folder.mkdirs();
		} else {
			final File[] files = folder.listFiles();
			LoadTask current = null;
			int i = 0;
			
			for (i = 0; i < files.length; i++) {
				if (files[i].getName().endsWith(".dat")) {
					current = new LoadTask(files[i]);
					break;
				}
			}
			
			if (current == null) {
				return;
			}
			
			final int init = i;
			final LoadTask start = current;
			loading = true;
			
			new BukkitRunnable() {
				
				private LoadTask working = start;
				private int index = init;
				
				@Override
				public void run() {
					if (this.isCancelled()) {
						return;
					}
					
					if (index >= files.length) {
						loading = false;
						cancel();
						return;
					}
					
					for (int i = 0; i < plugin.getTaskSpeed(); i++) {
						if (working.step()) {
							plugin.getServer().broadcastMessage(plugin.prefix() + " " + working.getFinishMessage());
							
							for (int temp = ++index; temp < files.length; index = ++temp) {
								if (files[temp].getName().endsWith(".dat")) {
									working = new LoadTask(files[temp]);
									break;
								}
							}
							
							break;
						}
					}
				}
			}.runTaskTimerAsynchronously(plugin, 0, 1);
		}
	}
	
	private void saveSettings() {
		File folder = new File(plugin.getDataFolder(), "/arenas/");
		
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		for (ArenaRegion arena : regions.values()) {
			NBTStorageFile file = new NBTStorageFile(folder, arena.getName().toLowerCase()).read();
			
			file.setBoolean("auto", autoreload.containsKey(arena));
			file.setBoolean("reloading", tasks.contains(ReloadTask.from(arena)));
			file.write();
		}
		
		regions.clear();
		autoreload.clear();
	}
	
	public boolean isLoading() {
		return loading;
	}
}
