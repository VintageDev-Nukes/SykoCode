package me.Ikillnukes.lotto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import me.Ikillnukes.lotto.Util.TicketAction;

public class Lotto extends JavaPlugin
{
    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
    protected static Lotto plugin;
    
    public void onEnable() {
    	plugin = this;
    	Util.setInstance(this);
        this.getCommand("lotto").setExecutor((CommandExecutor)new Commands(this));
        if (!this.getConfig().contains("Prefix")) {
        	String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "SykoLotto" + ChatColor.DARK_GRAY + "]";
            this.getConfig().set("Prefix", (Object)(prefix));
            Util.prefix = prefix;
        } else {
        	Util.prefix = this.getConfig().getString("Prefix");
        }
        if (!this.getConfig().contains("Enter-Amount")) {
            this.getConfig().set("Enter-Amount", (Object)1000);
            Util.price = 100;
        } else {
        	Util.price = this.getConfig().getInt("Enter-Amount");
        }
        if (!this.getConfig().contains("WarnTime")) {
            this.getConfig().set("WarnTime", (Object)600);
            Util.time = 600;
        } else {
        	Util.time = this.getConfig().getInt("WarnTime");
        }
        if (!this.getConfig().contains("LoopTime")) {
            this.getConfig().set("LoopTime", (Object)1800);
        }
        if (!this.getConfig().contains("Enabled")) {
            this.getConfig().set("Enabled", (Object)true);
        }
        if (this.getConfig().getBoolean("Enabled")) {
            run();
        }
        if (!this.setupEconomy()) {
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        Util.warnTime = this.getConfig().getInt("WarnTime");
        Util.loopTime = this.getConfig().getInt("LoopTime");
        this.setupPermissions();
        this.setupChat();
        this.saveConfig();
    }
    
    public void onDisable() {
    	Database.saveLotto();
    	Database.disconnect();
    }
    
    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            perms = permissionProvider.getProvider();
        }
        return (perms != null);
    }

    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }

        return (econ != null);
    }
    
    public void Broadcast() {
    	String participants = "";
    	StringBuilder sb = new StringBuilder();
    	String prefix = "";
    	final int length = Database.lotto.size();
    	int tickets = 0;
    	if(length > 0) {
	    	for(UUID uuid : Database.lotto.keySet()) {
	    		sb.append(prefix);
	    		prefix = ", ";
	    		sb.append(ChatColor.YELLOW+Bukkit.getOfflinePlayer(uuid).getName()+" ["+ChatColor.DARK_GREEN+Database.lotto.get(uuid)+ChatColor.YELLOW+"]");
	    	}
	    	participants = sb.toString();
	    	for(Member mem : Database.lotto.values()) {
	    		tickets += mem.amount;
	    	}
    	}
        Bukkit.broadcastMessage(ChatColor.BLUE + " =============== " + ChatColor.YELLOW + "Lotería" + ChatColor.BLUE + " ==============");
        Bukkit.broadcastMessage(ChatColor.YELLOW + " Entra la lotería, ¡puedes ganar una suculenta recompensa!");
        Bukkit.broadcastMessage(ChatColor.BLUE + " Comando : " + ChatColor.YELLOW + "/lotto");
        Bukkit.broadcastMessage(ChatColor.BLUE + " Bote : " + ChatColor.YELLOW + "$" + Util.pot);
        Bukkit.broadcastMessage(ChatColor.BLUE + " Coste : " + ChatColor.YELLOW + "$" + plugin.getConfig().getInt("Enter-Amount"));
        Bukkit.broadcastMessage(ChatColor.BLUE + " # de participantes : " + ChatColor.YELLOW + Database.lotto.size() + " (con "+ChatColor.GREEN+tickets+ChatColor.YELLOW+" boletos comprados)");
        Bukkit.broadcastMessage(ChatColor.BLUE + " Tiempo restante : " + ChatColor.YELLOW + Util.timeConversion(1800-Util.time));
        if(length > 0) {
        	Bukkit.broadcastMessage(ChatColor.BLUE + " Participantes : " + participants);
        }
        Bukkit.broadcastMessage(ChatColor.BLUE + " =====================================");
    }
    
    public void finish() {
        List<UUID> uuids = new ArrayList<UUID>();
        for(UUID uuid : Database.lotto.keySet()) {
       	Member mem = Database.lotto.get(uuid);
       	if(mem.amount > 0) {
            	for(int i = 0; i < mem.amount; ++i) {
             		uuids.add(uuid);
              	}
        	}
        }
    	if (uuids.size() > 0) {
    		final Random rand = new Random();
            UUID uuid = uuids.get(rand.nextInt(uuids.size()));
            Bukkit.broadcastMessage(ChatColor.BLUE + " =============== " + ChatColor.YELLOW + "Lotería" + ChatColor.BLUE + " ==============");
            Bukkit.broadcastMessage(ChatColor.GREEN + " Ganador : " + ChatColor.YELLOW + ChatColor.ITALIC + Bukkit.getOfflinePlayer(uuid).getName() + ChatColor.RESET + ChatColor.YELLOW + " [$"+ChatColor.DARK_GREEN+Util.pot+ChatColor.YELLOW+"]");
            Bukkit.broadcastMessage(ChatColor.BLUE + " =====================================");
            Lotto.econ.depositPlayer(Bukkit.getOfflinePlayer(uuid), (double)Util.pot);
            HashMap<UUID, Member> tempList = new HashMap<UUID, Member>();
            for(Entry<UUID, Member> entry : Database.lotto.entrySet()) {
            	if(entry.getValue().autoenter) {
            		Member mem = entry.getValue();
            		mem.amount = 0;
            		UUID uuid1 = entry.getKey();
            		Player player = Bukkit.getPlayer(uuid1);
            		if(Util.tickets(player, mem.nextRoundAmount, TicketAction.BuyTickets)) {
            			tempList.put(uuid1, mem);
            			player.sendMessage(ChatColor.GREEN+"Has autocomprado "+ChatColor.DARK_GREEN+mem.nextRoundAmount+ChatColor.BLUE+" tickets más.");
            		}
            	}
            }
            Database.lotto.clear();
            Database.clearall();
            Database.lotto = tempList;
            Database.saveLotto();
    	} else {
             Bukkit.broadcastMessage(ChatColor.BLUE + " =============== " + ChatColor.YELLOW + "Lotería" + ChatColor.BLUE + " ==============");
             Bukkit.broadcastMessage(ChatColor.RED + " No hay participantes");
             Bukkit.broadcastMessage(ChatColor.BLUE + " =====================================");
        }
        Util.pot = 0;
        run();
    }
    
    public void run() {
    	Util.time = 0;
    	Util.task = Bukkit.getServer().getScheduler().runTaskTimer((Plugin)plugin, (Runnable)new Runnable() {
            public void run() {
                if (Util.time == Util.loopTime) { //Default: 30 minutes
                    finish();
                    Util.task.cancel();
                } else {
	                if (Util.time % Util.warnTime == 0) { //Default: 10 minutes
	                	Broadcast();
	                }
                	++Util.time;
                }
            }
        }, 0L, 20L);
    }
    
}
