package Model.Entities;

public class ChargerModel {
    private String chargerManufacture;
    private String chargerModel;
    private int chargerPower;
    private double chargerPrice;
    private String LionelGroulxNumber;
    private String MacDonaldNumber;
    private String type;

    public ChargerModel(String chargerManufacture, String chargerModel, int chargerPower, double chargerPrice) {
        this.chargerManufacture = chargerManufacture;
        this.chargerModel = chargerModel;
        this.chargerPower = chargerPower;
        this.chargerPrice = chargerPrice;
        if (chargerPower <= 100){
            this.type = "ON";
        }else {
            this.type = "OC";
        }
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

    public double getChargerPrice() {
        return chargerPrice;
    }

    public void setChargerPrice(double chargerPrice) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
