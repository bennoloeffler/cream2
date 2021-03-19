package bel.cream2.deamon


import bel.en.MainGUI
import bel.en.data.AbstractConfiguration
import bel.en.email.ReadAndForwardExchangeMails
import bel.en.evernote.*
import bel.en.localstore.NoteStoreLocal
import bel.en.localstore.SyncHandler
import bel.util.AdressMagic
import bel.util.RegexUtils
import bel.util.Util
import com.evernote.auth.EvernoteService
import com.evernote.edam.error.EDAMErrorCode
import com.evernote.edam.error.EDAMSystemException
import com.evernote.edam.type.Note
import com.evernote.thrift.transport.TTransportException
import groovy.util.logging.Log4j2
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceRequestException
import org.apache.commons.lang3.StringUtils
import org.codehaus.groovy.runtime.StackTraceUtils

import java.text.SimpleDateFormat

/**
 * This is used by syncer, when CREAM is in Deamon-Mode.
 * Method 'doIt' is called from within the background sync thread.
 * 0.) Show to the user, that it is in Deamon-Mode (sync often...)
 * TODO: 2.) starts the "ABO-Mode" while syncing Notes (if there is a diff - send the html-diff to the user - ONCE PER DAY)
 * 3.) reads emails, findes notes and creates links - or sends error message to user
 */
@Log4j2
//@CompileStatic
class DeamonCreamWorker {

    static boolean testMode = false

    static ENSharedNotebook inboxNotebook
    static ENSharedNotebook mailNotebook
    static ENSharedNotebook defaultNotebook
    static OverviewCreator overviewCreator


    static def connectMailNotebooks() {
        inboxNotebook = new ENSharedNotebook(ENConnection.get(), AbstractConfiguration.config.creamNotebooks.inboxNotebook)
        mailNotebook = new ENSharedNotebook(ENConnection.get(), AbstractConfiguration.getConfig().getCreamNotebooks().getMailsNotebook())
        defaultNotebook = new ENSharedNotebook(ENConnection.get(), AbstractConfiguration.getConfig().getCreamNotebooks().getDefaultNotebook())
    }


