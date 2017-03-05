package me.Ikillnukes.sjc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

public class Commands
	implements CommandExecutor
{

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player p = null;
		if(sender instanceof Player) {
			p = (Player)sender;
		}
		if(args.length == 0) 
		{
			sender.sendMessage(ChatColor.AQUA+"<> = required parameters | [] = optional parameters");
			sender.sendMessage(ChatColor.AQUA+"Aliases: "+ChatColor.DARK_AQUA+"'"+Utils.command+", /"+Joiner.on(", /").join(Utils.getPluginAliases())+"'");
			sender.sendMessage(ChatColor.DARK_AQUA+Utils.getSpacer(3)+" "+ChatColor.AQUA+Utils.name+" help "+ChatColor.DARK_AQUA+Utils.getSpacer(3));
			sender.sendMessage(ChatColor.AQUA+"Use command "+ChatColor.DARK_AQUA+"'"+Utils.command+" append <username> <command1>[, command2, command3, ...]'"+ChatColor.AQUA+" to add new commands to a player. Remember that if the player is already connected the commands will run automatically!");
			sender.sendMessage(ChatColor.AQUA+"Use command "+ChatColor.DARK_AQUA+"'"+Utils.command+" set <username> <command1>[, command2, command3, ...]'"+ChatColor.AQUA+" to put new commands, deleting the old ones. Remember that if the player is already connected the commands will run automatically!");
			sender.sendMessage(ChatColor.AQUA+"Use command "+ChatColor.DARK_AQUA+"'"+Utils.command+" appendplayer <username> <command1>[, command2, command3, ...]'"+ChatColor.AQUA+" to add new commands to a player (they are executed by the player). Remember that if the player is already connected the commands will run automatically!");
			sender.sendMessage(ChatColor.AQUA+"Use command "+ChatColor.DARK_AQUA+"'"+Utils.command+" setplayer <username> <command1>[, command2, command3, ...]'"+ChatColor.AQUA+" to put new commands, deleting the old ones (they are executed by the player). Remember that if the player is already connected the commands will run automatically!");
			sender.sendMessage(ChatColor.AQUA+"Use command "+ChatColor.DARK_AQUA+"'"+Utils.command+" clear <username>'"+ChatColor.AQUA+" to clear all an existing commands from a player.");
			sender.sendMessage(ChatColor.AQUA+"Use command "+ChatColor.DARK_AQUA+"'"+Utils.command+" list'"+ChatColor.AQUA+" to list all database entries.");
			sender.sendMessage(ChatColor.AQUA+"Use command "+ChatColor.DARK_AQUA+"'"+Utils.command+" clearall'"+ChatColor.AQUA+" to clear all database entries.");
			sender.sendMessage(ChatColor.AQUA+"Use command "+ChatColor.DARK_AQUA+"'"+Utils.command+" toggle [property]'"+ChatColor.AQUA+" to toggle custom properties.");
		} 
		else if(args.length == 1) 
		{
			if(args[0].equalsIgnoreCase("list")) 
			{
				if(Utils.hasPermision(p, "sjc.list")) 
				{
					int size = Utils.database.size();
					if(size > 0) 
					{
						Utils.sendPluginMessage(sender, ChatColor.AQUA+"The current database has "+ChatColor.DARK_AQUA+size+ChatColor.AQUA+" entries: ", true);
						for(Entry<String, Executor> entry : Utils.database.entrySet()) 
						{
							sender.sendMessage(ChatColor.AQUA+entry.getKey()+": "+ChatColor.DARK_AQUA+"/"+((size > 1) ? Joiner.on(", /").join(entry.getValue().getCommands()) : entry.getValue().commands.get(0)));
						}
					} 
					else 
					{
						Utils.sendPluginMessage(sender, ChatColor.YELLOW+"The database is empty!", true);
					}
				}
			} 
			else if(args[0].equalsIgnoreCase("clearall")) 
			{
				if(Utils.hasPermision(p, "sjc.clearall")) 
				{
					Utils.database.clear();
					Utils.sendPluginMessage(sender, ChatColor.YELLOW+"The database has been cleared!", true);
				}
			} 
			else if(args[0].equalsIgnoreCase("toggle")) 
			{
				if(Utils.hasPermision(p, "sjc.toggle")) 
				{
					sender.sendMessage(ChatColor.AQUA+"List of toggleable properties: (Usage: "+ChatColor.DARK_AQUA+"'"+Utils.command+" toggle <property>'"+ChatColor.AQUA+")");
					sender.sendMessage(ChatColor.AQUA+"Property: "+ChatColor.DARK_AQUA+"waitforlogin "+ChatColor.AQUA+"| Description: Wait until the player has logged in to execute the commands.");
				}
			}
		}
		else if(args.length == 2) 
		{
			if(args[0].equalsIgnoreCase("clear")) 
			{
				if(Utils.hasPermision(p, "sjc.clear")) 
				{
					String username = args[1];
					if(Utils.database.containsKey(username))
						Utils.database.remove(username);
					Utils.sendPluginMessage(sender, ChatColor.DARK_AQUA+username+ChatColor.AQUA+" has been deleted from the current list!", true);
				}
			}
			else if(args[0].equalsIgnoreCase("toggle")) 
			{
				if(Utils.hasPermision(p, "sjc.toggle")) 
				{
					if(args[1].equalsIgnoreCase("waitforlogin")) 
					{
						if(Utils.hasPermision(p, "sjc.toggle.waitforlogin")) 
						{
							Utils.waitForLogin = !Utils.waitForLogin;
						}
					}
				}
			}
		}
		else if(args.length >= 3) 
		{
			if(args[0].equalsIgnoreCase("append") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("appendplayer") || args[0].equalsIgnoreCase("setplayer")) 
			{
				if(Utils.hasPermision(p, "sjc."+args[0])) 
				{
					String username = args[1];
					List<String> commandsReceived = new ArrayList<String>();
					String fullArgs = Utils.getArgs(args, 2);
					if(fullArgs.contains(","))
						commandsReceived = Arrays.asList(fullArgs.replace(", ", ",").split(","));
					else
						commandsReceived.add(fullArgs);
					Player player = Bukkit.getPlayerExact(username);
					boolean asPlayer = args[0].contains("player");
					if(player != null) 
					{
						for(String s : commandsReceived) {
							Utils.runCommands(null, player, s, asPlayer, Utils.waitForLogin);
						}
					} 
					else 
					{
						List<String> newCommands = new ArrayList<String>();
						Executor tempExe = null;
						if(args[0].contains("append"))
							if(Utils.database.containsKey(username)) 
							{
								tempExe = Utils.database.get(username);
								newCommands = tempExe.getCommands();
							}
						newCommands.addAll(commandsReceived);
						HashMap<String, Boolean> tempCommands = new HashMap<String, Boolean>();
						for(String c : newCommands) {
							tempCommands.put(c, ((tempExe != null) ? tempExe.commands.get(c) : asPlayer));
						}
						Utils.database.put(username, new Executor(tempCommands));
						if(args[0].contains("append"))
							Utils.sendPluginMessage(sender, ChatColor.AQUA+"New commands has been appended to "+ChatColor.DARK_AQUA+args[1]+ChatColor.AQUA+" account.", true);
						else
							Utils.sendPluginMessage(sender, ChatColor.AQUA+"New commands has been set to "+ChatColor.DARK_AQUA+args[1]+ChatColor.AQUA+" account.", true);
					}
				}
			}
		}
		return false;
	}

}
