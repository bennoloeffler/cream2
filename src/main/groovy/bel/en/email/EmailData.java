package bel.en.email;

import java.io.Serializable;
import java.util.Date;


/**
 * representation of an Email
 */
public class EmailData implements Serializable {
    public static long serialver = 1; // TODO: provide right one
    String sender;
    String[] to;
    String[] cc;
    String subject;
    String text;
    Attachment[] attachments;
    Date date;

    public EmailData(String sender, String subject, String text, Date date, String[] to, String[] cc) {
        this(sender, subject, text, new Attachment[0], date, to, cc);
    }
    public EmailData(String sender, String subject, String text, Attachment[] attachments, Date date, String[] to, String[] cc) {
        //assert(attachments != null);
        this.sender = sender;
        this.subject = subject;
        this.text = text;
        this.attachments = attachments;
        this.date = date;
        this.to = to;
        this.cc = cc;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    public Attachment[] getAttachments() {
        return attachments;
    }

    public Date getDate() {
        return date;
    }

    public String[] getTo() {return to;}

    public String[] getCc() {return cc;}

    public String toString() {
        return "SENDER: " + sender + "; " + "SUBJECT: " + subject + "\nTEXT\n" + text;
    }
}

