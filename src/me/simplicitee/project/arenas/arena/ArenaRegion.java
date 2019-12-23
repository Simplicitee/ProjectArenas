package me.simplicitee.project.arenas.arena;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import me.simplicitee.project.arenas.util.BlockInfo;

public class ArenaRegion {

	private String name;
	private World world;
	private Map<Location, BlockInfo> region;
	private Map<Integer, Set<Location>> layers;
	private int minLayer = 256, maxLayer = -1;
	
	public ArenaRegion(String name, World world, Map<Location, BlockInfo> region) {
		this.name = name;
		this.world = world;
		this.region = region;
		this.layers = new HashMap<>();
		
		for (Location loc : region.keySet()) {
			int y = loc.getBlockY();
			if (y < minLayer) {
				minLayer = y;
			}
			
			if (y > maxLayer) {
				maxLayer = y;
			}
			
			if (!layers.containsKey(y)) {
				layers.put(y, new HashSet<>());
			}
			
			layers.get(y).add(loc);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public World getWorld() {
		return world;
	}
	
	public int getMinLayerY() {
		return minLayer;
	}
	
	public int getMaxLayerY() {
		return maxLayer;
	}
	
	public int getLayerCount() {
		return layers.size();
	}
	
	public Set<Location> getLayer(int y) {
		Set<Location> layer = new HashSet<>();
		
		if (layers.containsKey(y)) {
			layer.addAll(layers.get(y));
		}
		
		return layer;
	}
	
	public Set<Location> getLocations() {
		return new HashSet<>(region.keySet());
	}
	
	public BlockInfo getBlockInfo(Location loc) {
		if (region.containsKey(loc)) {
			return region.get(loc);
		}
		
		return null;
	}
}
