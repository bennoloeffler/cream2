package bel.en.email;

import bel.cream2.deamon.DeamonCreamWorker;
import bel.en.data.AbstractConfiguration;
import bel.en.data.CreamUserData;
import bel.en.evernote.ENConfiguration;
import bel.util.HtmlToPlainText;
import bel.util.Util;
import lombok.extern.log4j.Log4j2;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reads mail from Exchange server
 *
 * Test-Cases:
 * 1.) an external person (non-cream-user) writes an email directly to crm@...
 * That may be spam - but that may also be a known customer... so forward it
 * 2.) an forwarded mail may known: (eg. customer sabine.kiefer.die.richtige@googlemail.com)
 * LINK IT
 * 3.) an forwarded mail may be unknown: eg. sabine.kiefer@gmx.de
 * IGNORE IT
 * 4.) an internal person writes a mail and sends it cc (AUCH bcc) to crm@...
 * LINK IT
 * 5.) eine adresse im mailtext mit l: prefixen
 * LINK IT
 * 6.) im betreff ganz vorne: email
 * LINK IT
 *
 *
 */
@Log4j2
public class ReadAndForwardExchangeMails {

    static boolean mailOff = false;

    String user = "crm@v-und-s.de";
    String pw = "Hup71467";
    //String urlService = "https://vunds.epc-cloud.de/EWS/Exchange.asmx";
    //String urlService = "https://mail.v-und-s.de/EWS/Exchange.asmx";
    String urlService = "https://outlook.office365.com/ews/exchange.asmx";
    public static String emailCRM = "crm@v-und-s.de";

    // EVERNOTE email address
    // bennoloeffler.173c3b6@m.evernote.com // gmx
    // loeffler425.6d39d31@m.evernote.com // v-und-s
    String emailEvernote = "loeffler425.6d39d31@m.evernote.com";

    private ExchangeService service;
    private List<EmailMessage> emails = new ArrayList<>();

    public void doIt() throws Exception {
        System.out.print("going to check for mail at crm@v-und-s.de  -->   ");
        login();
        readMails();
        forwardMails();
        logout();
    }

    private void logout() {
        try {
            service.close();
        } catch (Exception e) {
            // ignore
        }
    }

    private void forwardMails() throws Exception{

        //String allCreamUsers = ENConfiguration.getConfig().getUsers().stream().map(CreamUserData::getEmail).collect(Collectors.joining(", "));
        List<String> allCreamUsers = getAllUsersEmails();
        allCreamUsers.add(emailCRM);
        for(EmailMessage m: emails) {


            // first of all, detect, if there is a potential mail adress to search for and link to in evernote
            // subject or l: --> link to
            // yes? --> link to and stop
            // no?
            //   to, cc?? (check if recipients has content AFTER removed allCreamUsers) --> link to and stop
            //   no?
            //      first mail --> link to
            //      no? --> NO LINK
            //
            String plainTextBody ="";
            if(m.getBody().getBodyType() == BodyType.HTML) {
                //System.out.println("html body");
                plainTextBody = HtmlToPlainText.convert(m.getBody().toString());

            } else {
                System.out.println("text body");
                plainTextBody = m.getBody().toString();

            }
            List<String> recipients = m.getToRecipients().getItems().stream().map(EmailAddress::getAddress).collect(Collectors.toList());
            recipients.addAll(m.getCcRecipients().getItems().stream().map(EmailAddress::getAddress).collect(Collectors.toList()));
            String subjectLinkMail = Util.extractEmailStartOfDocument(m.getSubject());
            if(subjectLinkMail != null) {
                recipients.add(subjectLinkMail);
            }

            String mailInMailBody = Util.extractEmailStartOfDocument(plainTextBody); // maybe very first element
            if(mailInMailBody == null) {
                mailInMailBody = Util.extractEmailLinkTo(plainTextBody); // otherwise, check for l: in the text
            }
            if(mailInMailBody != null) {
                recipients.add(mailInMailBody);
            }

            recipients.add(m.getFrom().getAddress());
            recipients.removeAll(allCreamUsers);
            String mailsRecipientsString = recipients.stream().collect(Collectors.joining(", "));
            //System.out.println("mailsString: '" + mailsString+"'");

            log.debug("Found mail from "+m.getFrom()+" with subject: " + m.getSubject());
            if (m.getSubject().startsWith("h:") || m.getSubject().startsWith("H:")) {
                log.debug("RECOGNIZED: sending help");
                sendHelp(m, "HILFE kommt hier ;-) ...");
            } else if(m.getSubject().startsWith("m:") || m.getSubject().startsWith("M:")) {
                log.debug("RECOGNIZED: manual adresse an backoffice");
                forwardToManualAdresses(m);
            } else if(m.getSubject().startsWith("a:") || m.getSubject().startsWith("A:")) {
                log.debug("RECOGNIZED: automatic adresse");
                forwardToAutomaticAdresses(m);
            } else if(m.getSubject().startsWith("n:") || m.getSubject().startsWith("N:")) {
                log.debug("RECOGNIZED: new Contact");
                newContact(m);
            } else if(m.getSubject().startsWith("ü:") || m.getSubject().startsWith("Ü:") ||
                    m.getSubject().startsWith("o:") || m.getSubject().startsWith("O:")) {
                log.debug("RECOGNIZED: overview");
                DeamonCreamWorker.syncAndGenerateOverviews();
            } else if(mailsRecipientsString != null && !mailsRecipientsString.trim().equals("")) {
                log.debug("RECOGNIZED: forward linking mail to evernote");
                log.debug("potential linkage mail-adresses are: "  + mailsRecipientsString);
                forwardLinkMail(m, subjectLinkMail, mailsRecipientsString);
            } else {
                if(allCreamUsers.contains(m.getFrom().getAddress())) {
                    log.warn("NOT RECOGNIZED: send error message, because unrecognizable mail came from inside: " + m.getFrom().getAddress());
                    sendErrorAndHelp(m);
                } else {
                    log.warn("NOT RECOGNIZED: deleting mail, because unrecognizable mail came from OUTSIDE: " + m.getFrom().getAddress());
                    log.warn("Error parsing mail from external. IGNORING and DELETING: " + m.getFrom().getAddress());
                }
            }

            m.delete(DeleteMode.MoveToDeletedItems);
        }
    }

