package Model;

import Model.Entities.Bus;
import Model.Entities.BusBatteryConfig;
import Model.Entities.ChargerModel;
import Model.Entities.ScheduleLine;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScheduleGenerator {


    public ConfigPlanGenerator configPlanGenerator;

    private int busNumber;
    private BusBatteryConfig busBatteryConfig;
    private ArrayList<ChargerModel> chargerModels;
    private int totalS = 40;
    private double busV = 40;
    private double rate = 1;
    private int batterySize;
    private int ocPower = 0;
    private int onPower = 0;

    private ArrayList<ScheduleLine> scheduleLines;
    private List<Float> MacdonaldSchedule;//min
    private List<Float> LionelSchedule;//min
    private ArrayList<Bus> busList;
    private Bus curBus;



    public ScheduleGenerator(ConfigPlanGenerator configPlanGenerator) {
        this.configPlanGenerator = configPlanGenerator;
        this.chargerModels = configPlanGenerator.getChargerModels();
        this.busBatteryConfig = configPlanGenerator.getBusBatteryConfig();
        this.busNumber = configPlanGenerator.getBusNumber();
        generateSchedule();
    }

    private void generateSchedule(){
        //Lionel : 5h22 start 1h20 end , 1h/turn, 60 turns/ per day
        //battery size Q : 294/394 kwh (only one)
        //charger W: 450kw,50kw/ 300kw,100kw  (choose one pair only)
        //charge time t: t = Q/W (The Q here is the battery size left after driving, not the total size)
        //Total distance: 40km(assume)
        //Bus speed V:  40km/h(assume)
        //1 km/kwh(assume), can drive 294km/394km after full charge

        //Policy:
        //If battery lower than 50% (the rest can drive less than xx km), go to charge until it full （OC or ON）
        //If waiting time longer than 0.3 hour ,use slower charger(overnight), else use fast charger (opportunity)
        //If not caught new time , add one bus
        //If no free charger when a bus need charge, add one charger

        scheduleLines = new ArrayList<>();
        MacdonaldSchedule = readBusSchedule("Bus_schedule/Macdonald.txt");//E
        LionelSchedule = readBusSchedule("Bus_schedule/Lionel-Groulx.txt");//W
//        MacdonaldSchedule.forEach(line-> System.out.println(line[0]+":"+line[1]));
//        LionelSchedule.forEach(line-> System.out.println(line[0]+":"+line[1]));
        busList = new ArrayList<>();
        float lastBusMacdonald = MacdonaldSchedule.get(MacdonaldSchedule.size()-1);
        float lastBusLionel = LionelSchedule.get(LionelSchedule.size()-1);


        //first Line
        int busId = 1;
        batterySize = busBatteryConfig.getBatterySize();
        curBus = new Bus(String.valueOf(busId));

        String firstTripComp = "";
        int firstAtSoc = 50;

        int firstChargerIdSeq = 1;
        String firstChargerLoc = "M";
        String firstChargerType = "";
        int firstChargerPower = 100000;
        for (ChargerModel chargerModel: chargerModels) {
            if (chargerModel.getType().equals("OC")){
                ocPower = chargerModel.getChargerPower();
            }else if (chargerModel.getType().equals("ON")){
                onPower = chargerModel.getChargerPower();
            }

            if (chargerModel.getChargerPower() < firstChargerPower){
                firstChargerPower = chargerModel.getChargerPower();
                firstChargerType = chargerModel.getType();
            }
        }
        String firstChargerId = firstChargerLoc +"-"+ firstChargerType +"-"+ firstChargerPower +"-"+ firstChargerIdSeq;

        float firstBtcEndTime = 4;
        float firstChargingTime = (float)batterySize/(float) firstChargerPower;//hour
        float firstBtcStartTime = 4-firstChargingTime;
        curBus.addChargeTime(new String[]{"E",timeTranslateToString(firstBtcStartTime,":"),timeTranslateToString(firstBtcEndTime,":")});

        int firstBtSoc = (int)(rate * batterySize);

        String firstTripId = "E" + timeTranslateToString(MacdonaldSchedule.get(0),"-");
        String firstTripStartTime = timeTranslateToString(MacdonaldSchedule.get(0),"h");
        String firstTripEndTime = timeTranslateToString(MacdonaldSchedule.get(0)+1,"h");
        curBus.addAssignTrip(new String[]{"E",firstTripStartTime,firstTripEndTime});
        curBus.setCurFinshTrip(firstTripId);

        //the state after assign trip
        curBus.setCurLocation("W");
        curBus.setCurTime(MacdonaldSchedule.get(0)+1);
        curBus.setCurState(firstBtSoc - totalS);
        
        int index = 0;
        MacdonaldSchedule.remove(index);

        //put all attribute into one line in the schedule
        ScheduleLine firstScheduleLine = new ScheduleLine();
        firstScheduleLine.setBusId(String.valueOf(busId));
        firstScheduleLine.setTripComp(firstTripComp);
        firstScheduleLine.setAtSoc(String.valueOf(firstAtSoc));
        firstScheduleLine.setChargerId(firstChargerId);
        firstScheduleLine.setBtcStartTime(timeTranslateToString(firstBtcStartTime,"h"));
        firstScheduleLine.setBtcEndTime(timeTranslateToString(firstBtcEndTime,"h"));
        firstScheduleLine.setBtSoc(String.valueOf(firstBtSoc));
        firstScheduleLine.setBatterySizeSchCol(String.valueOf(batterySize));
        firstScheduleLine.setTripId(firstTripId);
        firstScheduleLine.setTaStartTime(firstTripStartTime);
        firstScheduleLine.setTaEndTime(firstTripEndTime);

        //add bus and schedule
        busList.add(curBus);
        scheduleLines.add(firstScheduleLine);

        while (!MacdonaldSchedule.isEmpty() || !LionelSchedule.isEmpty()){
            //if ocPower != 0 and onPower != 0
            if(curBus.getCurLocation().equals("W")){
                System.out.println("Now in location W Lionel");

                if (LionelSchedule.isEmpty() || curBus.getCurTime() > LionelSchedule.get(LionelSchedule.size()-1)){
                    addNewBus();
                    continue;
                }
                createPerBusSchedule("W");

            }else{
                System.out.println("Now in location E Macdonald");
                if (MacdonaldSchedule.isEmpty() || curBus.getCurTime() > MacdonaldSchedule.get(MacdonaldSchedule.size()-1)){
                    addNewBus();
                    continue;
                }
                createPerBusSchedule("E");

            }
        }


        this.busNumber = busList.size();


        int c = 0;

        for(ChargerModel chargerModel:chargerModels){
            chargerModel.setLionelGroulxNumber(String.valueOf(33+c));
            chargerModel.setMacDonaldNumber(String.valueOf(44+c));
            c++;
        }
        busBatteryConfig.setNumber(String.valueOf(this.busNumber));

    }

    //----------------------------------------Read bus schedule File-----------------------------------------------------

    private List<Float> readBusSchedule(String fileName){
        List<Float> busSchedule = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            busSchedule = stream.map(line-> timeTranslateToFloat(line,":")).collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Read file Error!");
        }
        return busSchedule;
    }

    //----------------------------------------------create and update----------------------------------------------------------

    private void addNewBus(){
        int busId = Integer.parseInt(curBus.getBusId());
        busId++;
        curBus = new Bus(String.valueOf(busId));
        curBus.setCurState(batterySize);
        System.out.println("Add new bus" + busId);
        if (MacdonaldSchedule.isEmpty()){
            updateCurBus(LionelSchedule.get(0),"","","","E");
            LionelSchedule.remove(0);
        }else if (LionelSchedule.isEmpty()){
            updateCurBus(MacdonaldSchedule.get(0),"","","","W");
            MacdonaldSchedule.remove(0);
        } else {
            if (MacdonaldSchedule.get(0)<= LionelSchedule.get(0)){//choose the earliest time in Mac
                updateCurBus(MacdonaldSchedule.get(0),"","","","W");
                MacdonaldSchedule.remove(0);
            }else{//choose the earliest time in Lio
                updateCurBus(LionelSchedule.get(0),"","","","E");
                LionelSchedule.remove(0);
            }
        }
        busList.add(curBus);
    }

    private void createPerBusSchedule(String location){
        String station;
        if(location.equals("W")){
            station = "LG";
        }else {
            station = "M";
        }
        System.out.println("Start next trip");
        if (curBus.getCurState() < 0.5 * batterySize){//need to charge
            System.out.println("Need to charge");
            String chargerId = "";
            float assignTripStartTime = 0;
            float btcStartTime = 0;
            float btcEndTime = 0;
            for (float time: LionelSchedule) {
                if (curBus.getCurTime() < time){
                    float ocChargeTime = (float)(batterySize - (curBus.getCurState()/rate))/(float)ocPower;
                    float onChargeTime = (float)(batterySize - (curBus.getCurState()/rate))/(float)onPower;
                    btcStartTime = curBus.getCurTime();
                    if (time - curBus.getCurTime() <= 0.3 && time > curBus.getCurTime() + ocChargeTime){//use OC charger
                        System.out.println("Use OC charger");
                        chargerId = station + "-OC-" + ocPower + "-";//need to add number
                        btcEndTime = btcStartTime + ocChargeTime;
                    }else {//use ON charger
                        System.out.println("Use ON charger");
                        chargerId = station + "-ON-" + onPower + "-";//need to add number
                        btcEndTime = btcStartTime + onChargeTime;
                        if (btcEndTime > time){
                            continue;
                        }
                    }
                    assignTripStartTime = time;
                    LionelSchedule.remove(time);
                    System.out.println("Now assign time" + time);
                    break;
                }
            }
            curBus.setCurState(batterySize);
            updateCurBus(assignTripStartTime,chargerId,timeTranslateToString(btcStartTime,"h"),timeTranslateToString(btcEndTime,"h"),location);

        }else {//not need to charge
            System.out.println("No need to charge");
            float assignTripStartTime = 0;
            for (float time: LionelSchedule) {
                if (curBus.getCurTime() < time){
                    assignTripStartTime = time;
                    LionelSchedule.remove(time);
                    System.out.println("Now assign time" + time);
                    break;
                }
            }

            updateCurBus(assignTripStartTime,"","","",location);

        }
    }

    private void updateCurBus(float assignTripStartTime,String chargerId, String btcStartTime, String btcEndTime, String location){
        System.out.println("update the bus current state");
        String tripID = location + timeTranslateToString(assignTripStartTime,"-");
        createNewScheduleLine(String.valueOf(curBus.getBusId()),String.valueOf(batterySize),curBus.getCurFinshTrip(),String.valueOf(curBus.getCurState()),chargerId,btcStartTime,btcEndTime,String.valueOf(curBus.getCurState()),tripID,timeTranslateToString(assignTripStartTime,"h"),timeTranslateToString(assignTripStartTime+1,"h"));
        curBus.setCurState(curBus.getCurState()- totalS);
        if (location.equals("W")){
            curBus.setCurLocation("E");
        }else {
            curBus.setCurLocation("W");
        }
        curBus.setCurTime(assignTripStartTime+1);
        curBus.setCurFinshTrip(tripID);
        curBus.addAssignTrip(new String[]{location,timeTranslateToString(assignTripStartTime,"h"),timeTranslateToString(assignTripStartTime+1,"h")});
        if (!btcStartTime.equals("") && !btcEndTime.equals("")){
            curBus.addChargeTime(new String[]{location,btcStartTime,btcEndTime});
        }
        System.out.println(curBus.toString());
    }

    private void createNewScheduleLine(String busId, String batterySizeSchCol, String tripComp, String atSoc, String chargerId, String btcStartTime,String btcEndTime, String btSoc, String tripId, String taStartTime,String taEndTime){
        System.out.println("Create new line in schedule");
        ScheduleLine scheduleLine = new ScheduleLine();
        scheduleLine.setBusId(busId);
        scheduleLine.setTripComp(tripComp);
        scheduleLine.setAtSoc(atSoc);
        scheduleLine.setChargerId(chargerId);
        scheduleLine.setBtcStartTime(btcStartTime);
        scheduleLine.setBtcEndTime(btcEndTime);
        scheduleLine.setBtSoc(btSoc);
        scheduleLine.setBatterySizeSchCol(batterySizeSchCol);
        scheduleLine.setTripId(tripId);
        scheduleLine.setTaStartTime(taStartTime);
        scheduleLine.setTaEndTime(taEndTime);
        scheduleLines.add(scheduleLine);
        System.out.println(scheduleLine.toString());
    }

    //----------------------------------------time translate tools-----------------------------------------------------

    private String timeTranslateToString(float time ,String separator){
        String e = String.valueOf(time);
        float s = Float.valueOf(e);
        int num1 = (int) s;
        BigDecimal b1 = new BigDecimal(e);
        BigDecimal b2 = new BigDecimal(num1);
        float num2 = b1.subtract(b2).floatValue();
        if (num1<0){
            num1 = 12+num1;
        }
        if (num1>=24){
            num1 = num1-24;
        }
        return num1 + separator + (int)Math.ceil(num2 * 60);
    }

    private float timeTranslateToFloat(String time, String separator){
        float hour = Float.parseFloat(time.split(separator)[0]);
        if (time.split(separator)[0].equals("0")||time.split(separator)[0].equals("1") ){//should optimal
            hour = 24 + hour;
        }
        float min = (float)Float.parseFloat(time.split(separator)[1])/60;
        return hour + min;
    }


    //-----------------------------------------------get set------------------------------------------------------------------

    public ConfigPlanGenerator getConfigPlanGenerator() {
        return configPlanGenerator;
    }

    public void setConfigPlanGenerator(ConfigPlanGenerator configPlanGenerator) {
        this.configPlanGenerator = configPlanGenerator;
    }

    public int getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(int busNumber) {
        this.busNumber = busNumber;
    }

    public BusBatteryConfig getBusBatteryConfig() {
        return busBatteryConfig;
    }

    public void setBusBatteryConfig(BusBatteryConfig busBatteryConfig) {
        this.busBatteryConfig = busBatteryConfig;
    }

    public ArrayList<ChargerModel> getChargerModels() {
        return chargerModels;
    }

    public void setChargerModels(ArrayList<ChargerModel> chargerModels) {
        this.chargerModels = chargerModels;
    }

    public ArrayList<ScheduleLine> getScheduleLines() {
        return scheduleLines;
    }
}
