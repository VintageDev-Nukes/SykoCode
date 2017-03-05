package me.Ikillnukes.refsys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.Ikillnukes.refsys.Utils.Debug;

public class Utils 
{
	
	protected enum Debug { log, warning, severe, crash }
	
	protected static ReferralSystem plugin;
	protected static Map<String, String> properties = new HashMap<String, String>();
	protected static Map<String, Object> settings = new HashMap<String, Object>(); 
	protected static Map<String, List<Member>> referrals = new HashMap<String, List<Member>>();
	
	protected static void setInstance(ReferralSystem instance)
	{
	    plugin = instance;
	    properties.put("name", plugin.ymlFile.getName());
	    properties.put("command", "/"+(String)plugin.ymlFile.getCommands().keySet().toArray()[0]);
	    //properties.put("prefix", ChatColor.GRAY+"["+ChatColor.DARK_GRAY+properties.get("name")+ChatColor.GRAY+"]");
	    properties.put("version", plugin.ymlFile.getVersion());
	    properties.put("mainAuthor", plugin.ymlFile.getAuthors().get(0));
	}
	
	protected static void debug(final String msg) 
	{
		debug(msg, Debug.log);
	}
	
	protected static void debug(final String msg, final Debug type) 
	{
		switch(type) 
		{
		case log:
			plugin.getLogger().info(msg);
			break;
		case warning:
			plugin.getLogger().warning(msg);
			break;
		case severe:
			plugin.getLogger().severe(msg);
			break;
		case crash:
			if(!msg.isEmpty())
				debug(msg, Debug.severe);
			disable();
			break;
		}
	}
	
	protected static void disable() 
	{
		plugin.getServer().getPluginManager().disablePlugin(plugin);
	}
	
	protected static void sendPluginMessage(final CommandSender sender, final String message, final boolean prefixed)
	{
	    sender.sendMessage(((prefixed) ? properties.get("prefix") + " " : "") + message);
	}
	
	protected static boolean hasPermision(final Player player, final String node) 
	{
		if(player != null && !player.hasPermission(node)) 
		{
			player.sendMessage(ChatColor.RED+"You don't have permission to do that!");
			return false;
		}
		return true;
	}
	
	protected static String getSpacer(int size) 
	{
		String spacer = "";
		for(int i = 0; i < size; ++i)
			spacer += "-";
		return spacer;
	}
	
	protected static String getArgs(final String[] args, final int num)
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
	
	protected static List<String> getPluginAliases() 
	{
		List<String> aliases = new ArrayList<String>();
		Command cmd = plugin.getServer().getPluginCommand(properties.get("command").replace("/", ""));
		for (String alias : cmd.getAliases()) {
			aliases.add(alias);
		}
		return aliases;
	}
	
	protected static ConfigurationSection getConfigurationSection(final String section)
	{
	    return plugin.getConfig().getConfigurationSection(section);
	}
	
	//Custom methods
	
	protected static void rewardPlayer(final Player player, final String referral) 
	{
		final int approvalDelay = (int)settings.get("ApprovalDelay");
		final String username = player.getName();
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    {
			@Override
			public void run()
			{
				if(Bukkit.getPlayerExact(referral) != null) 
				{
					Member mem = Member.getMember(Utils.referrals.get(username), referral);
					if(!mem.rewarded) 
					{
						mem.rewarded = true;
						Utils.debug(referral+" has been rewarded!");
						//runCommands();
					}
				}
			}
	    }, approvalDelay*20L);
	}
	
	protected static void addPlayer(final Player player, final String referral) 
	{
		String username = player.getName();
		List<Member> tempList = new ArrayList<Member>();
		if(Utils.referrals.containsKey(username))
			tempList = Utils.referrals.get(username);
		tempList.add(new Member(username));
		Utils.referrals.put(username, tempList);
	}
	
	protected static void runReward(final Player player) 
	{
		final List<String> commands = (List<String>)settings.get("RewardICommands");
		final String username = player.getName();
		final int size = Utils.referrals.get(username).size();
		for(final String command : commands) 
		{
			final String c = command.replace("{player}", username).replace("{multiplier}", ((Integer)Math.round(size/(int)Utils.settings.get("RewardIEvery"))).toString()).replace("{referrals}", ((Integer)size).toString());
			final Pattern p = Pattern.compile("(\\w+)\\((.+?)\\)");
			final Matcher m = p.matcher(c);
			String finalCommand = c;
			if(m.matches()) 
			{
				switch(m.group(1)) 
				{
				case "Math":
					ScriptEngineManager mgr = new ScriptEngineManager();
				    ScriptEngine engine = mgr.getEngineByName("JavaScript");
				    try {
						finalCommand = c.replace(m.group(0), (String)engine.eval(m.group(2)));
					} catch (ScriptException e) {
						e.printStackTrace();
						Utils.debug("Cannot cast reward!", Debug.severe);
						return;
					}
					break;
				default:
					Utils.debug("Cannot give reward because "+m.group(1)+" isn't defined!", Debug.severe);
					break;
				}
			}
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
		}
	}
	
}