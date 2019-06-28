package bel.en.email;

import bel.cream2.deamon.DeamonCreamWorker;
import bel.en.data.AbstractConfiguration;
import bel.en.data.CreamUserData;
import bel.en.evernote.ENConfiguration;
import bel.util.ENMLToPlainText;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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

    //String user = "crm";
    //String pw = "Hup71467";
    String user = "crm@v-und-s.de";
    String pw = "Hup71467";
    //String urlService = "https://vunds.epc-cloud.de/EWS/Exchange.asmx";
    //String urlService = "https://mail.v-und-s.de/EWS/Exchange.asmx";
    String urlService = "https://outlook.office365.com/ews/exchange.asmx";
    String emailCRM = "crm@v-und-s.de";
    String emailEvernote = "bennoloeffler.173c3b6@m.evernote.com";

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
        List<String> allCreamUsers = ENConfiguration.getConfig().getUsers().stream().map(CreamUserData::getEmail).collect(Collectors.toList());
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

            List<String> recipients = m.getToRecipients().getItems().stream().map(EmailAddress::getAddress).collect(Collectors.toList());
            recipients.addAll(m.getCcRecipients().getItems().stream().map(EmailAddress::getAddress).collect(Collectors.toList()));
            String subjectLinkMail = Util.extractEmailStartOfDocument(m.getSubject());
            if(subjectLinkMail != null) {
                recipients.add(subjectLinkMail);
            }

            String mailInMailBody = Util.extractEmailStartOfDocument(m.getBody().toString()); // maybe very first element
            if(mailInMailBody == null) {
                mailInMailBody = Util.extractEmailLinkTo(m.getBody().toString()); // otherwise, check for l: in the text
            }
            if(mailInMailBody != null) {
                recipients.add(mailInMailBody);
            }

            recipients.add(m.getFrom().getAddress());
            recipients.removeAll(allCreamUsers);
            String mailsRecipientsString = recipients.stream().collect(Collectors.joining(", "));
            //System.out.println("mailsString: '" + mailsString+"'");

            log.debug("Found mail from "+m.getFrom()+" with subject: " + m.getSubject());
            if (m.getSubject().startsWith("*h") || m.getSubject().startsWith("*H")) {
                log.debug("RECOGNIZED: sending help");
                sendHelp(m, "Sie haben mich um Hilfe gebeten...");
            } else if(m.getSubject().startsWith("*a") || m.getSubject().startsWith("*A")) {
                log.debug("RECOGNIZED: adresse");
                forwardToAdresses(m);
            } else if(m.getSubject().startsWith("*n") || m.getSubject().startsWith("*N")) {
                log.debug("RECOGNIZED: new Contact");
                newContact(m);
            } else if(m.getSubject().startsWith("*ü") || m.getSubject().startsWith("*Ü") ||
                    m.getSubject().startsWith("*o") || m.getSubject().startsWith("*O")) {
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
        sendHelp(m, "FEHLER - Konnte Betreff nicht verstehen:  " + m.getSubject());
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

    private void forwardToAdresses(EmailMessage m) throws Exception {
        // TODO
        //System.out.println("ADRESSE");
        String s = m.getSubject();
        s = s.replace("*ADDRESS", "");
        s = s.replace("*ADRESSE", "");
        s = s.replace("*ADR", "");
        s = s.replace("*A", "");
        s = s.replace("*a", "");
        s = "(ADRESSE_NEU)  " + s + "   @"+ AbstractConfiguration.getConfig().getCreamNotebooks().getInboxNotebook();
        m.setSubject(s);
        m.update(ConflictResolutionMode.AlwaysOverwrite);
        //m.setSender();
        //m.set();
        m.forward(null, new EmailAddress(emailEvernote));
    }

    private void newContact(EmailMessage m) throws Exception {
        //String s = "(KONTAKT_NEU) "+m.getSender()+" @"+ AbstractConfiguration.getConfig().getCreamNotebooks().getInboxNotebook();
        //m.setSubject(s);
        //m.update(ConflictResolutionMode.AlwaysOverwrite);
        //m.forward(null, new EmailAddress(emailEvernote));

        //----

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

        String newSubject = "(NEU_KONTAKT) " + shortName + " " + date;

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
        m.setBody(new MessageBody(errorMessage+"<br/>"+
                "---------------------------------------------------<br/><br/><br/>"+ mailHelpHtml
                )

        );



        String mailFrom = m.getFrom().getAddress();
        m.update(ConflictResolutionMode.AlwaysOverwrite);
        m.forward(null, new EmailAddress(mailFrom));
    }

    public static final String mailHelpHtml = "<h3>Hilfe:</h3>" +
            "<b>*h *H</b> zu Beginn der Betreff-Zeile<br/><br/><br/>" +
            "" +
            "<h3>Übersichten generieren:</h3>" +
            "<b>*ü *Ü *o oder *O</b> zu Begin des Betreffs<br/><br/><br/>"+
            "" +
            "<h3>Neuen Kontakt eintragen:</h3>" +
            "<b>*n *N *neu *Neu</b> zu Begin des Betreffs<br/>"+
            "Alle Kontaktdaten am Anfang der Mail. VORSICHT: HTML-MAIL! Dann ggf. todos und zwar so: <b>todo: BEL: 14.7.2018 anrufen</b> Visitenkarte am Ende.<br/><br/><br/>" +
            "" +
            "<h3>Adresse für Anna:</h3>" +
            "*a *A *anna *Anna zu Beginn, dann optional Ansprechpartner (Firma).<br/>" +
            "Im Mailtext zeilenweise email oder tel des Kontaktes und optional Adresse, tel, Visitenkarten-Bild in der Mail.<br/><br/><br/>" +
            "" +
            "<h3>Ablage der Mail:</h3>" +
            "Email-Adresse zu Beginn der Betreff-Zeile oder ins To-, Bcc- oder Cc-Feld.<br/>" +
            "ODER: ein 'l:' vor die email mitten im text also z.B. so:  l:vorname.nachname@domain.de<br/>" +
            "ODER: ein 'l:' vor die DOMAIN mitten im text also z.B. so:  l:@bosch.de<br/>" +
            "(einfach ein kleines l mit Doppelpunkt, l: steht für linkto:<br/>" +
            "vor ne mail-adresse mittenn in der weitergeleiteten oder geschriebenen mail.<br/>"+
            "TITELZEILE der Notiz sollte am Ende [firma.de] enthalten. Also domain in eckigen Klammern.<br/><br/><br/>";

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
        // setup connection
        DeamonCreamWorker.connectToEvernoteAndReadConfig();

        // NOT NEEDED to sync...
        //SyncHandler.init(ENConnection.get(), new NoteStoreLocal(ENConfiguration.getConfig()));
        //SyncHandler.get().sync(null);
        new ReadAndForwardExchangeMails().doIt();

        System.out.println("FINISHED MAIL SERVER CYCLE\n\n");
    }
}
