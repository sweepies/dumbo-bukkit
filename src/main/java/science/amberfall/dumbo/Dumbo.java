package science.amberfall.dumbo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Dumbo extends JavaPlugin implements Listener {

    static Dumbo plugin;

    public static Plugin getPlugin() {
        JavaPlugin plugin = Dumbo.plugin;
        return plugin;
    }

    private static TaskChainFactory taskChainFactory;

    private AtomicBoolean readyToQuote = new AtomicBoolean(false);

    @Override
    public void onEnable() {

        plugin = this;

        taskChainFactory = BukkitTaskChainFactory.create(this);

        if (Bukkit.getServer().getVersion().contains("git-Spigot")) {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            console.sendMessage("[Dumbo]" + ChatColor.RED + "We highly recommend that you use Paper over Spigot, as they have been hostile to me personally and the development of this plugin. Paper offers many performance improvements and optimizations over Spigot. Please download and install Paper from https://paperci.emc.gs to use this plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            createConfig();
            Bukkit.getServer().getPluginManager().registerEvents(this, this);
        }
    }

    @Override
    public void onDisable() {}

    private void createConfig() {
        try {

            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            File file = new File(getDataFolder(), "config.yml");

            if (!file.exists()) {
                getLogger().info("Fetching quotes from GitHub..");
                taskChainFactory.newChain().asyncFirst(() -> {
                    try {
                        InputStream in = new URL("https://raw.githubusercontent.com/sweepyoface/dumbo/master/quotes.yml").openStream();
                        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
                        return yaml.getStringList("quotes");
                    } catch (IOException e) {
                        getLogger().severe("Unable to fetch quotes from Github.");
                        getServer().getPluginManager().disablePlugin(this);
                        return null;
                    }
                }).abortIfNull().syncLast(quotes -> {
                    Dumbo.getPlugin().getConfig().addDefault("color", "&6");
                    Dumbo.getPlugin().getConfig().addDefault("quotes", quotes);
                    Dumbo.getPlugin().getConfig().options().copyDefaults(true);
                    saveConfig();
                    reloadConfig();
                    getLogger().info("Done!");
                }).execute(() -> readyToQuote.set(true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String randomQuote() {
        Random random = new Random();
        List<String> quotes = this.getConfig().getStringList("quotes");
        Integer quote = random.nextInt(quotes.size());
        return quotes.get(quote);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (label.equalsIgnoreCase(("dumbo")) && sender instanceof Player) {
            if (args.length == 0) {
                if (player.hasPermission("dumbo.quote") || player.isOp()) {
                    String quote = randomQuote();
                    if (!readyToQuote.get()) {
                        player.sendMessage(ChatColor.DARK_RED + "Error: Quotes are still being initialized, please wait or contact a server administrator.");
                    } else {
                        Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("color")) + randomQuote());
                    }
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("dumbo.reload") || player.isOp()) {
                    this.reloadConfig();
                    player.sendMessage(ChatColor.GREEN + "Dumbo config reloaded!");
                } else {
                    player.sendMessage(ChatColor.DARK_RED + "You do not have access to that command.");
                }
            } else if (args[0].equalsIgnoreCase("ver")||(args[0].equalsIgnoreCase("version"))) {
                if (player.hasPermission("dumbo.version") || player.isOp()) {
                    player.sendMessage(ChatColor.GREEN + "Dumbo version: " + this.getDescription().getVersion());
                } else {
                    player.sendMessage(ChatColor.DARK_RED + "You do not have access to that command.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Unknown argument. See /help dumbo");
            }
        }
        return true;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent ev) {
        if (!ev.isCancelled()) {
            String[] msgArray = ev.getMessage().trim().split("\\s+");
            if (msgArray[0].equalsIgnoreCase(".dumbo")) {
                Player player = ev.getPlayer();
                if (player.hasPermission("dumbo.quote") || player.isOp()) {
                    if (!readyToQuote.get()) {
                        player.sendMessage(ChatColor.DARK_RED + "Error: Quotes are still being initialized, please wait or contact a server administrator.");
                    } else {
                        String quote = randomQuote();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', Dumbo.getPlugin().getConfig().getString("color")) + quote);
                            }
                        }.runTaskLater(this, 1);
                    }
                } else {
                    player.sendMessage(ChatColor.DARK_RED + "You do not have access to that command.");
                }
            }
        }
    }
}
