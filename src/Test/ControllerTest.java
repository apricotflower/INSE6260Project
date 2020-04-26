package Test;
import Controller.Controller;

import Controller.Entities.ChargerCheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class ControllerTest {
    private Controller con;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void beforeMethod() {
        con = new Controller();
    }

    @Test
    public void testCheckPriceFormatTrue(){
        assertTrue(con.checkPriceFormat("34"));
        assertTrue(con.checkPriceFormat("99.234"));
    }

    @Test
    public void testCheckPriceFormatFalse(){
        assertFalse(con.checkPriceFormat("dionef"));
        assertFalse(con.checkPriceFormat("-343"));
    }

}
