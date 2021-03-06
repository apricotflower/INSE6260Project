package Model;

import Model.Entities.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import Model.Parameter.PARAMETER;

/**
 *
 * @author  Xiayan Zhong
 * This class is the algorithm to generate the schedule.
 *Parameter:
 * Lionel : 5h22 start 1h20 end , 1h/turn
 * Mac: 4h 55 start 0h20 end ,1h/turn
 * battery size Q : 294/394 kwh (only one)
 * charger W: 450kw,50kw/ 300kw,100kw  (choose one pair only)
 * charge time t: t = Q/W (The Q here is the battery size left after driving, not the total size)
 * Total distance: 40km(assume)
 * Speed: 40km/h(assume)
 * 1 km/kwh(assume), can drive 294km/394km after full charge
 *
 * Policy:
 * If battery lower than 50% (the rest can drive less than xx km), go to charge.（OC or ON）
 * If waiting time longer than 120 min ,use slower charger(overnight) to charge to full, else use fast charger (opportunity) to charge to one time tirp + 50% battery
 * If not caught new time , add one bus
 * If no free charger when a bus need charge, add one charger
 * If there is no bus trip time assign in the future time in current station and there is bus trip time in the other station after 60 min, add an empty bus trip to other station
 *
 */


public class ScheduleGenerator {

    private ConfigPlanGenerator configPlanGenerator;

    private int busNumber;
    private BusBatteryConfig busBatteryConfig;
    private ArrayList<ChargerModel> chargerModels;
    
    private final int totalS = PARAMETER.TOTAL_DISTANCE;
    private final int rate = PARAMETER.RATE;
    private final int policy_oc_on_condition = PARAMETER.POLICY_ON_OC_CONDITION;

    private int batterySize;
    private int ocPower = 0;
    private int onPower = 0;
    private double policy_min_state;

    private ArrayList<ScheduleLine> scheduleLines;
    private List<Integer> MacdonaldSchedule;//unit:min
    private List<Integer> LionelSchedule;//unit:min
    private ArrayList<Bus> busList;
    private List<Charger> MacChargerList;
    private List<Charger> LionelChargerList;

    private Bus curBus;
    private List<Integer> curLocSchedule;
    private List<Integer> otherLocSchedule;
    private List<Charger> curLocChargerList;
    private String station;

    private int emptyBusNum = 1;


    public ScheduleGenerator(ConfigPlanGenerator configPlanGenerator) {
        this.configPlanGenerator = configPlanGenerator;
        this.chargerModels = configPlanGenerator.getChargerModels();
        this.busBatteryConfig = configPlanGenerator.getBusBatteryConfig();
        this.busNumber = configPlanGenerator.getBusNumber();
        generateSchedule();
    }