    static def processEvernoteInbox() {
        List<Note> notesInInbox = inboxNotebook.allNotes
        if(notesInInbox) {
            log.debug("found ${notesInInbox.size()} notes in INBOX")
        }
        notesInInbox.forEach { noteInInbox ->

            // move to permanent mail store and replace $AT$ with @ again
            noteInInbox.title = noteInInbox.title.replace("\$AT\$", "@")
            noteInInbox.title = noteInInbox.title.replace("RE:", "")
            noteInInbox.title = noteInInbox.title.replace("FW:", "")
            noteInInbox.title = noteInInbox.title.replace("AW:", "")
            noteInInbox.title = noteInInbox.title.replace("WG:", "").trim()
            noteInInbox.notebookGuid = mailNotebook.getSharedNotebook().notebookGuid
            //inboxNotebook.updateNote(noteInInbox)
            log.debug("next note in INBOX: " + noteInInbox.getTitle())

            if (noteInInbox.title.contains("LINK_TO:")) {

                // extract mailAdresses to attach to
                String[] strings = noteInInbox.title.split("LINK_TO:")
                def linkName = strings[0].trim()
                String[] mailAdresses = []
                if (strings.length > 1) {
                    mailAdresses = strings[1].split(",")

                    log.debug("found mailAddresses to link to: " + mailAdresses)
                } else {
                    log.debug("found NO mailAddresses to link to.")
                    noteInInbox.title = "(NICHT ZUGEORDNET) - keine E-Mail-Adresse -  " + noteInInbox.title
                    mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)
                    return

                }
                // notwendig?
                mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)

                mailNotebook.loadNoteRessources((Note)noteInInbox) // because inboxNotebook is not found down in SyncHandler.get().loadRessources(note);
                def raw = ENHelper.getRawText(noteInInbox)

                def treffer = []
                mailAdresses.each { String mailAddr ->
                    mailAddr = mailAddr.trim()
                    String evernoteLink = inboxNotebook.getInternalLinkTo(noteInInbox, linkName + ", mail: " + mailAddr)
                    List<Note> listOfNotes = SyncHandler.get().allNotes
                    listOfNotes.forEach { localNote ->
                        //if (localNote.content.contains(mailAddr) || localNote.content.contains(mailAddr.toLowerCase())) {
                        //if(localNote.title.contains("metall-freaks.de")){
                        //    println "treffer"
                        //}
                        if (StringUtils.containsIgnoreCase(localNote.content, mailAddr) || RegexUtils.domainFits(localNote.title, mailAddr)) {
                            log.debug("going to link to this note: " + localNote.title)
                            treffer << localNote
                            localNote = inboxNotebook.getSharedNoteStore().getNote(ENConnection.get().businessAuthToken, localNote.guid, true, false, false, false)
                            ENHelper.addHistoryEntry(localNote, evernoteLink)
                            def regexTODO = /(?m)^(todo|Todo|TODO):.*$/ //complete line starting with todo
                            def matcher = raw =~ regexTODO

                            (0..<matcher.count).each {
                                String todoStr = matcher[it][0]
                                todoStr = todoStr.replaceAll("^(todo|Todo|TODO):", " ")
                                ENHelper.addTodoEntry(localNote, todoStr)
                            }
                            // save directly - before sync...
                            SyncHandler.get().updateNoteImmediately(localNote)
                        }
                    }
                }



                if (!treffer) {

                    def shortName = noteInInbox.title[0..2]
                    String mail = getUserMail(shortName)

                    // neue notiz in C_ALL
                    def newMailToCreateAdressFrom = mailAdresses[0].trim()
                    def mailParts = newMailToCreateAdressFrom.split("@")
                    def mailName = mailParts[0]
                    def mailDomain = mailParts[1]
                    Note n = new Note()
                    n.title = "(NICHT ZUORDENBAR -> NEU ANGELEGT) $mailName (Firma?) [$mailDomain]"
                    n.content = ENHelper.createNoteFromEmailText("")
                    ENHelper.addTodoEntry(n, "$shortName: neue Notiz checken (vielleicht löchen?)")

                    // add TODOs from email
                    def regexTODO = /(?m)^(todo|Todo|TODO):.*$/ //complete line starting with todo
                    def matcher = raw =~ regexTODO
                    (0..<matcher.count).each {
                        String todoStr = matcher[it][0]
                        todoStr = todoStr.replaceAll("^(todo|Todo|TODO):", " ")
                        ENHelper.addTodoEntry(n, todoStr)
                    }

                    // move original to mails-notebook
                    //inboxNotebook.loadNoteRessources(noteInInbox) // because inboxNotebook is not found down in SyncHandler.get().loadRessources(note);
                    noteInInbox.notebookGuid = mailNotebook.getSharedNotebook().notebookGuid
                    mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)

                    // create link from the new to original
                    String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date())
                    String evernoteLink = mailNotebook.getInternalLinkTo(noteInInbox, "$shortName: $date erste Email an " + newMailToCreateAdressFrom)
                    ENHelper.addHistoryEntry(n, evernoteLink)
                    defaultNotebook.createNote(n)

