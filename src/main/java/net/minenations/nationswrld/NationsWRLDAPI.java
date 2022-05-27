package net.minenations.nationswrld;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.mongodb.MongoClient;

import net.md_5.bungee.api.ChatColor;
import net.minenations.nationswrld.database.GlobalUser;
import net.minenations.nationswrld.database.HotWallet;

public class NationsWRLDAPI {

	public static void addWRLDOwed(UUID uuid, double amount) {
		new BukkitRunnable() {
			@Override
			public void run() {
				GlobalUser user = NationsWRLD.getInstance().getDatabaseHandler().getUser(uuid);
				HotWallet wallet = NationsWRLD.getInstance().getDatabaseHandler().getHotWallet();
				user.setWRLDOwed(user.getWRLDOwed() + amount);
				wallet.setCurrentWRLDSent(wallet.getCurrentWRLDSent() + amount);
				NationsWRLD.getInstance().getDatabaseHandler().saveUser(user);
				NationsWRLD.getInstance().getDatabaseHandler().saveWallet(wallet);
				new BukkitRunnable() {
					@Override
					public void run() {
						if(Bukkit.getPlayer(uuid) != null) {
							Bukkit.getPlayer(uuid).sendMessage(ChatColor.GOLD + "You've been sent " + amount + " WRLD! " + ChatColor.GRAY + "Type /claim to acquire."); 
						}
					}
				}.runTask(NationsWRLD.getInstance());
			}
		}.runTaskAsynchronously(NationsWRLD.getInstance());
	}
	
	public static void setWRLDOwed(UUID uuid, double amount) {
		new BukkitRunnable() {
			@Override
			public void run() {
				GlobalUser user = NationsWRLD.getInstance().getDatabaseHandler().getUser(uuid);
				HotWallet wallet = NationsWRLD.getInstance().getDatabaseHandler().getHotWallet();
				user.setWRLDOwed(user.getWRLDOwed() + amount);
				wallet.setCurrentWRLDSent(wallet.getCurrentWRLDSent());
				NationsWRLD.getInstance().getDatabaseHandler().saveUser(user);
				NationsWRLD.getInstance().getDatabaseHandler().saveWallet(wallet);
			}
		}.runTaskAsynchronously(NationsWRLD.getInstance());
	}
	
	/*
	public static void rentOutNFT(UUID from, UUID to) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Wallet fromWallet = 
				List<Uint> stakes = NationsWRLD.getRentalContract().getStakes(null);
			}
		}.runTaskAsynchronously(NationsWRLD.getInstance());
	}
	*/
	
	public static CompletableFuture<List<String>> getRentedOutNFTs(UUID uuid) {
		return CompletableFuture.supplyAsync(() -> NationsWRLD.getInstance().getDatabaseHandler().getUser(uuid).getRentedOutNFTs());
	}
	public static CompletableFuture<Double> getWRLDOwed(UUID uuid) {
		return CompletableFuture.supplyAsync(() -> NationsWRLD.getInstance().getDatabaseHandler().getUser(uuid).getWRLDOwed());
	}
	
	public static MongoClient getMongoClient() {
		return NationsWRLD.getInstance().getDatabaseHandler().getMongoClient();
	}
}
