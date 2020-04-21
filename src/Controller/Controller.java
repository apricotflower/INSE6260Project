package Controller;

import Controller.Entities.ChargerCheckBox;
import Model.Entities.ChargerModel;
import Model.ConfigPlanGenerator;
import Model.Entities.ScheduleLine;
import Model.ScheduleGenerator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import java.util.ArrayList;
import Model.Entities.BusBatteryConfig;

/**
 *
 * @author  Xiayan Zhong
 * This class is controller.
 *
 */


public class Controller {
    //base data
    public ArrayList<String> chargerListManufacture;
//    public HashMap<String,ArrayList<String>> manufactureToCharger;
    public ArrayList<String> busTypeList;
    public ArrayList<String> batteryTypeList;


    @FXML
    public Button runButton;
    @FXML
    public ComboBox chargerManufacture;
    @FXML
    public ListView chargerModelPower;
    @FXML
    public ComboBox busType;
    @FXML
    public ComboBox batterySize;
    @FXML
    public TextField unitCostBusBattery;
    @FXML
    public Button displayButton;
    @FXML
    public Label costLabel;
    @FXML
    public Label cost;
    @FXML
    public Label errorWarning;

    //bus
    @FXML
    public TableView busTable;
    @FXML
    public TableColumn planBusNumber;
    @FXML
    public TableColumn planBattery;
    @FXML
    public TableColumn planBusType;

    //charger
    @FXML
    public TableView chargerTable;
    @FXML
    public TableColumn planChargerType;
    @FXML
    public TableColumn planNumLionel;
    @FXML
    public TableColumn planMacDonald;

    //schedule
    @FXML
    public TableView scheduleTable;
    @FXML
    public TableColumn busId;
    @FXML
    public TableColumn batterySizeSchCol;
    @FXML
    public TableColumn tripComp;
    @FXML
    public TableColumn atSoc;
    @FXML
    public TableColumn chargerId;
    @FXML
    public TableColumn btcStartTime;
    @FXML
    public TableColumn btcEndTime;
    @FXML
    public TableColumn btSoc;
    @FXML
    public TableColumn tripId;
    @FXML
    public TableColumn taStartTime;
    @FXML
    public TableColumn taEndTime;


    ConfigPlanGenerator configPlanGenerator;
    ScheduleGenerator scheduleGenerator;




