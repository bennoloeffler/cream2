package bel.en.email;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * read from exchange server...
 */
class EmailReaderExchange implements EmailSource {
    private final int SIZE_OF_CHUNK_TO_READ = 5;

    private static ExchangeService service;


    public EmailReaderExchange(String urlService, String user, String pw) throws Exception {
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
    }

    public int hasMessages() throws Exception {
        Folder inbox = Folder.bind( service, WellKnownFolderName.Inbox );
        int numberEmails= inbox.getTotalCount();
        return numberEmails;
    }

    public EmailData[] readNextMessages() throws Exception {
        return readNextMessages(BodyType.Text);
    }

    public EmailData[] readNextMessages(BodyType bodyType) throws Exception {
        List<EmailData> msgDataList = new ArrayList<>();
            PropertySet mailPropSet = new PropertySet(BasePropertySet.FirstClassProperties);
            mailPropSet.setRequestedBodyType(BodyType.HTML);


            Folder inbox = Folder.bind( service, WellKnownFolderName.Inbox );
            int numberEmails= inbox.getTotalCount();

            ItemView mailView= new ItemView(numberEmails);
            mailView.setPropertySet(mailPropSet);
            FindItemsResults results = service.findItems(inbox.getId(), mailView );

            for (Iterator<Object> iterator = results.iterator(); iterator.hasNext(); ) {
                EmailMessage item =  (EmailMessage)iterator.next();
                EmailMessage emailMessage = EmailMessage.bind(service, item.getId(), mailPropSet);

                // TO
                List<EmailAddress> recipients = emailMessage.getToRecipients().getItems();
                ArrayList<String> toA = new ArrayList<>();
                for(EmailAddress to: recipients) {
                    toA.add(to.getAddress());
                }

                // CC
                recipients = emailMessage.getCcRecipients().getItems();
                ArrayList<String> ccA = new ArrayList<>();
                for(EmailAddress cc: recipients) {
                    ccA.add(cc.getAddress());
                }

                EmailData data = new EmailData(
                        emailMessage.getFrom().getAddress().toString(),
                        emailMessage.getSubject().toString(),
                        emailMessage.getBody().toString(),
                        emailMessage.getDateTimeReceived(),
                        (String[])(toA.toArray(new String[toA.size()])),
                        (String[])(ccA.toArray(new String[ccA.size()]))
                );
                msgDataList.add(data);
                emailMessage.delete(DeleteMode.MoveToDeletedItems);
            }
        return (EmailData[]) msgDataList.toArray(new EmailData[msgDataList.size()]);
    }

}
