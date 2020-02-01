package Entities;

public class ChargerModel {
    private String chargerManufacture;
    private String chargerModel;
    private int chargerPower;
    private int chargerPrice;
    private String LionelGroulxNumber;
    private String MacDonaldNumber;

    public ChargerModel(String chargerManufacture, String chargerModel, int chargerPower, int chargerPrice) {
        this.chargerManufacture = chargerManufacture;
        this.chargerModel = chargerModel;
        this.chargerPower = chargerPower;
        this.chargerPrice = chargerPrice;
    }

    public String getChargerManufacture() {
        return chargerManufacture;
    }

    public void setChargerManufacture(String chargerManufacture) {
        this.chargerManufacture = chargerManufacture;
    }

    public String getChargerModel() {
        return chargerModel;
    }

    public void setChargerModel(String chargerModel) {
        this.chargerModel = chargerModel;
    }

    public int getChargerPower() {
        return chargerPower;
    }

    public void setChargerPower(int chargerPower) {
        this.chargerPower = chargerPower;
    }

    public int getChargerPrice() {
        return chargerPrice;
    }

    public void setChargerPrice(int chargerPrice) {
        this.chargerPrice = chargerPrice;
    }

    public String getLionelGroulxNumber() {
        return LionelGroulxNumber;
    }

    public void setLionelGroulxNumber(String lionelGroulxNumber) {
        LionelGroulxNumber = lionelGroulxNumber;
    }

    public String getMacDonaldNumber() {
        return MacDonaldNumber;
    }

    public void setMacDonaldNumber(String macDonaldNumber) {
        MacDonaldNumber = macDonaldNumber;
    }
}
