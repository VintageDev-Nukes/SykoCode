package me.XXLuigiMario.SykoLotto;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SykoLotto extends JavaPlugin {

	protected static Economy econ = null;
	private HashMap<UUID, Integer> tickets = new HashMap<UUID, Integer>();
	private HashMap<UUID, Double> notifyWin = new HashMap<UUID, Double>();
	private int price = 100;
	private int base = 0;
	private int warnTime = 360;
	private int loopTime = 1800;
	private double jackpot = 0;
	private double multiplier = 1;
	private long timeStamp;
	private int time;
	private int lastWarn;
	private boolean forceJackpot = false;
	private boolean enabled = true;
	private boolean useMultiplier = false;
	private BukkitTask task;
	protected static SykoLotto plugin;
	private final SecureRandom rand = new SecureRandom();

	public void onEnable() {
		plugin = this;
		getCommand("lotto").setExecutor(new Commands());
		if (!setupEconomy()) {
			getServer().getPluginManager().disablePlugin(this);
		}
		if (enabled) {
			run();
		}
		jackpot = getConfig().getInt("jackpot");
	}
	
	public void onDisable() {
		if (task != null) {
			task.cancel();
		}
		getConfig().set("jackpot", jackpot);
		saveConfig();
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = (Economy) rsp.getProvider();
		return econ != null;
	}

	public void addTickets(Player player, int amount) {
		UUID uuid = player.getUniqueId();
		int oldTickets = 0;
		if (tickets.containsKey(uuid)) {
			oldTickets = tickets.get(uuid);
		}
		tickets.put(uuid, oldTickets + amount);
	}

	public void announce() {
		lastWarn = 0;
		int totalTickets = 0;
		for (int tickets : tickets.values()) {
			totalTickets += tickets;
		}
		int size = tickets.size();
		List<String> list = Arrays.asList(
				ChatColor.YELLOW + "Entra la lotería, ¡puedes ganar una suculenta recompensa!",
				"Comando: " + ChatColor.YELLOW + "/lotto",
				"Bote: " + formatPrize() + ChatColor.YELLOW + (base > 0 ? " (premio extra de " + ChatColor.GREEN + econ.format(base) + ChatColor.YELLOW + ")" : ""),
				"# de participantes: " + ChatColor.YELLOW + tickets.size() + " (con " + ChatColor.GREEN + totalTickets + ChatColor.YELLOW + " boletos comprados)",
				"Tiempo restante: " + ChatColor.YELLOW + timeConversion(1800 - time)
				);
		if (size > 0) {
			StringBuilder sb = new StringBuilder();
			String prefix = "" + ChatColor.YELLOW;
			for (UUID uuid : tickets.keySet()) {
				sb.append(prefix);
				prefix = ", ";
				sb.append(Bukkit.getOfflinePlayer(uuid).getName() + "[" + ChatColor.DARK_GREEN + tickets.get(uuid) + ChatColor.YELLOW + "]");
			}
			list.add(ChatColor.BLUE + "Participantes : " + sb.toString());
		}
		broadcastInfo(list, "Lotería");
	}
	
	protected void sendStats(Player player) {
		UUID uuid = player.getUniqueId();
		int tickets = getTickets(uuid);
		sendInfo(player, Arrays.asList(
				"Bote: " + ChatColor.YELLOW + formatPrize(),
				"Boletos comprados: " + ChatColor.YELLOW + tickets,
				"Tu apuesta: " + ChatColor.YELLOW + econ.format(tickets * SykoLotto.plugin.getPrice()),
				"Tu probablilidad: " + ChatColor.YELLOW + (getPercentage(uuid) * 100) + "%",
				"Tiempo restante: " + ChatColor.YELLOW + timeConversion(1800 - time),
				"Jackpot: " + ChatColor.YELLOW + formatJackpot()
				), "Información");
	}
	
	protected void cancel(String reason, boolean show) {
		time = 0;
		lastWarn = 0;
		for (UUID uuid : tickets.keySet()) {
			econ.depositPlayer(Bukkit.getOfflinePlayer(uuid), tickets.get(uuid));
		}
		Bukkit.broadcastMessage(ChatColor.GREEN + "El sorteo actual ha sido cancelado, se ha devuelto el dinero de los boletos.");
		if (show) {
			Bukkit.broadcastMessage(ChatColor.GREEN + "Razón: " + reason);
		}
	}

	public void end() {
		//Only half of the money taken from tickets goes to the jackpot
		time = 0;
		lastWarn = 0;
		if (base > 0) {
			int minimum = (int) (Bukkit.getOnlinePlayers().size() * 0.25);
			if (tickets.size() < minimum) {
				cancel("Al haber un premio extra, debe participar un 25% del servidor como mínimo.", true);
				return;
			}
		}
		if (!tickets.isEmpty()) {
			UUID uuid = getRandomWinner();
			double prize = getPrize();
			double total = prize;
			boolean wonJackpot = shouldJackpot();
			if (wonJackpot) {
				total += getJackpot();
				jackpot = 0;
			}
			OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
			broadcastInfo(Arrays.asList((
					"Ganador: "	+ ChatColor.YELLOW + ChatColor.ITALIC + player.getName() 
					+ ChatColor.YELLOW + " [" + ChatColor.DARK_GREEN + econ.format(prize) 
					+ (wonJackpot ? ChatColor.DARK_RED + "" + ChatColor.BOLD 
							+ " + JACKPOT " + ChatColor.DARK_GREEN + "= " 
							+ econ.format(total) : "") + ChatColor.YELLOW + "]")
					), "Lotería");
			econ.depositPlayer(player, total);
			if (player.isOnline()) {
				player.getPlayer().sendMessage(ChatColor.GREEN + "Has ganado " + econ.format(total) +  " de la lotería!");
			} else {
				notifyWin.put(uuid, total);
			}
			tickets.clear();
			jackpot += getPrize() + (getPrize() * 0.1);
			multiplier = 1;
			base = 0;
		} else {
			broadcastInfo(Arrays.asList("No hay participantes."), "Lotería");
		}
		run();
	}
	
	private void sendInfo(Player player, List<String> lines, String title) {
		String header = makeHeader(title);
		player.sendMessage(header);
		for (String line : lines) {
			player.sendMessage(ChatColor.BLUE + line);
		}
		player.sendMessage(ChatColor.BLUE + charStr('=', header.length() - 7));
	}
	
	private String makeHeader(String title) {
		String header = "";
		if (title.length() <= 15) {
			int size = 25;
			while (header.length() < 1 || header.length() > 50) {
				size--;
				String spacer = charStr('=', size);
				header = ChatColor.BLUE + spacer + " " + ChatColor.YELLOW + title + " " + ChatColor.BLUE + spacer;
			}
		}
		return header;
	}
	
	private void broadcastInfo(List<String> lines, String header) {
		for (Player player : getServer().getOnlinePlayers()) {
			sendInfo(player, lines, header);
		}
	}

	public void run() {
		timeStamp = System.currentTimeMillis();
		Random random = new Random();
		task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
			public void run() {
				long current = System.currentTimeMillis();
				if (TimeUnit.MILLISECONDS.toSeconds(current - timeStamp) >= 1) {
					timeStamp = current;
					if (time == 0) {
						int online = Bukkit.getOnlinePlayers().size();
						if (online >= 10) {
							base = online * 50;
							if (useMultiplier && random.nextDouble() <= 0.1) {
								multiplier = 2;
							}
						}
						if (useMultiplier && online >= 25) {
							if (random.nextDouble() <= 0.05) {
								multiplier = 2.5;
							} else {
								multiplier = 1.5;
							}
						}
						broadcastInfo(Arrays.asList("Nuevo sorteo! " + ChatColor.GREEN + "Bote inicial: " + formatPrize()), "Lotería");
					}
					time++;
					lastWarn++;
					if (time == loopTime) {
						end();
					} else if (lastWarn == warnTime) {
						announce();
					} else if (loopTime - time == 30) {
						broadcastInfo(Arrays.asList("Quedan 30 segundos!"), "Lotería");
					}
				}
			}
		}, 0, 1);
	}

	private UUID getRandomWinner() {
		while(true) {
			for (UUID uuid : tickets.keySet()) {
				if (rand.nextDouble() <= getPercentage(uuid)) {
					return uuid;
				}
			}
		}
	}

	private int getTotalTickets() {
		int totalTickets = 0;
		for (int tickets : tickets.values()) {
			totalTickets += tickets;
		}
		return totalTickets;
	}

	protected int getTickets(UUID uuid) {
		return tickets.containsKey(uuid) ? tickets.get(uuid) : 0;
	}

	protected double getPercentage(UUID uuid) {
		double percentage = (double) getTickets(uuid) / (double) getTotalTickets();
		return Double.isNaN(percentage) ? 0 : percentage;
	}
	
	private double getBasePrize() {
		return (getTotalTickets() * getValue()) + base;
	}

	private double getPrize() {
		return getBasePrize() * multiplier;
	}
	
	private double getJackpot() {
		return jackpot - (jackpot * 0.05);
	}
	
	protected String formatPrize() {
		return ChatColor.YELLOW + econ.format(getBasePrize()) + (multiplier > 1 ? " " + ChatColor.DARK_RED + ChatColor.BOLD + "x" + multiplier : "");
	}
	
	protected int getPrice() {
		return price;
	}
	
	protected double getValue() {
		return price - (price * 0.1);
	}
	
	private boolean shouldJackpot() {
		if (forceJackpot) {
			forceJackpot = false;
			return true;
		} else {
			return jackpot > 0 && rand.nextDouble() <= 0.01;
		}
	}
	
	protected boolean isParticipating(Player player) {
		return tickets.containsKey(player.getUniqueId());
	}
	
	protected void forceJackpot() {
		forceJackpot = true;
	}
	
	protected void handleJoin(Player player) {
		UUID uuid = player.getUniqueId();
		if (notifyWin.containsKey(uuid)) {
			player.sendMessage(ChatColor.GREEN + "Mientras estabas desconectado, has ganado " + econ.format(notifyWin.get(uuid)) +  " de la lotería!");
			notifyWin.remove(uuid);
		}
	}
	
	protected static String charStr(char c, int size) {
		StringBuilder sb = new StringBuilder();
		while (sb.length() != size) {
			sb.append(c);
		}
		return sb.toString();
	}
	
	private String formatJackpot() {
		return econ.format(jackpot) + " - tasa 5% = " + econ.format(getJackpot());
	}

	private String timeConversion(int totalSeconds) {
		int seconds = totalSeconds % 60;
		int totalMinutes = totalSeconds / 60;
		int minutes = totalMinutes % 60;
		int hours = totalMinutes / 60;

		return (hours > 0 ? hours + " horas " : "")
				+ (minutes > 0 ? minutes + " minutos " : "")
				+ (seconds > 0 ? seconds + " segundos" : "");
	}
}
