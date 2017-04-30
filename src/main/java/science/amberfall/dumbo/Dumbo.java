package science.amberfall.dumbo;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dumbo extends JavaPlugin implements Listener {

    private TaskChainFactory taskChainFactory;
    private AtomicBoolean readyToQuote = new AtomicBoolean(false);

    @Override
    public void onEnable() {

        if (Bukkit.getServer().getVersion().contains("git-Spigot")) {
        	
        	ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        	
            console.sendMessage("[Dumbo]" + ChatColor.RED + "We highly recommend that you use Paper over Spigot, as they have been hostile to me personally and the development of this plugin. Paper offers many performance improvements and optimizations over Spigot. Please download and install Paper from https://paperci.emc.gs to use this plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            
        } else {
            taskChainFactory = BukkitTaskChainFactory.create(this);
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
                        return new URL("https://raw.githubusercontent.com/sweepyoface/dumbo/master/quotes.yml").openStream();
                    }catch(IOException e) {
                        getLogger().severe("Unable to fetch quotes from Github.");
                        getServer().getPluginManager().disablePlugin(this);
                        return null;
                    }
                }).abortIfNull().syncLast(in -> {
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
                    String colour = "&6";

                    getConfig().addDefault("color", colour);
                    getConfig().addDefault("quotes", yaml.getStringList("quotes"));

                    getConfig().options().copyDefaults(true);
                    saveConfig();
                    reloadConfig();
                }).execute(() -> readyToQuote.set(true));

                getLogger().info("Done!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String randomQuote() {

        Random random = new Random();
        List < String > quotes = this.getConfig().getStringList("quotes");
        Integer quote = random.nextInt(quotes.size());

        return quotes.get(quote);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase(("dumbo")) && sender instanceof Player) {
            Player player = (Player) sender;
            if(!readyToQuote.get()) {
                player.sendMessage(ChatColor.RED + "Quotes aren't ready to be used!");
                return true;
            }
            if (args.length == 0) {
                if (player.hasPermission("dumbo.quote") || player.isOp()) {
                    Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("color")) + randomQuote());
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("dumbo.reload") || player.isOp()) {
                    this.reloadConfig();
                    player.sendMessage(ChatColor.GREEN + "Dumbo config reloaded!");
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
                if(!readyToQuote.get()) {
                    player.sendMessage(ChatColor.RED + "Quotes aren't ready to be used!");
                }
                if (player.hasPermission("dumbo.quote") || player.isOp()) {
                    Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("color")) + randomQuote());
                } else {
                    player.sendMessage(ChatColor.DARK_RED + "You do not have access to that command.");
                }
            }
        }
    }
}