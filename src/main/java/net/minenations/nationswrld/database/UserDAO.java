package net.minenations.nationswrld.database;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

public class UserDAO extends BasicDAO<GlobalUser, String> {

	public UserDAO(Class<GlobalUser> entityClass, Datastore ds) {
		super(entityClass, ds);
	}
}
