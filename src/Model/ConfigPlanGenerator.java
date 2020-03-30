package Model;

import java.util.ArrayList;

import Model.Entities.ChargerModel;
import Model.Entities.BusBatteryConfig;

public class ConfigPlanGenerator {
    private ArrayList<ChargerModel> chargerModels;
    private BusBatteryConfig busBatteryConfig;
    private int busNumber;
    private ScheduleGenerator scheduleGenerator;
    private float expenditure;

    public ConfigPlanGenerator(ArrayList<ChargerModel> chargerModels, BusBatteryConfig busBatteryConfig) {
        this.chargerModels = chargerModels;
        this.busBatteryConfig = busBatteryConfig;
        generateConfigPlan();
    }

    private void generateConfigPlan(){
        scheduleGenerator = new ScheduleGenerator(this);
        this.chargerModels = scheduleGenerator.getChargerModels();
        this.busNumber = scheduleGenerator.getBusNumber();
        this.busBatteryConfig = scheduleGenerator.getBusBatteryConfig();

    }

    public int getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(int busNumber) {
        this.busNumber = busNumber;
    }

    public float getExpenditure() {
        int chargerPriceSum = 0;
        for (ChargerModel chargerModel:this.chargerModels){
            System.out.println(chargerModel.getChargerModel());
            int chargerNum = Integer.parseInt(chargerModel.getLionelGroulxNumber()) + Integer.parseInt(chargerModel.getMacDonaldNumber());
            System.out.println("Charger number: " + chargerNum);
            int chargerPrice = chargerModel.getChargerPrice() * chargerNum;
            System.out.println("charger price: " + chargerPrice);
            chargerPriceSum = chargerPriceSum + chargerPrice;
        }
        this.expenditure = (float)(chargerPriceSum + busBatteryConfig.getUnitPrice()*Integer.parseInt(busBatteryConfig.getNumber()));
        System.out.println(this.expenditure);
        return this.expenditure;
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

    public ScheduleGenerator getScheduleGenerator() {
        return scheduleGenerator;
    }

    public void setScheduleGenerator(ScheduleGenerator scheduleGenerator) {
        this.scheduleGenerator = scheduleGenerator;
    }
}
