package bel.en.exchange;

import bel.util.Util;
import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.ContactsFolder;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Contact;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.ContactSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.property.definition.PropertyDefinitionBase;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

import java.net.URI;
import java.util.*;

/**
 * Reads and writes contacts from and to Exchange-Server
 */
public class ExchangeBridge {

    private static ExchangeService service;

    PropertySet mailPropSet = new PropertySet(BasePropertySet.FirstClassProperties);



    public static class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {
        public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {
            return redirectionUrl.toLowerCase().startsWith("https://");
        }
    }

    public ExchangeBridge() throws Exception {

        // create the service
        service = new ExchangeService();
        service.setTraceEnabled(true);

        // authenticate and locate
        // dont need the domain "vunds"... And loeffler@v-und-s.de does not work.
        ExchangeCredentials credentials = new WebCredentials("crm", "c_r-M17");
        service.setCredentials(credentials);
        //service.autodiscoverUrl("loeffler@v-und-s.de", new RedirectionUrlCallback());
        service.setUrl(new URI("https://vunds.epc-cloud.de/EWS/Exchange.asmx"));

        mailPropSet.setRequestedBodyType(BodyType.Text);

    }



    public static void main(String[] args) {
        try {
            ExchangeBridge msees = new ExchangeBridge();
            //msees.sentExampleMail();
            //msees.readContacts();
            msees.readMailExample();
            //msees.readOneEmailText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sentExampleMail() throws Exception {
        EmailMessage message = new EmailMessage(service);
        message.setSubject("TestSubject");
        message.setBody(new MessageBody("TestText: " + Util.readableTime(System.currentTimeMillis())));
        message.getToRecipients().add("loeffler@v-und-s.de");
        //message.setInReplyTo("V&S CRM");
        message.setSender(new EmailAddress("CRM", "crm@v-und-s.de" ));
        message.sendAndSaveCopy();
    }

    private void readMailExample()  throws Exception {
        // do something
        Folder inbox = Folder.bind(service, WellKnownFolderName.Inbox);
        System.out.println("messages in inbox: " + inbox.getTotalCount());
        List<Map> mails = readEmails();

        for(Map mail: mails) {
            System.out.println("\nfrom 'fromAddress': " + mail.get("fromAddress") + " with 'subject': "+ mail.get("subject"));
            System.out.println("'toRecipients': " + mail.get("toRecipients"));
            System.out.println("'ccRecipients': " + mail.get("ccRecipients"));
            System.out.println("'bccRecipients': " + mail.get("bccRecipients"));

            String emailBody = (String)mail.get("emailBody");
            System.out.println("body:\n" + emailBody );
        }

    }
    private void readOneEmailText() throws Exception {
        PropertySet plainPropertySet = new PropertySet(BasePropertySet.FirstClassProperties);
        plainPropertySet.setRequestedBodyType(BodyType.Text);
        Folder folder = Folder.bind( service, WellKnownFolderName.Inbox );
        ItemView mailView= new ItemView(1);
        mailView.setPropertySet(plainPropertySet);
        FindItemsResults results = service.findItems(folder.getId(), mailView );
        ArrayList items = results.getItems();
        //Item item =  (Item)items.get(0);
        EmailMessage emailMessage =  (EmailMessage)items.get(0);
        //plainPropertySet.setRequestedBodyType(BodyType.Text);

        //Item mail = Item.bind(service, item.getId(), plainPropertySet);
        emailMessage = EmailMessage.bind(service, emailMessage.getId(), plainPropertySet);

        System.out.println("subject: " + emailMessage.getSubject().toString());
        System.out.println("body type: " + emailMessage.getBody().getBodyType().toString());
        System.out.println("body: " + emailMessage.getBody().toString());


    }


/**
 * Number of email we want to read is defined as NUMBER_EMAILS_FETCH,
 */
    public List<Map> readEmails(){
        int NUMBER_EMAILS_FETCH = 30;
        List<Map> msgDataList = new ArrayList<Map>();
        try{
            Folder folder = Folder.bind( service, WellKnownFolderName.Inbox );
            ItemView mailView= new ItemView(NUMBER_EMAILS_FETCH);
            mailView.setPropertySet(mailPropSet);
            FindItemsResults results = service.findItems(folder.getId(), mailView );

            int i =1;
            for (Iterator<Object> iterator = results.iterator(); iterator.hasNext(); ) {
                EmailMessage item =  (EmailMessage)iterator.next();
                Map messageData = new HashMap();
                messageData = readEmailItem(item.getId());
                //System.out.println("\nEmails #" + (i++ ) + ":" );
                //System.out.println("subject : " + messageData.get("subject").toString());
                //System.out.println("Sender : " + messageData.get("senderName").toString());
                msgDataList.add(messageData);
            }
        }catch (Exception e) { e.printStackTrace();}
        return msgDataList;
    }
    /**
     * Reading one email at a time. Using Item ID of the email.
     * Creating a message data map as a return value.
     */
    public Map readEmailItem(ItemId itemId){
        Map messageData = new HashMap();
        try{

            //Item itm = Item.bind(service, itemId, mailPropSet);
            //Item itm = Item.bind(service, itemId, PropertySet.FirstClassProperties);
            EmailMessage emailMessage = EmailMessage.bind(service, itemId, mailPropSet);
            //EmailMessage emailMessage = EmailMessage.bind(service, itemId);
            messageData.put("emailItemId", emailMessage.getId().toString());
            messageData.put("subject", emailMessage.getSubject().toString());
            messageData.put("fromAddress",emailMessage.getFrom().getAddress().toString());
            messageData.put("senderName",emailMessage.getSender().getName().toString());
            Date dateTimeCreated = emailMessage.getDateTimeCreated();
            messageData.put("SendDate",dateTimeCreated.toString());
            Date dateTimeRecieved = emailMessage.getDateTimeReceived();
            messageData.put("RecievedDate",dateTimeRecieved.toString());
            messageData.put("Size",emailMessage.getSize()+"");
            messageData.put("emailBody",emailMessage.getBody().toString());
            //messageData.put("emailText",emailMessage.getBody());
            messageData.put("emailBodyType",emailMessage.getBody().getBodyType().toString());


            // TO
            List<EmailAddress> recipients = emailMessage.getToRecipients().getItems();
            String allAdresses ="";
            for(EmailAddress to: recipients) {
                allAdresses  += to.getAddress() + ";";
            }
            messageData.put("toRecipients", allAdresses);

            // CC
            recipients = emailMessage.getCcRecipients().getItems();
            allAdresses ="";
            for(EmailAddress to: recipients) {
                allAdresses  += to.getAddress() + ";";
            }
            messageData.put("ccRecipients", allAdresses);

            // BCC
            recipients = emailMessage.getBccRecipients().getItems();
            allAdresses ="";
            for(EmailAddress to: recipients) {
                allAdresses  += to.getAddress() + ";";
            }
            messageData.put("bccRecipients", allAdresses);


        }catch (Exception e) {
            e.printStackTrace();
        }
        return messageData;
    }

    public void readContacts() throws Exception {


        ContactsFolder folder = ContactsFolder.bind(service, WellKnownFolderName.Contacts);
        ItemView view = new ItemView(10);
        PropertySet propertyDefinitionBases = new PropertySet(
                BasePropertySet.IdOnly,
                ContactSchema.Id,
                //ContactSchema.FileAs,
                //ContactSchema.FileAsMapping,

                ContactSchema.DisplayName,
                ContactSchema.GivenName,
                //ContactSchema.Initials,
                //ContactSchema.MiddleName,
                //ContactSchema.NickName,
                ContactSchema.CompleteName,
                ContactSchema.CompanyName,
                ContactSchema.EmailAddresses,
                ContactSchema.PhysicalAddresses,
                ContactSchema.PhoneNumbers,
                ContactSchema.AssistantName,
                ContactSchema.Birthday,
                ContactSchema.BusinessHomePage,
                /*
        ContactSchema.Children,
        ContactSchema.Companies,
        ContactSchema.ContactSource,
        ContactSchema.Department,
        ContactSchema.Generation,
        ContactSchema.ImAddresses,
        */
                ContactSchema.JobTitle,
                /*
        ContactSchema.Manager,
        ContactSchema.Mileage,
        ContactSchema.OfficeLocation,
        ContactSchema.PostalAddressIndex,
        ContactSchema.Profession,
        ContactSchema.SpouseName,
        */
                ContactSchema.Surname,
                /*
        ContactSchema.WeddingAnniversary,
        ContactSchema.HasPicture,
        ContactSchema.PhoneticFullName,
        ContactSchema.PhoneticFirstName,
        ContactSchema.PhoneticLastName,
        ContactSchema.Alias,
        */
                ContactSchema.Notes,
                /*
        ContactSchema.Photo,
        ContactSchema.UserSMIMECertificate,
        ContactSchema.MSExchangeCertificate,
        ContactSchema.DirectoryId,
        ContactSchema.ManagerMailbox,
        ContactSchema.DirectReports,
        */
                ContactSchema.EmailAddress1,
                ContactSchema.EmailAddress2,
                ContactSchema.EmailAddress3,
                /*
        ContactSchema.ImAddress1,
        ContactSchema.ImAddress2,
        ContactSchema.ImAddress3,
        ContactSchema.AssistantPhone,
        ContactSchema.BusinessFax,
        */
                ContactSchema.BusinessPhone,
                ContactSchema.BusinessPhone2,
                /*
        ContactSchema.Callback,
        ContactSchema.CarPhone,
        */
                ContactSchema.CompanyMainPhone,
                /*
        ContactSchema.HomeFax,
        ContactSchema.HomePhone,
        ContactSchema.HomePhone2,
        ContactSchema.Isdn,
        */
                ContactSchema.MobilePhone,
        /*
        ContactSchema.OtherFax,
        */
                ContactSchema.OtherTelephone,
        /*
        ContactSchema.Pager,
        */
                ContactSchema.PrimaryPhone,
                //ContactSchema.RadioPhone,
                //ContactSchema.Telex,
                //ContactSchema.TtyTddPhone,
                ContactSchema.BusinessAddressStreet,
                ContactSchema.BusinessAddressCity,
                ContactSchema.BusinessAddressState,
                ContactSchema.BusinessAddressCountryOrRegion,
                ContactSchema.BusinessAddressPostalCode,
                ContactSchema.HomeAddressStreet,
                ContactSchema.HomeAddressCity,
                ContactSchema.HomeAddressState,
                ContactSchema.HomeAddressCountryOrRegion,
                ContactSchema.HomeAddressPostalCode,
                ContactSchema.OtherAddressStreet,
                ContactSchema.OtherAddressCity,
                ContactSchema.OtherAddressState,
                ContactSchema.OtherAddressCountryOrRegion,
                ContactSchema.OtherAddressPostalCode
        );
        view.setPropertySet(propertyDefinitionBases);

        FindItemsResults<Item> findResults = service.findItems(folder.getId(), view);
        service.loadPropertiesForItems(findResults, propertyDefinitionBases);

        //FindItemsResults<Item> findResults = service.findItems(folder.getId(), view);
        for (Item item : findResults.getItems()) {
            Contact c = (Contact)item;
            Collection<PropertyDefinitionBase> loadedPropertyDefinitions = c.getLoadedPropertyDefinitions();
            for (Iterator<PropertyDefinitionBase> iterator = loadedPropertyDefinitions.iterator(); iterator.hasNext(); ) {
                PropertyDefinitionBase next = iterator.next();
                System.out.println("TYPE: " + next.getType().toString());
                //getObjectFromPropertyDefinition(ContactSchema.DisplayName)
                System.out.println("Name: " + next.getPrintableName() + "      Value: " + next.toString());

            }

            System.out.println(c.getFileAsMapping());
            System.out.println(c.getDisplayName());
            System.out.println(c.getGivenName());
            System.out.println(c.getInitials());
            System.out.println(c.getMiddleName());
            System.out.println(c.getNickName());
            System.out.println(c.getCompleteName());
            System.out.println(c.getCompanyName());
            System.out.println(c.getEmailAddresses());
            System.out.println(c.getPhysicalAddresses());
            System.out.println(c.getPhoneNumbers());
            System.out.println(c.getAssistantName());
            System.out.println(c.getBirthday());
            System.out.println(c.getBusinessHomePage());
            System.out.println(c.getChildren());
            System.out.println(c.getCompanies());
            System.out.println(c.getContactSource());
            System.out.println(c.getDepartment());
            System.out.println(c.getGeneration());
            System.out.println(c.getImAddresses());
            System.out.println(c.getJobTitle());
            System.out.println(c.getManager());
            System.out.println(c.getMileage());
            System.out.println(c.getOfficeLocation());
            System.out.println(c.getPostalAddressIndex());
            System.out.println(c.getProfession());
            System.out.println(c.getSpouseName());
            System.out.println(c.getSurname());
            System.out.println(c.getWeddingAnniversary());
            System.out.println(c.getHasPicture());
            System.out.println(c.getPhoneticFullName());
            System.out.println(c.getPhoneticFirstName());
            System.out.println(c.getPhoneticLastName());
            System.out.println(c.getAlias());
            System.out.println(c.getNotes());
            //System.out.println(c.get.Photo());
            System.out.println(c.getUserSMIMECertificate());
            System.out.println(c.getMSExchangeCertificate());
            System.out.println(c.getDirectoryId());
            System.out.println(c.getManagerMailbox());
            System.out.println(c.getDirectReports());
            System.out.println(c.getEmailAddresses()); //adress 1 2 und 3
            System.out.println(c.getImAddresses()); // adress 1  2 und 3
            //System.out.println(c.getAssistantPhone());
            //System.out.println(c.getBusinessFax());
            //System.out.println(c.getBusinessPhone());
            //System.out.println(c.getBusinessPhone2());
            //System.out.println(c.getCallback());
            //System.out.println(c.getCarPhone());
            //System.out.println(c.getCompanyMainPhone());
            //System.out.println(c.getHomeFax());
            //System.out.println(c.getHomePhone());
            //System.out.println(c.getHomePhone2());
            //System.out.println(c.getIsdn());
            //System.out.println(c.getMobilePhone());
            //System.out.println(c.getOtherFax());
            //System.out.println(c.getOtherTelephone());
            //System.out.println(c.getPager());
            //System.out.println(c.getPrimaryPhone());
            //System.out.println(c.getRadioPhone());
            //System.out.println(c.getTelex());
            //System.out.println(c.getTtyTddPhone());
            //System.out.println(c.getBusinessAddressStreet());
            //System.out.println(c.getBusinessAddressCity());
            //System.out.println(c.getBusinessAddressState());
            //System.out.println(c.getBusinessAddressCountryOrRegion());
            //System.out.println(c.getBusinessAddressPostalCode());
            //System.out.println(c.getHomeAddressStreet());
            //System.out.println(c.getHomeAddressCity());
            //System.out.println(c.getHomeAddressState());
            //System.out.println(c.getHomeAddressCountryOrRegion());
            //System.out.println(c.getHomeAddressPostalCode());
            //System.out.println(c.getOtherAddressStreet());
            //System.out.println(c.getOtherAddressCity());
            //System.out.println(c.getOtherAddressState());
            //System.out.println(c.getOtherAddressCountryOrRegion());
            //System.out.println(c.getOtherAddressPostalCode());

            /*
            System.out.println("givenName: " + c.getGivenName());
            System.out.println("surName: " + c.getSurname());
            System.out.println("email: " + c.getEmailAddresses());
            System.out.println("job: " + c.getJobTitle());
            System.out.println("comp name: " + c.getCompanyName());
            System.out.println("adress Office Loc : " + c.getOfficeLocation());
            System.out.println("notes : " + c.getNotes());

            System.out.println("DISPLAY NAME : " + c.getObjectFromPropertyDefinition(ContactSchema.DisplayName));

            System.out.println("id:" + item.getId());
            System.out.println(item.getCategories());
            System.out.println("sub==========" + item.getSubject());
            */
        }

    }
}