    private void sendErrorAndHelp(EmailMessage m) throws Exception{
        if (mailOff) return;
        sendHelp(m, "CREAM FEHLER - Konnte Betreff nicht verstehen:  " + m.getSubject());
    }

    public static List<String> getAllUsersEmails() {
        List<String> allCreamUsers = ENConfiguration.getConfig().getUsers().stream().map(CreamUserData::getEmail).collect(Collectors.toList());
        return allCreamUsers;
    }

    private void forwardLinkMail(EmailMessage m, String subjectLinkMail, String mailsRecipientsString) throws Exception {
        // remove AW: and WE: from the very beginning of the mail
        // find CREAM user (eg BEL) from FROM
        // put BEL: Date at the very beginning of newSubject
        String mailFrom = m.getFrom().getAddress();
        log.debug("mail to forward comes from: '" + mailFrom+"'");
        String shortName = AbstractConfiguration.getConfig().getShortName(mailFrom);
        if(shortName == null) {
            shortName = "From: " + mailFrom;
            log.debug("NO cream user, but from somebody else: " + shortName);
        } else {
            log.debug("resolved cream user: " + shortName);
        }

        String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        String oldSubject = m.getSubject();
        if(subjectLinkMail != null) { // then delete the mail adress from subject
            //recipients.add(subjectLinkMail);
            oldSubject = oldSubject.replaceFirst(subjectLinkMail.replace(".", "\\."), "");
        }

        String newSubject = shortName + " " + date + " --> " + oldSubject.trim() + " LINK_TO: " +mailsRecipientsString;
        newSubject = newSubject.replace("@", "$AT$");

        newSubject += "   @"+ AbstractConfiguration.getConfig().getCreamNotebooks().getInboxNotebook();
        //System.out.println("old subject: " + oldSubject);
        //System.out.println("new subject: " + newSubject);
        //System.out.println("---");
        m.setSubject(newSubject);
        m.update(ConflictResolutionMode.AlwaysOverwrite);
        m.forward(null, new EmailAddress(emailEvernote));
    }

    private void forwardToManualAdresses(EmailMessage m) throws Exception {
        // TODO
        //System.out.println("ADRESSE");
        String s = m.getSubject();
        String creamUser = "Fehler:_beim_Ermitteln";
        try {
            creamUser = AbstractConfiguration.getConfig().getShortName(m.getSender().getAddress());
        } catch (Exception e) {

        }
        s += " --> bitte Adressdaten anlegen oder hinzufügen in <b>C_"+ creamUser +"</b>";


        s = s.replace("M:", "");
        s = s.replace("m:", "");
        s = "(ADRESSE_NEU_MANUAL) von: "+ creamUser + " " + s + "   @"+ AbstractConfiguration.getConfig().getCreamNotebooks().getInboxNotebook();
        m.setSubject(s);
        m.update(ConflictResolutionMode.AlwaysOverwrite);
        //m.setSender();
        //m.set();
        m.forward(null, new EmailAddress(emailEvernote));
    }

    private void forwardToAutomaticAdresses(EmailMessage m) throws Exception {
        String marker = "(ADRESSE_NEU_AUTO)";
        automaticAdress(m, marker);
    }

