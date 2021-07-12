package me.fredthedoggy.auctionedplots;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuctionCommand implements CommandExecutor {

    AuctionedPlots instance = AuctionedPlots.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("auctionplots.use") && !player.hasPermission("auctionplots.admin")) {

            return true;
        }
        Boolean admin = player.hasPermission("auctionplots.admin");
        if (args.length < 1) {
            return true;
        }
        switch (args[0]) {
            case "start":
            case "create":
                start(args, player, admin);
                break;
            case "reload":
            case "restart":
                reload(player, admin);
                break;

        }
        return true;
    }

    private void start(String[] args, Player player, boolean admin) {
        if (!admin) {

            return;
        }
    }

    private void bid(String[] args, Player player, boolean admin) {
    }

    private void reload(Player player, boolean admin) {
        if (!admin) {
            return;
        }
        instance.reload();
    }
}