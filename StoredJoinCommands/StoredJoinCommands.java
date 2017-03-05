package me.Ikillnukes.sjc;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class StoredJoinCommands
	extends JavaPlugin 
{
	
	protected PluginDescriptionFile ymlFile = getDescription();
	
	public void onEnable()
	{
		Utils.setInstance(this);
		getServer().getPluginManager().registerEvents(new Listeners(), this);
		Commands commands = new Commands();
	    for (String command : ymlFile.getCommands().keySet()) {
	    	getCommand(command).setExecutor(commands);
	    }
		Utils.log(Utils.name + " v" + Utils.version + " by " + Utils.mainAuthor + " has been enabled.");
	}
	
	public void onDisable() {
		Utils.log(Utils.name + " v" + Utils.version + " by " + Utils.mainAuthor + " has been disabled.");
	}
	  
}
