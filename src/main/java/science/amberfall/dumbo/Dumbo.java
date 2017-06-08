package science.amberfall.dumbo;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dumbo extends JavaPlugin implements Listener {

    private File quotesFile = new File(getDataFolder(), "quotes.json");
    private static Dumbo plugin;
    private static TaskChainFactory taskChainFactory;
    private AtomicBoolean readyToQuote = new AtomicBoolean(false);

    public static Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {

        plugin = this;

        taskChainFactory = BukkitTaskChainFactory.create(this);

        // Disable if server is branded Spigot
        if (Bukkit.getServer().getVersion().contains("git-Spigot")) {
            getLogger().severe(Locale.ERR_UsePaper);
            disableMe();
        } else {
            Bukkit.getServer().getPluginManager().registerEvents(this, this);
            saveDefaultConfig();
            fetchQuotes(null);
        }

    }

    @Override
    public void onDisable() {}

    // Print a message and disable the plugin
    private void disableMe() {
        getLogger().severe(Locale.ERR_DisablingPlugin);
        Bukkit.getPluginManager().disablePlugin(this);
    }


    private void fetchQuotes(Player p) {

        if (!quotesFile.exists()) {

            // Make sure to not try to get a quote when it's fetching
            readyToQuote.set(false);

            taskChainFactory.newChain().asyncFirst(() -> {
                try {
                    getLogger().info(Locale.QUOTES_FetchingQuotes);
                    FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/sweepyoface/dumbo-quotes/master/quotes.json"), quotesFile);
                    getLogger().info(Locale.QUOTES_DoneFetching);
                    return true;
                } catch (Exception e) {
                    // If this method was called via the updateQuotes method (from the command) as a result of the quotes file not existing, send the player the message as well
                    if (p != null) {
                        p.sendMessage(ChatColor.RED + Locale.ERR_UnableToFetchQuotes);
                    }
                    getLogger().severe(Locale.ERR_UnableToFetchQuotes);
                    disableMe();
                    return null;
                }
            }).abortIfNull().syncLast(outcome -> {
                reloadConfig();
                if (p != null) {
                    p.sendMessage(ChatColor.GREEN + Locale.QUOTES_DoneFetching);
                }
            }).execute(() -> readyToQuote.set(true));

        } else {
            getLogger().info(Locale.QUOTES_FileExists);
            readyToQuote.set(true);
        }
    }

    private void updateQuotes(Player p) {

        if (quotesFile.exists()) {
            try {

                quotesFile.delete();

                // Make sure to not try to get a quote when it's updating
                readyToQuote.set(false);

                taskChainFactory.newChain().asyncFirst(() -> {
                    try {
                        getLogger().info(Locale.QUOTES_FetchingQuotes);
                        FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/sweepyoface/dumbo-quotes/master/quotes.json"), quotesFile);
                        getLogger().info(Locale.QUOTES_DoneFetching);
                        return true;
                    } catch (Exception e) {
                        getLogger().severe(Locale.ERR_UnableToFetchQuotes);
                        disableMe();
                        return null;
                    }
                }).syncLast(outcome -> {
                    reloadConfig();
                    if (outcome) {
                        p.sendMessage(ChatColor.GREEN + Locale.QUOTES_DoneFetching);
                    } else {
                        p.sendMessage(ChatColor.RED + Locale.ERR_UnableToFetchQuotes);
                    }
                }).execute(() -> {
                    readyToQuote.set(true);
                });

            } catch (Exception e) {
                getLogger().severe(Locale.ERR_UnableToDeleteQuotes);
                p.sendMessage(ChatColor.RED + Locale.ERR_UnableToDeleteQuotes);
            }
        } else {
            fetchQuotes(p);
        }
    }

    public String randomQuote() {
        if (quotesFile.exists()) {
            try {
                FileReader reader = new FileReader(quotesFile);
                Gson gson = new Gson();
                Quotes quotes = gson.fromJson(reader, Quotes.class);
                String[] quotesList = quotes.getQuotes();
                Random random = new Random();
                Integer quote = random.nextInt(quotesList.length);
                return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("color")) + quotesList[quote];
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ChatColor.RED + Locale.ERR_UnableToReadQuotesFile;
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase(("dumbo"))) {
            if (!(sender instanceof ConsoleCommandSender)) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    if (player.hasPermission("dumbo.quote")) {
                        if (!readyToQuote.get()) {
                            player.sendMessage(ChatColor.DARK_RED + Locale.QUOTES_NotInitialized);
                        } else {
                            Bukkit.getServer().broadcastMessage(randomQuote());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("dumbo.reload")) {
                        this.reloadConfig();
                        player.sendMessage(ChatColor.GREEN + Locale.PLUGIN_ConfigReloaded);
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + Locale.CMD_NoAccess);
                    }
                } else if (args[0].toLowerCase().matches("(version|ver)")) {
                    if (player.hasPermission("dumbo.version")) {
                        player.sendMessage(ChatColor.GREEN + "Dumbo version: " + this.getDescription().getVersion());
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + Locale.CMD_NoAccess);
                    }
                } else if (args[0].equalsIgnoreCase("getquotes")) {
                    if (player.hasPermission("dumbo.getquotes")) {
                        player.sendMessage(ChatColor.YELLOW + Locale.QUOTES_FetchingQuotes);
                        updateQuotes(player);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + Locale.CMD_UnknownArg);
                }
            } else {
                getLogger().severe(Locale.CMD_CantRunFromConsole);
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
                if (player.hasPermission("dumbo.quote")) {
                    if (!readyToQuote.get()) {
                        player.sendMessage(ChatColor.DARK_RED + Locale.QUOTES_NotInitialized);
                    } else {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', Dumbo.getPlugin().getConfig().getString("color")) + randomQuote());
                            }
                        }.runTaskLater(this, 1); // So the quote is sent after the .dumbo chat message
                    }
                } else {
                    player.sendMessage(ChatColor.DARK_RED + Locale.CMD_NoAccess);
                }
            }
        }
    }
}
