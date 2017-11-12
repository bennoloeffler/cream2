package bel.learn._21_mvc.table;

import bel.learn._01_bean.Person;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

/**
 *
 */
public class OnePersonController implements Observer, ActionListener {

    Model m;
    OnePersonView v;
    public static final String NEUE_PERSON_ACTION = "NEUE_PERSON_ACTION";
    public static final String HERR_FRAU_ACTION = "HERR_FRAU_ACTION";
    public static final String FULL_NAME_ACTION = "FULL_NAME_ACTION";
    public static final String ALTER_ACTION = "ALTER_ACTION";
    public static final String KONTO_ACTION = "KONTO_ACTION";

    public OnePersonController(Model m, OnePersonView v) {
        this.m = m;
        this.v = v;
        m.addObserver(this);
        v.getButton().setActionCommand(NEUE_PERSON_ACTION);
        v.getButton().addActionListener(this);

        v.getAnrede().setActionCommand(HERR_FRAU_ACTION);
        v.getAnrede().addActionListener(this);

        v.getFullName().setActionCommand(FULL_NAME_ACTION);
        v.getFullName().addActionListener(this);

        v.getAlter().setActionCommand(ALTER_ACTION);
        v.getAlter().addActionListener(this);

        v.getKonto().setActionCommand(KONTO_ACTION);
        v.getKonto().addActionListener(this);
    }


    //
    // Observer
    //

    @Override
    public void update(Observable o, Object arg) {
        int s = m.getSelected();
        if(s >= 0) {
            Person p = m.getPersons().get(m.getSelected());
            v.getAnrede().setText(p.isMale()?"Herr":"Frau");
            v.getFullName().setText(p.getFullName());
            v.getAlter().setText(String.valueOf(p.getAge()));
            v.getKonto().setText(String.valueOf(String.format("%.2f",p.getMoney())));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        int s = m.getSelected();
        Person p = null;
        if(s >= 0) {
            p = m.getPersons().get(m.getSelected());
        }
        switch(e.getActionCommand()) {

            case NEUE_PERSON_ACTION: {
                int[] noten = {0};
                m.addPerson(new Person(true, "?", -1, noten, 0.0));
                m.setSelected(m.getPersons().size() - 1);
                break;
            }
            case HERR_FRAU_ACTION: {
                if(p!=null) {
                    if(v.getAnrede().getText().equals("Frau")) {
                        p.setMale(false);
                    } else {
                        p.setMale(true);
                    }
                }
                break;
            }
            case FULL_NAME_ACTION: {
                if(p!=null) {
                    p.setFullName(v.getFullName().getText());
                }
                break;
            }
            case ALTER_ACTION: {
                if(p!=null) {
                    p.setAge(Integer.parseInt(v.getAlter().getText()));
                }
                break;
            }
            case KONTO_ACTION: {
                if(p!=null) {
                    try {
                        String value = v.getKonto().getText().replace(",", ".");
                        p.setMoney(Double.parseDouble(value));
                    } catch (Exception e1) {
                        System.out.println(e1);
                        e1.printStackTrace();
                    }
                }
                break;
            }
        }
    }
}