    private void generateSchedule(){

        //initialize
        scheduleLines = new ArrayList<>();
        MacdonaldSchedule = readBusSchedule(PARAMETER.MACDONALD_SCHEDULE_FILE_PATH);//E
        LionelSchedule = readBusSchedule(PARAMETER.LIONEL_GROULX_SCHEDULE_FILE_PATH);//W
        busList = new ArrayList<>();
        MacChargerList = new ArrayList<>();
        LionelChargerList = new ArrayList<>();

        batterySize = busBatteryConfig.getBatterySize();
        policy_min_state = 0.5 * batterySize;

        for (ChargerModel chargerModel: chargerModels) {
            if (chargerModel.getType().equals(PARAMETER.OC_CHARGER)){
                ocPower = chargerModel.getChargerPower();
            }else if (chargerModel.getType().equals(PARAMETER.ON_CHARGER)){
                onPower = chargerModel.getChargerPower();
            }
        }

        //first Line
        addNewBus();

        //main loop
        while (!MacdonaldSchedule.isEmpty() || !LionelSchedule.isEmpty()){
            findCurLocList();
            if (addEmptyBusTrip()){
                if (curBus.getCurState() < policy_min_state) {
                    int atSoc = curBus.getCurState();
                    int btcStartTime = curBus.getCurTime();
                    String chargerId;
                    int ocChargeTime = chargeTimeCalculator(PARAMETER.OC_CHARGER);
                    int onChargeTime = chargeTimeCalculator(PARAMETER.ON_CHARGER);
                    if(ocPower == 0 && onPower != 0){
                        if(curBus.getCurTime() + onChargeTime + 60 > otherLocSchedule.get(otherLocSchedule.size()-1)){
                            addNewBus();
                            continue;
                        }
                        chargerId = chargerAssignerHelper(PARAMETER.ON_CHARGER);
                    }else if (ocPower != 0){
                        if(curBus.getCurTime() + ocChargeTime + 60 > otherLocSchedule.get(otherLocSchedule.size()-1)){
                            addNewBus();
                            continue;
                        }
                        chargerId = chargerAssignerHelper(PARAMETER.OC_CHARGER);
                    }else{
                        addNewBus();
                        continue;
                    }

                    updateCurBus(curBus.getCurTime(),atSoc,chargerId,timeTranslateToString(btcStartTime,"h"),timeTranslateToString(curBus.getCurTime(),"h"),curBus.getCurLocation(),true);

                }else{
                    updateCurBus(curBus.getCurTime(),curBus.getCurState(),"","","",curBus.getCurLocation(),true);
                }

            }else{
                if(curLocSchedule.isEmpty() || curBus.getCurTime() > curLocSchedule.get(curLocSchedule.size()-1)){
                    addNewBus();
                    continue;
                }
                chargerAssigner(curBus.getCurLocation());
            }
        }

        // get the bus number, charger number
        this.busNumber = busList.size();
        System.out.println("Generate lines number: " + scheduleLines.size());

        for(ChargerModel chargerModel:chargerModels){
            if(chargerModel.getType().equals(PARAMETER.ON_CHARGER)){
                setChargerNum(chargerModel,PARAMETER.ON_CHARGER);
            }else if(chargerModel.getType().equals(PARAMETER.OC_CHARGER)){
                setChargerNum(chargerModel,PARAMETER.OC_CHARGER);
            }
        }

        busBatteryConfig.setNumber(String.valueOf(this.busNumber));

    }

    //----------------------------------------Read bus schedule File-----------------------------------------------------

