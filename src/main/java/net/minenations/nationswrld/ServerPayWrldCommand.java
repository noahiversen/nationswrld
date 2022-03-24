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
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(player.hasPermission("nationswrld.serverpay")) {
				if(args.length == 0) {
					player.sendMessage(ChatColor.GREEN + "Usage: /serverpaywrld <player> <amount>");
				}
				else if(args.length > 1) {
					Player targetPlayer = Bukkit.getPlayer(args[0]);
					if(targetPlayer != null) {
						try {
							double amount = Double.valueOf(args[1]);						
							NationsWRLD.getWalletAPI().getNFTPlayer(targetPlayer).getPrimaryWallet().payWRLD(amount, Network.POLYGON, "Gift");
						} catch(NumberFormatException e) {
							player.sendMessage(ChatColor.RED + "Amount must be a number.");
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "That player is not online.");
					}
				}
				else {
					player.sendMessage(ChatColor.GREEN + "Usage: /paywrld <player> <amount>");
				}
			}
		}
		return false;
	}
}
