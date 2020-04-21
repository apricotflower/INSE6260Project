package Model.Entities;

/**
 *
 * @author  Xiayan Zhong
 * This class is entity of one line in configuration plan about bus and battery.
 *
 */

public class BusBatteryConfig {
    private String busType;
    private int batterySize;
    private double unitPrice;
    private String number;

    public BusBatteryConfig() {
    }

    public BusBatteryConfig(String busType, int batterySize, double unitPrice) {
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