    public void initialize() {
        loadData();

        chargerManufacture.setItems(FXCollections.observableArrayList(chargerListManufacture));
        chargerManufacture.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                setChargerModelPower(newValue.toString());
            }
        });

        busType.setItems(FXCollections.observableArrayList(busTypeList));
        batterySize.setItems(FXCollections.observableArrayList(batteryTypeList));

        busTable.setEditable(true);
        chargerTable.setEditable(true);

        setRunButton();
        setDisplayButton();
        displayButton.setVisible(false);


    }

    //-------------------------------------------------set initial config------------------------------------------------------------

    public void loadData(){
        chargerListManufacture = new ArrayList<>();
        chargerListManufacture.add("HELIOX");
        chargerListManufacture.add("ABB");

        busTypeList = new ArrayList<>();
        busTypeList.add("NOVA LFSe+");

        batteryTypeList = new ArrayList<>();
        batteryTypeList.add("294");
        batteryTypeList.add("394");
    }


    public HBox generateChargerListView(String[] charger){
        HBox hbox = new HBox(3);
        ChargerCheckBox chargerModelPower = new ChargerCheckBox(charger[0],Integer.parseInt(charger[1]));
//        CheckBox chargerModelPower = new CheckBox(charger);
        TextField chargerPrice = new TextField();
        chargerPrice.setPrefColumnCount(3);
        chargerPrice.setPromptText("price");
        chargerPrice.setDisable(true);
        chargerModelPower.selectedProperty().addListener(new ChangeListener<Boolean>(){
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean old_val, Boolean new_val) {
                if (chargerModelPower.isSelected()){
                    chargerPrice.setDisable(false);
//                    chargerModelPower.setPrice(Integer.parseInt(chargerPrice.getText()));
                }else {
                    chargerPrice.clear();
                    chargerPrice.setDisable(true);
                }
            }
        });
        hbox.getChildren().addAll(chargerModelPower,chargerPrice);
        return hbox;
    }


    public void setChargerModelPower(String chargerManufactureString){
        ObservableList<HBox> items = FXCollections.observableArrayList ();
        ArrayList<String[]> chargerList = new ArrayList<>();

        if (chargerManufactureString.equals("HELIOX")){
            chargerList.add(new String[]{"OC 450kW(450kW)","450"});
            chargerList.add(new String[]{"FAST DC 50kW(50kW)","50"});

        }else if (chargerManufactureString.equals("ABB")){
            chargerList.add(new String[]{"HVC 300PD(300kW)","300"});
            chargerList.add(new String[]{"HVC 100PU-S(100kW)","100"});
        }

        for (String[] charger : chargerList){
            items.add(generateChargerListView(charger));
        }

        chargerModelPower.setItems(items);
    }

    //----------------------------------------------modify data in configuration plan---------------------------------------------------

    @FXML
    public void tableBusListener(TableColumn.CellEditEvent<BusBatteryConfig,String> value) {
        TableColumn<BusBatteryConfig, String> editedCol = value.getTableColumn();
        BusBatteryConfig std = value.getRowValue();
        System.out.println("Change bus number " + editedCol.getCellData(std) + " to " + value.getNewValue());
        configPlanGenerator.getBusBatteryConfig().setNumber(value.getNewValue());
        cost.setText(String.valueOf(configPlanGenerator.getExpenditure()));
    }

    @FXML
    public void numLionelListener(TableColumn.CellEditEvent<ChargerModel,String> value) {
        TableColumn<ChargerModel, String> editedCol = value.getTableColumn();
        ChargerModel std = value.getRowValue();
        System.out.println("Change Lionel charger number " + editedCol.getCellData(std) + " to " + value.getNewValue());
//        System.out.println(value.getTablePosition().getRow());
        configPlanGenerator.getChargerModels().get(value.getTablePosition().getRow()).setLionelGroulxNumber(value.getNewValue());
        cost.setText(String.valueOf(configPlanGenerator.getExpenditure()));
    }

    @FXML
    public void numMacDonaldListener(TableColumn.CellEditEvent<ChargerModel,String> value) {
        TableColumn<ChargerModel, String> editedCol = value.getTableColumn();
        ChargerModel std = value.getRowValue();
        System.out.println("Change MacDonald charger number " + editedCol.getCellData(std) + " to " + value.getNewValue());
        configPlanGenerator.getChargerModels().get(value.getTablePosition().getRow()).setMacDonaldNumber(value.getNewValue());
        cost.setText(String.valueOf(configPlanGenerator.getExpenditure()));
    }

   // ---------------------------------------------generate configuration plan and schedule-----------------------------------------------

    private void generateBusPlan(){
        ObservableList<BusBatteryConfig> busList = FXCollections.observableArrayList();
        busList.add(configPlanGenerator.getBusBatteryConfig());
        busTable.setItems(FXCollections.observableArrayList(busList));
        planBusNumber.setCellValueFactory(new PropertyValueFactory<String,String>("number"));
        planBattery.setCellValueFactory(new PropertyValueFactory<String,Integer>("batterySize"));
        planBusType.setCellValueFactory(new PropertyValueFactory<String,String>("busType"));

        planBusNumber.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    private void generateChargerPlan(){
        ObservableList<ChargerModel> chargerList = FXCollections.observableArrayList();
        for (ChargerModel chargerModelCal: configPlanGenerator.getChargerModels()){
            chargerList.add(chargerModelCal);
        }
        chargerTable.setItems(FXCollections.observableArrayList(chargerList));
        planChargerType.setCellValueFactory(new PropertyValueFactory<String,String>("chargerModel"));
        planNumLionel.setCellValueFactory(new PropertyValueFactory<String,String>("LionelGroulxNumber"));
        planMacDonald.setCellValueFactory(new PropertyValueFactory<String,String>("MacDonaldNumber"));

        planNumLionel.setCellFactory(TextFieldTableCell.forTableColumn());
        planMacDonald.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    public void generateSchedule(){
        ObservableList<ScheduleLine> scheduleList = FXCollections.observableArrayList();
        for (ScheduleLine scheduleLine: configPlanGenerator.getScheduleGenerator().getScheduleLines()){
            scheduleList.add(scheduleLine);
        }

        scheduleTable.setItems(FXCollections.observableArrayList(scheduleList));

        busId.setCellValueFactory(new PropertyValueFactory<String,String>("busId"));
        batterySizeSchCol.setCellValueFactory(new PropertyValueFactory<String,String>("batterySizeSchCol"));
        tripComp.setCellValueFactory(new PropertyValueFactory<String,String>("tripComp"));
        atSoc.setCellValueFactory(new PropertyValueFactory<String,String>("atSoc"));
        chargerId.setCellValueFactory(new PropertyValueFactory<String,String>("chargerId"));
        btcStartTime.setCellValueFactory(new PropertyValueFactory<String,String>("btcStartTime"));
        btcEndTime.setCellValueFactory(new PropertyValueFactory<String,String>("btcEndTime"));
        btSoc.setCellValueFactory(new PropertyValueFactory<String,String>("btSoc"));
        tripId.setCellValueFactory(new PropertyValueFactory<String,String>("tripId"));
        taStartTime.setCellValueFactory(new PropertyValueFactory<String,String>("taStartTime"));
        taEndTime.setCellValueFactory(new PropertyValueFactory<String,String>("taEndTime"));

    }


    //---------------------------------------------------------Run button and Display button---------------------------------------------------------




    private void setDisplayButton(){
        displayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for (int i = 0; i < busTable.getItems().size(); i++) {
                    BusBatteryConfig b = (BusBatteryConfig)busTable.getItems().get(i);
                    System.out.println(b.getBusType());
                    System.out.println(b.getNumber());
                    System.out.println(b.getBatterySize());
                }

                for (int i = 0; i < chargerTable.getItems().size(); i++) {
                    ChargerModel c = (ChargerModel) chargerTable.getItems().get(i);
                    System.out.println(c.getChargerModel());
                    System.out.println(c.getLionelGroulxNumber());
                    System.out.println(c.getMacDonaldNumber());
                }

                scheduleGenerator = new ScheduleGenerator(configPlanGenerator);

            }
        });

    }


    private void setRunButton(){
        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Generate configuration plan!");
                BusBatteryConfig busBatteryConfig = null;
                try{
                    checkPrice(unitCostBusBattery.getText());
                    busBatteryConfig = new BusBatteryConfig(busType.getValue().toString(),Integer.parseInt(batterySize.getValue().toString()), Double.parseDouble(unitCostBusBattery.getText()));
                }catch (Exception e){
                    System.out.println("Error!");
                    errorWarning.setText("Input Error! Input Again!");
                    return;
                }

                ArrayList<ChargerModel> chargerModels = new ArrayList<>();
                ObservableList<HBox> items = chargerModelPower.getItems();
                for (HBox item: items){
                    int flag = 0;
                    ChargerCheckBox chargerType = new ChargerCheckBox("",0);
                    TextField chargerPrice;
                    boolean checkChargerSelected = false;
                    for(Object hboxItem :item.getChildren()){
                        if (flag == 0){
                            chargerType = (ChargerCheckBox)hboxItem;
                            checkChargerSelected = chargerType.isSelected();
                            flag++;
                        }else if (flag == 1){
                            if (checkChargerSelected){
                                chargerPrice = (TextField)hboxItem;
                                try {
                                    checkPrice(chargerPrice.getText());
                                    chargerModels.add(new ChargerModel(chargerManufacture.getValue().toString(), chargerType.getModel(), chargerType.getPower(), Double.parseDouble(chargerPrice.getText())));
                                }catch (Exception e){
                                    System.out.println("Error!");
                                    errorWarning.setText("Input Error! Input Again!");
                                    return;
                                }
                            }
                            flag--;
                        }
                    }
                }

                configPlanGenerator = new ConfigPlanGenerator(chargerModels,busBatteryConfig);

                generateBusPlan();
                generateChargerPlan();
                cost.setText(String.valueOf(configPlanGenerator.getExpenditure()));
                generateSchedule();
                errorWarning.setText("");
            }
        });

    }

    public void checkPrice(String price) throws Exception {
        if (!checkPriceFormat(price)) {
            throw new Exception();
        }
    }

    public boolean checkPriceFormat(String text) {
        if (text != null && !text.isEmpty()) {
            String ipRegEx = "\\d\\.\\d*|[1-9]\\d*|\\d*\\.\\d*|\\d";
            if (text.matches(ipRegEx)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }


}
