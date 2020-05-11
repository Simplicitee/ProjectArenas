package me.simplicitee.project.arenas.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import me.simplicitee.project.arenas.util.BlockInfo;

public class ArenaRegion {

	private String name;
	private String world;
	private BlockInfo[][][] region;
	private int minX, maxX, minY, maxY, minZ, maxZ;
	
	public ArenaRegion(String name, String world, BlockInfo[][][] region, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.name = name;
		this.world = world;
		this.region = region;
		
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	
	public String getName() {
		return name;
	}
	
	public World getWorld() {
		return Bukkit.getWorld(world);
	}
	
	public int getMinX() {
		return minX;
	}
	
	public int getMinY() {
		return minY;
	}
	
	public int getMinZ() {
		return minZ;
	}
	
	public int getMaxX() {
		return maxX;
	}
	
	public int getMaxY() {
		return maxY;
	}
	
	public int getMaxZ() {
		return maxZ;
	}
	
	public int getLength() {
		return maxX - minX;
	}
	
	public int getHeight() {
		return maxY - minY;
	}
	
	public int getWidth() {
		return maxZ - minZ;
	}
	
	public int getSize() {
		return getLength() * getHeight() * getWidth();
	}
	
	private String regionStringHelper(int x, int y, int z) {
		return "&a(&f" + x + "&a, &f" + y + "&a, &f" + z + "&a)";
	}
	
	public String getRegionString() {
		return ChatColor.translateAlternateColorCodes('&', regionStringHelper(minX, minY, minZ) + " -> " + regionStringHelper(maxX, maxY, maxZ));
	}
	
	public BlockInfo getBlockInfo(Location loc) {
		return getBlockInfo(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	public BlockInfo getBlockInfo(int x, int y, int z) {
		if (x < minX || x > maxX) {
			return null;
		}
		
		if (y < minY || y > maxY) {
			return null;
		}
		
		if (z < minZ || z > maxZ) {
			return null;
		}
		
		return region[x - minX][y - minY][z - minZ];
	}
	
	public void reload(World world, int x, int y, int z) {
		getBlockInfo(x, y, z).reload(world);
	}
}
