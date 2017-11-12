package bel.en.data;

import bel.en.evernote.ENCREAMNotebooks;

import java.util.List;
import java.util.Map;

/**
 * just for testing in order to be able to get the value from somewhere else...
 */
public interface Configuration {

    List<CreamUserData> getUsers();
    CreamUserData getCurrentUser();
    List<CreamUserData> getAdmins();
    String getShortName(String emailAdress);
    String getEmail(String shortName);
    CreamUserData getUser(String shortName);


    Map<String, CreamAttributeDescription> getFirmaAttributesDescription();
    Map<Integer, CreamAttributeDescription> getFirmaAttributesOrderedDescription();
    Map<String, CreamAttributeDescription> getFirmaTagsDescription();
    Map<Integer, CreamAttributeDescription> getFirmaTagsOrderedDescription();

    Map<String, CreamAttributeDescription> getPersonAttributesDescription();
    Map<Integer, CreamAttributeDescription> getPersonAttributesOrderedDescription();
    Map<String, CreamAttributeDescription> getPersonTagsDescription();
    Map<Integer, CreamAttributeDescription>  getPersonTagsOrderedDescription();

    ENCREAMNotebooks getCreamNotebooks();

    List<String> getAllFirmaTagsAsList();
    List<String> getAllPersonTagsAsList();
}
