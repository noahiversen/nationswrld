package net.minenations.nationswrld;

import org.bukkit.plugin.java.JavaPlugin;

import com.nftworlds.wallet.api.WalletAPI;

public class NationsWRLD extends JavaPlugin {

	private static WalletAPI walletAPI;
	private static NationsWRLD instance;
	
	@Override
	public void onEnable() {
		instance = this;
		walletAPI = new WalletAPI();
		getCommand("paywrld").setExecutor(new PayWrldCommand());
		getCommand("wallet").setExecutor(new WalletCommand());
		getCommand("wrldbal").setExecutor(new WrldBalCommand());
		getCommand("serverpaywrld").setExecutor(new ServerPayWrldCommand());
		getServer().getPluginManager().registerEvents(new PeerToPeerPayListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerTransactListener(), this);
	}
	
	@Override
	public void onDisable() {
		walletAPI = null;
		instance = null;
	}
	
	public static WalletAPI getWalletAPI() {
		return walletAPI;
	}
	
	public static NationsWRLD getInstance() {
		return instance;
	}
}
