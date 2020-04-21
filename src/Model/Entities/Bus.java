package Model.Entities;

/**
 *
 * @author  Xiayan Zhong
 * This class is entity of bus.
 *
 */

public class Bus {
    private String busId;
    private String curFinshTrip;
    private int curState;
    private String curLocation;//where is the bus after last operation
    private int curTime;//what time is it after last operation

    public Bus(String busId) {
        this.busId = busId;
    }


    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

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