    private void newContact(EmailMessage m) throws Exception {
        String marker = "(NEU_KONTAKT)";
        automaticAdress(m, marker);
    }

    private void automaticAdress(EmailMessage m, String marker) throws Exception {
        // remove AW: and WE: from the very beginning of the mail
        // find CREAM user (eg BEL) from FROM
        // put BEL: Date at the very beginning of newSubject
        String mailFrom = m.getFrom().getAddress();
        log.debug("mail to forward comes from: '" + mailFrom+"'");
        String shortName = AbstractConfiguration.getConfig().getShortName(mailFrom);
        if(shortName == null) {
            shortName = "From: " + mailFrom;
            log.debug("NO cream user, but from somebody else: " + shortName);
        } else {
            log.debug("resolved cream user: " + shortName);
        }

        String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date());

        String newSubject = marker + " " + shortName + " " + date;

        newSubject += "   @"+ AbstractConfiguration.getConfig().getCreamNotebooks().getInboxNotebook();
        //System.out.println("old subject: " + oldSubject);
        //System.out.println("new subject: " + newSubject);
        //System.out.println("---");
        m.setSubject(newSubject);
        MessageBody body = m.getBody();
        //System.out.println(body.toString());
        String plainText = body.toString();
        if(body.getBodyType() == BodyType.HTML) {
            plainText = HtmlToPlainText.convert(body.toString());
            body.setBodyType(BodyType.Text);
        }

        body.setText("START_KONTAKT\n"+plainText+"\nENDE_KONTAKT\n");

