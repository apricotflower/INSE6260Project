package Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ConfigPlanGeneratorTest.class,
        ControllerTest.class,
        ScheduleGeneratorTest.class,
       })

public class TestSuite {


}
