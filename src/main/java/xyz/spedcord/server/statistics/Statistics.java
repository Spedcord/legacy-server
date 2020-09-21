package xyz.spedcord.server.statistics;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
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
        return this.obligatoryId;
    }

    public long getTotalJobs() {
        return this.totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public long getTotalRegistrations() {
        return this.totalRegistrations;
    }

    public void setTotalRegistrations(long totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }

    public long getTotalCompanies() {
        return this.totalCompanies;
    }

    public void setTotalCompanies(long totalCompanies) {
        this.totalCompanies = totalCompanies;
    }

    public double getTotalMoneyMade() {
        return this.totalMoneyMade;
    }

    public void setTotalMoneyMade(double totalMoneyMade) {
        this.totalMoneyMade = totalMoneyMade;
    }

}
