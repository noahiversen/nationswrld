package net.minenations.nationswrld;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.nftworlds.wallet.objects.Network;
import com.nftworlds.wallet.objects.Wallet;

import net.md_5.bungee.api.ChatColor;

public class PayWrldCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(args.length == 0) {
				player.sendMessage(ChatColor.GREEN + "Usage: /paywrld <player> <amount>");
			}
			else if(args.length > 1) {
				Player targetPlayer = Bukkit.getPlayer(args[0]);
				if(targetPlayer != null) {
					try {
						double amount = Double.valueOf(args[1]);
						Wallet fromWallet = NationsWRLD.getWalletAPI().getPrimaryWallet(player);
						if(fromWallet != null) {
							Wallet toWallet = NationsWRLD.getWalletAPI().getPrimaryWallet(targetPlayer);
							if(toWallet != null) {
								if(fromWallet.getPolygonWRLDBalance() >= amount) {
									new BukkitRunnable() {
										@Override
										public void run() {
		 									NationsWRLD.getWalletAPI().createPlayerPayment(player, targetPlayer, amount, Network.POLYGON, "Gift");
										}
									}.runTaskAsynchronously(NationsWRLD.getInstance());
								}
								else {
									player.sendMessage(ChatColor.RED + "You do not have sufficient amount of $WRLD. Current balance: " + fromWallet.getPolygonWRLDBalance());
								}
							}
							else {
								player.sendMessage(ChatColor.RED + "That player does not have a connected wallet.");
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "You do not have a connected wallet.");
						}
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
		return false;
	}
}
