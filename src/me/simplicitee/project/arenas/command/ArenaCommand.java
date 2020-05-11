package me.simplicitee.project.arenas.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.simplicitee.project.arenas.ProjectArenas;
import me.simplicitee.project.arenas.arena.ArenaRegion;

public class ArenaCommand implements CommandExecutor {
	
	private ProjectArenas plugin;
	
	public ArenaCommand(ProjectArenas plugin) {
		this.plugin = plugin;
		
		plugin.getCommand("projectarenas").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(plugin.prefix() + ChatColor.WHITE + " - Commands - <name> is an arena name");
			sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " /projectarenas create <name>");
			sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " /projectarenas delete <name>");
			sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " /projectarenas reload <name>");
			sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " /projectarenas automate <name>");
			sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " /projectarenas info <name>");
			sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " /projectarenas restart");
			sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " /projectarenas progress");
			sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " /projectarenas exit");
			sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " /projectarenas list");
			return true;
		} else if (args.length == 1) {
			String arg = args[0];
			if (arg.equalsIgnoreCase("exit")) {
				if (!sender.hasPermission("projectarenas.editor")) {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " You do not have permission to edit arenas!");
					return true;
				}
				
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command can only be run by players!");
					return true;
				}
				
				Player player = (Player) sender;
				
				if (plugin.getEditor().isEditting(player)) {
					plugin.getEditor().exit(player);
					player.sendMessage(plugin.prefix() + ChatColor.GREEN + " Exited arena edit mode!");
				} else {
					player.sendMessage(plugin.prefix() + ChatColor.RED + " You are not in arena edit mode!");
				}
				return true;
			} else if (arg.equalsIgnoreCase("list")) {
				if (plugin.getManager().getDynamicArenaList().isEmpty()) {
					sender.sendMessage(plugin.prefix() + ChatColor.WHITE + " No arenas!");
				} else {
					sender.sendMessage(plugin.prefix() + ChatColor.WHITE + " - Arenas -");
					for (String arena : plugin.getManager().getDynamicArenaList()) {
						sender.sendMessage(plugin.prefix() + ChatColor.WHITE + " " + arena);
					}
				}
				return true;
			} else if (arg.equalsIgnoreCase("progress")) {
				sender.sendMessage(plugin.prefix() + " " + plugin.getManager().getCurrentLoaderProgress());
				return true;
			} else if (arg.equalsIgnoreCase("restart")) {
				if (!sender.hasPermission("projectarenas.editor")) {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " You do not have permission to edit arenas!");
					return true;
				}
				
				plugin.reloadConfig();
				sender.sendMessage(plugin.prefix() + ChatColor.AQUA + " Reloaded config!");
				return true;
			}
		} else if (args.length == 2) {
			String arg = args[0];
			String name = args[1].toLowerCase();
			ArenaRegion arena = plugin.getManager().getArena(name);
			
			if (arg.equalsIgnoreCase("create")) {
				if (!sender.hasPermission("projectarenas.editor")) {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " You do not have permission to edit arenas!");
					return true;
				}
				
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command can only be run by players!");
					return true;
				}
				
				Player player = (Player) sender;
				
				if (arena != null) {
					player.sendMessage(plugin.prefix() + ChatColor.RED + " Arena with that name already exists!");
					return true;
				}
				
				player.sendMessage(plugin.prefix() + ChatColor.GREEN + " You have entered arena edit mode for creation.");
				plugin.getEditor().startEditing(player, name);
				return true;
			} else if (arg.equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("projectarenas.reload")) {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " You do not have permission to edit arenas!");
					return true;
				}
				
				if (arena == null) {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " Arena with that name doesn't exist!");
					return true;
				}
				
				if (plugin.getManager().queueReload(arena)) {
					sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " Queued reload of arena '" + ChatColor.WHITE + name + ChatColor.GREEN + "'!");
				} else {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " Arena '" + ChatColor.WHITE + name + ChatColor.RED + "' cannot be queued right now!");
				}
				return true;
			} else if (arg.equalsIgnoreCase("automate")) {
				if (!sender.hasPermission("projectarenas.editor")) {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " You do not have permission to edit arenas!");
					return true;
				}
				
				if (arena == null) {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " Arena with that name doesn't exist!");
					return true;
				}
				
				if (plugin.getManager().toggleAutoReloader(arena)) {
					sender.sendMessage(plugin.prefix() + ChatColor.GREEN + " Setting arena '" + ChatColor.WHITE + name + ChatColor.GREEN + "' to auto reload!");
				} else {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " Cancelling arena '" + ChatColor.WHITE + name + ChatColor.RED + "' auto reload!");
				}
				return true;
			} else if (arg.equalsIgnoreCase("delete")) {
				if (!sender.hasPermission("projectarenas.editor")) {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " You do not have permission to edit arenas!");
					return true;
				}
				
				if (arena == null) {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " Arena with that name doesn't exist!");
					return true;
				}
				
				plugin.getManager().delete(arena);
				sender.sendMessage(plugin.prefix() + ChatColor.RED + " Deleted arena '" + ChatColor.WHITE + name + ChatColor.RED + "'!");
				return true;
			} else if (arg.equalsIgnoreCase("info")) {
				if (arena == null) {
					sender.sendMessage(plugin.prefix() + ChatColor.RED + " Arena with that name doesn't exist!");
					return true;
				}
				
				sender.sendMessage(plugin.prefix() + ChatColor.WHITE + bold(" Arena '") + ChatColor.GREEN + bold(name) + ChatColor.WHITE + bold("' Info "));
				sender.sendMessage(plugin.prefix() + ChatColor.WHITE + " World: " + ChatColor.GREEN + arena.getWorld().getName());
				sender.sendMessage(plugin.prefix() + ChatColor.WHITE + " Region: " + ChatColor.GREEN + arena.getRegionString());
				sender.sendMessage(plugin.prefix() + ChatColor.WHITE + " Status: " + ChatColor.GREEN + plugin.getManager().getArenaStatus(arena));
				sender.sendMessage(plugin.prefix() + ChatColor.WHITE + " Size: " + ChatColor.GREEN + arena.getSize() + ChatColor.WHITE + " blocks");
				sender.sendMessage(plugin.prefix() + ChatColor.WHITE + " Est. Reload Time: " + ChatColor.GREEN + (arena.getSize() / (plugin.getTaskSpeed() * 20)) + ChatColor.WHITE + " seconds");
				return true;
			}
		}
		
		sender.sendMessage(ChatColor.RED + "Unknown arguments given! Check /pas");
		return true;
	}
	
	private String bold(String str) {
		return ChatColor.BOLD + str;
	}
}
