package me.simplicitee.project.arenas.arena.task;

import org.bukkit.ChatColor;
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
	private BlockInfo[][][] data;
	private int x, y, z;
	private int maxX, maxY, maxZ, minX, minY, minZ;
	
	public CreateTask(String name, World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		super(name);
		this.world = world;
		this.data = new BlockInfo[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];
		this.x = minX;
		this.y = minY;
		this.z = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		
		file = new NBTStorageFile(ProjectArenas.getInstance().getArenasFolder(), name.toLowerCase()).read();
		
		file.setString("name", name);
		file.setString("world", world.getName());
		file.setBoolean("auto", false);
		file.setBoolean("reloading", false);
		file.setInt("minX", minX);
		file.setInt("minY", minY);
		file.setInt("minZ", minZ);
		file.setInt("maxX", maxX);
		file.setInt("maxY", maxY);
		file.setInt("maxZ", maxZ);
	}
	
	@Override
	public boolean step() {
		if (x > maxX || y > maxY || z > maxZ) {
			return false;
		}
		
		BlockInfo info;
		BlockPosition bp = new BlockPosition(x, y, z);
		TileEntity tile = ((CraftWorld) world).getHandle().getTileEntity(bp);
		
		if (tile != null) {
			NBTTagCompound nbt = tile.save(new NBTTagCompound());
			info = new BlockInfo(x, y, z, world.getBlockAt(x, y, z).getBlockData(), nbt);
		} else {
			info = new BlockInfo(x, y, z, world.getBlockAt(x, y, z).getBlockData());
		}
		
		try {
			data[x - minX][y - minY][z - minZ] = info;
		} catch (Exception e) {
			return false;
		}
		
		String path = x + "." + y + "." + z + ".";
		file.setString(path + "data", info.getData().getAsString());
		
		String nbt = "E";
		if (info.getNBT() != null) {
			nbt = info.getNBT().asString();
		}
		
		file.setString(path + "nbt", nbt);
		
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
		
		file.write();
		ArenaRegion arena = new ArenaRegion(this.arena, world.getName(), data, minX, minY, minZ, maxX, maxY, maxZ);
		ProjectArenas.getInstance().getManager().registerArena(arena);
		return true;
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
