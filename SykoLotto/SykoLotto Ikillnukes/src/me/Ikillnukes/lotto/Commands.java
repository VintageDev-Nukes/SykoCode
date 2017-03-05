package me.Ikillnukes.lotto;
  	
import org.bukkit.entity.Player;

import me.Ikillnukes.lotto.Member.ModifyField;
import me.Ikillnukes.lotto.Util.TicketAction;

import java.math.BigDecimal;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

public class Commands implements CommandExecutor
{
    public static Lotto plugin;
    
    public Commands(final Lotto plugin) {
        Commands.plugin = plugin;
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        final Player player = (Player)sender;
        final UUID uuid = player.getUniqueId();
        final boolean isRegistered = Database.lotto.containsKey(uuid);
        int errorCode = -1;
        Member mem = null;
        if(isRegistered) {
        	mem = Database.lotto.get(uuid);
        }
        if (cmd.getName().equalsIgnoreCase("lotto")) {
        	if(args.length == 0) {
	        	sender.sendMessage(ChatColor.BLUE + "Usa el comando " + ChatColor.YELLOW + "'/lotto enter'" + ChatColor.BLUE + " para participar en la lotería.");
	        	sender.sendMessage(ChatColor.BLUE + "Usa el comando " + ChatColor.YELLOW + "'/lotto buy [cantidad]'" + ChatColor.BLUE + " para participar en la lotería.");
	        	sender.sendMessage(ChatColor.BLUE + "Usa el comando " + ChatColor.YELLOW + "'/lotto stats'" + ChatColor.BLUE + " para ver tus estadísticas actuales.");
	            sender.sendMessage(ChatColor.BLUE + "Usa el comando " + ChatColor.YELLOW + "'/lotto change [±cantidad]'" + ChatColor.BLUE + " para cambiar el numero de boletos comprados.");
	            sender.sendMessage(ChatColor.BLUE + "Usa el comando " + ChatColor.YELLOW + "'/lotto newnum [cantidad]'" + ChatColor.BLUE + " para establecer un nuevo numero de boletos a usar.");
	            sender.sendMessage(ChatColor.BLUE + "Usa el comando " + ChatColor.YELLOW + "'/lotto autoenter [cantidad]'" + ChatColor.BLUE + " para entrar a todas las rondas con una cantidad especificada.");
	            sender.sendMessage(ChatColor.BLUE + "Usa el comando " + ChatColor.YELLOW + "'/lotto changeautoenter [±cantidad]'" + ChatColor.BLUE + " para variar la cantidad al entrar en una nueva ronda.");
	            sender.sendMessage(ChatColor.BLUE + "Usa el comando " + ChatColor.YELLOW + "'/lotto newautoenter [cantidad]'" + ChatColor.BLUE + " para establecer una nueva cantidad al entrar en una nueva ronda.");
	            if(player.hasPermission("lotto.forcestop")) {
	            	sender.sendMessage(ChatColor.BLUE + "Usa el comando " + ChatColor.YELLOW + "'/lotto forcestop'" + ChatColor.BLUE + " para forzar a que la ronda actual acabe.");
	            }
	            if(player.hasPermission("lotto.reload")) {
	            	sender.sendMessage(ChatColor.BLUE + "Usa el comando " + ChatColor.YELLOW + "'/lotto reload [razón]'" + ChatColor.BLUE + " para actualizar la configuración actual.");
	            }
        	} else {
        			if (args[0].equalsIgnoreCase("enter")) {
        			if(!isRegistered) {
        				Database.lotto.put(uuid, new Member(0, false, 0));
        				sender.sendMessage(ChatColor.GREEN+"Has entrado en la lotería, recuerda usar "+ChatColor.YELLOW+"'/lotto buy [cantidad]'"+ChatColor.GREEN+" para entrar a la lotería.");
        			} else {
        				if(mem.amount == 0) {
        					errorCode = 0;
        				} else {
        					errorCode = 1;
        				}
        			}
        		} else if(args[0].equalsIgnoreCase("buy")) {
        			if(!isRegistered) {
        				errorCode = 2;
        			} else {
        				if(args.length > 1) {
        					if(StringUtils.isNumeric(args[1])) {
        						int amount = Integer.parseInt(args[1]);
        						if(amount > 0) {
	        						if(Util.tickets(player, amount, TicketAction.BuyTickets)) {
	        							sender.sendMessage(ChatColor.GREEN+"Has entrado en la lotería, recuerda usar "+ChatColor.YELLOW+"'/lotto buy [cantidad]'"+ChatColor.GREEN+" para entrar a la lotería.");
	        							Bukkit.broadcastMessage(ChatColor.GREEN+player.getName()+ChatColor.BLUE+" ha entrado en la lotería.");
	        						}
        						} else {
        							errorCode = 3;
        						}
        					} else {
        						errorCode = 4;
        					}
        				} else {
        					errorCode = 5;
        				}
        			}
        		} else if(args[0].equalsIgnoreCase("stats")) {
        			if(!isRegistered) {
        				errorCode = 2;
        			} else {
    		        	final int length = Database.lotto.size();
    		        	int tickets = 0;
    		        	final int amount = Database.lotto.get(uuid).amount;
    		        	double prob = 0;
    		        	if(length > 0) {
    			        	for (Member mem1 : Database.lotto.values()) {
    			        		tickets += mem1.amount;
    			        	}
    		        	}
    		        	sender.sendMessage(ChatColor.BLUE + " =============== " + ChatColor.YELLOW + "Información sobre la lotería actual" + ChatColor.BLUE + " ==============");
    		        	if(length > 0) {
    		        		prob = amount*100/tickets;
    		        	}
    		        	sender.sendMessage(ChatColor.BLUE + " Boletos comprados : " + ChatColor.YELLOW + amount);
    			        sender.sendMessage(ChatColor.BLUE + " Tu apuesta : " + ChatColor.YELLOW + "$"+(plugin.getConfig().getInt("Enter-Amount")*amount));
    			        sender.sendMessage(ChatColor.BLUE + " Tu probabilidad : " + new BigDecimal(String.valueOf(prob)).setScale(2, BigDecimal.ROUND_HALF_UP) + "% ["+amount+"/"+tickets+"]");
    		        	sender.sendMessage(ChatColor.BLUE + " =====================================");
        			}
        		} else if(args[0].equalsIgnoreCase("change")) {
        			if(!isRegistered) {
        				errorCode = 2;
        			} else {
        				if(args.length > 1) {
        					if(StringUtils.isNumeric(args[1])) {
        						int amount = Integer.parseInt(args[1]);
        						if(amount != 0) {
        							if(amount > 0) {
			        					if(Util.tickets(player, amount, TicketAction.AddTickets)) {
			        						sender.sendMessage(ChatColor.GREEN+"Has comprado "+ChatColor.DARK_GREEN+amount+ChatColor.GREEN+" boletos más por "+ChatColor.GREEN+"$"+(Util.price*amount)+ChatColor.GREEN+".");
			        					}
		        					} else {
		        						if(Util.tickets(player, amount, TicketAction.DeleteTickets)) {
			        						sender.sendMessage(ChatColor.GREEN+"Has retirado "+ChatColor.DARK_GREEN+amount+ChatColor.GREEN+" boletos.");
			        					}
		        					}
	        					}
        					} else {
        						errorCode = 4;
        					}
        				} else {
        					errorCode = 5;
        				}
        			}
        		} else if(args[0].equalsIgnoreCase("newnum")) {
        			if(!isRegistered) {
        				errorCode = 2;
        			} else {
        				if(args.length > 1) {
        					if(StringUtils.isNumeric(args[1])) {
        						int amount = Integer.parseInt(args[1]);
        						if(amount != mem.amount) {
	        						if(amount >= 0) {
	        							int newamount = mem.amount-amount;
	        							boolean success = false;
	        							if(newamount > 0) {
	        								success = Util.tickets(player, newamount, TicketAction.AddTickets);
	        							} else if(newamount < 0) {
	        								success = Util.tickets(player, newamount, TicketAction.DeleteTickets);
	        							}
	        							if(success) {
	        								sender.sendMessage(ChatColor.GREEN+"Ahora tienes "+ChatColor.DARK_GREEN+amount+ChatColor.GREEN+" boletos, habiéndote "+((newamount > 0) ? "gastado" : "devuelto")+ChatColor.GREEN+"$"+(newamount*Util.price)+ChatColor.GREEN+".");
	        							}
	        						} else {
	        							errorCode = 7;
	        						}
        						}
        					} else {
        						errorCode = 4;
        					}
        				} else {
        					errorCode = 5;
        				}
        			}
        		} else if(args[0].equalsIgnoreCase("forcestop")) {
        			if(player.hasPermission("lotto.forcestop")) {
        				Lotto.plugin.finish();
                        Util.task.cancel();
        			} else {
        				errorCode = 6;
        			} 			
        		} else if(args[0].equalsIgnoreCase("cancel")) {
        			if(player.hasPermission("lotto.cancel")) {
        				Util.cancel();
        			} else {
        				errorCode = 6;
        			} 	
        		} else if(args[0].equalsIgnoreCase("run")) {
        			if(player.hasPermission("lotto.run")) {
        				Util.run();
        			} else {
        				errorCode = 6;
        			} 	
        		} else if(args[0].equalsIgnoreCase("reload")) {
        			if(player.hasPermission("lotto.reload")) {
        				Lotto.plugin.reloadConfig();
        				Util.sendPluginMessage(sender, "¡Configuración actualizada!", true);
        				if(args.length > 1) {
        					Bukkit.broadcastMessage(args[1]);
        				}
        			} else {
        				errorCode = 6;
        			}
        		} else if(args[0].equalsIgnoreCase("autoenter")) {
        			if(!isRegistered) {
        				errorCode = 2;
        			} else {
        				if(args.length > 1) {
        					if(StringUtils.isNumeric(args[1])) {
        						int amount = Integer.parseInt(args[1]);
        						if(amount > 0) {
        							Database.lotto.put(uuid, mem.modify(ModifyField.autoenter, true).modify(ModifyField.nextroundamount, amount));
        	        				sender.sendMessage(ChatColor.BLUE+"En la próxima ronda entrarás con "+ChatColor.DARK_GREEN+amount+ChatColor.BLUE+" boletos.");
        						} else {
        							errorCode = 3;
        						}
        					} else {
        						errorCode = 4;
        					}
        				} else {
        					errorCode = 5;
        				}
        			}
        		} else if(args[0].equalsIgnoreCase("changeautoenter")) {
        			if(!isRegistered) {
        				errorCode = 2;
        			} else {
        				if(mem.autoenter) {
        					if(args.length > 1) {
            					if(StringUtils.isNumeric(args[1])) {
            						int amount = Integer.parseInt(args[1]);
            						if(amount != 0) {
            							if(amount > 0) {
            								Database.lotto.put(uuid, mem.modify(ModifyField.nextroundamount, mem.nextRoundAmount+amount));
    			        					sender.sendMessage(ChatColor.GREEN+"Has comprado "+ChatColor.DARK_GREEN+amount+ChatColor.GREEN+" boletos más para la próxima ronda por "+ChatColor.GREEN+"$"+(Util.price*amount)+ChatColor.GREEN+".");
    		        					} else {
    		        						Database.lotto.put(uuid, mem.modify(ModifyField.nextroundamount, mem.nextRoundAmount-amount));
    			        						sender.sendMessage(ChatColor.GREEN+"Has retirado "+ChatColor.DARK_GREEN+amount+ChatColor.GREEN+" boletos para la próxima ronda.");
    		        					}
    	        					}
            					} else {
            						errorCode = 4;
            					}
            				} else {
            					errorCode = 5;
            				}
        				} else {
        					errorCode = 8;
        				}
        			}
        		} else if(args[0].equalsIgnoreCase("newautoenter")) {
        			if(!isRegistered) {
        				errorCode = 2;
        			} else {
        				if(mem.autoenter) {
        					if(args.length > 1) {
            					if(StringUtils.isNumeric(args[1])) {
            						int amount = Integer.parseInt(args[1]);
            						if(amount != mem.amount) {
    	        						if(amount >= 0) {
    	        							int newamount = mem.nextRoundAmount-amount;
    	        							if(newamount > 0) {
    	        								Database.lotto.put(uuid, mem.modify(ModifyField.nextroundamount, mem.nextRoundAmount+newamount));
    	        							} else if(newamount < 0) {
    	        								Database.lotto.put(uuid, mem.modify(ModifyField.nextroundamount, mem.nextRoundAmount-amount));
    	        							}
    	        							sender.sendMessage(ChatColor.GREEN+"En la próxima entrarás con "+ChatColor.DARK_GREEN+amount+ChatColor.GREEN+" boletos.");
    	        						} else {
    	        							errorCode = 7;
    	        						}
            						}
            					} else {
            						errorCode = 4;
            					}
            				} else {
            					errorCode = 5;
            				}
        				} else {
        					errorCode = 8;
        				}
        			}
        		}
        	}
        }
        if(errorCode > -1) {
        	switch(errorCode) {
        	case 0:
        		sender.sendMessage(ChatColor.RED+"Ya estás participando en la lotería. Debes usar el comando "+ChatColor.YELLOW+"'/lotto buy [cantidad]'"+ChatColor.RED+" para comprar boletos.");
        		break;
        	case 1:
        		sender.sendMessage(ChatColor.RED+"Ya estás participando en la lotería.");
        		break;
        	case 2:
        		sender.sendMessage(ChatColor.RED+"Debes primero inscribirte en la lotería usando "+ChatColor.YELLOW+"'/lotto enter'"+ChatColor.RED+".");
        		break;
        	case 3:
        		sender.sendMessage(ChatColor.RED+"El número especificado debe ser positivo, es decir, mayor de 0.");
        		break;
        	case 4:
        		sender.sendMessage(ChatColor.RED+"El argumento especificado no es numérico.");
        		break;
        	case 5:
        		sender.sendMessage(ChatColor.RED+"Debes especificar al menos un parametro más.");
        		break;
        	case 6:
        		sender.sendMessage(ChatColor.RED+"No tienes permisos para ejecutar este comando.");
        		break;
        	case 7:
        		sender.sendMessage(ChatColor.RED+"El número especificado debe ser mayor o igual que 0.");
        		break;
        	case 8:
        		sender.sendMessage(ChatColor.RED+"Para poder hacer esto debes usar primero el comando "+ChatColor.YELLOW+"'/lotto autoenter [cantidad]'"+ChatColor.RED+".");
        		break;
        	}
        }
        return false;
    }
    
}
