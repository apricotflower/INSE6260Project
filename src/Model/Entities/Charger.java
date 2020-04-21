package Model.Entities;

import java.util.ArrayList;

/**
 *
 * @author  Xiayan Zhong
 * This class is entity of charger.
 *
 */

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

    @Override
    public String toString() {
        for(Integer[] time: occupyTimeList){
            System.out.println(timeTranslateToString(time[0],"h")+ " " + timeTranslateToString(time[1],"h"));
        }
        return "Charger{" +
                "chargerId=" + chargerId +
                ", chargerPower=" + chargerPower +
                ", type='" + type + '\'' +
                ", location='" + location + '\'' +
                '}';
    }

    private String timeTranslateToString(int min ,String separator){
        int hours = min / 60;
        int minutes = min % 60;

        if (hours < 0){
            hours = 12+hours;
        }
        if (hours>=24){
            hours = hours-24;
        }
        return hours + separator + minutes;
    }
}
