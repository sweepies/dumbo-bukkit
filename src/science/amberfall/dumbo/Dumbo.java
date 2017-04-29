package science.amberfall.dumbo;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Dumbo extends JavaPlugin implements Listener {
    
    @Override
    public void onEnable() {
        createConfig();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
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
            	
                InputStream in = new URL("https://raw.githubusercontent.com/sweepyoface/dumbo/master/quotes.yml").openStream();
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
				
                String color = "&6";
                
                this.getConfig().addDefault("color", color);
                this.getConfig().addDefault("quotes", yaml.getStringList("quotes"));

                this.getConfig().options().copyDefaults(true);
                saveConfig();
                reloadConfig();
                
                getLogger().info("Done!");  
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String randomQuote() {
        
        Random random = new Random();
        List<String> quotes = this.getConfig().getStringList("quotes");
        Integer quote = random.nextInt(quotes.size());
                
        return (String) quotes.get(quote);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        Player player = (Player) sender;
        
        if (label.equalsIgnoreCase(("dumbo")) && sender instanceof Player) {
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
                Player player = (Player) ev.getPlayer();
                if (player.hasPermission("dumbo.quote") || player.isOp()) {    
                    Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("color")) + randomQuote());
                } else {    
                    player.sendMessage(ChatColor.DARK_RED + "You do not have access to that command.");
                }
            }
        }
    }
}
