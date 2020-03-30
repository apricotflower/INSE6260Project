package Model.Entities;

public class ScheduleLine {
    private String busId;
    private String batterySizeSchCol;
    private String tripComp;
    private String atSoc;
    private String chargerId;
    private String btcStartTime;
    private String btcEndTime;
    private String btSoc;
    private String tripId;
    private String taStartTime;
    private String taEndTime;

    public ScheduleLine() {
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getBatterySizeSchCol() {
        return batterySizeSchCol;
    }

    public void setBatterySizeSchCol(String batterySizeSchCol) {
        this.batterySizeSchCol = batterySizeSchCol;
    }

    public String getTripComp() {
        return tripComp;
    }

    public void setTripComp(String tripComp) {
        this.tripComp = tripComp;
    }

    public String getAtSoc() {
        return atSoc;
    }

    public void setAtSoc(String atSoc) {
        this.atSoc = atSoc;
    }

    public String getChargerId() {
        return chargerId;
    }

    public void setChargerId(String chargerId) {
        this.chargerId = chargerId;
    }

    public String getBtcStartTime() {
        return btcStartTime;
    }

    public void setBtcStartTime(String btcStartTime) {
        this.btcStartTime = btcStartTime;
    }

    public String getBtcEndTime() {
        return btcEndTime;
    }

    public void setBtcEndTime(String btcEndTime) {
        this.btcEndTime = btcEndTime;
    }

    public String getBtSoc() {
        return btSoc;
    }

    public void setBtSoc(String btSoc) {
        this.btSoc = btSoc;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getTaStartTime() {
        return taStartTime;
    }

    public void setTaStartTime(String taStartTime) {
        this.taStartTime = taStartTime;
    }

    public String getTaEndTime() {
        return taEndTime;
    }

    public void setTaEndTime(String taEndTime) {
        this.taEndTime = taEndTime;
    }

    @Override
    public String toString() {
        return "ScheduleLine{" +
                "busId='" + busId + '\'' +
                ", batterySizeSchCol='" + batterySizeSchCol + '\'' +
                ", tripComp='" + tripComp + '\'' +
                ", atSoc='" + atSoc + '\'' +
                ", chargerId='" + chargerId + '\'' +
                ", btcStartTime='" + btcStartTime + '\'' +
                ", btcEndTime='" + btcEndTime + '\'' +
                ", btSoc='" + btSoc + '\'' +
                ", tripId='" + tripId + '\'' +
                ", taStartTime='" + taStartTime + '\'' +
                ", taEndTime='" + taEndTime + '\'' +
                '}';
    }
}
