package Entities;

import javafx.scene.control.CheckBox;

public class ChargerCheckBox extends CheckBox{
    private String model;
    private int power;


    public ChargerCheckBox(String s, int power) {
        super(s);
        this.model = s;
        this.power = power;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

}
