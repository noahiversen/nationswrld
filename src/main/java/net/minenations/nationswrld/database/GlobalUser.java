package net.minenations.nationswrld.database;

import java.util.List;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;

public class GlobalUser {
	
	@Id
	public String id;
	
	@Indexed(options = @IndexOptions(unique = true))
	public String uuid;

	@Property("sent_matic")
	private boolean sentMatic;
	
	@Property("wrld_owed")
	private double wrldOwed;
	
	@Property("can_claim")
	private boolean canClaim;
	
	@Property("address")
	private String address;
	
	@Property("timestamp")
	private long timeStamp;
	
	@Property("rented-nfts")
	private List<String> rentedNFTs;
	
	@Property("rented-out-nfts")
	private List<String> rentedOutNFTS;
	
	public GlobalUser() {
		
	}
	
	public void setWRLDOwed(double wrldOwed) {
		this.wrldOwed = wrldOwed;
	}
	
	public double getWRLDOwed() {
		return wrldOwed;
	}

	public void setID(String id) {
		this.id = id;
	}
	
	public void setSentMatic(boolean sentMatic) {
		this.sentMatic = sentMatic;
	}
	
	public boolean didSendMatic() {
		return sentMatic;
	}
	
	public void setCanClaim(boolean canClaim) {
		this.canClaim = canClaim;
	}
	
	public boolean canClaim() {
		return canClaim;
	}
	
	public void setUUID(String uuid) {
		this.uuid = uuid;
	}
	
	public String getID() {
		return id;
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public void setRentedOutNFTs(List<String> rentedOutNFTS) {
		this.rentedOutNFTS = rentedOutNFTS;
	}
	
	public List<String> getRentedOutNFTs() {
		return rentedOutNFTS;
	}
	
	public void setRentedNFTs(List<String> rentedNFTs) {
		this.rentedNFTs = rentedNFTs;
	}
	
	public List<String> getRentedNFTs() {
		return rentedNFTs;
	}
}
