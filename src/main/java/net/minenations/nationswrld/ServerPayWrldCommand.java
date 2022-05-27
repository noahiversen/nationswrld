package net.minenations.nationswrld;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.nftworlds.wallet.objects.Network;

import net.md_5.bungee.api.ChatColor;

public class ServerPayWrldCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		if(!(sender instanceof Player)) {
			if(sender.hasPermission("nationswrld.serverpay")) {
				if(args.length == 0) {
					sender.sendMessage(ChatColor.GREEN + "Usage: /serverpaywrld <player> <amount>");
				}
				else if(args.length > 1) {
					Player targetPlayer = Bukkit.getPlayer(args[0]);
					if(targetPlayer != null) {
						try {
							double amount = Double.parseDouble(args[1]);						
							NationsWRLD.getWalletAPI().sendWRLD(targetPlayer, amount, Network.POLYGON, "Gift");
						} catch(NumberFormatException e) {
							sender.sendMessage(ChatColor.RED + "Amount must be a number.");
						}
					}
					else {
						sender.sendMessage(ChatColor.RED + "That player is not online.");
					}
				}
				else {
					sender.sendMessage(ChatColor.GREEN + "Usage: /serverpaywrld <player> <amount>");
				}
			}
		}
		return false;
	}
}
