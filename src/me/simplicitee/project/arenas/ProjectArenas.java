package me.simplicitee.project.arenas;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.simplicitee.project.arenas.arena.ArenaEditor;
import me.simplicitee.project.arenas.arena.ArenaManager;
import me.simplicitee.project.arenas.command.ArenaCommand;
import me.simplicitee.project.arenas.storage.Config;

public class ProjectArenas extends JavaPlugin {

	private static ProjectArenas instance;
	private ArenaManager manager;
	private ArenaEditor editor;
	private Config config;
	
	@Override
	public void onEnable() {
		instance = this;
		
		loadConfig();
		
		manager = new ArenaManager(this);
		editor = new ArenaEditor(this);
		
		new ArenaCommand(this);
	}
	
	public void loadConfig() {
		config = new Config(new File("config.yml"));
		FileConfiguration c = config.get();
		
		c.addDefault("AutomatedReloadInterval", 600);
		c.addDefault("MessagePrefix", "&7[&aProjectArenas&7]");
		c.addDefault("TaskSpeed", 20000);
		
		config.save();
	}
	
	@Override
	public void onDisable() {
		manager.disable();
	}
	
	public static ProjectArenas getInstance() {
		return instance;
	}
	
	public void registerEvents(Listener listener) {
		instance.getServer().getPluginManager().registerEvents(listener, instance);
	}
	
	public ArenaManager getManager() {
		return manager;
	}
	
	public ArenaEditor getEditor() {
		return editor;
	}
	
	public String prefix() {
		return ChatColor.translateAlternateColorCodes('&', config.get().getString("MessagePrefix"));
	}
	
	public int getAutoInterval() {
		return Math.abs(config.get().getInt("AutomatedReloadInterval"));
	}
	
	public int getTaskSpeed() {
		return Math.abs(config.get().getInt("TaskSpeed"));
	}
	
	public void reloadConfig() {
		config.reload();
	}
	
	public File getArenasFolder() {
		File folder = new File(getDataFolder(), "/arenas/");
		
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		return folder;
	}
}
