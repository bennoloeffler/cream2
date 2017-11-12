package bel.en.email;

/**
 * This one should be the last to register.
 * If no other one was responsible, this one generates an error message mail.
 */
public class EmailInterpreterError implements EmailInterpreter {

    public boolean tryAction(EmailData email, EmailDrain drain) {
        try {
            drain.send(email.getSender(), "CRM: Fehler, konnte Mail nicht verarbeiten... " + email.getSubject(), email.getText());
        } catch(Exception e){} // ignore...
        return true;
    }

    public String getName() {
        return getClass().getName();
    }

    public String getHelp() {
        return "Only used in case of no other EmailInterpreter could handle the mail.";
    }

}
