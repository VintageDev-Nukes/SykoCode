package me.Ikillnukes.refsys;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

public class Commands
	implements CommandExecutor
{

	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args)
	{
		Player p = null;
		if(sender instanceof Player)
			p = (Player)sender;
		String username = "console";
		if(p != null)
			username = p.getName();
		if(args.length == 0) 
		{
			sender.sendMessage(ChatColor.LIGHT_PURPLE+"<> = required parameters | [] = optional parameters");
			sender.sendMessage(ChatColor.LIGHT_PURPLE+"Aliases: "+ChatColor.DARK_PURPLE+"'"+Utils.properties.get("command")+", /"+Joiner.on(", /").join(Utils.getPluginAliases())+"'");
			sender.sendMessage(ChatColor.DARK_PURPLE+Utils.getSpacer(3)+" "+ChatColor.LIGHT_PURPLE+Utils.properties.get("name")+" help "+ChatColor.DARK_PURPLE+Utils.getSpacer(3));
			
		}
		else if(args.length == 1) 
		{
			
		}
		else if(args.length == 2) 
		{
			if(args[0].equalsIgnoreCase("addplayer")) //&& p != null 
			{
				//Debería añadirle permisos pero por ahora lo voy a dejar así
				List<Member> members = new ArrayList<Member>();
				if(Utils.referrals.containsKey(username))
					members = Utils.referrals.get(username);
				members.add(new Member(args[1]));
				Utils.referrals.put(username, members);
				//Utils.debug("Set");
				Utils.sendPluginMessage(sender, ChatColor.LIGHT_PURPLE+"Player "+ChatColor.DARK_PURPLE+args[1]+ChatColor.LIGHT_PURPLE+" added to your referred users!", true);
				//Utils.save();
			}
		}
		return false;
	}

}