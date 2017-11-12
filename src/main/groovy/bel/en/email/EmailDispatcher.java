package bel.en.email;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class EmailDispatcher {
    private static final Logger LOGGER = Logger.getLogger(EmailDispatcher.class.getName());
    private static final boolean debug = "on".equals(System.getProperty("DEBUG"));

    /**
     * this is the email drain, the interpreters can use to send messages
     */
    private EmailDrain drain;

    /**
     * this is the chain of responsibility for interpretation of emails
     */
    private ArrayList emailInterpreters = new ArrayList();

    /**
     * use this interpreter, if no other was found
     */
    private EmailInterpreter emailInterpreterError = new EmailInterpreterError();

    /**
     * signals successful dispatching to clients who are interessted in.
     */
    private boolean lastEmailsuccessfulDispatched = false;
    private boolean exception = false;

    /**
     * ctor.
     * @param drain
     */
    public EmailDispatcher(EmailDrain drain) {
        this.drain = drain;
    }

    /**
     * Register an email interpreter at the end of the chain of responsabilty.
     * @param emailInterpreter the one to register
     */
    public void register(EmailInterpreter emailInterpreter) {
        emailInterpreters.add(emailInterpreter);
    }

    /**
     * Deregister an email interpreter.
     * @param emailInterpreter the one to deregister.
     */
    public void deRegister(EmailInterpreter emailInterpreter) {
        emailInterpreters.remove(emailInterpreter);
    }

    /**
     * Try to dispatch an email to an interpreter. This may fail,
     * if no interpreter is responsible.
     * @param emailData email to dispatch
     */
    public void dispatch(EmailData emailData) throws Exception {
        //ArrayList deregister = new ArrayList();
        //boolean lastEmailsuccessfulDispatched = false;
        exception = false;
        for (Iterator iterator = emailInterpreters.iterator(); iterator.hasNext();) {
            EmailInterpreter emailInterpreter = (EmailInterpreter) iterator.next();
            try {
                lastEmailsuccessfulDispatched = emailInterpreter.tryAction(emailData, drain);
                if(lastEmailsuccessfulDispatched) break;
            } catch (ActionFailedException e) {
                // TODO: sent email to admin
                LOGGER.severe("EmailInterpreter '" + emailInterpreter.getName() + "' caused an exception. Email will be moved to failed emails folder! " + e.toString());
                //deregister.add(emailInterpreter);
                exception = true;
            }
        }
        if (!lastEmailsuccessfulDispatched && !exception) {

            // This is a very special use case, that is not modelled as Email Interpreter
            // because it needs access to all email Interpreters in order to get their
            // help string.
            if(emailData.getSubject().equalsIgnoreCase("HELP") || emailData.getSubject().equalsIgnoreCase("H")) {
                LOGGER.info("sending help to: " + emailData.getSender());
                StringBuffer allHelp = new StringBuffer("HELP | H - ask for this help");
                for (Iterator iterator = emailInterpreters.iterator(); iterator.hasNext();) {
                    EmailInterpreter emailInterpreter = (EmailInterpreter) iterator.next();
                    allHelp.append("\n\n" + emailInterpreter.getHelp());
                }
                drain.send(emailData.getSender(), "[CRM] H --> Here comes the requested help mail.", allHelp.toString() );
                LOGGER.info("finished sending");
            } else {
                //try {
                LOGGER.info("Message format was not recognized. Ignoring Message with subject: "+emailData.getSubject());
                /*
                LOGGER.info("Message format was not recognized. Sending error message to sender: " + emailData.getSender());
                String text = "Your message format was not recognized!\nThe Subject was:\n" +
                        emailData.getSubject() +
                        "The text you sent to us is listed below:\n" +
                        emailData.getText();
                EmailData errorAnswer = new EmailData(emailData.getSender(), "RE: " + emailData.getSubject(), text);

                emailInterpreterError.tryAction(errorAnswer, drain);

              }  catch (ScriptFailedException e) {
                // TODO: sent email to admin
                LOGGER.error("email was not succesfully handled and '" + emailInterpreterError.getName() + "' caused an exception too.", e);
                throw new RuntimeException("dispatch could not even answer to failed request to sender...");
               } */
            }
        }
    }



    public boolean isLastEmailsuccessfulDispatched() {
        return lastEmailsuccessfulDispatched;
    }
    public boolean isLastEmailCausedException() {
        return exception;
    }

    public void setErrorInterpreter(EmailInterpreter emailInterpreterError) {
        this.emailInterpreterError = emailInterpreterError;
    }

    public List getAllInterpreters() {
        return new ArrayList(emailInterpreters);
    }
}
