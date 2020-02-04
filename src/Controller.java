import Entities.ChargerCheckBox;
import Entities.ChargerModel;
import Model.ConfigPlanGenerator;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;

import Entities.BusBatteryConfig;


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
    public TableColumn planBusType;
    @FXML
    public TableColumn planBusNumber;
    @FXML
    public TableColumn planBattery;
    @FXML
    public TableColumn planChargerType;
    @FXML
    public TableColumn planNumLionel;
    @FXML
    public TableColumn planMacDonald;
    @FXML
    public TableView chargerTable;
    @FXML
    public TableView busTable;
    @FXML
    public Label cost;


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


    }

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
                BusBatteryConfig busBatteryConfig = new BusBatteryConfig(busType.getValue().toString(),Integer.parseInt(batterySize.getValue().toString()), Integer.parseInt(unitCostBusBattery.getText()));
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
                                chargerModels.add(new ChargerModel(chargerManufacture.getValue().toString(),chargerType.getModel(),chargerType.getPower(),Integer.parseInt(chargerPrice.getText())));
                            }
                            flag--;
                        }
                    }
                }

                configPlanGenerator = new ConfigPlanGenerator(chargerModels,busBatteryConfig);

                generateBusPlan();
                generateChargerPlan();
                cost.setText(String.valueOf(configPlanGenerator.getExpenditure()));

            }
        });

    }


}
