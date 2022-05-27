package net.minenations.nationswrld;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;

import com.nftworlds.wallet.NFTWorlds;
import com.nftworlds.wallet.api.WalletAPI;
import com.nftworlds.wallet.contracts.nftworlds.WRLD;
import com.nftworlds.wallet.contracts.wrappers.polygon.PolygonWRLDToken;
import com.nftworlds.wallet.objects.NFTPlayer;
import com.nftworlds.wallet.objects.Wallet;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minenations.nationswrld.database.DatabaseHandler;
import net.minenations.nationswrld.database.GlobalUser;
import net.minenations.nationswrld.database.HotWallet;

public class NationsWRLD extends JavaPlugin {

	private static WalletAPI walletAPI;
	private static NationsWRLD instance;
	private LuckPerms luckPermsAPI;
	private DatabaseHandler databaseHandler;
	
	private boolean hotWalletAttempted = false;
	
	private long claimTimeStamp = 0;
	
	private Web3j testNet;
	private static RentalContract rentalContract;
	
	@Override
	public void onEnable() {
		instance = this;
		walletAPI = new WalletAPI();
		luckPermsAPI = LuckPermsProvider.get();
		saveDefaultConfig();
		new BukkitRunnable() {
			@Override
			public void run() {
				databaseHandler = new DatabaseHandler();
				beginHotWalletTest();
				//if(getConfig().getBoolean("test-hot-wallet")) {
					HotWallet wallet = databaseHandler.getHotWallet();
					wallet.setCurrentWRLDSent(0);
					wallet.setCurrentWRLDClaimed(0);
					databaseHandler.saveWallet(wallet);
					/*for(GlobalUser user : databaseHandler.getAllUsers()) {
						if(!user.canClaim()) {
							user.setTimeStamp(0);
							user.setCanClaim(true);
							databaseHandler.saveUser(user);
						}
					}
					*/
				//}
			}
		}.runTaskAsynchronously(this);
		getCommand("paywrld").setExecutor(new PayWrldCommand());
		getCommand("wallet").setExecutor(new WalletCommand());
		getCommand("wrldbal").setExecutor(new WrldBalCommand());
		getCommand("serverpaywrld").setExecutor(new ServerPayWrldCommand());
		getCommand("matic").setExecutor(new MaticCommand());
		getCommand("claim").setExecutor(new ClaimCommand());
		getServer().getPluginManager().registerEvents(new PeerToPeerPayListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerTransactListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerLoginListener(), this);
		AsyncPlayerPreLoginEvent.getHandlerList().unregister(NFTWorlds.getInstance());
		PlayerLoginEvent.getHandlerList().unregister(NFTWorlds.getInstance());
		PlayerJoinEvent.getHandlerList().unregister(NFTWorlds.getInstance());
		startPolygonPaymentListener();
		
		testNet = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/9aa3d95b3bc440fa88ea12eaa4456161"));
		Credentials dummyCredentials;
		try {
			dummyCredentials = Credentials.create(Keys.createEcKeyPair());
			rentalContract = RentalContract.load("0x9B79c075cCb954b231241DCeC25499Cd65d01Edd", testNet, dummyCredentials, new DefaultGasProvider());
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		databaseHandler.closeClient();
		walletAPI = null;
		instance = null;
	}
	
	public LuckPerms getLuckPermsAPI() {
		return luckPermsAPI;
	}
	
	public static WalletAPI getWalletAPI() {
		return walletAPI;
	}
	
	public static NationsWRLD getInstance() {
		return instance;
	}
	
	public DatabaseHandler getDatabaseHandler() {
		return databaseHandler;
	}
	
	private void beginHotWalletTest() {
		if(getConfig().getBoolean("test-hot-wallet")){
			new BukkitRunnable() {
				@Override
				public void run() {
					if(hotWalletAttempted) {
						HotWallet wallet = getDatabaseHandler().getHotWallet();
						wallet.setWalletGood(false);
						getDatabaseHandler().saveWallet(wallet);
                		for(GlobalUser gUser : getDatabaseHandler().getAllUsers()) {
                			if(!gUser.canClaim() && gUser.getTimeStamp() == 0) {
                				gUser.setTimeStamp(System.currentTimeMillis());
                				getDatabaseHandler().saveUser(gUser);
                			}
                		}
					}
					hotWalletAttempted = true;
					claimTimeStamp = System.currentTimeMillis();
		            JSONObject json = new JSONObject();
		            json.put("network", "Polygon");
		            json.put("token", "POLYGON_WRLD");
		            json.put("recipient_address", "0xBC57ccfFa22FB8c1643204e13851b3F8764B1351");
		            json.put("amount", Convert.toWei(BigDecimal.valueOf(0), Convert.Unit.ETHER).toBigInteger());
		            String requestBody = json.toString();
		            HttpRequest request = HttpRequest.newBuilder()
		                    .uri(URI.create(NFTWorlds.getInstance().getNftConfig().getHotwalletHttpsEndpoint() + "/send_tokens"))
		                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
		                    .build();
		            try {
		                JSONObject response = new JSONObject(HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body());
		                NFTWorlds.getInstance().getLogger().info(response.toString());
		            } catch (IOException | InterruptedException e) {
		                e.printStackTrace();
		            }
				}
			}.runTaskTimerAsynchronously(NationsWRLD.getInstance(), 20, 6000);
		}
	}
	
    private void startPolygonPaymentListener() {
        EthFilter transferFilter = new EthFilter(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST,
                NFTWorlds.getInstance().getWrld().getPolygonWRLDTokenContract().getContractAddress()
        ).addOptionalTopics(WRLD.TRANSFER_EVENT_TOPIC);

        NFTWorlds.getInstance().getPolygonRPC().getPolygonWeb3j().ethLogFlowable(transferFilter).subscribe(log -> {
                    String eventHash = log.getTopics().get(0);

                    if (eventHash.equals(WRLD.TRANSFER_EVENT_TOPIC)) {
                        this.paymentListener_handleTransferEvent(log);
                    }
                },
                error -> {
                    error.printStackTrace();
                });
    }
    
	@SuppressWarnings("rawtypes")
	private void paymentListener_handleTransferEvent(Log log) {
        List<String> topics = log.getTopics();
        List<Type> data = FunctionReturnDecoder.decode(log.getData(), PolygonWRLDToken.TRANSFER_EVENT.getNonIndexedParameters());
        TypeReference<Address> addressTypeReference = new TypeReference<Address>() {
        };
        
        Uint256 amount = (Uint256) data.get(0);
        double received = Convert.fromWei(amount.getValue().toString(), Convert.Unit.ETHER).doubleValue();
        Address fromAddress = (Address) FunctionReturnDecoder.decodeIndexedValue(topics.get(1), addressTypeReference);
        Address toAddress = (Address) FunctionReturnDecoder.decodeIndexedValue(topics.get(2), addressTypeReference);
        if(fromAddress.toString().equalsIgnoreCase("0xa9e851b5ecafb00098ed2602a450a60c67c5b45a")
        		&& toAddress.toString().equalsIgnoreCase("0xBC57ccfFa22FB8c1643204e13851b3F8764B1351") && getConfig().getBoolean("test-hot-wallet")) {
        	new BukkitRunnable() {
        		@Override
        		public void run() {
            		for(GlobalUser gUser : getDatabaseHandler().getAllUsers()) {
            			if(!gUser.canClaim() && gUser.getTimeStamp() > 0) {
            				if((System.currentTimeMillis() / 60000) - (gUser.getTimeStamp() / 60000) >= 60) {
            					gUser.setCanClaim(true);
            					gUser.setTimeStamp(0);
            					gUser.setAddress("");
            					getDatabaseHandler().saveUser(gUser);
            					getLogger().info(gUser.getAddress());
            				}
            			}
            		}
                	HotWallet wallet = getDatabaseHandler().getHotWallet();
                	wallet.setWalletGood(true);
                	hotWalletAttempted = false;
                	getLogger().info("Hot wallet test successful. Trying again in 5 minutes.");
                	wallet.setClaimTime((int) (System.currentTimeMillis() - claimTimeStamp) / 1000);
                	getDatabaseHandler().saveWallet(wallet);
        		}
        	}.runTaskAsynchronously(this);
        }
        else if(fromAddress.toString().equalsIgnoreCase("0xa9e851b5ecafb00098ed2602a450a60c67c5b45a")) {
        	new BukkitRunnable() {
        		@Override
        		public void run() {
                	boolean foundReceiver = false;
                	for (Entry<UUID, NFTPlayer> entry : NFTPlayer.getPlayers().entrySet()) {
                        NFTPlayer nftPlayer = entry.getValue();
                        for (Wallet wallet : nftPlayer.getWallets()) {

                            if (wallet.getAddress().equalsIgnoreCase(toAddress.toString())) {
                                foundReceiver = true;
                                GlobalUser user = getDatabaseHandler().getUser(wallet.getAssociatedPlayer());
                                if(!user.canClaim()) {
                                	HotWallet hotWallet = getDatabaseHandler().getHotWallet();
                                    user.setCanClaim(true);
                                    user.setTimeStamp(0);
                                    user.setAddress("");
                                    hotWallet.setCurrentWRLDClaimed(hotWallet.getCurrentWRLDClaimed() + user.getWRLDOwed());
                                    hotWallet.setTotalWRLDClaimed(hotWallet.getCurrentWRLDClaimed() + user.getWRLDOwed());
                                    user.setWRLDOwed(user.getWRLDOwed() - received);
                                    getDatabaseHandler().saveUser(user);
                                    getDatabaseHandler().saveWallet(hotWallet);
                                    
                                    new BukkitRunnable() {
                                    	@Override
                                    	public void run() {
                                            if(Bukkit.getPlayer(wallet.getAssociatedPlayer()) != null) {
                                                getLogger().info(Bukkit.getPlayer(wallet.getAssociatedPlayer()) + " has received their queued WRLD");
                                                Bukkit.getPlayer(wallet.getAssociatedPlayer()).sendMessage(ChatColor.GREEN + "You have successfully received queued WRLD!");
                                                Bukkit.getPlayer(wallet.getAssociatedPlayer()).sendMessage(ChatColor.GREEN + "Link: " + ChatColor.UNDERLINE + "https://polygonscan.com/tx/" + log.getTransactionHash());
                                            }
                                    	}
                                    }.runTask(NationsWRLD.getInstance());
                                }
                                break;
                            }
                        }
                        if(foundReceiver) {
                        	break;
                        }
                    }
                	if(!foundReceiver) {
                		long time = (long) (getClaimTime() > 0 ? getClaimTime() * 20 : 200);
                    	new BukkitRunnable() {
                    		@Override
                    		public void run() {
                				HotWallet hotWallet = getDatabaseHandler().getHotWallet();
                        		for(GlobalUser gUser : getDatabaseHandler().getAllUsers()) {
                        			if(gUser.getAddress().equalsIgnoreCase(toAddress.toString()) && !gUser.canClaim()) {
                                        gUser.setCanClaim(true);
                                        gUser.setTimeStamp(0);
                                        gUser.setAddress("");
                                        hotWallet.setCurrentWRLDClaimed(hotWallet.getCurrentWRLDClaimed() + gUser.getWRLDOwed());
                                        hotWallet.setTotalWRLDClaimed(hotWallet.getCurrentWRLDClaimed() + gUser.getWRLDOwed());
                                        gUser.setWRLDOwed(gUser.getWRLDOwed() - received);
                                        getDatabaseHandler().saveUser(gUser);
                                        getDatabaseHandler().saveWallet(hotWallet);
                        				getLogger().info(gUser.getUUID() + " (OFFLINE) has received their queued WRLD");
                        				break;
                        			}
                        		}
                    		}
                    	}.runTaskLaterAsynchronously(NationsWRLD.getInstance(), time);
                	}
        		}
        	}.runTaskAsynchronously(this);
        }
    }
	
	public double getCurrentWRLDSent() {
		return getDatabaseHandler().getHotWallet().getCurrentWRLDSent();
	}
    
    public boolean isHotWalletGood() {
    	return getDatabaseHandler().getHotWallet().isWalletGood();
    }
    
    public int getClaimTime() {
    	return getDatabaseHandler().getHotWallet().getClaimTime();
    }
    
    public static RentalContract getRentalContract() {
    	return rentalContract;
    }
}
