package me.Ikillnukes.sjc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import de.st_ddt.crazylogin.events.CrazyLoginLoginEvent;
import me.XXLuigiMario.CustomCurrency.CustomCommand;
import me.XXLuigiMario.CustomCurrency.CustomCommands;

public class Listeners 
	implements Listener 
{

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent e)
	{
		String username = e.getPlayer().getName();
	    if(Utils.database.containsKey(username))
	    	Utils.runCommands(username, false);
	}
	
	@EventHandler
	public void onPlayerLogin(final CrazyLoginLoginEvent e) 
	{
		String username = e.getPlayer().getName();
	    if(Utils.database.containsKey(username))
	    	Utils.runCommands(username, true);
	}
	
	@EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent e) 
	{
        final String cmd = e.getMessage().split(" ")[0].replace("/", "");
        if (CustomCommands.existsCommand(cmd)) 
        {
            final CustomCommand command = CustomCommands.getCommand(cmd);
            if (command.isEnabled()) 
            {
                command.execute(e.getPlayer());
                e.setCancelled(true);
            }
        }
    }
	
}
