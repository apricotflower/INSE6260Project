package Test;
import Model.Entities.BusBatteryConfig;
import Model.Entities.ChargerModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import Model.ConfigPlanGenerator;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ConfigPlanGeneratorTest {
    private ConfigPlanGenerator configPlanGenerator;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void beforeMethod() {
        ArrayList<ChargerModel> chargerModels = new ArrayList<>();
        chargerModels.add(new ChargerModel("sampleManufacture","sampleModel",300,5.4));
        BusBatteryConfig busBatteryConfig = new BusBatteryConfig("SampleBusType",294, 3.4);
        configPlanGenerator = new ConfigPlanGenerator(chargerModels,busBatteryConfig);
    }

    @Test
    public void testGetExpenditure(){
        assertEquals(72.0,configPlanGenerator.getExpenditure(), 0.0f);
    }
}
