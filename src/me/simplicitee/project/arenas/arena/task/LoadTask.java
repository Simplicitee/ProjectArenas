package me.simplicitee.project.arenas.arena.task;

import java.io.File;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.simplicitee.project.arenas.ProjectArenas;
import me.simplicitee.project.arenas.arena.ArenaRegion;
import me.simplicitee.project.arenas.storage.NBTStorageFile;
import me.simplicitee.project.arenas.util.BlockInfo;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class LoadTask extends ArenaTask {

	private static HashMap<String, LoadTask> tasks = new HashMap<>();
	
	private ProjectArenas plugin;
	private World world;
	private boolean auto, reloading;
	private NBTStorageFile file;
	private int x, y, z, minX, maxX, minY, maxY, minZ, maxZ;
	private BlockInfo[][][] blockDatas;
	
	public LoadTask(File f) {
		super(f.getName().replace(".dat", ""));
		this.plugin = ProjectArenas.getInstance();
		this.file = new NBTStorageFile(f).read();
		
		this.minX = file.getInt("minX");
		this.minY = file.getInt("minY");
		this.minZ = file.getInt("minZ");
		this.maxX = file.getInt("maxX");
		this.maxY = file.getInt("maxY");
		this.maxZ = file.getInt("maxZ");
		this.x = minX;
		this.y = minY;
		this.z = minZ;
		this.blockDatas = new BlockInfo[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];
		this.world = plugin.getServer().getWorld(file.getString("world"));
		this.auto = file.getBoolean("auto");
		this.reloading = file.getBoolean("reloading");
		
		tasks.put(arena, this);
	}
	
	@Override
	public boolean step() {
		if (world == null) {
			world = plugin.getServer().getWorld(file.getString("world"));
			return false;
		}
		
		if (x > maxX || y > maxY || z > maxZ) {
			return false;
		}
		
		String path = x + "." + y + "." + z + ".";
		try {
			BlockData bData = plugin.getServer().createBlockData(file.getString(path + "data"));
			String nbt = file.getString(path + "nbt");
			NBTTagCompound tag = null;
			
			if (!nbt.equals("E")) {
				try {
					tag = MojangsonParser.parse(nbt);
				} catch (CommandSyntaxException e) {
					tag = null;
				}
			}
			
			blockDatas[x - minX][y - minY][z - minZ] = new BlockInfo(x, y, z, bData, tag);
		} catch (Exception e) {}
		
		x++;
		if (x <= maxX) {
			return false;
		} else {
			x = minX;
		}
		
		z++;
		if (z <= maxZ) {
			return false;
		} else {
			z = minZ;
		}
		
		y++;
		if (y <= maxY) {
			return false;
		}
		
		ArenaRegion arena = new ArenaRegion(this.arena, world.getName(), blockDatas, minX, minY, minZ, maxX, maxY, maxZ);
		plugin.getManager().registerArena(arena);
		
		if (reloading) {
			plugin.getManager().queueReload(arena);
		}
		
		if (auto) {
			plugin.getManager().toggleAutoReloader(arena);
		}
		
		tasks.remove(this.arena);
		return true;
	}
	
	@Override
	public String getType() {
		return "Load";
	}
	
	public static LoadTask from(String name) {
		if (tasks.containsKey(name)) {
			return tasks.get(name);
		}
		
		return new LoadTask(new File(name + ".dat"));
	}

	@Override
	public String getFinishMessage() {
		return ChatColor.GREEN + "Load of '" + ChatColor.WHITE + getArenaName() + ChatColor.GREEN + "' complete!";
	}

	@Override
	public String getProgressMessage() {
		return ChatColor.YELLOW + "Loading arena '" + ChatColor.WHITE + getArenaName() + ChatColor.YELLOW + "'";
	}

	@Override
	public String getStatus() {
		return "loading";
	}
}
