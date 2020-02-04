package Model;

public class ScheduleGenerator {
    public ConfigPlanGenerator configPlanGenerator;

    public ScheduleGenerator(ConfigPlanGenerator configPlanGenerator) {
        this.configPlanGenerator = configPlanGenerator;
        generateSchedule();
    }

    private void generateSchedule(){

    }

    public ConfigPlanGenerator getConfigPlanGenerator() {
        return configPlanGenerator;
    }

    public void setConfigPlanGenerator(ConfigPlanGenerator configPlanGenerator) {
        this.configPlanGenerator = configPlanGenerator;
    }
}
