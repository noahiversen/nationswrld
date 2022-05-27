package net.minenations.nationswrld.database;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.DefaultCreator;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import net.minenations.nationswrld.NationsWRLD;

public class DatabaseHandler {

    private MongoClient mc;
    private Morphia morphia;
    private Datastore datastore;
    private UserDAO userDAO;
    private WalletDAO walletDAO;

    public DatabaseHandler() {
    	FileConfiguration config = NationsWRLD.getInstance().getConfig();
    	if(config.contains("username") && config.contains("hostname")
    			&& config.contains("password") && config.contains("port")) {
            ServerAddress addr = new ServerAddress(config.getString("hostname"), config.getInt("port"));
            List<MongoCredential> credentials = new ArrayList<>();
            credentials.add(MongoCredential.createCredential(config.getString("username"), "users", config.getString("password").toCharArray()));
       
            mc = new MongoClient(addr, credentials);
    	}
    	else {
            mc = new MongoClient();	
    	}
        morphia = new Morphia();
        morphia.getMapper().getOptions().setObjectFactory(new DefaultCreator() {
            @Override
            protected ClassLoader getClassLoaderForClass() {
                return NationsWRLD.class.getClassLoader();
            }
        });
        morphia.map(GlobalUser.class);
        morphia.map(HotWallet.class);
        
        datastore = morphia.createDatastore(mc, "users");
        datastore.ensureIndexes();
        
        userDAO = new UserDAO(GlobalUser.class, datastore);
        walletDAO = new WalletDAO(HotWallet.class, datastore);
    }
    
    public GlobalUser getUser(UUID uuid) {
    	GlobalUser du = userDAO.findOne("uuid", uuid.toString());
    	if(du == null) {
    		du = new GlobalUser();
    		du.setRentedNFTs(new ArrayList<String>());
    		du.setRentedOutNFTs(new ArrayList<String>());
    		du.setID(uuid.toString());
    		du.setUUID(uuid.toString());
    		du.setSentMatic(false);
    		du.setCanClaim(true);
    		du.setWRLDOwed(0);
    		du.setTimeStamp(0);
    		du.setAddress("");
    		userDAO.save(du);
    	}
    	return du;
    }
    
    public HotWallet getHotWallet() {
    	HotWallet wallet = walletDAO.findOne("wallet", "wallet");
    	if(wallet == null) {
    		wallet = new HotWallet();
    		wallet.setID();
    		wallet.setWalletGood(true);
    		wallet.setClaimTime(0);
    		wallet.setCurrentWRLDClaimed(0);
    		wallet.setCurrentWRLDSent(0);
    		wallet.setTotalWRLDClaimed(0);
    		
    		walletDAO.save(wallet);
    	}
    	return wallet;
    }
    
    public void saveUser(GlobalUser user) {
    	userDAO.save(user);
    }
    
    public void saveWallet(HotWallet wallet) {
    	walletDAO.save(wallet);
    }
    
    public List<GlobalUser> getAllUsers() {
    	return userDAO.find().asList();
    }
    
    public void closeClient() {
    	mc.close();
    }
    
    public MongoClient getMongoClient() {
    	return mc;
    }
}
