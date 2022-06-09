package net.minenations.nationswrld;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.web3j.utils.Convert;

import com.nftworlds.wallet.NFTWorlds;

import net.md_5.bungee.api.ChatColor;
import net.minenations.nationswrld.database.GlobalUser;
import net.minenations.nationswrld.database.HotWallet;

public class ClaimCommand implements CommandExecutor {

	private double wrldOwed = 0;
	
	private List<String> playersClaimed = new ArrayList<String>();

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(args.length == 0) {
				NationsWRLDAPI.getWRLDOwed(player.getUniqueId()).thenAccept(amount -> {		
					player.sendMessage(ChatColor.GREEN + "");
					player.sendMessage(ChatColor.GOLD + "Current $WRLD Balance: " + ChatColor.WHITE + NationsWRLD.getWalletAPI().getPrimaryWallet(player).getPolygonWRLDBalance());
					player.sendMessage(ChatColor.GOLD + "Claimable $WRLD: " + ChatColor.WHITE + amount);
					player.sendMessage(ChatColor.GOLD + "Current Claim Time: " + ChatColor.WHITE + getCooldownTimeLeft(NationsWRLD.getInstance().getClaimTime()));
					player.sendMessage(ChatColor.GOLD + "Type "+ ChatColor.WHITE + "/claim confirm" + ChatColor.GOLD + " to retrieve your $WRLD. You must have a balance of " + ChatColor.WHITE + "5+ $WRLD" + ChatColor.GOLD + " to claim.");
					player.sendMessage("");
				});
			}
			else if(args[0].equalsIgnoreCase("confirm")) {
				new BukkitRunnable() {
					@Override
					public void run() {
						if(NationsWRLD.getInstance().getDatabaseHandler().lockUser(player.getUniqueId()) > 0) {
							if(!playersClaimed.contains(player.getName())) {
								playersClaimed.add(player.getName());
								if(NationsWRLD.getInstance().isHotWalletGood()) {
									GlobalUser user = NationsWRLD.getInstance().getDatabaseHandler().getUser(player.getUniqueId());
									//if(user.canClaim()) {
										if(NationsWRLD.getWalletAPI().getNFTPlayer(player).isLinked()) {
											if(user.getWRLDOwed() >= 5) {
												try {
													NFTWorlds.getInstance().getWrld().getPolygonBalanceAsync("0xa9e851b5ecafb00098ed2602a450a60c67c5b45a").thenAcceptAsync(balance -> {
														if(balance.compareTo(BigDecimal.valueOf(user.getWRLDOwed()).toBigInteger()) > 0) {
															playersClaimed.remove(player.getName());
															user.setTimeStamp(System.currentTimeMillis());
															user.setAddress(NationsWRLD.getWalletAPI().getPrimaryWallet(player).getAddress());
															NationsWRLD.getInstance().getDatabaseHandler().saveUser(user);
												            JSONObject json = new JSONObject();
												            json.put("network", "Polygon");
												            json.put("token", "POLYGON_WRLD");
												            json.put("recipient_address", NationsWRLD.getWalletAPI().getNFTPlayer(player).getPrimaryWallet().getAddress());
												            json.put("amount", Convert.toWei(BigDecimal.valueOf(user.getWRLDOwed()), Convert.Unit.ETHER).toBigInteger());
												            String requestBody = json.toString();
												            HttpRequest request = HttpRequest.newBuilder()
												                    .uri(URI.create(NFTWorlds.getInstance().getNftConfig().getHotwalletHttpsEndpoint() + "/send_tokens"))
												                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
												                    .build();
												            try {
												                JSONObject response = new JSONObject(HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body());
												                NFTWorlds.getInstance().getLogger().info(response.toString());
												                new BukkitRunnable() {
												                	@Override
												                	public void run() {
												                		player.sendMessage(ChatColor.GOLD + "Your claim of " + ChatColor.WHITE + user.getWRLDOwed() + " $WRLD" + ChatColor.GOLD + " has been queued. Please wait...");
												                	}
												                }.runTask(NationsWRLD.getInstance());
												            } catch (IOException | InterruptedException e) {
												                e.printStackTrace();
												            }
															NationsWRLD.getInstance().getLogger().info(player.getName() + " queued " + user.getWRLDOwed() + " WRLD from claiming");
														}
														else {
															playersClaimed.remove(player.getName());
															sendSyncMessage(player, ChatColor.RED + "Unable to send transaction. Please try again later.");
															user.setCanClaim(true);
															NationsWRLD.getInstance().getDatabaseHandler().saveUser(user);
														}
													});
												} catch (Exception e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
											else {
												sendSyncMessage(player, ChatColor.RED + "Your balance must be 5+ WRLD to claim.");
											}
										}
										else {
											sendSyncMessage(player, ChatColor.RED + "Your wallet must be linked.");
										}
									//}
									//else {
									//	sendSyncMessage(player, ChatColor.RED + "Your current claim is already in progress.");
									//}
								}
								else {
									sendSyncMessage(player, ChatColor.RED + "Hot wallet is unavailable. Please try claiming later.");
								}
							}
							else {
								sendSyncMessage(player, ChatColor.RED + "Your current claim is already in progress.");
							}
						}
						else {
							sendSyncMessage(player, ChatColor.RED + "Your current claim is already in progress.");
						}
					}
				}.runTaskAsynchronously(NationsWRLD.getInstance());
			}
			else if(args[0].equalsIgnoreCase("setclaim") && args.length > 1 && player.hasPermission("nationswrld.admin")) {
				NationsWRLDAPI.setWRLDOwed(player.getUniqueId(), Double.valueOf(args[1]));
				player.sendMessage(ChatColor.GREEN + "Claimable WRLD set to " + args[1]);
			}
			else if(args[0].equalsIgnoreCase("stats") && player.hasPermission("nationswrld.admin")) {
				wrldOwed = 0;
				new BukkitRunnable() {
					@Override
					public void run() {
						HotWallet wallet = NationsWRLD.getInstance().getDatabaseHandler().getHotWallet();
						for(GlobalUser user : NationsWRLD.getInstance().getDatabaseHandler().getAllUsers()) {
							if(user.getWRLDOwed() > 0) {
								wrldOwed += user.getWRLDOwed();
							}
						}
						new BukkitRunnable() {
							@Override
							public void run() {
								player.sendMessage(ChatColor.GREEN + "Total $WRLD Owed: " + wrldOwed);
								player.sendMessage(ChatColor.GREEN + "Current $WRLD Sent: " + wallet.getCurrentWRLDSent());
								player.sendMessage(ChatColor.GREEN + "Current $WRLD Claimed: " + wallet.getCurrentWRLDClaimed());
								player.sendMessage(ChatColor.GREEN + "Total $WRLD Claimed: " + wallet.getTotalWRLDClaimed());
								
							}
						}.runTask(NationsWRLD.getInstance());
					}
				}.runTaskAsynchronously(NationsWRLD.getInstance());
			}
			else {
				NationsWRLDAPI.getWRLDOwed(player.getUniqueId()).thenAccept(amount -> {		
					player.sendMessage(ChatColor.GREEN + "");
					player.sendMessage(ChatColor.GOLD + "Current $WRLD Balance: " + ChatColor.WHITE + NationsWRLD.getWalletAPI().getPrimaryWallet(player).getPolygonWRLDBalance());
					player.sendMessage(ChatColor.GOLD + "Claimable $WRLD: " + ChatColor.WHITE + amount);
					player.sendMessage(ChatColor.GOLD + "Current Claim Time: " + ChatColor.WHITE + getCooldownTimeLeft(NationsWRLD.getInstance().getClaimTime()));
					player.sendMessage(ChatColor.GOLD + "Type "+ ChatColor.WHITE + "/claim confirm" + ChatColor.GOLD + " to retrieve your $WRLD. You must have a balance of " + ChatColor.WHITE + "5+ $WRLD" + ChatColor.GOLD + " to claim.");
					player.sendMessage("");
				});
			}
		}
		return false;
	}

	public String getCooldownTimeLeft(int timeLeftInSeconds) {
		String timeMessage = "";
		if (timeLeftInSeconds >= 60) {
			timeMessage += timeLeftInSeconds / 60 + " minute";
			if (timeLeftInSeconds / 60 > 1) {
				timeMessage += "s";
			}
		}
		if (timeLeftInSeconds % 60 > 0) {
			if (timeLeftInSeconds > 60)
				timeMessage += " ";
			timeMessage += timeLeftInSeconds % 60 + " second";
			if (timeLeftInSeconds % 60 > 1) {
				timeMessage += "s";
			}
		}
		return timeMessage;
	}
	
	private void sendSyncMessage(Player player, String message) {
		new BukkitRunnable() {
			@Override
			public void run() {
				player.sendMessage(message);
			}
		}.runTask(NationsWRLD.getInstance());
	}

}