                    log.info("neue Adresse in C_ALL erzeugt. Aus dieser Notiz (jetzt in Mail-Ablage): " + noteInInbox.getTitle())
                    new ReadAndForwardExchangeMails().sentMailTo(mail, "CREAM - ERFOLG: Notiz AUTOMATISCH in C_ALL erzeugt...", "Titel der Notiz:\n$mailName (Firma?) [$mailDomain]", false)


                } else {
                    def htmlBodyString = noteInInbox.getTitle()+ " zugeordnet. Und zwar dort: <br/><br/>"
                    treffer.each { Note note ->
                        ENSharedNotebook notebook = SyncHandler.get().getNotebook(note)
                        def evernoteLink = notebook.getInternalLinkTo(note, note.title)
                        htmlBodyString += "Notizen-Titel: " + evernoteLink +"<br/>"
                    }
                    try {
                        def shortName = noteInInbox.title[0..2]
                        String mail = getUserMail(shortName)
                        new ReadAndForwardExchangeMails().sentMailTo(mail, "CREAM - ERFOLG: zugeordnet...", htmlBodyString, true)
                    } catch (Exception ex) {
                        //log.info("sender could not be resolved as CREAM-User: " + noteInInbox.getTitle())
                        log.info("EXTERNE EMAIL erfolgreich zugeordnet: " + noteInInbox.getTitle())
                    }
                }
            } else if (noteInInbox.title.contains("(NEU_KONTAKT)")) { // try to create a new entry...
                inboxNotebook.loadNoteRessources(noteInInbox) // because inboxNotebook is not found down in SyncHandler.get().loadRessources(note);
                def raw = ENHelper.getRawText(noteInInbox)
                String[] split = raw.split("(START_KONTAKT|ENDE_KONTAKT)")
                raw = split[1]
                //def regexTODO = /(?m)^(todo|Todo|TODO):.*$/ //complete line starting with todo
                def regexTODO = /(?m)^(todo|Todo|TODO):.*$/ //complete line starting with todo
                def matcher = raw =~ regexTODO

                def am = new AdressMagic(raw)
                def adr = null
                def newHeadline = null
                //def domain = am.email.split("@")[1]
                am.with {
                    newHeadline = "$titleInName$christianNames $surName ($company) [$www]"
                    adr = """<div><br/></div>
                                 <div><b>${ENHelper.escapeHTML(titleInName + christianNames + " " + surName)}</b></div>
                                 <div>${ENHelper.escapeHTML(functionDepartment)}</div>   
                                 <div>${ENHelper.escapeHTML(mobile)}</div>   
                                 <div>${ENHelper.escapeHTML(phone)}</div>   
                                 <div>${ENHelper.escapeHTML(email)}</div>   
                                 <div><br/></div>   

                                 <div><b>${ENHelper.escapeHTML(company)}</b></div>   
                                 <div>${ENHelper.escapeHTML(streetAndNr)}</div>   
                                 <div>${ENHelper.escapeHTML(zipCode + " " + town)}</div>   
                              """
                }

                //def newHeadline = am.christianNames + " " + am.surName + " ["+am.www+"]";
                def newBody = ENHelper.createNoteFromEmailText(adr)
                def shortName = noteInInbox.title[14..16]
                String mail = getUserMail(shortName)


                def usersNotebookGuid = defaultNotebook // in case it does not exist...
                // find the users notebook
                try {
                    usersNotebookGuid = SyncHandler.get().getNotebookByName("C_"+shortName)
                } catch (Exception e) {
                    log.warn("did not find notebook for user: " + shortName)
                }

                // create the note
                Note n = new Note()
                n.title = newHeadline.trim()
                n.content = newBody
                n = usersNotebookGuid.createNote(n)

                // finally move the original to docs in case it needs to be fixed manually
                noteInInbox.title = noteInInbox.title.replace("NEU_KONTAKT", "Kontaktdaten, original")
                noteInInbox.title += (" " + am.email)
                noteInInbox.title = noteInInbox.title.trim()
                noteInInbox.notebookGuid = mailNotebook.getSharedNotebook().notebookGuid
                mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)

                // create a link to the new one, that points to the original and a todo to check new entry
                //String evernoteLink = inboxNotebook.getInternalLinkTo(noteInInbox, linkName + ", mail: " + mailAddr)
                String evernoteLink = mailNotebook.getInternalLinkTo(noteInInbox, "Original-Adressdaten für " + am.email)

                ENHelper.addHistoryEntry(n, evernoteLink)

                (0..<matcher.count).each {
                    String todoStr = matcher[it][0]
                    todoStr = todoStr.replaceAll("^(todo|Todo|TODO):", " ")
                    ENHelper.addTodoEntry(n, todoStr)
                }

                //String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date())
                ENHelper.addTodoEntry(n, "ANL: $shortName: 1.1.00 NEUEN KONTAKT PRÜFEN")

                // finally, create a Link in case there is a duplicate hit for the email
                List<Note> doublettes = []
                SyncHandler.get().allNotes.forEach { localNote ->

                    am.allMails.each { mailAdress ->
                        if(StringUtils.containsIgnoreCase(localNote.content, mailAdress)) {
                            doublettes << localNote
                        }
                    }
                    if(StringUtils.containsIgnoreCase(localNote.title, am.www)) {
                        doublettes << localNote
                    }
                }
                doublettes.toUnique().each { localNote ->
                    ENSharedNotebook notebook = SyncHandler.get().getNotebook(localNote)
                    evernoteLink = notebook.getInternalLinkTo(localNote, "vermutlich Doublette... andere, alte Notiz mit mail: $am.allMails bzw. domain: $am.www")
                    log.debug("going to create doublette link to: " + localNote.title)
                    ENHelper.addHistoryEntry(n, evernoteLink)
                }
                SyncHandler.get().updateNoteImmediately(n)

                ENSharedNotebook notebook = SyncHandler.get().getNotebook(n)
                evernoteLink = notebook.getInternalLinkTo(n, n.title)
                def htmlBodyString = "Notizen-Titel der neuen Notiz: " + evernoteLink +"<br/>"
                if(doublettes) {
                    htmlBodyString += "Allerdings gibt's Doubletten-Verdacht... <br/>Also bitte bereinigen.<br/>"
                }
                new ReadAndForwardExchangeMails().sentMailTo(mail, "CREAM - ERFOLG: neu angelegt...", htmlBodyString, true)



            } else if (noteInInbox.title.contains("(ADRESSE_NEU_MANUAL)")) {
                // TODO: Check this - is it a "move"?
                mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)

                List<String> emailsOfNeuManual = RegexUtils.findEmailAdress(noteInInbox.getContent())
                List<String> allCreamUsers = ReadAndForwardExchangeMails.getAllUsersEmails();
                allCreamUsers.add(ReadAndForwardExchangeMails.emailCRM);
                emailsOfNeuManual.removeAll(allCreamUsers)
                emailsOfNeuManual = emailsOfNeuManual.toUnique()


                String foundNotesWithMail ="<br/>"
                if(emailsOfNeuManual.size() > 0) {

                    // Scan for notes contaiing email
                    List<Note> notesContainingEmail = findNotesContaining(emailsOfNeuManual)
                    notesContainingEmail.each {foundNotesWithMail += "E-Mails: " +emailsOfNeuManual+" in dieser Notiz gefunden: <br/>" + mailNotebook.getInternalLinkTo(it,  it.title )+"<br/><br/>"}

                    foundNotesWithMail += "<br/>"
                    // Scan for notes contaiing domain
                    List<Note> notesContainingDomain = findNotesContainingDomain(emailsOfNeuManual)
                    notesContainingDomain.each {foundNotesWithMail += "Domains dieser E-Mails " +emailsOfNeuManual+" in diesem Notiz-Titel: <br/>" + mailNotebook.getInternalLinkTo(it,  it.title )+"<br/><br/>"}

                    if (notesContainingEmail.empty && notesContainingDomain.empty) {
                        // do new entry with mail
                        // put link to email
                        foundNotesWithMail += "keine Notizen mit Domains oder E-Mail " +emailsOfNeuManual+" gefunden... Bitte neu anlegen."
                    }
                }

                def todoReceiver = ["loeffler@v-und-s.de", "anna@v-und-s.de"]
                if (!testMode) {
                   log.info("Neue manuelle Adresse - informiere backoffice " + noteInInbox.getTitle())
                    String link = mailNotebook.getRawExternalLinkTo(noteInInbox)
                    String htmlLink = mailNotebook.getInternalLinkTo(noteInInbox, "Zu den gesendeten Daten... Bitte hier_klicken.")
                    todoReceiver.each {
                        new ReadAndForwardExchangeMails().sentMailTo(it, "(ADRESSE_NEU_MANUAL) in C__MAILS_DOCS --> " + link, noteInInbox.getTitle() + "<br/>" + htmlLink + "<br/>" + foundNotesWithMail, true)
                    }
                }

                def htmlBodyString = "Aufforderung zum Eintrag wurde geschickt an:  " + todoReceiver +"<br/>"

                def shortName = noteInInbox.title[26..28]
                String mail = getUserMail(shortName)
                new ReadAndForwardExchangeMails().sentMailTo(mail, "CREAM - ERFOLG: ...", htmlBodyString, true)


            } else if (noteInInbox.title.contains("(ADRESSE_NEU_AUTO)")) { // try to create a new entry...

                inboxNotebook.loadNoteRessources(noteInInbox) // because inboxNotebook is not found down in SyncHandler.get().loadRessources(note);
                def shortName = noteInInbox.title[19..21]
                String mail = getUserMail(shortName)

                def raw = ENHelper.getRawText(noteInInbox)
                String[] split = raw.split("(START_KONTAKT|ENDE_KONTAKT)")
                raw = split[1]
                //def regexTODO = /(?m)^(todo|Todo|TODO):.*$/ //complete line starting with todo
                def regexTODO = /(?m)^(todo|Todo|TODO):.*$/ //complete line starting with todo
                def matcher = raw =~ regexTODO

                def am = new AdressMagic(raw)
                def adr = null
                def domain = am.www //email.split("@")[1]
                def newHeadline
                am.with {
                    newHeadline = "$titleInName$christianNames $surName ($company) [$www]"
                    adr = """<div><br/></div>
                                 <div><b>${ENHelper.escapeHTML(titleInName + christianNames + " " + surName)}</b></div>
                                 <div>${ENHelper.escapeHTML(functionDepartment)}</div>   
                                 <div>${ENHelper.escapeHTML(mobile)}</div>   
                                 <div>${ENHelper.escapeHTML(phone)}</div>   
                                 <div>${ENHelper.escapeHTML(email)}</div>   
                                 <div><br/></div>   

                                 <div><b>${ENHelper.escapeHTML(company)}</b></div>   
                                 <div>${ENHelper.escapeHTML(streetAndNr)}</div>   
                                 <div>${ENHelper.escapeHTML(zipCode + " " + town)}</div>   
                              """
                }

                def created = false
                List<Note> notesContainingDomain = findNotesContainingDomain([am.email]) // TODO: noch kacke... findet standort@bosch.de nicht sondern nur bosch.de
                if(notesContainingDomain.size() > 0){ // PROBLEM. Wo ablegen? Überall...
                    notesContainingDomain.each {
                        ENHelper.addAdressEntryAtEnd(it, adr)
                    }
                } else {
                    //def newHeadline = am.christianNames + " " + am.surName + " ["+am.www+"]";
                    def newBody = ENHelper.createNoteFromEmailText(adr)

                    def usersNotebookGuid = defaultNotebook // in case it does not exist...
                    // find the users notebook
                    try {
                        usersNotebookGuid = SyncHandler.get().getNotebookByName("C_"+shortName)
                    } catch (Exception e) {
                        log.warn("did not find notebook for user: " + shortName)
                    }

                    // create the note
                    Note n = new Note()
                    n.title = newHeadline.trim()
                    n.content = newBody
                    n = usersNotebookGuid.createNote(n) // get guid
                    //n.guid = newNote.guid
                    notesContainingDomain.add(n)
                    created = true
                }


                // finally move the original to docs in case it needs to be fixed manually
                noteInInbox.title = noteInInbox.title.replace("ADRESSE_NEU_AUTO", "Kontaktdaten, original")
                noteInInbox.title += (" " + am.email)
                noteInInbox.notebookGuid = mailNotebook.getSharedNotebook().notebookGuid
                mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)

                // create a link to the new one, that points to the original and a en_todo to check new entry
                //String evernoteLink = inboxNotebook.getInternalLinkTo(noteInInbox, linkName + ", mail: " + mailAddr)
                String evernoteLink = mailNotebook.getInternalLinkTo(noteInInbox, "Original-Adressdaten für " + am.email)

                notesContainingDomain.each {
                    ENHelper.addHistoryEntry(it, evernoteLink)
                }
                (0..<matcher.count).each {
                    String todoStr = matcher[it][0]
                    todoStr = todoStr.replaceAll("^(todo|Todo|TODO):", " ")

                    notesContainingDomain.each {
                        ENHelper.addTodoEntry(it, todoStr)
                    }
                }

                //String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date())
                notesContainingDomain.each {
                    ENHelper.addTodoEntry(it, "ANL: $shortName: 1.1.00 NEUEN KONTAKT PRÜFEN")
                }

                // finally, create a Link in case there is a duplicate hit for the email
                /*
                List<Note> listOfNotes = SyncHandler.get().allNotes
                //Configuration config = AbstractConfiguration.getConfig()
                listOfNotes.forEach { localNote ->
                    if (am.allMails.size() > 0 && StringUtils.containsIgnoreCase(localNote.content, am.allMails[0])) {
                        ENSharedNotebook notebook = SyncHandler.get().getNotebook(localNote)
                        evernoteLink = notebook.getInternalLinkTo(localNote, "vermutlich Doublette... andere, alte Notiz mit mail: $am.email")
                        log.debug("going to create doublette link to: " + localNote.title)
                        //localNote = inboxNotebook.getSharedNoteStore().getNote(ENConnection.get().businessAuthToken, localNote.guid, true, false, false, false)
                        ENHelper.addHistoryEntry(n, evernoteLink)
                        // save directly - before sync...
                        //SyncHandler.get().updateNoteImmediately(localNote)
                    }
                }
                */
                notesContainingDomain.each {
                    SyncHandler.get().updateNoteImmediately(it)
                }
                def htmlBodyString = ""
                if(created) {
                    htmlBodyString += "Neu anglegt!<br/>"
                } else {
                    htmlBodyString += "Zu existierenden Adressen hinzugefügt:<br/>"
                }
                notesContainingDomain.each {
                    String htmlLink = mailNotebook.getInternalLinkTo(it, it.title)
                    htmlBodyString += (htmlLink + "<br/>")
                }
                new ReadAndForwardExchangeMails().sentMailTo(mail, "CREAM - ERFOLG: Adresse automatisch abgelegt...", htmlBodyString, true)


            }


                // 1. falls keine existierende Mail gefunden (linken zu existierender wird  nicht klappen...)
                // neue Note anlegen im User-notebook vom Sender mit Titel:
                // Name (Firma) und im Notfall email-adresse
                //
                // 2. versuche sie automatisch zu verlinken -->
                // ADRESSE: <link zu note mit allen Adressdaten>
                // Name
                // funktion
                // email
                // handy
                // tel
                // <bitrix>
                // [] ANL: BEL: heute Adressdaten in Bitrix übernehmen
                //
                // Adress-Notiz wird verändert:
                // ADRESSE: email als title setzen und zu notebook C__MAILS verschieben
                //
                //

                // 3. erzeuge eine adresse im gesharten exchange adressbook ???

        }
    }

    public static String getUserMail(shortName) {
        def user = AbstractConfiguration.config.users.find() {
            it.shortName == shortName
        }
        if (!user) {
            //log.error("FUCK: could not find user for short Name: $shortName)")
            throw Exception("FUCK: users == null")
        }
        def mail = user.email
        mail
    }

    static List<Note> findNotesContainingDomain(List<String> stringsToSearchFor) {
        def result = []
        List<Note> listOfNotes = SyncHandler.get().allNotes
        listOfNotes.forEach { localNote ->
            stringsToSearchFor.forEach { searchString ->

                String[] split = searchString.split("@");
                def www = split.length == 2 ? split[1] : "no-domain-found-here...";
                if (localNote.getTitle().contains(www)) {
                    result << localNote
                }
            }
        }
        result
    }

    static List<Note> findNotesContaining(List<String> stringsToSearchFor) {
        def result = []
        List<Note> listOfNotes = SyncHandler.get().allNotes
        listOfNotes.forEach { localNote ->
            stringsToSearchFor.forEach { searchString ->
                if (localNote.content.contains(searchString)) {
                    result << localNote
                }
            }
        }
        result
    }

    static def overview() {
        if(overviewCreator == null) {
            overviewCreator = new OverviewCreator()
        }
        overviewCreator.createOverviews()
    }

    static def checkTwoSpacesAtBeginning() {
        def notes = SyncHandler.get().getAllNotes()
        for(def n: notes) {
            if(ENHelper.addTwoNewlinesAtTop(n)) {
                SyncHandler.get().updateNoteImmediately(n)
            }
        }
    }

    static def syncAndGenerateOverviews() {
        // TODO: CHECK LAST OVERVIEW TIME - avoid too often
        SyncHandler.get().sync(null)
        overview()
        checkTwoSpacesAtBeginning()
    }

    /**
     * emails are sent to crm@v-und-s.de FROM THE USER
     *
     * There are different use cases
     *
     * ATTACHMENT
     * 1. the first line contains those email adresses that the mail should be linked to
     * 2. send the email to evernote (which email?)
     * 3. use as Betreff: AlterBetreff LINK_TO: email1, email2, email3 Config(inbox) = @C__INBOX
     *    in order to be able to also process complex html appendixes.
     *
     * ADRESS
     * 1. the first line contains the word adr or ADR AS FIRST STATEMENT. There may be a picture or html or something
     * 2. it will be placed in the C__ADR_TODO
     * 3. it will be available in CREAM GUI in a "NEW ADRESSES". If it is clicked, then it will be available in Adress-Magic
     *
     * HELP (if H or if usecase is not detected)
     */
    static def processMails() {
        try {
            new ReadAndForwardExchangeMails().doIt()
        } catch (Exception e) {
            log.warn("cannot access exchange mailbox of crm@v-und-s.de")
        } // read crm@v-und-s.de mailbox and handle mails
    }

    static void connectToEvernoteAndReadConfig() {

        MainGUI.debugOffline = false
        if (testMode) {
            ENConfiguration.CONFIG_TITLE_STRING = "CREAM_CONFIG_NOTE___TEST"
        } else {
            ENConfiguration.CONFIG_TITLE_STRING = "CREAM_CONFIG_NOTE"
        }
        MainGUI.loadProperties()
        //MainGUI.properties.put("EVERNOTE_TOKEN", "S=s226:U=1abbe88:E=16638febe56:C=15ee14d8eb0:P=185:A=bennoloeffler-2708:V=2:H=011111aace1129aef72815a0bce6a4dd")
        //p.put("EVERNOTE_TOKEN", "S=s226:U=1abbe88:E=16d9204d825:C=1663a53abf0:P=1cd:A=en-devtoken:V=2:H=1bf6fff4a0898dbb4a6bcb39567ead44")

        def connected
        while(!connected) {
            try {
                ENAuth a = ENAuth.get(MainGUI.properties, EvernoteService.PRODUCTION)
                if(a.connectToEvernote()) {
                    ENSharedNotebook c__config = new ENSharedNotebook(ENConnection.get(), "C__CONFIG")
                    new ENConfiguration(c__config, ENConnection.get())
                    connected = true
                }

            } catch (EDAMSystemException es) { // e.g. Reason: RATE_LIMIT_REACHED
                log.warn("Could not connect to evernote. Reason: {}", es.getErrorCode())
                if (es.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
                    waitForRateLimitOver(es)
                } else {
                    log.warn("Could not startup... retry in 2 min...")
                    sleep(1000 * 60 * 2)
                }
            } catch (Exception e) {
                log.warn("Could not startup... retry in 2 min...")
                log.warn("REASON: " + e.getMessage())
                sleep(1000 * 60 * 2)
            }
        }
        MainGUI.saveProperties()
    }


    static void main(String[] args) {
        try {
            //println("starting CREAM deamon (crm@v-und-s.de and todo-lists)")
            log.info("""Starting CREAM deamon "Release" and (Version):  """ + MainGUI.VERSION_STRING)

            if (args.length >= 1) {
                if (args[0].equals("-testmode")) {
                    log.warn("\n\nstarting up in TESTMODE!\n\n")
                    testMode = true
                }
            }

            // connect to evernote
            connectToEvernoteAndReadConfig()
            connectMailNotebooks()

            // get all notes to local store... in order to fast create overviews
            SyncHandler.init(ENConnection.get(), new NoteStoreLocal(ENConfiguration.getConfig()))

            syncAndGenerateOverviews()


            int debug_faster = 1 // 2 4 5 means double / four / five times faster
            //noinspection GroovyInfiniteLoopStatement
            while (true) {
                try {
                    (0..20/debug_faster).each { // 40 minutes before sync
                        processMails()
                        //processEvernoteInbox()
                        (1..120/debug_faster).each { // two minute sleeping between mail poll
                            print "."
                            sleep(1000) {
                                println "going to exit CREAM deamon"
                                System.exit(0)
                            }
                        }
                        println()
                        processEvernoteInbox()  // look in C__INBOX and check, what to do
                    }
                    // TODO: in the morning at 6:00 and in the evening 18:00
                    // --> deliver changes of the last 12 hours - if any.
                    // Switch ABO-Mode on, if is after 6:00 or after 18:00 and
                    // the "lastAboRun" is older than 11 hours. Then set lastAboRun
                    // to now and persist - in case of restart.

                    syncAndGenerateOverviews()
                } catch (EDAMSystemException e) {
                    // Check for "normal exceptions like AUTH or RATE_LIMIT and handle them.
                    //e.printStackTrace()
                    if ( ! waitForRateLimitOver(e)) {
                        throw e // rethrow the real crashes... Rate limit an connection errors...
                    }

                } catch(UnknownHostException e1) {
                    log.info("\n\nWir müssen leider WARTEN. UnknownHostException. Kein Netz...\n\n")
                    sleep(60 * 1000)
                } catch(TTransportException e2) {
                    log.info("\n\nWir müssen leider WARTEN. TTransportException. Kein Netz...\n\n")
                    sleep(60 * 1000)
                } catch (ServiceRequestException e) {
                    log.info("\n\nWir müssen leider WARTEN. ServiceRequestException. Kann keine Mails verschicken...\n\n")
                    //println e.toString()
                    //e.printStackTrace()
                    sleep(60 * 1000)
                }
            }
        } catch (Exception e) {
            if(!waitForRateLimitOver(e)) {
                //ex.printStackTrace()
                log.error("*** DEAMON CRASHED! ****", e)
                e.printStackTrace()
                def admins = AbstractConfiguration.getConfig().getAdmins()
                String email
                if (admins) {
                    email = AbstractConfiguration.getConfig().getAdmins().get(0)?.getEmail()
                }
                def sw = new StringWriter()
                StackTraceUtils.printSanitizedStackTrace(e, new PrintWriter(sw))
                if (email) {
                    new ReadAndForwardExchangeMails().sentMailTo(email, "CREAM DEAMON CRASHED", sw.toString() + "\n\n" + e.toString())
                } else {
                    log.error("No admin configured in users.")
                }
            } else {
                main(args) // just again...
            }
        }
    }

    static boolean waitForRateLimitOver(Exception e) {
        if(e instanceof EDAMSystemException) {
            if (e.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
                log.info("\n\nWir muessen leider WARTEN. Evernote hat dicht gemacht... Dauer: " + Util.readableTime(e.rateLimitDuration * 1000))
                (e.rateLimitDuration..0).each {
                    sleep 1000
                    def percent = ((float)(1000 * it / e.rateLimitDuration)/10).round(1)
                    print "$percent% "
                    if(it % 20 == 0) println()
                }
                return true
            }
        }
        return false
    }

}
