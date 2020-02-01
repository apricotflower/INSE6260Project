package Entities;

public class BusBatteryConfig {
    private String busType;
    private int batterySize;
    private int unitPrice;
    private String number;

    public BusBatteryConfig() {
    }

    public BusBatteryConfig(String busType, int batterySize, int unitPrice) {
        this.busType = busType;
        this.batterySize = batterySize;
        this.unitPrice = unitPrice;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public int getBatterySize() {
        return batterySize;
    }

    public void setBatterySize(int batterySize) {
        this.batterySize = batterySize;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
