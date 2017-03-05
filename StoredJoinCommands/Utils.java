package me.Ikillnukes.sjc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utils 
{
	
	protected static StoredJoinCommands plugin;
	protected static HashMap<String, Executor> database = new HashMap<String, Executor>();
	protected static String name, command, prefix, version, mainAuthor;
	protected static boolean waitForLogin = true;
	
	protected static void setInstance(StoredJoinCommands instance)
	{
	    plugin = instance;
	    name = plugin.ymlFile.getName();
	    command = "/"+name.toLowerCase();
	    prefix = ChatColor.GRAY+"["+ChatColor.DARK_GRAY+name+ChatColor.GRAY+"]";
	    version = plugin.ymlFile.getVersion();
	    mainAuthor = plugin.ymlFile.getAuthors().get(0);
	}
	
	protected static void log(String message)
	{
		plugin.getLogger().info(message);
	}
	  
	protected static void warning(String message)
	{
	    plugin.getLogger().warning(message);
	}
	  
	protected static void severe(String message)
	{
	    plugin.getLogger().severe(message);
	}
	
	protected static void crash(String reason)
	{
	    severe(reason);
	    disable();
	}
	  
	protected static void disable()
	{
	    plugin.getServer().getPluginManager().disablePlugin(plugin);
	}
	
	protected static void sendPluginMessage(CommandSender sender, String message, boolean prefixed)
	{
	    sender.sendMessage(((prefixed) ? prefix : "") + " " + message);
	}
	
	protected static boolean hasPermision(Player player, String node) 
	{
		if(player != null && !player.hasPermission(node)) {
			player.sendMessage(ChatColor.RED+"You don't have permission to do that!");
			return false;
		}
		return true;
	}
	
	protected static String getSpacer(int size) 
	{
		String spacer = "";
		for(int i = 0; i < size; ++i) {
			spacer += "-";
		}
		return spacer;
	}
	
	protected static String getArgs(String[] args, int num)
	{
	    StringBuilder sb = new StringBuilder();
	    String prefix = "";
	    for(int i = num; i < args.length; i++) {
	        sb.append(prefix);
	        prefix = " ";
	        sb.append(args[i]);
	    }
	    return sb.toString();
	}
	
	protected static void runCommands(String username, boolean isLogin) 
	{
		Player player = Bukkit.getPlayerExact(username);
		Executor exe = database.get(username);
		for (Entry<String, Boolean> entry : exe.commands.entrySet()) {
			String c = entry.getKey();
			boolean asPlayer = entry.getValue();
				runCommands(exe, player, c, asPlayer, isLogin);
		}
	}
	
	protected static void runCommands(Executor exe, Player player, String command, boolean asPlayer, boolean isLogin) 
	{
		Utils.log(command);
		if(waitForLogin == isLogin) 
		{
			if(asPlayer) 
				Bukkit.dispatchCommand(player, command);
			else
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
			if(exe != null) 
				exe.commands.remove(command);
		}
	}
	
	protected static List<String> getPluginAliases() 
	{
		List<String> aliases = new ArrayList<String>();
		Command cmd = plugin.getServer().getPluginCommand(Utils.command.replace("/", ""));
		for (String alias : cmd.getAliases()) {
			aliases.add(alias);
		}
		return aliases;
	}
	
}
