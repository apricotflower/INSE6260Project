package Model;

import Model.Entities.*;

import java.io.IOException;
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
    private int rate = 1;
    private int batterySize;
    private int ocPower = 0;
    private int onPower = 0;

    private ArrayList<ScheduleLine> scheduleLines;
    private List<Integer> MacdonaldSchedule;//min
    private List<Integer> LionelSchedule;//min
    private ArrayList<Bus> busList;
    private List<Charger> MacChargerList;
    private List<Charger> LionelChargerList;
    private Bus curBus;




    public ScheduleGenerator(ConfigPlanGenerator configPlanGenerator) {
        this.configPlanGenerator = configPlanGenerator;
        this.chargerModels = configPlanGenerator.getChargerModels();
        this.busBatteryConfig = configPlanGenerator.getBusBatteryConfig();
        this.busNumber = configPlanGenerator.getBusNumber();
        generateSchedule();
    }

    private void generateSchedule(){
        //Lionel : 5h22 start 1h20 end , 1h/turn
        //battery size Q : 294/394 kwh (only one)
        //charger W: 450kw,50kw/ 300kw,100kw  (choose one pair only)
        //charge time t: t = Q/W (The Q here is the battery size left after driving, not the total size)
        //Total distance: 40km(assume)
        //1 km/kwh(assume), can drive 294km/394km after full charge

        //Policy:
        //If battery lower than 50% (the rest can drive less than xx km), go to charge until it full （OC or ON）
        //If waiting time longer than 0.3 hour ,use slower charger(overnight) to charge to full, else use fast charger (opportunity) to charge to one time
        //If not caught new time , add one bus
        //If no free charger when a bus need charge, add one charger

        //initialize
        scheduleLines = new ArrayList<>();
        MacdonaldSchedule = readBusSchedule("Bus_schedule/Macdonald.txt");//E
        LionelSchedule = readBusSchedule("Bus_schedule/Lionel-Groulx.txt");//W
        System.out.println("E schedule");
        MacdonaldSchedule.forEach(line-> System.out.println(timeTranslateToString(line,":")));
        System.out.println("W schedule");
        LionelSchedule.forEach(line-> System.out.println(timeTranslateToString(line,":")));
        busList = new ArrayList<>();
        MacChargerList = new ArrayList<>();
        LionelChargerList = new ArrayList<>();


        //first Line
        int busId = 1;
        batterySize = busBatteryConfig.getBatterySize();
        curBus = new Bus(String.valueOf(busId));

        String firstTripComp = "";
        int firstAtSoc = batterySize*rate;

        for (ChargerModel chargerModel: chargerModels) {
            if (chargerModel.getType().equals("OC")){
                ocPower = chargerModel.getChargerPower();
            }else if (chargerModel.getType().equals("ON")){
                onPower = chargerModel.getChargerPower();
            }
        }

        int firstBtSoc = (int)(rate * batterySize);

        String firstTripId = "E" + timeTranslateToString(MacdonaldSchedule.get(0),"-");
        String firstTripStartTime = timeTranslateToString(MacdonaldSchedule.get(0),"h");
        String firstTripEndTime = timeTranslateToString(MacdonaldSchedule.get(0)+60,"h");
        curBus.addAssignTrip(new String[]{"E",firstTripStartTime,firstTripEndTime});
        curBus.setCurFinshTrip(firstTripId);

        //the state after assign trip
        curBus.setCurLocation("W");
        curBus.setCurTime(MacdonaldSchedule.get(0)+60);
        curBus.setCurState(firstBtSoc - totalS);
        
        int index = 0;
        MacdonaldSchedule.remove(index);

        //put all attribute into one line in the schedule
        ScheduleLine firstScheduleLine = new ScheduleLine();
        firstScheduleLine.setBusId(String.valueOf(busId));
        firstScheduleLine.setTripComp(firstTripComp);
        firstScheduleLine.setAtSoc(String.valueOf(firstAtSoc));
        firstScheduleLine.setChargerId("");
        firstScheduleLine.setBtcStartTime("");
        firstScheduleLine.setBtcEndTime("");
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

        // get the bus number, charger number
        this.busNumber = busList.size();
        System.out.println("Generate lines number: " + scheduleLines.size());

        for(ChargerModel chargerModel:chargerModels){
            if(chargerModel.getType().equals("ON")){
                setChargerNum(chargerModel,"ON");
            }else if(chargerModel.getType().equals("OC")){
                setChargerNum(chargerModel,"OC");
            }
        }

        busBatteryConfig.setNumber(String.valueOf(this.busNumber));

    }

    //----------------------------------------Read bus schedule File-----------------------------------------------------

    private List<Integer> readBusSchedule(String fileName){
        List<Integer> busSchedule = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            busSchedule = stream.map(line-> timeTranslateToInt(line,":")).collect(Collectors.toList());
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
            updateCurBus(LionelSchedule.get(0),curBus.getCurState(),"","","","W");
            LionelSchedule.remove(0);
        }else if (LionelSchedule.isEmpty()){
            updateCurBus(MacdonaldSchedule.get(0),curBus.getCurState(),"","","","E");
            MacdonaldSchedule.remove(0);
        } else {
            if (MacdonaldSchedule.get(0)<= LionelSchedule.get(0)){//choose the earliest time in Mac
                updateCurBus(MacdonaldSchedule.get(0),curBus.getCurState(),"","","","E");
                MacdonaldSchedule.remove(0);
            }else{//choose the earliest time in Lio
                updateCurBus(LionelSchedule.get(0),curBus.getCurState(),"","","","W");
                LionelSchedule.remove(0);
            }
        }
        busList.add(curBus);
    }

    private String addNewCharger(int chargerNum,int power,String location,String type,String station,Integer btcStartTime, Integer btcEndTime, List<Charger> curLocChargerList){
        Charger charger = new Charger(curLocChargerList.size()+1);
        charger.setChargerPower(power);
        charger.setLocation(location);
        charger.setType(type);
        charger.addOccupyTime(new Integer[]{btcStartTime,btcEndTime});
        curLocChargerList.add(charger);
        return station + "-" + type + "-" + power + "-" + charger.getChargerId();

    }

    private String addChargerTime(List<Charger> curLocChargerList,String type, String location, String station, Integer btcStartTime, Integer btcEndTime){
        int power;
        if(type.equals("OC")){
            power = ocPower;
        }else {
            power = onPower;
        }

        if(curLocChargerList.isEmpty()){
            return addNewCharger(1,power,location,type,station,btcStartTime,btcEndTime,curLocChargerList);

        }else{
            for (Charger charger: curLocChargerList) {
                boolean timeIsOccupy = false;
                for (Integer[] chargeTime: charger.getOccupyTimeList()) {
                    if(btcStartTime >= chargeTime[0] && btcStartTime < chargeTime[1]){
                        timeIsOccupy = true;
                        break;
                    }
                    if(btcEndTime > chargeTime[0] && btcEndTime <= chargeTime[1]){
                        timeIsOccupy = true;
                        break;
                    }
                }
                if(!timeIsOccupy){
                    charger.addOccupyTime(new Integer[]{btcStartTime,btcEndTime});
                    return station + "-" + type + "-" + power + "-" + charger.getChargerId();
                }
            }

            return addNewCharger(curLocChargerList.size()+1,power,location,type,station,btcStartTime,btcEndTime,curLocChargerList);
        }
    }

    private void createPerBusSchedule(String location){
        String station;
        List<Integer> curLocSchedule;
        List<Charger> curLocChargerList;
        if(location.equals("W")){
            station = "LG";
            curLocSchedule = LionelSchedule;
            curLocChargerList = LionelChargerList;
        }else {
            station = "M";
            curLocSchedule = MacdonaldSchedule;
            curLocChargerList = MacChargerList;
        }
        System.out.println("Start next trip");
        if (curBus.getCurState() < 0.5 * batterySize){//need to charge
            System.out.println("Need to charge");
            String chargerId = "";
            int assignTripStartTime = 0;
            int btcStartTime = 0;
            int btcEndTime = 0;
            int atSoc = curBus.getCurState();
            for (Integer time: curLocSchedule) {

                if (curBus.getCurTime() < time){
                    int ocChargeTime = (int)(60*(float)((totalS/rate) - (curBus.getCurState()/rate))/(float)ocPower);
                    int onChargeTime = (int)(60*(float)(batterySize - (curBus.getCurState()/rate))/(float)onPower);
                    btcStartTime = curBus.getCurTime();
                    if (time - curBus.getCurTime() <= 20 && time > curBus.getCurTime() + ocChargeTime){//use OC charger
                        System.out.println("Use OC charger");
                        btcEndTime = btcStartTime + ocChargeTime;
                        chargerId = addChargerTime(curLocChargerList,"OC",location,station,btcStartTime,btcEndTime);
                        curBus.setCurState(totalS);

                    }else {//use ON charger
                        if (btcEndTime > time){
                            continue;
                        }
                        System.out.println("Use ON charger");
                        btcEndTime = btcStartTime + onChargeTime;
                        chargerId = addChargerTime(curLocChargerList,"ON",location,station,btcStartTime,btcEndTime);
                        curBus.setCurState(batterySize);
                    }
                    assignTripStartTime = time;
                    curLocSchedule.remove(time);
                    System.out.println("Now assign time" + time);
                    break;
                }

            }

            updateCurBus(assignTripStartTime,atSoc,chargerId,timeTranslateToString(btcStartTime,"h"),timeTranslateToString(btcEndTime,"h"),location);

        }else {//not need to charge
            System.out.println("No need to charge");
            int assignTripStartTime = 0;
            for (Integer time: curLocSchedule) {
                if (curBus.getCurTime() < time){
                    assignTripStartTime = time;
                    curLocSchedule.remove(time);
                    System.out.println("Now assign time" + time);
                    break;
                }
            }
            updateCurBus(assignTripStartTime,curBus.getCurState(),"","","",location);

        }
    }

    private void updateCurBus(int assignTripStartTime,int atSoc, String chargerId, String btcStartTime, String btcEndTime, String location){
        System.out.println("update the bus current state");
        String tripID = location + timeTranslateToString(assignTripStartTime,"-");
        createNewScheduleLine(String.valueOf(curBus.getBusId()),String.valueOf(batterySize),curBus.getCurFinshTrip(),String.valueOf(atSoc),chargerId,btcStartTime,btcEndTime,String.valueOf(curBus.getCurState()),tripID,timeTranslateToString(assignTripStartTime,"h"),timeTranslateToString(assignTripStartTime+60,"h"));
        curBus.setCurState(curBus.getCurState()- totalS);
        if (location.equals("W")){
            curBus.setCurLocation("E");
        }else {
            curBus.setCurLocation("W");
        }
        curBus.setCurTime(assignTripStartTime+60);
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

    public void setChargerNum(ChargerModel chargerModel, String type){
        List<Charger> onLionelList = LionelChargerList.stream().filter(charger->!charger.getType().equals(type)).collect(Collectors.toList());
        List<Charger> onMacList = MacChargerList.stream().filter(charger->!charger.getType().equals(type)).collect(Collectors.toList());
        chargerModel.setLionelGroulxNumber(String.valueOf(onLionelList.size()));
        chargerModel.setMacDonaldNumber(String.valueOf(onMacList.size()));
    }

    //----------------------------------------time translate tools-----------------------------------------------------

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


    private Integer timeTranslateToInt(String time, String separator){
        int min;
        if (time.split(separator)[0].equals("0")||time.split(separator)[0].equals("1") ){//should optimal
            min = Integer.parseInt(time.split(separator)[0])*60 + 24*60 + Integer.parseInt(time.split(separator)[1]);
        }else{
            min = Integer.parseInt(time.split(separator)[0])*60 + Integer.parseInt(time.split(separator)[1]);
        }
        return min;
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
