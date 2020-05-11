package me.simplicitee.project.arenas.util;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;

import com.projectkorra.projectkorra.util.TempBlock;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntity;

public class BlockInfo {

	public NBTTagCompound info;
	public BlockData data;
	public int x, y, z;
	public BlockPosition pos;
	
	public BlockInfo(int x, int y, int z, BlockData data, NBTTagCompound info) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.data = data;
		this.info = info;
		this.pos = new BlockPosition(x, y, z);
	}
	
	public BlockInfo(int x, int y, int z, BlockData data) {
		this(x, y, z, data, null);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public BlockData getData() {
		return data;
	}
	
	public NBTTagCompound getNBT() {
		return info;
	}
	
	public BlockPosition getPosition() {
		return pos;
	}
	
	public boolean reload(World world) {
		Block b = world.getBlockAt(x, y, z);
		
		if (TempBlock.isTempBlock(b)) {
			TempBlock.get(b).revertBlock();
		}
		
		if (b.getBlockData().matches(data)) {
			return false;
		}
		
		b.setBlockData(data, false);
		
		if (info != null) {
			TileEntity tile = ((CraftWorld) world).getHandle().getTileEntity(pos);
			
			if (tile != null) {
				tile.load(info);
			}
		}
		
		return true;
	}
}
