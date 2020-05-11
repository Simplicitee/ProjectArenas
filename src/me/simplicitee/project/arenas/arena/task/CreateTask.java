package me.simplicitee.project.arenas.arena.task;

import java.util.Base64;

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
	private int[] maxes, minis;
	private BlockInfo[][][] data;
	private int x, y, z;
	
	public CreateTask(String name, World world, int[] maxes, int[] minis) {
		super(name);
		this.world = world;
		this.maxes = maxes;
		this.minis = minis;
		this.data = new BlockInfo[Math.abs(maxes[0] - minis[0]) + 1][Math.abs(maxes[1] - minis[1]) + 1][Math.abs(maxes[2] - minis[2]) + 1];
		this.x = minis[0];
		this.y = minis[1];
		this.z = minis[2];
		
		file = new NBTStorageFile(ProjectArenas.getInstance().getArenasFolder(), name.toLowerCase()).read();
		
		file.setString("name", name);
		file.setString("world", world.getName());
		file.setBoolean("auto", false);
		file.setBoolean("reloading", false);
		file.setInt("minX", minis[0]);
		file.setInt("minY", minis[1]);
		file.setInt("minZ", minis[2]);
		file.setInt("maxX", maxes[0]);
		file.setInt("maxY", maxes[1]);
		file.setInt("maxZ", maxes[2]);
	}
	
	@Override
	public StepResult step() {
		BlockInfo info;
		BlockPosition bp = new BlockPosition(x, y, z);
		TileEntity tile = ((CraftWorld) world).getHandle().getTileEntity(bp);
		
		if (tile != null) {
			NBTTagCompound nbt = tile.save(new NBTTagCompound());
			info = new BlockInfo(x, y, z, world.getBlockAt(x, y, z).getBlockData(), nbt);
		} else {
			info = new BlockInfo(x, y, z, world.getBlockAt(x, y, z).getBlockData());
		}
		
		data[x - minis[0]][y - minis[1]][z - minis[2]] = info;
		String path = x + "." + y + "." + z + ".";
		file.setString(path + "data", Base64.getEncoder().encodeToString(info.getData().getAsString().getBytes()));
		
		String nbt = "E";
		if (info.getNBT() != null) {
			nbt = info.getNBT().asString();
		}
		
		file.setString(path + "nbt", Base64.getEncoder().encodeToString(nbt.getBytes()));
		
		x++;
		if (x > maxes[0]) {
			x = minis[0];
			z++;
		}
		
		if (z > maxes[2]) {
			z = minis[2];
			y++;
		}
		
		if (y > maxes[1]) {
			ArenaRegion arena = new ArenaRegion(this.arena, world.getName(), data, minis[0], minis[1], minis[2], maxes[0], maxes[1], maxes[2]);
			ProjectArenas.getInstance().getManager().registerArena(arena);
			file.write();
			return StepResult.FINISHED;
		}
		
		return StepResult.CHANGED;
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
