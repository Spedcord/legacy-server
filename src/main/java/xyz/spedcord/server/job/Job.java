package xyz.spedcord.server.job;

import java.util.List;

public class Job {

    private int id;
    private long startedAt;
    private long endedAt;
    private double cargoWeight;
    private double pay;
    private String fromCity;
    private String toCity;
    private String cargo;
    private String truck;
    private List<Location> positions; // Will be used to track the player's position to prevent cheating

    public Job(int id, long startedAt, long endedAt, double cargoWeight, double pay, String fromCity, String toCity, String cargo, String truck, List<Location> positions) {
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
    }

    public int getId() {
        return id;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getEndedAt() {
        return endedAt;
    }

    public double getCargoWeight() {
        return cargoWeight;
    }

    public double getPay() {
        return pay;
    }

    public String getFromCity() {
        return fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    public String getCargo() {
        return cargo;
    }

    public String getTruck() {
        return truck;
    }

    public List<Location> getPositions() {
        return positions;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public void setEndedAt(long endedAt) {
        this.endedAt = endedAt;
    }

    public void setPay(double pay) {
        this.pay = pay;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }
}
