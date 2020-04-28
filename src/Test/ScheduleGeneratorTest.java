package Test;
import Model.ConfigPlanGenerator;
import Model.Entities.Bus;
import Model.Entities.BusBatteryConfig;
import Model.Entities.Charger;
import Model.Entities.ChargerModel;
import Model.ScheduleGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 *
 * @author  Xiayan Zhong
 * This class is for test ScheduleGenerator
 *
 */

public class ScheduleGeneratorTest {
    private ConfigPlanGenerator configPlanGenerator;
    private ScheduleGenerator scheduleGenerator;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void beforeMethod() {
        ArrayList<ChargerModel> chargerModels = new ArrayList<>();
        chargerModels.add(new ChargerModel("sampleManufacture","sampleModel",300,5.4));
        BusBatteryConfig busBatteryConfig = new BusBatteryConfig("SampleBusType",294, 3.4);
        configPlanGenerator = new ConfigPlanGenerator(chargerModels,busBatteryConfig);
        scheduleGenerator = new ScheduleGenerator(configPlanGenerator);
    }

    @Test
    public void testReadBusSchedule(){
        int a = scheduleGenerator.timeTranslateToInt("8:44",":");
        int b = scheduleGenerator.timeTranslateToInt("9:10",":");
        List<Integer> busSchedule = new ArrayList<>();
        busSchedule.add(a);
        busSchedule.add(b);
        assertEquals(busSchedule,scheduleGenerator.readBusSchedule("TestFile/test.txt"));
    }

    @Test
    public void testAddNewBus(){
        scheduleGenerator.setLionelSchedule(scheduleGenerator.readBusSchedule("Bus_schedule/Lionel-Groulx.txt"));
        scheduleGenerator.setMacdonaldSchedule(scheduleGenerator.readBusSchedule("Bus_schedule/Macdonald.txt"));
        scheduleGenerator.addNewBus();
        assertEquals("19",scheduleGenerator.getCurBus().getBusId());
    }

    @Test
    public void testChangeBusLocation(){
        Bus curBus = new Bus("1");
        curBus.setCurLocation("W");
        scheduleGenerator.setCurBus(curBus);
        scheduleGenerator.changeBusLocation();
        assertEquals("E",scheduleGenerator.getCurBus().getCurLocation());
    }

    @Test
    public void testFindCurLocList(){
        scheduleGenerator.getCurBus().setCurLocation("W");
        scheduleGenerator.findCurLocList();
        assertEquals(scheduleGenerator.getLionelSchedule(),scheduleGenerator.getCurLocSchedule());
        assertEquals(scheduleGenerator.getMacdonaldSchedule(),scheduleGenerator.getOtherLocSchedule());
        assertEquals("LG",scheduleGenerator.getStation());
    }

    @Test
    public void testAddEmptyBusTripFalse(){
        scheduleGenerator.getCurBus().setCurTime(200);
        List<Integer> testList = new ArrayList<>();
        testList.add(300);
        scheduleGenerator.setCurLocSchedule(testList);
        assertFalse(scheduleGenerator.addEmptyBusTrip());
    }

    @Test
    public void testAddEmptyBusTripTrueNoCharge(){
        scheduleGenerator.getCurBus().setCurTime(200);
        List<Integer> testList = new ArrayList<>();
        testList.add(100);
        scheduleGenerator.setCurLocSchedule(testList);
        List<Integer> testList2 = new ArrayList<>();
        testList2.add(1000);
        scheduleGenerator.setOtherLocSchedule(testList2);
        assertTrue(scheduleGenerator.addEmptyBusTrip());
    }

    @Test
    public void testAddEmptyBusTripTrueOcCharge(){
        scheduleGenerator.getCurBus().setCurTime(150);
        List<Integer> testList = new ArrayList<>();
        testList.add(100);
        scheduleGenerator.setCurLocSchedule(testList);
        List<Integer> testList2 = new ArrayList<>();
        testList2.add(260);
        scheduleGenerator.setOtherLocSchedule(testList2);
        scheduleGenerator.setOcPower(100);
        scheduleGenerator.setOnPower(10);
        assertTrue(scheduleGenerator.addEmptyBusTrip());
    }

    @Test
    public void testAddEmptyBusTripTrueOnCharge(){
        scheduleGenerator.getCurBus().setCurTime(200);
        List<Integer> testList = new ArrayList<>();
        testList.add(100);
        scheduleGenerator.setCurLocSchedule(testList);
        List<Integer> testList2 = new ArrayList<>();
        testList2.add(2000);
        scheduleGenerator.setOtherLocSchedule(testList2);
        scheduleGenerator.setOcPower(0);
        scheduleGenerator.setOnPower(10);
        assertTrue(scheduleGenerator.addEmptyBusTrip());
    }

    @Test
    public void testChargeTimeCalculatorOC(){
        scheduleGenerator.getCurBus().setCurState(112);
        scheduleGenerator.setOcPower(100);
        assertEquals(45,scheduleGenerator.chargeTimeCalculator("OC"));
    }

    @Test
    public void testChargeTimeCalculatorON(){
        scheduleGenerator.getCurBus().setCurState(112);
        scheduleGenerator.setOnPower(10);
        assertEquals(1092,scheduleGenerator.chargeTimeCalculator("ON"));
    }

    @Test
    public void testChargeTimeCalculatorError(){
        scheduleGenerator.getCurBus().setCurState(112);
        scheduleGenerator.setOnPower(10);
        assertEquals(-1,scheduleGenerator.chargeTimeCalculator("ioin"));
    }

    @Test
    public void testUpdateCurBus(){
        scheduleGenerator.updateCurBus(400,100,"","","","W",true);
        assertEquals("empty-8",scheduleGenerator.getCurBus().getCurFinshTrip());
    }

    @Test
    public void testAddNewCharger(){
        assertEquals("LG" + "-" + "OC" + "-" + 220 + "-" + 1,scheduleGenerator.addNewCharger(1,220,"W","OC","LG",400,500,new ArrayList<>()));
    }

    @Test
    public void testAddChargerTimeNew(){
        List<Charger> curLocChargerList = new ArrayList<>();
        assertEquals("LG" + "-" + "OC" + "-" + 300 + "-" + 1,scheduleGenerator.addChargerTime(curLocChargerList,"OC","W","LG",400,500));
    }

    @Test
    public void testTimeTranslateToString(){
        assertEquals("4:15",scheduleGenerator.timeTranslateToString(255,":"));
    }

    @Test
    public void testTimeTranslateToInt(){
        Integer a = 255;
        assertEquals(a,scheduleGenerator.timeTranslateToInt("4:15",":"));
    }




}