    public List<Integer> readBusSchedule(String fileName){
        List<Integer> busSchedule = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            busSchedule = stream.map(line-> timeTranslateToInt(line,":")).collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Read file Error!");
        }
        return busSchedule;
    }

    //----------------------------------------------create and update----------------------------------------------------------

    public void addNewBus(){
        int busId = 0;
        if(curBus == null){
            busId = 1;
        }else{
            busId = Integer.parseInt(curBus.getBusId());
            busId++;
        }
        curBus = new Bus(String.valueOf(busId));
        curBus.setCurState(batterySize);
        if (MacdonaldSchedule.isEmpty()){
            updateCurBus(LionelSchedule.get(0),curBus.getCurState(),"","","",PARAMETER.LIONEL_GROULX_DIRECTION,false);
            LionelSchedule.remove(0);
        }else if (LionelSchedule.isEmpty()){
            updateCurBus(MacdonaldSchedule.get(0),curBus.getCurState(),"","","",PARAMETER.MACDONALD_DIRECTION,false);
            MacdonaldSchedule.remove(0);
        } else {
            if (MacdonaldSchedule.get(0)<= LionelSchedule.get(0)){//choose the earliest time in Mac
                updateCurBus(MacdonaldSchedule.get(0),curBus.getCurState(),"","","",PARAMETER.MACDONALD_DIRECTION,false);
                MacdonaldSchedule.remove(0);
            }else{//choose the earliest time in Lio
                updateCurBus(LionelSchedule.get(0),curBus.getCurState(),"","","",PARAMETER.LIONEL_GROULX_DIRECTION,false);
                LionelSchedule.remove(0);
            }
        }
        busList.add(curBus);
    }

    //-------------------------------------------update curBus --------------------------------------------------------

    public void changeBusLocation(){//to another side
        if(curBus.getCurLocation().equals(PARAMETER.LIONEL_GROULX_DIRECTION)){
            curBus.setCurLocation(PARAMETER.MACDONALD_DIRECTION);
        }else{
            curBus.setCurLocation(PARAMETER.LIONEL_GROULX_DIRECTION);
        }
    }

    public void findCurLocList(){//find schedule and chargers in current location
        if(curBus.getCurLocation().equals(PARAMETER.LIONEL_GROULX_DIRECTION)){
            station = PARAMETER.LIONEL_GROULX_STATION;
            curLocSchedule = LionelSchedule;
            otherLocSchedule = MacdonaldSchedule;
            curLocChargerList = LionelChargerList;
        }else{
            station = PARAMETER.MACDONALD_STATION;
            curLocSchedule = MacdonaldSchedule;
            otherLocSchedule = LionelSchedule;
            curLocChargerList = MacChargerList;
        }
    }

    public boolean addEmptyBusTrip(){
        for(int time : curLocSchedule){
            if (time > curBus.getCurTime()){
                return false;
            }
        }
        for(int time : otherLocSchedule){
            if (curBus.getCurState() - totalS < policy_min_state){//decide if the bus need to charge after reaching other station
                if(ocPower == 0 && onPower != 0){//only have on charger
                    if(time > curBus.getCurTime() + 60 + chargeTimeCalculator(PARAMETER.ON_CHARGER)){
                        return true;
                    }
                }else{//hava oc and on or only have oc, use oc
                    if(time > curBus.getCurTime() + 60 + chargeTimeCalculator(PARAMETER.OC_CHARGER)){
                        return true;
                    }
                }
            }else{
                if(time > curBus.getCurTime() + 60){
                    return true;
                }
            }


        }

        return false;
    }

    public void updateCurBus(int assignTripStartTime,int atSoc, String chargerId, String btcStartTime, String btcEndTime, String location,boolean isEmpty){//isEmpty==true, is empty bus, else false
        curBus.setCurLocation(location);
        changeBusLocation();

        String tripID;
        if(isEmpty){
            tripID = "empty-"+emptyBusNum;
            emptyBusNum++;
        }else{
            tripID = location + timeTranslateToString(assignTripStartTime,"-");
        }

        if (assignTripStartTime == 0){
            addNewBus();
//            System.out.println("bug!!!");
            return;
        }
        createNewScheduleLine(String.valueOf(curBus.getBusId()),String.valueOf(batterySize),curBus.getCurFinshTrip(),String.valueOf(atSoc),chargerId,btcStartTime,btcEndTime,String.valueOf(curBus.getCurState()),tripID,timeTranslateToString(assignTripStartTime,"h"),timeTranslateToString(assignTripStartTime+60,"h"));
        curBus.setCurState(curBus.getCurState()- totalS);
        curBus.setCurTime(assignTripStartTime+60);
        curBus.setCurFinshTrip(tripID);

    }

    //-------------------------------------------charger tools------------------------------------------------------

    public int chargeTimeCalculator(String type){
        if (type.equals(PARAMETER.OC_CHARGER)){
            return (int) (60 * (float) (((totalS + 0.5 * batterySize) / rate) - (curBus.getCurState() / rate)) / (float) ocPower);
        }else if(type.equals(PARAMETER.ON_CHARGER)){
            return (int) (60 * (float) (batterySize - (curBus.getCurState() / rate)) / (float) onPower);
        }else{
            System.out.println("Charging type error!");
            return -1;
        }
    }

    private void setChargerNum(ChargerModel chargerModel, String type){
        List<Charger> LionelList = LionelChargerList.stream().filter(charger->charger.getType().equals(type)).collect(Collectors.toList());
        List<Charger> MacList = MacChargerList.stream().filter(charger->charger.getType().equals(type)).collect(Collectors.toList());
        chargerModel.setLionelGroulxNumber(String.valueOf(LionelList.size()));
//        LionelList.forEach(e-> System.out.println(e.toString()));
        chargerModel.setMacDonaldNumber(String.valueOf(MacList.size()));
//        MacChargerList.forEach(e-> System.out.println(e.toString()));
    }

    public String addNewCharger(int chargerNum,int power,String location,String type,String station,Integer btcStartTime, Integer btcEndTime, List<Charger> curLocChargerList){
        Charger charger = new Charger(chargerNum);
        charger.setChargerPower(power);
        charger.setLocation(location);
        charger.setType(type);
        charger.addOccupyTime(new Integer[]{btcStartTime,btcEndTime});
        curLocChargerList.add(charger);
        return station + "-" + type + "-" + power + "-" + charger.getChargerId();

    }

    public String addChargerTime(List<Charger> curLocChargerList,String type, String location, String station, Integer btcStartTime, Integer btcEndTime){
        int power;
        if(type.equals(PARAMETER.OC_CHARGER)){
            power = ocPower;
        }else {
            power = onPower;
        }

        if(curLocChargerList.stream().filter(charger -> charger.getType().equals(type)).collect(Collectors.toList()).size() == 0){
            return addNewCharger(1,power,location,type,station,btcStartTime,btcEndTime,curLocChargerList);

        }else{
            for (Charger charger: curLocChargerList) {
                if(type.equals(charger.getType())){
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
            }

            return addNewCharger(curLocChargerList.stream().filter(charger -> charger.getType().equals(type)).collect(Collectors.toList()).size()+1,power,location,type,station,btcStartTime,btcEndTime,curLocChargerList);
        }
    }

    public String chargerAssignerHelper(String type){// type is "OC" or "ON"
        int ocChargeTime = chargeTimeCalculator(PARAMETER.OC_CHARGER);
        int onChargeTime = chargeTimeCalculator(PARAMETER.ON_CHARGER);
//        System.out.println("Use "+ type +" charger");
        int btcStartTime = curBus.getCurTime();
        int btcEndTime;
        if(type.equals(PARAMETER.OC_CHARGER)){
            btcEndTime = btcStartTime + ocChargeTime;
        }else{
            btcEndTime = btcStartTime + onChargeTime;
        }
        String location = curBus.getCurLocation();
        String chargerId = addChargerTime(curLocChargerList, type, location, station, btcStartTime, btcEndTime);
        curBus.setCurTime(btcEndTime);
        if(type.equals(PARAMETER.OC_CHARGER)){
            curBus.setCurState((int) (totalS + 0.5 * batterySize));//update the curState of oc charger
        }else{
            curBus.setCurState(batterySize);//update the curState of on charger
        }
        return chargerId;
    }

    private void chargerAssigner(String location){
        findCurLocList();
        if (curBus.getCurState() < policy_min_state){//need to charge
            int ocChargeTime = chargeTimeCalculator(PARAMETER.OC_CHARGER);
            int onChargeTime = chargeTimeCalculator(PARAMETER.ON_CHARGER);
            String chargerId = "";
            int assignTripStartTime = 0;
            int btcStartTime = 0;
            int atSoc = curBus.getCurState();

            if (ocPower != 0 && onPower != 0) {
                boolean addNew = true;

                for (Integer time : curLocSchedule) {
                    if (curBus.getCurTime() < time) {
                        addNew = false;
                        btcStartTime = curBus.getCurTime();
                        if (time - curBus.getCurTime() <= policy_oc_on_condition && time > curBus.getCurTime() + ocChargeTime) {//use OC charger
                            chargerId = chargerAssignerHelper(PARAMETER.OC_CHARGER);
                            assignTripStartTime = time;
                            curLocSchedule.remove(time);
                            break;

                        } else {//use ON charger
                            if (curBus.getCurTime() + onChargeTime > time) {
                                int flag= 0;
                                for(Integer newTime: curLocSchedule){
                                    if(curBus.getCurTime()+onChargeTime <= newTime){
                                        chargerId = chargerAssignerHelper(PARAMETER.ON_CHARGER);
                                        assignTripStartTime = newTime;
                                        curLocSchedule.remove(newTime);
                                        flag = 1;
                                        break ;
                                    }
                                }
                                if(flag == 1){
                                    break ;
                                }else {
                                    addNewBus();
                                    return;
                                }
                            }else {
                                chargerId = chargerAssignerHelper(PARAMETER.ON_CHARGER);
                                assignTripStartTime = time;
                                curLocSchedule.remove(time);
                                break ;
                            }

                        }
                    }
                }
                if(addNew){
                    addNewBus();
                    return;
                }


            }else if (ocPower != 0 && onPower == 0){// use oc because no on charger
                boolean addNew = true;
                for (Integer time : curLocSchedule) {
                    if (curBus.getCurTime() + ocChargeTime < time) {
                        btcStartTime = curBus.getCurTime();
                        chargerId = chargerAssignerHelper(PARAMETER.OC_CHARGER);
                        assignTripStartTime = time;
                        curLocSchedule.remove(time);
                        addNew = false;
                        break;
                    }
                }

                if(addNew){
                    addNewBus();
                    return;
                }

            }else if (onPower != 0 && ocPower == 0){// use on because no oc charger

                boolean addNew = true;
                for (Integer time : curLocSchedule) {
                    btcStartTime = curBus.getCurTime();
                    if (curBus.getCurTime()+ onChargeTime < time) {
                        chargerId = chargerAssignerHelper(PARAMETER.ON_CHARGER);

                        assignTripStartTime = time;
                        curLocSchedule.remove(time);
                        addNew = false;
                        break;
                    }
                }

                if(addNew){
                    addNewBus();
                    return;
                }

            }else{
                System.out.println("No charger here! ");
            }
            updateCurBus(assignTripStartTime, atSoc, chargerId, timeTranslateToString(btcStartTime, "h"), timeTranslateToString(curBus.getCurTime(), "h"), location,false);



        }else {//not need to charge
            int assignTripStartTime = 0;
            for (Integer time: curLocSchedule) {
                if (curBus.getCurTime() < time){
                    assignTripStartTime = time;
                    curLocSchedule.remove(time);
                    break;
                }
            }
            updateCurBus(assignTripStartTime,curBus.getCurState(),"","","",location,false);

        }
    }

    //----------------------------------------show schedule tools-----------------------------------------------------------
    private void createNewScheduleLine(String busId, String batterySizeSchCol, String tripComp, String atSoc, String chargerId, String btcStartTime,String btcEndTime, String btSoc, String tripId, String taStartTime,String taEndTime){
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
    }

    //----------------------------------------time translate tools-----------------------------------------------------

    public String timeTranslateToString(int min ,String separator){
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


    public Integer timeTranslateToInt(String time, String separator){
        int min;
        if (time.split(separator)[0].equals("0")||time.split(separator)[0].equals("1")||time.split(separator)[0].equals("2")||time.split(separator)[0].equals("3") ){//should optimal
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

    public Bus getCurBus() {
        return curBus;
    }

    public void setCurBus(Bus curBus) {
        this.curBus = curBus;
    }

    public List<Integer> getMacdonaldSchedule() {
        return MacdonaldSchedule;
    }

    public void setMacdonaldSchedule(List<Integer> macdonaldSchedule) {
        MacdonaldSchedule = macdonaldSchedule;
    }

    public List<Integer> getLionelSchedule() {
        return LionelSchedule;
    }

    public void setLionelSchedule(List<Integer> lionelSchedule) {
        LionelSchedule = lionelSchedule;
    }

    public List<Integer> getCurLocSchedule() {
        return curLocSchedule;
    }

    public void setCurLocSchedule(List<Integer> curLocSchedule) {
        this.curLocSchedule = curLocSchedule;
    }

    public List<Integer> getOtherLocSchedule() {
        return otherLocSchedule;
    }

    public void setOtherLocSchedule(List<Integer> otherLocSchedule) {
        this.otherLocSchedule = otherLocSchedule;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public int getTotalS() {
        return totalS;
    }

    public int getRate() {
        return rate;
    }

    public int getPolicy_oc_on_condition() {
        return policy_oc_on_condition;
    }

    public int getBatterySize() {
        return batterySize;
    }

    public void setBatterySize(int batterySize) {
        this.batterySize = batterySize;
    }

    public int getOcPower() {
        return ocPower;
    }

    public void setOcPower(int ocPower) {
        this.ocPower = ocPower;
    }

    public int getOnPower() {
        return onPower;
    }

    public void setOnPower(int onPower) {
        this.onPower = onPower;
    }

    public double getPolicy_min_state() {
        return policy_min_state;
    }

    public void setPolicy_min_state(double policy_min_state) {
        this.policy_min_state = policy_min_state;
    }

    public void setScheduleLines(ArrayList<ScheduleLine> scheduleLines) {
        this.scheduleLines = scheduleLines;
    }

    public ArrayList<Bus> getBusList() {
        return busList;
    }

    public void setBusList(ArrayList<Bus> busList) {
        this.busList = busList;
    }

    public List<Charger> getMacChargerList() {
        return MacChargerList;
    }

    public void setMacChargerList(List<Charger> macChargerList) {
        MacChargerList = macChargerList;
    }

    public List<Charger> getLionelChargerList() {
        return LionelChargerList;
    }

    public void setLionelChargerList(List<Charger> lionelChargerList) {
        LionelChargerList = lionelChargerList;
    }

    public List<Charger> getCurLocChargerList() {
        return curLocChargerList;
    }

    public void setCurLocChargerList(List<Charger> curLocChargerList) {
        this.curLocChargerList = curLocChargerList;
    }

    public int getEmptyBusNum() {
        return emptyBusNum;
    }

    public void setEmptyBusNum(int emptyBusNum) {
        this.emptyBusNum = emptyBusNum;
    }
}