        //System.out.println(body.toString());
        m.update(ConflictResolutionMode.AlwaysOverwrite);
        m.forward(null, new EmailAddress(emailEvernote));
    }

    private void sendHelp(EmailMessage m, String errorMessage) throws Exception{
        m.setSubject(errorMessage);
        // TODO: Create html-File, that is editable for everybody - or even better: evernote help note
        m.setBody(new MessageBody (errorMessage+"<br/>"+
                "---------------------------------------------------<br/><br/><br/>"+ mailHelpHtml)
        );
        String mailFrom = m.getFrom().getAddress();
        m.update(ConflictResolutionMode.AlwaysOverwrite);
        m.forward(null, new EmailAddress(mailFrom));
    }

    public static final String mailHelpHtml = "<br/><b>_H_ilfe:</b><br/>" +
            "<b>h:</b> zu Beginn der Betreff-Zeile<br/><br/>" +
            "" +
            "<b>_Ü_bersichten neu generieren:</b><br/> " +
            "<b>ü:</b> oder <b>o:</b> zu Begin des Betreffs  (<b>o</b>verview)<br/><br/>"+
            "" +
            "<b>Adresse _a_utomatische erzeugen oder hinzufügen:</b><br/> " +
            "<b>a:</b> zu Beginn des Betreffs<br/>" +
            "Wenn z. B. aus E-Mail die Adressdaten vorliegen.<br/>" +
            "Sollten Domain oder Email schon in anderer Notiz existiert,<br/> dann wird die Adresse dort hinzugefügt, sonst neu angelegt.<br/>" +
            "Im Mailtext zeilenweise die Adressdaten und die notwendigen TODOs.<br/>" +
            "<b>Sonstigen, störenden Text entfernen.</b><br/><br/>" +
            "" +
            "<b>_n_eue Notiz bei Ablage des Kontaktes erzwingen:</b><br/>" +
            "<b>n:</b> zu Beginn des Betreffs<br/>"+
            "Es wird auf jeden Fall einen neue Adresse (also Notiz) angelegt. <br/>Alle Kontaktdaten am Anfang der Mail. Sonstigen, störenden Text entfernen.<br/> Dann ggf. todos und zwar so: <br/> <b>todo: BEL: 14.7.2018 anrufen</b><br/>Visitenkarte am Ende.<br/><br/>" +
            "" +
            "<b>Adresse _m_anuell vom Backoffice anlegen:</b><br/>" +
            "<b>m:</b> zu Beginn, dann optional Ansprechpartner (Firma).<br/>" +
            "Backoffice bekommt eine E-Mail mit Aufforderung, Adresse korrekt anzulegen. <br/>Grunddaten, z. B. Visitenkarte, werden in Evernote abgelegt.<br/>"+
            "D.h. der Mailtext enthält ggf. Visitenkarten-Bild, Kontaktdaten aus Mail und die notwendigen TODOs"+
            "<br/><br/>" +
            "" +
            "<b>Ablegen und Ver_L_inken von Mails:</b><br/> " +
            "Einfach E-Mail ins To-, Bcc- oder Cc-Feld und crm@v-und-s.de ins CC oder BCC<br/>" +
            "Sollte eine neue Notiz angelegt werden (müssen), dann liegt die in C_ALL.<br/>" +
            "Manuelle Alternativen, die Adresse oder Domain anzugeben:<br/>"+
            "Email-Adresse oder domain (z.B. bel@gmx.de oder @bosch.com) zu Beginn der Betreff-Zeile<br/>" +
            "ODER: ein 'l:' oder 'L:' vor die email mitten im text also z.B. so:  <b>L:vorname.nachname@domain.de</b><br/>" +
            "ODER: ein 'l:' oder 'L' vor die DOMAIN mitten im text also z.B. so:  <b>l:@bosch.de</b><br/>" +
            "(einfach ein L oder l mit Doppelpunkt, l: L: steht für L_inkto:<br/>" +
            "vor ne mail-adresse mittenn in der weitergeleiteten oder geschriebenen mail.<br/>"+
            "Die TITELZEILE der Notiz sollte am Ende die Domain enthalten.<br/> Z. B. <b>[bosch.de]</b> - also domain in eckigen Klammern.<br/><br/><br/>";

    private void readMails() throws Exception {
        PropertySet mailPropSet = new PropertySet(BasePropertySet.FirstClassProperties);
        mailPropSet.setRequestedBodyType(BodyType.HTML);

        Folder inbox = Folder.bind( service, WellKnownFolderName.Inbox );
        int numberEmails= inbox.getTotalCount();
        if(numberEmails == 0) {
            System.out.println("no mails...");
            return;
        } else {
            System.out.println("found " + numberEmails + " mails. Processing...");
        }

        ItemView mailView= new ItemView(numberEmails);
        mailView.setPropertySet(mailPropSet);
        FindItemsResults results = service.findItems(inbox.getId(), mailView );

        for (Iterator<Object> iterator = results.iterator(); iterator.hasNext(); ) {
            EmailMessage item =  (EmailMessage)iterator.next();
            EmailMessage emailMessage = EmailMessage.bind(service, item.getId(), mailPropSet);
            emails.add(emailMessage);
        }
    }

    private void login() throws URISyntaxException {

        /**
         * ? OAUTH (switched off)!
         * https://stackoverflow.com/questions/57009837/how-to-get-oauth2-access-token-for-ews-managed-api-in-service-daemon-application
         * Eventually, I made EWS client work as expected by grating full_access_as_app permission.
         * It is located in API permissions
         * > Add a permission
         * > APIs my organization uses
         * > Office 365 Exchange Online
         * > Application permissions
         * > full_access_as_app
         *
         * AND
         * > allow crm@v-und-s.de to send spam mails
         *
         * ANYWAY: We registered CREAM like that
         * MS App registration:
         * name: CREAM_client_key
         * value: Tx.UZm-9jnQ.pb442MVIAR7_.29D6G9yMD
         * ID: 13097d5a-6d15-4e60-96d7-fa29815cad57
         */

        service = new ExchangeService();
        //service.setTraceEnabled(true);
        ExchangeCredentials credentials = new WebCredentials(user, pw);
        service.setCredentials(credentials);
        service.setUrl(new URI(urlService));
        //System.out.println("connected to exchange mail server (crm@v-und-s.de)");
    }

    public void sentMailTo(String mailAdress, String subject, String body) {
        sentMailTo(mailAdress, subject, body, false);
    }

    public void sentMailTo(String mailAdress, String subject, String body, boolean html) {
        if (mailOff){
            System.out.println("WARNING: no mails sent...");
            return;
        }
        try {
            login();
            EmailMessage msg= new EmailMessage(service);
            msg.setSubject(subject);
            if (html) {
                MessageBody mb = new MessageBody(BodyType.HTML, body);
                msg.setBody(mb);
            } else {
                msg.setBody(MessageBody.getMessageBodyFromText(body));
            }
            msg.getToRecipients().add(mailAdress);
            msg.sendAndSaveCopy();
        } catch (Exception e) {
            log.error("Could not send mail. Error: ", e);
        } finally {
            logout();
        }
    }


    /**
     * just for testing
     * @param args
     */
    public static void main(String[] args) throws Exception {

        boolean testWithEN = false;

        if(testWithEN) {
            DeamonCreamWorker.connectToEvernoteAndReadConfig();
            new ReadAndForwardExchangeMails().doIt();
            System.out.println("FINISHED MAIL SERVER CYCLE\n\n");
        } else {
            ReadAndForwardExchangeMails mails = new ReadAndForwardExchangeMails();
            mails.login();
            mails.readMails();
            for(EmailMessage m: mails.emails) {
                mails.sendErrorAndHelp(m);
            }
            mails.logout();
        }
    }
}
