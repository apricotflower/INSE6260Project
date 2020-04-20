package Model.Entities;

import java.util.ArrayList;

public class Bus {
    private String busId;
//    private ArrayList<String[]> chargeTimeList;
//    private ArrayList<String[]> assignTripList;
    private String curFinshTrip;
    private int curState;
    private String curLocation;//where is the bus after last operation
    private int curTime;//what time is it after last operation

    public Bus(String busId) {
        this.busId = busId;
//        chargeTimeList = new ArrayList<>();
//        assignTripList = new ArrayList<>();
    }

//    public void addChargeTime(String[] chargeTime){//{location, startTime, endTime}
//        chargeTimeList.add(chargeTime);
//    }
//
//    public void addAssignTrip(String[] assignTrip){//{startLocation, startTime, endTime}
//        assignTripList.add(assignTrip);
//    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

//    public ArrayList<String[]> getChargeTimeList() {
//        return chargeTimeList;
//    }
//
//    public void setChargeTimeList(ArrayList<String[]> chargeTimeList) {
//        this.chargeTimeList = chargeTimeList;
//    }
//
//    public ArrayList<String[]> getAssignTripList() {
//        return assignTripList;
//    }
//
//    public void setAssignTripList(ArrayList<String[]> assignTripList) {
//        this.assignTripList = assignTripList;
//    }

    public String getCurLocation() {
        return curLocation;
    }

    public void setCurLocation(String curLocation) {
        this.curLocation = curLocation;
    }

    public int getCurTime() {
        return curTime;
    }

    public void setCurTime(int curTime) {
        this.curTime = curTime;
    }

    public int getCurState() {
        return curState;
    }

    public void setCurState(int curState) {
        this.curState = curState;
    }

    public String getCurFinshTrip() {
        return curFinshTrip;
    }

    public void setCurFinshTrip(String curFinshTrip) {
        this.curFinshTrip = curFinshTrip;
    }

    @Override
    public String toString() {
        return "Bus{" +
                "busId='" + busId + '\'' +
                ", curFinshTrip='" + curFinshTrip + '\'' +
                ", curState=" + curState +
                ", curLocation='" + curLocation + '\'' +
                ", curTime=" + curTime +
                '}';
    }
}
