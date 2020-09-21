package xyz.spedcord.server.job;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.List;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class Job {

    private final double cargoWeight;
    private final String fromCity;
    private final String cargo;
    private final String truck;
    private final List<Location> positions; // Will be used to track the player's position to prevent cheating
    private int id;
    private long startedAt;
    private long endedAt;
    private double pay;
    private String toCity;
    private int verifyState;

    @BsonCreator
    public Job(@BsonId int id, @BsonProperty("startedAt") long startedAt, @BsonProperty("endedAt") long endedAt, @BsonProperty("cargoWeight") double cargoWeight, @BsonProperty("pay") double pay, @BsonProperty("fromCity") String fromCity, @BsonProperty("toCity") String toCity, @BsonProperty("cargo") String cargo, @BsonProperty("truck") String truck, @BsonProperty("positions") List<Location> positions, @BsonProperty("verifyState") int verifyState) {
        this.id = id;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.cargoWeight = cargoWeight;
        this.pay = pay;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.cargo = cargo;
        this.truck = truck;
        this.positions = positions;
        this.verifyState = verifyState;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getStartedAt() {
        return this.startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getEndedAt() {
        return this.endedAt;
    }

    public void setEndedAt(long endedAt) {
        this.endedAt = endedAt;
    }

    public double getCargoWeight() {
        return this.cargoWeight;
    }

    public double getPay() {
        return this.pay;
    }

    public void setPay(double pay) {
        this.pay = pay;
    }

    public String getFromCity() {
        return this.fromCity;
    }

    public String getToCity() {
        return this.toCity;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    public String getCargo() {
        return this.cargo;
    }

    public String getTruck() {
        return this.truck;
    }

    public List<Location> getPositions() {
        return this.positions;
    }

    public int getVerifyState() {
        return this.verifyState;
    }

    public void setVerifyState(int verifyState) {
        this.verifyState = verifyState;
    }

}
