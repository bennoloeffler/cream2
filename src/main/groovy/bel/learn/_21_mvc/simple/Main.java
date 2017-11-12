package bel.learn._21_mvc.simple;

/**
 * Created by VundS02 on 15.01.2017.
 */
public class Main {

    public static void main(String[] args) {
        Model m = new Model();

        ViewButtonLabel viewButtonLabel = new ViewButtonLabel();
        ControllerButtonLabel cbl = new ControllerButtonLabel(m, viewButtonLabel);

        ViewTextField viewTextField = new ViewTextField();
        ControllerTextField ctf = new ControllerTextField(m, viewTextField);

        viewButtonLabel.setVisible(true);
        viewTextField.setVisible(true);

    }
}
