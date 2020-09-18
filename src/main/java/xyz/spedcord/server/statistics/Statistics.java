package xyz.spedcord.server.statistics;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class Statistics {

    private final int obligatoryId;
    private long totalJobs;
    private long totalRegistrations;
    private long totalCompanies;
    private double totalMoneyMade;

    @BsonCreator
    public Statistics(@BsonProperty("obligatoryId") int obligatoryId, @BsonProperty("totalJobs") long totalJobs, @BsonProperty("totalRegistrations") long totalRegistrations, @BsonProperty("totalCompanies") long totalCompanies, @BsonProperty("totalMoneyMade") double totalMoneyMade) {
        this.obligatoryId = obligatoryId;
        this.totalJobs = totalJobs;
        this.totalRegistrations = totalRegistrations;
        this.totalCompanies = totalCompanies;
        this.totalMoneyMade = totalMoneyMade;
    }

    public int getObligatoryId() {
        return obligatoryId;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public long getTotalRegistrations() {
        return totalRegistrations;
    }

    public void setTotalRegistrations(long totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }

    public long getTotalCompanies() {
        return totalCompanies;
    }

    public void setTotalCompanies(long totalCompanies) {
        this.totalCompanies = totalCompanies;
    }

    public double getTotalMoneyMade() {
        return totalMoneyMade;
    }

    public void setTotalMoneyMade(double totalMoneyMade) {
        this.totalMoneyMade = totalMoneyMade;
    }

}
