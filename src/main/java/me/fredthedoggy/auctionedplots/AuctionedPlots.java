package me.fredthedoggy.auctionedplots;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AuctionedPlots extends JavaPlugin {

    private Economy economy;
    private static AuctionedPlots instance;
    DatabaseManager databaseManager;
    FileConfiguration config;
    Logger logger = getLogger();

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginCommand("auctionedplots").setExecutor(new AuctionCommand());
        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        File pluginFolder = new File("plugins/AuctionedPlots");
        if (!pluginFolder.isDirectory()) pluginFolder.delete();
        if (!pluginFolder.isDirectory()) pluginFolder.mkdir();
        File dbFile = new File("plugins/AuctionedPlots/database.json");
        databaseManager = new DatabaseManager(dbFile);
        saveDefaultConfig();
        config = this.getConfig();
        for (String path : config.getConfigurationSection("plots").getKeys(false)) {
            String name = config.getString("plots." + path + ".name");
            String world = config.getString("plots." + path + ".world");
            Integer startingPrice = config.getInt("plots." + path + ".starting-price");
            Integer bidIncrement = config.getInt("plots." + path + ".bid-increment");
            Integer auctionLength = config.getInt("plots." + path + ".auction-length");
            Integer auctionRepeat = config.getInt("plots." + path + ".auction-repeat");
            if (name == null) {
                logger.log(Level.WARNING, "Missing Name in \"" + path + "\" Plot");
                return;
            }
            if (world == null) {
                logger.log(Level.WARNING, "Invalid World in \"" + path + "\" Plot");
                return;
            }
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) {
                logger.log(Level.WARNING, "Invalid World in \"" + path + "\" Plot");
                return;
            }
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(bukkitWorld));
            ProtectedRegion region = regions.getRegion(path);
            if (region == null) {
                logger.log(Level.WARNING, "Invalid Region (Missing? Wrong World?) in \"" + path + "\" Plot");
                return;
            }
            DefaultDomain members = region.getMembers();
            DatabaseType.Plot db = databaseManager.getPlot(path);
            if (db == null) {
                db = new DatabaseType().new Plot(path, null);
            }
            members.removeAll();
            if (db.owner != null) members.addPlayer(db.owner);
            region.setMembers(members);
            databaseManager.setPlot(db);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Date date = new Date();
                for (DatabaseType.Plot plot : databaseManager.getPlots()) {
                    if (plot.nexttime == 0) continue;
                    if (plot.nexttime > date.toInstant().minusSeconds(60 * config.getInt("plots." + plot.plot + ".auction-length")).getEpochSecond())
                        continue;
                    if (plot.selling == null) {
                        plot.selling = new DatabaseType().new Sell();
                        System.out.println("Plot" + plot.plot + " is now Selling");
                    }
                    if (plot.nexttime > date.getTime()) {
                        System.out.println("Plot" + plot.plot + " is done Selling");
                        UUID bidder = null;
                        Integer bid = config.getInt("plots." + plot.plot + ".starting-pirce");
                        for (Map.Entry<UUID, Integer> entry : plot.selling.bids.entrySet()) {
                            if (entry.getValue() <= bid) continue;
                            bid = entry.getValue();
                            bidder = entry.getKey();
                        }
                        World bukkitWorld = Bukkit.getWorld(config.getString("plots." + plot.plot + ".world"));
                        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                        RegionManager regions = container.get(BukkitAdapter.adapt(bukkitWorld));
                        ProtectedRegion region = regions.getRegion(plot.plot);
                        DefaultDomain members = region.getMembers();
                        if (bidder == null) {
                            System.out.println("Plot" + plot.plot + " Did Not Sell");
                            plot.owner = null;
                            members.removeAll();
                            continue;
                        }
                        plot.owner = bidder;
                        members.removeAll();
                        members.addPlayer(bidder);
                        System.out.println("Plot" + plot.plot + " Sold to " + Bukkit.getOfflinePlayer(bidder).getName());
                        return;
                    }
                    System.out.println("Plot" + plot.plot + " is currently Selling");
                }
            }
        }.runTaskTimerAsynchronously(this, 40, 40);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static AuctionedPlots getInstance() {
        return instance;
    }

    public void reload() {
        Bukkit.getScheduler().cancelTasks(this);
        onDisable();
        onEnable();
    }
}
