package me.simplicitee.project.arenas.arena.task;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.simplicitee.project.arenas.ProjectArenas;
import me.simplicitee.project.arenas.arena.ArenaRegion;
import me.simplicitee.project.arenas.storage.NBTStorageFile;
import me.simplicitee.project.arenas.util.BlockInfo;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class LoadTask {

	private static HashMap<String, LoadTask> tasks = new HashMap<>();
	
	private ProjectArenas plugin;
	private String name;
	private World world;
	private boolean auto, reloading;
	private NBTStorageFile file;
	private int current, size;
	private Map<Location, BlockInfo> blockDatas;
	
	public LoadTask(File f) {
		this.plugin = ProjectArenas.getInstance();
		this.file = new NBTStorageFile(f).read();
		this.blockDatas = new HashMap<>();
		
		this.name = file.getString("name");
		this.size = file.getInt("size");
		this.world = plugin.getServer().getWorld(file.getString("world"));
		this.auto = file.getBoolean("auto");
		this.reloading = file.getBoolean("reloading");
		this.current = 0;
		
		tasks.put(name, this);
	}
	
	public boolean step() {
		if (world == null) {
			world = plugin.getServer().getWorld(file.getString("world"));
			return false;
		}
		
		if (current < size) {
			int x = file.getInt("locations." + current + ".x"), y = file.getInt("locations." + current + ".y"), z = file.getInt("locations." + current + ".z");
			Location loc = new Location(world, x, y, z);
			BlockData bData = plugin.getServer().createBlockData(new String(Base64.getDecoder().decode(file.getString("locations." + current + ".data"))));
			String nbt = new String(Base64.getDecoder().decode(file.getString("locations." + current + ".nbt")));
			NBTTagCompound tag = null;
			
			if (!nbt.equals("empty")) {
				try {
					tag = MojangsonParser.parse(nbt);
				} catch (CommandSyntaxException e) {
					tag = null;
				}
			}
			
			BlockInfo info = new BlockInfo(x, y, z, bData, tag);
			blockDatas.put(loc, info);
			
			current++;
			return false;
		}
		
		ArenaRegion arena = new ArenaRegion(name, world.getName(), blockDatas);
		plugin.getManager().registerArena(arena);
		
		if (reloading) {
			plugin.getManager().queueReloading(arena);
		}
		
		if (auto) {
			plugin.getManager().toggleAutoReloader(arena);
		}
		
		tasks.remove(name);
		return true;
	}
	
	public String getArenaName() {
		return name;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LoadTask)) {
			return false;
		}
		
		LoadTask task = (LoadTask) other;
		return this.name.equals(task.name);
	}
	
	public static LoadTask from(String name) {
		if (tasks.containsKey(name)) {
			return tasks.get(name);
		}
		
		return new LoadTask(new File(name + ".dat"));
	}
}
