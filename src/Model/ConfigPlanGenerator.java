package Model;

import java.util.ArrayList;
import java.util.HashMap;

import Entities.ChargerModel;
import Entities.BusBatteryConfig;

public class ConfigPlanGenerator {
    private ArrayList<ChargerModel> chargerModels;
    private BusBatteryConfig busBatteryConfig;
    private float expenditure;
    private int busNumber;
//    private HashMap<String,Integer> chargerLionelGroulx;
//    private HashMap<String,Integer> chargerMacDonald;

    public ConfigPlanGenerator(ArrayList<ChargerModel> chargerModels, BusBatteryConfig busBatteryConfig) {
        this.chargerModels = chargerModels;
        this.busBatteryConfig = busBatteryConfig;
        generateConfigPlan();

    }

    private void generateConfigPlan(){
        this.busNumber = 100;
//        this.chargerLionelGroulx.put("OC 450kW",20);
//        this.chargerLionelGroulx.put("FAST DC 50kW", 30);
//        this.chargerMacDonald.put("OC 450kW",40);
//        this.chargerMacDonald.put("FAST DC 50kW", 70);

        int c = 0;
        int chargerPriceSum = 0;
        for(ChargerModel chargerModel:chargerModels){
            chargerPriceSum = chargerModel.getChargerPrice() + chargerPriceSum;
            chargerModel.setLionelGroulxNumber(String.valueOf(33+c));
            chargerModel.setMacDonaldNumber(String.valueOf(44+c));
            c++;
        }
        busBatteryConfig.setNumber(String.valueOf(this.busNumber));
        this.expenditure = (float)(chargerPriceSum + busBatteryConfig.getUnitPrice()*Integer.parseInt(busBatteryConfig.getNumber()));
    }

    public int getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(int busNumber) {
        this.busNumber = busNumber;
    }

//    public HashMap<String, Integer> getChargerLionelGroulx() {
//        return chargerLionelGroulx;
//    }
//
//    public void setChargerLionelGroulx(HashMap<String, Integer> chargerLionelGroulx) {
//        this.chargerLionelGroulx = chargerLionelGroulx;
//    }
//
//    public HashMap<String, Integer> getChargerMacDonald() {
//        return chargerMacDonald;
//    }
//
//    public void setChargerMacDonald(HashMap<String, Integer> chargerMacDonald) {
//        this.chargerMacDonald = chargerMacDonald;
//    }

    public float getExpenditure() {
        return expenditure;
    }


    public ArrayList<ChargerModel> getChargerModels() {
        return chargerModels;
    }

    public void setChargerModels(ArrayList<ChargerModel> chargerModels) {
        this.chargerModels = chargerModels;
    }

    public BusBatteryConfig getBusBatteryConfig() {
        return busBatteryConfig;
    }

    public void setBusBatteryConfig(BusBatteryConfig busBatteryConfig) {
        this.busBatteryConfig = busBatteryConfig;
    }
}
