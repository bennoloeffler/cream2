package bel.en.email;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

import java.net.URI;

/**
 * Interface to send mail via Exchange Server
 */
public class EmailSenderExchange implements EmailDrain {



    private static ExchangeService service;
    private final String emailFrom;


    public EmailSenderExchange(String urlService, String user, String pw, String emailFrom) throws Exception {
        // create the service
        service = new ExchangeService();
        //service.setTraceEnabled(true);

        // authenticate and locate
        // dont need the domain "vunds"... And loeffler@v-und-s.de does not work.
        ExchangeCredentials credentials = new WebCredentials(user, pw);
        service.setCredentials(credentials);
        // that does not work. So redirection does not work...
        //service.autodiscoverUrl("loeffler@v-und-s.de", new RedirectionUrlCallback());
        service.setUrl(new URI(urlService));
        this.emailFrom = emailFrom;
    }


    @Override
    public void send(String to, String subject, String text) throws Exception {
        EmailMessage message = new EmailMessage(service);
        message.setSubject(subject);
        message.setBody(new MessageBody(text));
        message.getToRecipients().add(to);
        message.setSender(new EmailAddress(emailFrom));
        message.sendAndSaveCopy();
    }

}
