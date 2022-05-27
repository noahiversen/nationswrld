package net.minenations.nationswrld;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import com.nftworlds.wallet.NFTWorlds;
import com.nftworlds.wallet.objects.TransactionObjects;

public class MaticCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(player.hasPermission("nationswrld.admin")) {
				if(NationsWRLD.getWalletAPI().getNFTPlayer(player).isLinked()) {
					player.sendMessage(ChatColor.GOLD + "Queueing $MATIC payment. Please wait...");
					new BukkitRunnable() {
						@Override
						public void run() {
							HttpRequest request = HttpRequest.newBuilder()
				                    .uri(URI.create("https://players-api.nftworlds.com/wallets/query?accountId=" + player.getUniqueId().toString().replaceAll("-", ""))).build();
					        try {
					        	HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
					        	if(httpResponse.statusCode() == 400) {
						            Transfer transfer = new Transfer(NFTWorlds.getInstance().getPolygonRPC().getPolygonWeb3j(), TransactionObjects.polygonTransactionManager);
						            transfer.sendFunds(NationsWRLD.getWalletAPI().getNFTPlayer(player).getPrimaryWallet().getAddress(), BigDecimal.valueOf(Double.parseDouble("0.1")), Convert.Unit.ETHER, BigInteger.valueOf(90_100_000_000L), BigInteger.valueOf(9_000_000)).sendAsync().thenAccept((c)-> {
						            	new BukkitRunnable() {
						            		@Override
						            		public void run() {
						            			if(player.isOnline()) {
									                player.sendMessage(
									                        ChatColor.translateAlternateColorCodes('&',
									                                "&6You've been paid! &7Reason&f: " + "Matic Drip" + "\n" +
									                                        "&a&nhttps://polygonscan.com/tx/" +
									                                        c.getTransactionHash() + "&r\n "));
						            			}
						            		}
						            	}.runTask(NationsWRLD.getInstance());
						            });
					        	}
					        	else {
						        	JSONObject response = new JSONObject(httpResponse.body());
						        	NationsWRLD.getInstance().getLogger().info(response.toString());
						        	if(response.has("managedWalletAddress") && response.get("managedWalletAddress").equals(NationsWRLD.getWalletAPI().getNFTPlayer(player).getPrimaryWallet().getAddress())) {
						        		new BukkitRunnable() {
						        			@Override
						        			public void run() {
						        				if(player.isOnline()) {
							        				player.sendMessage(ChatColor.RED + "$MATIC cannot be sent to a managed wallet.");
						        				}
						        			}
						        		}.runTask(NationsWRLD.getInstance());
						        	}
					        	}
					        } catch (Exception e) {
					            NFTWorlds.getInstance().getLogger().warning("caught error in maticPayment:");
					            e.printStackTrace();
					        }
						}
					}.runTaskAsynchronously(NationsWRLD.getInstance());
				}
				else {
					player.sendMessage(ChatColor.RED + "Your wallet must be linked.");
				}
			}
		}
		return false;
	}
}
