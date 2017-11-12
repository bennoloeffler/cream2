package bel.en.email;


import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.util.ByteArrayDataSource;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;


/**
 *
 */
public class EmailSender implements EmailDrain {
    private static final Logger LOGGER = Logger.getLogger(EmailSender.class.getName());
    private static final boolean debug = "on".equals(System.getProperty("DEBUG"));

    private String host;
    private String user;
    private String password;
    private String sender;

    public EmailSender(String host, String user, String password, String sender) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.sender = sender;
    }

    public void answerToSender(String sendTo, String subject) {
        send(sendTo, subject, "");
    }

    public void answerToSender(String sendTo, EmailData email) {
        send(sendTo, email.getSubject(), email.getText());
    }
    public void send(String sendTo, String subject, String text) {
        answerToSender(sendTo, subject, text, null);
    }

    public void send(String sendTo, String subject, String text, Attachment[] attachements) {
        answerToSender(sendTo, subject, text, null, attachements);
    }
    public void answerToSender(String sendTo, String subject, String text, File f) {
        answerToSender(sendTo, subject, text, f, null);
    }

    public void answerToSender(String sendTo, String subject, String text, File f, Attachment[] attachements) {
        try {
            Properties props = System.getProperties();
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.auth", "true");
            //props.setProperty("mail.user", "crm");
            //props.setProperty("mail.password", "c_r-M17");
            //props.setProperty("mail.smtp.starttls.enable","true");
            //props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.host", host);

            Authenticator auth = new Authenticator() {

                private PasswordAuthentication pwdAuth = new PasswordAuthentication(user, password);

                protected PasswordAuthentication getPasswordAuthentication() {
                    return pwdAuth;
                }
            };


            // Get a Session object
            Session session = Session.getDefaultInstance(props,auth);
            //Session session = Session.getInstance(props, null);

            // construct the message
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(sender));


            //LOGGER.info("sending to: >>" + to + "<<");
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(sendTo, false));

            msg.setSubject(subject);


            msg.setHeader("X-Mailer", "MOA");
            msg.setSentDate(new Date());
            if(f != null || attachements != null) { // if there are some attachments to be added, we cannot just use a simple message...

                MimeMultipart wholeMessage = new MimeMultipart();
                MimeBodyPart mimeText = new MimeBodyPart();
                mimeText.setText(text);
                wholeMessage.addBodyPart(mimeText);
                if(f != null){
                    File[] all = f.listFiles(new FileFilter() {
                        public boolean accept(File pathname) {
                            return pathname.isFile();
                        }
                    });
                    if (all != null) {
                        for (int i = 0; i < all.length; i++) {
                            File file = all[i];
                            MimeBodyPart attachment = new MimeBodyPart();
                            FileDataSource fds = new FileDataSource(file.getAbsolutePath());
                            attachment.setDataHandler(new DataHandler(fds));
                            attachment.setFileName(file.getName());
                            wholeMessage.addBodyPart(attachment);
                        }
                    }
                }
                if (attachements != null){
                    for (int i = 0; i < attachements.length; i++) {
                        MimeBodyPart attachment = new MimeBodyPart();
                        attachment.setDisposition(Part.ATTACHMENT);
                        ByteArrayDataSource ds = new ByteArrayDataSource(attachements[i].getData(), "application/x-any");
                        attachment.setDataHandler(new DataHandler(ds));
                        attachment.setFileName(attachements[i].getFileName());
                        wholeMessage.addBodyPart(attachment);
                    }
                }
                msg.setContent(wholeMessage);

            } else {
                msg.setText(text);
            }
            // send the thing off
            Transport.send(msg);
            /*
            msg.saveChanges();
            Transport transport = session.getTransport("smtp");
            transport.connect(host, "", "");
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();
*/
            //System.out.println("\nMail was sent successfully.");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        EmailSender ems = new EmailSender("vunds.epc-cloud.de", "vunds\\crm", "c_r-M17", "crm@v-und-s.de");
        ems.send("loeffler@v-und-s.de", "Subject: testmail", "test text");
        ems = new EmailSender("vunds.epc-cloud.de", "crm", "c_r-M17", "crm@v-und-s.de");
        ems.send("loeffler@v-und-s.de", "Subject: testmail", "test text");    }
}

