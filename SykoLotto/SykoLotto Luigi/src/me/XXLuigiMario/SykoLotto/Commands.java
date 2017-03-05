package me.XXLuigiMario.SykoLotto;

import net.milkbowl.vault.economy.EconomyResponse;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0) {
				player.sendMessage(ChatColor.BLUE + "Usa el comando "
						+ ChatColor.YELLOW + "'/lotto enter (quantity)'"
						+ ChatColor.BLUE + " para participar en la loteria.");
				player.sendMessage(ChatColor.BLUE + "Usa el comando "
						+ ChatColor.YELLOW + "'/lotto stats'" + ChatColor.BLUE
						+ " para ver tus estadísticas actuales.");
				player.sendMessage(ChatColor.BLUE + "Usa el comando "
						+ ChatColor.YELLOW + "'/lotto end'" + ChatColor.BLUE
						+ " para forzar el final del sorteo.");
				player.sendMessage(ChatColor.BLUE + "Usa el comando "
						+ ChatColor.YELLOW + "'/lotto cancel'" + ChatColor.BLUE
						+ " para cancelar el sorteo y devolver el dinero de los boletos");
				player.sendMessage(ChatColor.BLUE + "Usa el comando "
						+ ChatColor.YELLOW + "'/lotto jackpot'" + ChatColor.BLUE
						+ " para forzar que caiga el jackpot");
			} else {
				if (args[0].equalsIgnoreCase("enter")) {
					if (args.length == 1) {
						buyTickets(player, 1);
					} else if (args.length == 2) {
						if (StringUtils.isNumeric(args[1])) {
							int tickets = Integer.parseInt(args[1]);
							if (tickets < 1) {
								player.sendMessage(ChatColor.DARK_RED + "¡Debes comprar 1 boleto como mínimo!");
							} else {
								buyTickets(player, tickets);
							}
						} else {
							player.sendMessage(ChatColor.DARK_RED + "¡Debes especificar un número!");
						}
					} else {
						player.sendMessage(ChatColor.DARK_RED + "Demasiados argumentos.");
					}
				} else if (args[0].equalsIgnoreCase("stats")) {
					if (args.length == 1) {
						SykoLotto.plugin.sendStats(player);
					} else {
						player.sendMessage(ChatColor.DARK_RED + "Demasiados argumentos.");
					}
				} else if (args[0].equalsIgnoreCase("end")) {
					if (player.isOp()) {
						SykoLotto.plugin.end();
						player.sendMessage(ChatColor.GREEN + "Sorteo finalizado.");
					} else {
						player.sendMessage(ChatColor.DARK_RED + "No tienes permiso para hacer eso.");
					}
				} else if (args[0].equalsIgnoreCase("cancel")) {
					if (player.isOp()) {
						SykoLotto.plugin.cancel("", false);
						player.sendMessage(ChatColor.GREEN + "Sorteo cancelado.");
					} else {
						player.sendMessage(ChatColor.DARK_RED + "No tienes permiso para hacer eso.");
					}
				} else if (args[0].equalsIgnoreCase("jackpot")) {
					if (player.isOp()) {
						SykoLotto.plugin.forceJackpot();
						player.sendMessage(ChatColor.GREEN + "Este sorteo dará jackpot.");
					} else {
						player.sendMessage(ChatColor.DARK_RED + "No tienes permiso para hacer eso.");
					}
				} else {
					player.sendMessage(ChatColor.DARK_RED + "Error de sintaxis.");
				}
			}
		}
		return false;
	}

	private void buyTickets(Player player, int tickets) {
		int price = SykoLotto.plugin.getPrice();
		int total = price * tickets;
		if (SykoLotto.econ.has(player, total)) {
			EconomyResponse resp = SykoLotto.econ.withdrawPlayer(player, total);
			if (resp.transactionSuccess()) {
				if (SykoLotto.plugin.isParticipating(player)) {
					player.sendMessage(ChatColor.GREEN + "¡Has comprado " + tickets + " " + (tickets == 1 ? "boleto" : "boletos") + " más!");
				} else {
					player.sendMessage(ChatColor.GREEN + "¡Has entrado en la lotería, buena suerte!");
				}
				Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + ChatColor.BLUE + " ha comprado " + ChatColor.DARK_GREEN + tickets + ChatColor.BLUE + " " + (tickets == 1 ? "boleto" : "boletos") + " a " + ChatColor.YELLOW + SykoLotto.econ.format(price) + ChatColor.BLUE + ", sumando " + ChatColor.YELLOW + SykoLotto.econ.format(SykoLotto.plugin.getValue() * tickets) + ChatColor.BLUE + " al bote total (" + SykoLotto.plugin.formatPrize() + ChatColor.BLUE + ").");
				SykoLotto.plugin.addTickets(player, tickets);
			} else {
				player.sendMessage(ChatColor.DARK_RED + "Se ha producido un error al hacer la transacción, por favor, contacta un administrador.");
			}
		} else {
			player.sendMessage(ChatColor.DARK_RED + "No tienes suficiente dinero.");
		}
	}
}
