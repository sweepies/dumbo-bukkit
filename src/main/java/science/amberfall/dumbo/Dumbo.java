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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dumbo extends JavaPlugin implements Listener {

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

        if (Bukkit.getServer().getVersion().contains("git-Spigot")) {
            getLogger().severe(Locale.ERR_UsePaper);
            disableMe();
        } else {
            Bukkit.getServer().getPluginManager().registerEvents(this, this);
            createConfig();
        }
    }

    @Override
    public void onDisable() {}

    private void disableMe() {
        getLogger().severe(Locale.ERR_DisablingPlugin);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    private void createConfig() {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdirs()) {
                getLogger().severe(Locale.ERR_UnableToCreateDataFolder);
                disableMe();
            }
        }

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            getLogger().info(Locale.QUOTES_FetchingQuotes);
            taskChainFactory.newChain().asyncFirst(() -> {
                try {
                    InputStream in = new URL("https://raw.githubusercontent.com/sweepyoface/dumbo/master/quotes.yml").openStream();
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
                    in.close(); // Say goodbye to memory leaks
                    return yaml.getStringList("quotes");

                } catch (IOException e) {
                    getLogger().severe(Locale.ERR_UnableToFetchQuotes);
                    disableMe();
                    return null;
                }
            }).abortIfNull().syncLast(quotes -> {
                Dumbo.getPlugin().getConfig().addDefault("color", "&6");
                Dumbo.getPlugin().getConfig().addDefault("quotes", quotes);
                Dumbo.getPlugin().getConfig().options().copyDefaults(true);
                saveConfig();
                reloadConfig();
                getLogger().info(Locale.QUOTES_DoneFetching);
            }).execute(() -> readyToQuote.set(true));
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
        if (label.equalsIgnoreCase(("dumbo"))) {
            if (!(sender instanceof ConsoleCommandSender)) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    if (player.hasPermission("dumbo.quote") || player.isOp()) {
                        if (!readyToQuote.get()) {
                            player.sendMessage(ChatColor.DARK_RED + Locale.QUOTES_NotInitialized);
                        } else {
                            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("color")) + randomQuote());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("dumbo.reload") || player.isOp()) {
                        this.reloadConfig();
                        player.sendMessage(ChatColor.GREEN + Locale.PLUGIN_ConfigReloaded);
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + Locale.CMD_NoAccess);
                    }
                } else if (args[0].equalsIgnoreCase("ver") || (args[0].equalsIgnoreCase("version"))) {
                    if (player.hasPermission("dumbo.version") || player.isOp()) {
                        player.sendMessage(ChatColor.GREEN + "Dumbo version: " + this.getDescription().getVersion());
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + Locale.CMD_NoAccess);
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
                if (player.hasPermission("dumbo.quote") || player.isOp()) {
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