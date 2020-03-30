package Model.Entities;

import java.util.ArrayList;

public class Charger {
    private int chargerId;
    private int chargerPower;
    private String type;//OC or ON
    private String location;// W or E
    private ArrayList<Integer[]> occupyTimeList;

    public Charger(int chargerId) {
        this.chargerId = chargerId;
        occupyTimeList = new ArrayList<>();
    }

    public void addOccupyTime(Integer[] occupyTime){
        occupyTimeList.add(occupyTime);
    }

    public int getChargerId() {
        return chargerId;
    }

    public void setChargerId(int chargerId) {
        this.chargerId = chargerId;
    }

    public int getChargerPower() {
        return chargerPower;
    }

    public void setChargerPower(int chargerPower) {
        this.chargerPower = chargerPower;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<Integer[]> getOccupyTimeList() {
        return occupyTimeList;
    }

    public void setOccupyTimeList(ArrayList<Integer[]> occupyTimeList) {
        this.occupyTimeList = occupyTimeList;
    }
}
