package science.amberfall;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
                String[] quotes = { 
                    "<Qball> what if I bring spuds Redrield",
                    "<Qball> what if we make a dysoshphere around the sun",
                    "<Qball> what if i DONT give a shit where it was", "<Qball> what if they decompile it",
                    "<Qball> what if I give you spud",
                    "<Qball> what if I were going for life in prison and a lawyer saved me",
                    "<Qball> what if you were being charged with manslaughter for shooting a dude with your 50 cal cause he broke into your house and a lawyer kept you from jail time",
                    "<Qball> what if you have a hash map of a hash map of a hash map",
                    "<Qball> what if someone made a premium plugin that saves nudes to your server directory",
                    "<Qball> what if mc was written in Swift", "<Qball> what if mc was wrote in LUA",
                    "<Qball> what if I want 7zip", "<Qball> what if java didn't have a vm",
                    "<Qball> what if it compiled straight to native code",
                    "<Qball> what if I register them at runtime", "<Qball> what if minecraft were made in Lua",
                    "<Qball> what if Choco loved me", "<Qball> what if I ran my mc server on port 21",
                    "<Qball> what if Z750 cared about potatoes",
                    "<Qball> What if you don't want to update, like me? I'm keeping my server 1.8 forever, with ViaVersion, however.",
                    "<Qball> What if Minecraft was coded in VB",
                    "<Qball> what if I have a link to a gitrepo for a download that has the link but the link is adfly",
                    "<Qball> Maybe I'll bother learning it when its a full release and the syntax actually makes sense",
                    "<Qball> I think you go async to avoid crashing server",
                    "<Qball> well zombies need sleep to I think",
                    "<Qball> Redrield: I think so I don't see any Koltin Developer jobs out there I see Java Developer jobs",
                    "<Qball> what if Java went native" 
                };
                
                String color = "&6";
                this.getConfig().addDefault("color", color);
                this.getConfig().addDefault("quotes", quotes);
                
                this.getConfig().options().copyDefaults(true);
                saveConfig();
                reloadConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String randomQuote() {
        
        Random random = new Random();
        String[] quotes = this.getConfig().getStringList("quotes").stream().toArray(String[]::new);
        Integer quote = random.nextInt(quotes.length);
                
        return (String) quotes[quote];
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        Player player = (Player) sender;
        
        if (label.equalsIgnoreCase(("dumbo")) && sender instanceof Player) {
            getLogger().info(Arrays.toString(args));
            if (Arrays.toString(args).equals("[]")) {
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
