package me.simplicitee.project.arenas.arena.task;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;

import me.simplicitee.project.arenas.ProjectArenas;
import me.simplicitee.project.arenas.arena.ArenaRegion;
import me.simplicitee.project.arenas.storage.NBTStorageFile;
import me.simplicitee.project.arenas.util.BlockInfo;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntity;

public class CreateTask extends ArenaTask {

	private NBTStorageFile file;
	private World world;
	private int[] maxes, minis;
	private Map<Location, BlockInfo> data;
	private Location current;
	private int x, y, z, i;
	
	public CreateTask(String name, World world, int[] maxes, int[] minis) {
		super(name);
		this.world = world;
		this.maxes = maxes;
		this.minis = minis;
		this.data = new HashMap<>();
		this.x = minis[0];
		this.y = minis[1];
		this.z = minis[2];
		this.i = 0;
		this.current = new Location(world, x, y, z);
		
		file = new NBTStorageFile(ProjectArenas.getInstance().getArenasFolder(), name.toLowerCase()).read();
		
		file.setString("name", name);
		file.setString("world", world.getName());
		file.setBoolean("auto", false);
		file.setBoolean("reloading", false);
	}
	
	@Override
	public boolean step() {
		BlockInfo info;
		BlockPosition bp = new BlockPosition(x, y, z);
		TileEntity tile = ((CraftWorld) world).getHandle().getTileEntity(bp);
		
		if (tile != null) {
			NBTTagCompound nbt = tile.save(new NBTTagCompound());
			info = new BlockInfo(x, y, z, current.getBlock().getBlockData(), nbt);
		} else {
			info = new BlockInfo(x, y, z, current.getBlock().getBlockData());
		}
		
		data.put(current.clone(), info);
		file.setInt("locations." + i + ".x", x);
		file.setInt("locations." + i + ".y", y);
		file.setInt("locations." + i + ".z", z);
		file.setString("locations." + i + ".data", Base64.getEncoder().encodeToString(info.getData().getAsString().getBytes()));
		
		String nbt = "E";
		if (info.getNBT() != null) {
			nbt = info.getNBT().asString();
		}
		
		file.setString("locations." + i + ".nbt", Base64.getEncoder().encodeToString(nbt.getBytes()));
		
		x++;
		
		if (x > maxes[0]) {
			x = minis[0];
			y++;
		}
		
		if (y > maxes[1]) {
			y = minis[1];
			z++;
		}
		
		if (z > maxes[2]) {
			ArenaRegion arena = new ArenaRegion(this.arena, world.getName(), data);
			ProjectArenas.getInstance().getManager().registerArena(arena);
			file.setInt("size", arena.getSize());
			file.write();
			return true;
		}
		
		current.setX(x);
		current.setY(y);
		current.setZ(z);
		i++;
		
		return false;
	}

	@Override
	public String getType() {
		return "Create";
	}

	@Override
	public String getFinishMessage() {
		return ChatColor.GREEN + "Creation of '" + ChatColor.WHITE + getArenaName() + ChatColor.GREEN + "' complete!";
	}

	@Override
	public String getProgressMessage() {
		return ChatColor.YELLOW + "Creating arena '" + ChatColor.WHITE + getArenaName() + ChatColor.YELLOW + "'";
	}

	@Override
	public String getStatus() {
		return "";
	}
}
