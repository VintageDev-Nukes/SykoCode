package me.Ikillnukes.refsys;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import me.Ikillnukes.refsys.Utils.Debug;

public class ReferralSystem
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
		if (!new File(getDataFolder() + "/config.yml").exists())
		{
			Utils.debug("Config not found! Creating one...");
		    createConfig();
		}
		else
		{
			Utils.debug("Config found! Loading...");
		}
		if(loadConfig())
			Utils.debug("Config loaded successfully!");
		Utils.debug(Utils.properties.get("name") + " v" + Utils.properties.get("version") + " by " + Utils.properties.get("mainAuthor") + " has been enabled.");
		//Database.loadFromFile();
	}
	
	public void onDisable() 
	{
		saveDatabase();
		Utils.debug(Utils.properties.get("name") + " v" + Utils.properties.get("version") + " by " + Utils.properties.get("mainAuthor") + " has been disabled.");
	}
	
	private void createConfig()
	{
		getConfig().addDefault("Settings.Prefix", (ChatColor.GRAY+"["+ChatColor.DARK_GRAY+Utils.properties.get("name")+ChatColor.GRAY+"]").replace("§", "&"));
		getConfig().addDefault("Settings.ApprovalDelay", 7200);
		getConfig().addDefault("Settings.Individual.RewardEvery", 5);
		getConfig().addDefault("Settings.Individual.RewardMultiplier", 5);
		getConfig().addDefault("Settings.Individual.RewardSingle", false);
		//getConfig().addDefault("Settings.Individual.AmountFormula", "{multiplier}*5");
		getConfig().addDefault("Settings.Individual.Commands", Arrays.asList(new String[] {"eco give {player} Math(10*{multiplier})", "cc give {player} Math(5*{referrals})"}));
		//getConfig().addDefault("Database.Individual", Arrays.asList(new Member[] {new Member("bababa"), new Member("ebebe")}));
		getConfig().options().header("This is the configuration file of "+Utils.properties.get("name")+", please feel free to edit it!\nSettings explanation:\nApprovalDelay => How many time has to wait the player for be rewarded by the plugin?\nIndividual settings:\n  RewardEvery => How many players has to enter to reward the refered user?\n  RewardMultiplier => When the goal is reached, how many times we have to multiply the main reward?\n  RewardSingle => If it is set to true, the commands will be executed when a new referral enters the server, otherwise, the commands will be executed when the goal (RewardEvery) is reached.\nRemember to use the given variables: {player} (The player that is referring users), {multiplier} (The current multiplier amount), {referrals} (The number of referals a user have)\nAnd the custom functions like: Math(10*20) or Math(10*{multiplier})\n");
		//En el futuro quizás use Regex para que la gente se pueda crear sus propios matches?
		getConfig().options().copyDefaults(true);
		saveConfig();
		Utils.debug("Config created succesfully!");
	}
	 
	private boolean loadConfig() 
	{
		try 
		{
			ConfigurationSection settings = Utils.getConfigurationSection("Settings");
			Utils.properties.put("prefix", ChatColor.translateAlternateColorCodes('&', settings.getString("Prefix")));
			Utils.settings.put("ApprovalDelay", (Object)settings.getInt("ApprovalDelay"));
			Utils.settings.put("RewardIEvery", (Object)settings.getInt("Individual.RewardEvery"));
			Utils.settings.put("RewardIMultiplier", (Object)settings.getInt("Individual.RewardMultiplier"));
			Utils.settings.put("RewardISingle", (Object)settings.getBoolean("Individual.RewardSingle"));
			Utils.settings.put("RewardICommands", (Object)settings.getList("Individual.Commands"));
			//Utils.waitForLogin = settings.getBoolean("WaitForLogin");
			if(getConfig().contains("Database.Individual")) 
			{
				ConfigurationSection idb = Utils.getConfigurationSection("Database.Individual");
				Set<String> sets = idb.getKeys(false);
				if(sets.size() > 0) {
					for(String key : sets) 
					{
						Utils.referrals.put(key, (List<Member>)idb.getList(key));
					}
				}
			}
			return true;
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			Utils.debug("Config loading failed! Disabling plugin...", Debug.warning);
		    Utils.disable();
		    return false;
		}
	}
	
	private void saveDatabase() 
	{
		try 
		{
			if(Utils.referrals.size() > 0) {
				for(Entry<String, List<Member>> entry : Utils.referrals.entrySet()) 
				{
					//if(getConfig().contains("Database.Individual."+entry.getKey()))
					getConfig().set(entry.getKey(), entry.getValue());
					//getConfig().set("Database.Individual."+entry.getKey(), entry.getValue());
				}
				saveConfig();
			}
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			Utils.debug("Database saving failed!", Debug.warning);
		} 
	}
	
	/*
	 
	 Voy a implementar la opción de listen to ip, entonces quien se meta en un periodo de x minutos al sv habiendo pasado por el websend
	 será cacheado y tomado en cuenta como jugador referido por x jugador.
	 
	 Para el referal normal simplemente añadire un HashMap<String, List<String>>, el string tendrá el usuario referidor (quizás use una UUID) y la List contendrá una lista de todos los usuarios referidos por dicho usuario (quizás use una List<UUID>)
	 Los comandos para esto será /referral addplayer %jugador que va a ser referido%
	 
	 Para los clanes habrá 2 rangos: Leader & Members, el leader será el que cree el clan inicialmente (solo se puede crear uno por miembro y solo se podra crear en las primeras x horas de juego)
	 El rango Leader se dará al crear el clan automaticamente, los Leaders podrán añadir miembros a su clan
	 El comando principal sera: /clan create %nombre%, /clan addmember %nombre del usuario%
	 
	 Para evitar fraudes, timos y estafas será fundamental comprobar si el usuario ha jugado anteriormente al servidor, y cuanto lleva jugando (para ello haré un segundo plugin que reemplace al OnTime), y tambien muy importante la IP
	 
	 Hacer diferentes premios por rangos
	 
	 */
	  
}
