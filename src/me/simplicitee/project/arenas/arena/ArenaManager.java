package me.simplicitee.project.arenas.arena;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import me.simplicitee.project.arenas.ProjectArenas;
import me.simplicitee.project.arenas.arena.task.LoadTask;
import me.simplicitee.project.arenas.arena.task.ReloadTask;
import me.simplicitee.project.arenas.arena.task.SaveTask;
import me.simplicitee.project.arenas.storage.NBTStorageFile;

public class ArenaManager {

	private ProjectArenas plugin;
	private Queue<ReloadTask> reloaders;
	private Queue<SaveTask> saving;
	private Queue<LoadTask> loading;
	private Map<String, ArenaRegion> regions;
	private Map<ArenaRegion, BukkitRunnable> autoreload;
	
	public ArenaManager(ProjectArenas plugin) {
		this.plugin = plugin;
		this.reloaders = new LinkedList<>();
		this.saving = new LinkedList<>();
		this.loading = new LinkedList<>();
		this.regions = new HashMap<>();
		this.autoreload = new HashMap<>();
		this.startLoading();
		this.startSaving();
		this.startReloading();
		this.loadStored();
	}
	
	public void startReloading() {
		new BukkitRunnable() {

			@Override
			public void run() {
				if (!reloaders.isEmpty()) {
					ReloadTask next = reloaders.peek();
					
					for (int i = 0; i < plugin.reloadSpeed(); i++) {
						if (next.step()) {
							reloaders.poll();
							plugin.getServer().broadcastMessage(plugin.prefix() + ChatColor.GREEN + " Reload of '" + ChatColor.WHITE + next.getArenaName() + ChatColor.GREEN + "' complete! (" + (next.getElapsedTime() / 1000) + "s)");
							break;
						}
					}
				}
			}
			
		}.runTaskTimer(plugin, 0, 1);
	}
	
	public void startSaving() {
		new BukkitRunnable() {

			@Override
			public void run() {
				if (!saving.isEmpty()) {
					SaveTask next = saving.peek();
					
					for (int i = 0; i < plugin.saveSpeed(); i++) {
						if (next.step()) {
							saving.poll();
							plugin.getServer().broadcastMessage(plugin.prefix() + ChatColor.GREEN + " Save of '" + ChatColor.WHITE + next.getArenaName() + ChatColor.GREEN + "' complete!");
							break;
						}
					}
				}
			}
			
		}.runTaskTimer(plugin, 0, 1);
	}
	
	public void startLoading() {
		new BukkitRunnable() {

			@Override
			public void run() {
				if (!loading.isEmpty()) {
					LoadTask next = loading.peek();
					
					for (int i = 0; i < plugin.loadSpeed(); i++) {
						if (next.step()) {
							loading.poll();
							plugin.getServer().broadcastMessage(plugin.prefix() + ChatColor.GREEN + " Load of '" + ChatColor.WHITE + next.getArenaName() + ChatColor.GREEN + "' complete!");
							break;
						}
					}
				}
			}
			
		}.runTaskTimer(plugin, 0, 1);
	}
	
	public void disable() {
		this.saveSettings();
	}
	
	public String getCurrentLoaderProgress() {
		if (reloaders.isEmpty()) {
			return ChatColor.GREEN + "No loaders running!";
		}
		
		ReloadTask loader = reloaders.peek();
		double percent = loader.getPercentCompletion();
		
		return ChatColor.YELLOW + (Math.round(percent * 100) + "% of arena '" + ChatColor.WHITE + loader.getArenaName() + ChatColor.YELLOW + "' reloaded. (" + (loader.getElapsedTime() / 1000) + "s)");
	}
	
	public String getArenaStatus(ArenaRegion arena) {
		ReloadTask loader = ReloadTask.from(arena);
		String status = "";
		if (reloaders.contains(loader)) {
			if (reloaders.peek().equals(loader)) {
				status = ChatColor.YELLOW + "reloading [" + ChatColor.WHITE + (Math.round(loader.getPercentCompletion() * 100) + "%") + ChatColor.YELLOW + "]";
			} else {
				status = ChatColor.RED + "queued";
			}
		} else if (autoreload.containsKey(arena)) {
			status = ChatColor.AQUA + "auto reload";
		} else {
			status = ChatColor.GREEN + "idle";
		}
		return status;
	}
	
	public boolean isReloadQueued(ArenaRegion arena) {
		return reloaders.contains(ReloadTask.from(arena));
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
		reloaders.remove(ReloadTask.from(arena));
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
				ProjectArenas.getInstance().getManager().queueReloading(arena);
			}
			
		};
		
		run.runTaskTimer(plugin, plugin.autoInterval() * 20, plugin.autoInterval() * 20);
		autoreload.put(arena, run);
		return true;
	}
	
	public boolean queueReloading(ArenaRegion arena) {
		ReloadTask loader = ReloadTask.from(arena);
		
		if (saving.contains(SaveTask.from(arena))) {
			return false;
		} else if (loading.contains(LoadTask.from(arena.getName()))) {
			return false;
		} else if (!reloaders.contains(loader)) {
			plugin.getServer().broadcastMessage(plugin.prefix() + ChatColor.RED + " Reload of '" + ChatColor.WHITE + arena.getName() + ChatColor.RED + "' queued!");
			reloaders.add(loader);
			return true;
		}
		
		return false;
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
			File[] files = folder.listFiles();
			
			for (File file : files) {
				if (file.getName().endsWith(".dat")) {
					loading.add(new LoadTask(file));
				}
			}
		}
	}
	
	public void saveArena(ArenaRegion arena) {
		SaveTask task = SaveTask.from(arena);
		if (!saving.contains(task)) {
			saving.add(task);
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
			file.setBoolean("reloading", reloaders.contains(ReloadTask.from(arena)));
			file.write();
		}
		
		regions.clear();
		autoreload.clear();
	}
}
