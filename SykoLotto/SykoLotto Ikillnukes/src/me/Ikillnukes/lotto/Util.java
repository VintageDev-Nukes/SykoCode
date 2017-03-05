package me.Ikillnukes.lotto;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import net.milkbowl.vault.economy.EconomyResponse;

public class Util
{
	
	public enum TicketAction { BuyTickets, AddTickets, DeleteTickets }
	
    public static Lotto plugin;
    protected static BukkitTask task;
	protected static int time, warnTime, loopTime, pot, price;
	protected static String prefix; 
	//protected static boolean isRunning;
    //public static HashMap<UUID, Integer> lotto;
    
    protected static void setInstance(Lotto instance)
    {
    	plugin = instance;
    	time = 0;
    	prefix = plugin.getConfig().getString("Prefix")+" ";
    }
    
    protected static void handleException(Exception e)
    {
    	crash("An unexpected error has occurred: '" + e.getMessage() + "'");
    	e.printStackTrace();
    }
    
    protected static void crash(String reason)
    {
    	severe(reason);
    	disable();
    }
    
    protected static void severe(String message)
    {
    	plugin.getLogger().severe(message);
    }
    
    protected static void disable()
    {
    	plugin.getServer().getPluginManager().disablePlugin(plugin);
    }
    
    protected static boolean tickets(Player player, int tickets, TicketAction action) {
    	int price = Util.price * tickets;
    	UUID uuid = player.getUniqueId();
    	Member mem = Database.lotto.get(uuid);
    	int errorCode = -1;
    	EconomyResponse resp = null;
    	switch(action) {
    	case BuyTickets:
    	case AddTickets:
    		if (Lotto.econ.has(player, price)) {
    			resp = Lotto.econ.withdrawPlayer(player, price);
    			if (resp.transactionSuccess()) {
	    			if(action == TicketAction.BuyTickets) {
	    				mem.amount = tickets;
	              	} else if(action == TicketAction.AddTickets) {
	    				mem.amount += tickets;
	              	}
    			} else {
    				errorCode = 0;
    			}
    		} else {
    			errorCode = 1;
    		}
    		break;
    	case DeleteTickets:
    		resp = Lotto.econ.depositPlayer(player, price);
    		if (resp.transactionSuccess()) {
				mem.amount -= tickets;
            	Database.lotto.put(uuid, mem);
    		} else {
    			errorCode = 0;
    		}
    		break;
    	}
    	if(errorCode > 0) {
    		switch(errorCode) {
    		case 0:
    			player.sendMessage(ChatColor.RED + "Se ha producido un error al hacer la transacción, por favor, contacta un administrador.");
    			break;
    		case 1:
    			player.sendMessage(ChatColor.RED + "No tienes suficiente dinero.");
    			break;
    		}
    		return false;
    	} else {
    		pot += price;
    		return true;
    	}
    }
    
    protected static void sendPluginMessage(CommandSender sender, String message, boolean prefixed)
    {
    	String msgprefix = "";
	    if (prefixed) {
	    	msgprefix = prefix;
	    }
	    sender.sendMessage(msgprefix + ChatColor.GRAY + message);
    }
    
    protected static void cancel() {
    	for(UUID uuid : Database.lotto.keySet()) {
    		Member mem = Database.lotto.get(uuid);
    		Lotto.econ.depositPlayer(Bukkit.getPlayer(uuid), mem.amount*Util.price);
    	}
    	task.cancel();
    }
    
    protected static void run() {
    	Lotto.plugin.run();
    }
    
    //Other methods
    
    protected static String timeConversion(int totalSeconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;

        return ((hours > 0) ? hours + " horas " : "") + ((minutes > 0) ? minutes + " minutos " : "") + ((seconds > 0) ? seconds + " segundos" : "");
    }
    
}
