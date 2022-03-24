package net.minenations.nationswrld;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nftworlds.wallet.objects.Wallet;

import net.md_5.bungee.api.ChatColor;

public class WrldBalCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			Wallet wallet = NationsWRLD.getWalletAPI().getPrimaryWallet(player);
			if(NationsWRLD.getWalletAPI().getNFTPlayer(player).isLinked()) {
				player.sendMessage(ChatColor.GREEN + "Current $WRLD: " + wallet.getPolygonWRLDBalance());
			}
			else {
				player.sendMessage(ChatColor.RED + "You do not have a connected wallet.");
			}
		}
		return false;
	}

}
