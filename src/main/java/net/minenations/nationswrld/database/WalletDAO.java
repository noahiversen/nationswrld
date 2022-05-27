package net.minenations.nationswrld.database;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

public class WalletDAO extends BasicDAO<HotWallet, String> {

	public WalletDAO(Class<HotWallet> entityClass, Datastore ds) {
		super(entityClass, ds);
	}
}
