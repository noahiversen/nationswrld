package net.minenations.nationswrld.database;

import java.util.List;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

public class HotWallet {

	@Id
	private String id;
	
	@Property("wallet")
	private String wallet;
	
	@Property("wallet_good")
	private boolean walletGood;
	
	@Property("claim_time")
	private int claimTime;
	
	@Property("current_wrld_sent")
	private double currentWRLDSent;
	
	@Property("current_wrld_claimed")
	private double currentWRLDClaimed;
	
	@Property("total_wrld_claimed")
	private double totalWRLDClaimed;
	
	@Property("rented_nfts")
	private List<String> rentedNFTs;
	
	public boolean isWalletGood() {
		return walletGood;
	}
	
	public void setWalletGood(boolean walletGood) {
		this.walletGood = walletGood;
	}
	
	public void setClaimTime(int claimTime) {
		this.claimTime = claimTime;
	}
	
	public int getClaimTime() {
		return claimTime;
	}
	
	public void setID() {
		this.id = "wallet";
		this.wallet = "wallet";
	}
	
	
	public void setCurrentWRLDClaimed(double currentWRLDClaimed) {
		this.currentWRLDClaimed = currentWRLDClaimed;
	
	}
	
	public double getTotalWRLDClaimed() {
		return totalWRLDClaimed;
	}
	
	public void setTotalWRLDClaimed(double totalWRLDClaimed) {
		this.totalWRLDClaimed = totalWRLDClaimed;
	}
	
	public double getCurrentWRLDClaimed() {
		return currentWRLDClaimed;
	}
	
	public double getCurrentWRLDSent() {
		return currentWRLDSent;
	}
	
	public void setCurrentWRLDSent(double currentWRLDSent) {
		this.currentWRLDSent = currentWRLDSent;
	}
	
	public void setRentedNFTs(List<String> rentedNFTs) {
		this.rentedNFTs = rentedNFTs;
	}
	
	public List<String> getRentedNFTs() {
		return rentedNFTs;
	}
}
