package bel.cream2.deamon

import bel.en.MainGUI
import bel.en.data.AbstractConfiguration
import bel.en.data.Configuration
import bel.en.email.ReadAndForwardExchangeMails
import bel.en.evernote.*
import bel.en.localstore.NoteStoreLocal
import bel.en.localstore.SyncHandler
import bel.util.AdressMagic
import bel.util.Util
import com.evernote.auth.EvernoteService
import com.evernote.edam.error.EDAMErrorCode
import com.evernote.edam.error.EDAMSystemException
import com.evernote.edam.type.Note
import com.evernote.edam.type.Notebook
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

/*
    static def doIt() {
        println ("Starting CreamDeamonWorker (sync already done. NOW: reading cream-Mailbox, send abo-diffs, generating overviews, appending mails to notes, ...")
        processMails()
        processEvernoteInbox()
        syncAndGenerateOverviews()
        println ("done... waiting for next cycle")
    }
*/

    static def processEvernoteInbox() {
        List<Note> notesInInbox = inboxNotebook.allNotes
        if(notesInInbox) {
            log.debug("found $notesInInbox.size notes in INBOX")
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
                    noteInInbox.title = "(NICHT ZUGEORDNET)  " + noteInInbox.title
                    mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)
                    return

                }

                mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)

                def treffer = false
                mailAdresses.each { mailAddr ->
                    mailAddr = mailAddr.trim()
                    String evernoteLink = inboxNotebook.getInternalLinkTo(noteInInbox, linkName + ", mail: " + mailAddr)
                    List<Note> listOfNotes = SyncHandler.get().allNotes
                    listOfNotes.forEach { localNote ->
                        //if (localNote.content.contains(mailAddr) || localNote.content.contains(mailAddr.toLowerCase())) {
                        if (StringUtils.containsIgnoreCase(localNote.content, mailAddr)) {
                            log.debug("going to link to this note: " + localNote.title)
                            treffer = true
                            localNote = inboxNotebook.getSharedNoteStore().getNote(ENConnection.get().businessAuthToken, localNote.guid, true, false, false, false)
                            ENHelper.addHistoryEntry(localNote, evernoteLink)
                            // save directly - before sync...
                            SyncHandler.get().updateNoteImmediately(localNote)
                        }
                    }
                }
                if (!treffer) {
                    def shortName = noteInInbox.title[0..2]
                    def user = AbstractConfiguration.config.users.find() {
                        it.shortName == shortName
                    }
                    if (!user) {
                        log.error("FUCK: users == null")
                    }
                    def mail = user.email
                    noteInInbox.title = "(NICHT ZUORDENBAR)   " + noteInInbox.title
                    mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)
                    log.warn("Konnte Ablage von $mail nicht zuorden." + noteInInbox.getTitle())
                    new ReadAndForwardExchangeMails().sentMailTo(mail, "Konnte mail nicht zuordnen...", noteInInbox.getTitle())
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
                am.with {
                    newHeadline = "$titleInName$christianNames $surName ($company)"
                    adr = """<div><br/></div>
                                 <div><b>${ENHelper.escapeHTML(titleInName + christianNames + " " + surName)}</b></div>
                                 <div>${ENHelper.escapeHTML(functionDepartment)}</div>   
                                 <div>${ENHelper.escapeHTML(mobile)}</div>   
                                 <div>${ENHelper.escapeHTML(phone)}</div>   
                                 <div>${ENHelper.escapeHTML(email)}</div>   
                                 <div>${ENHelper.escapeHTML(streetAndNr)}</div>   
                                 <div>${ENHelper.escapeHTML(zipCode + " " + town)}</div>   
                                 <div>${ENHelper.escapeHTML(company)}</div>   
                              """
                }
                //println(adr)

                def newBody = ENHelper.createNoteFromEmailText(adr)
                //println()
                //println()
                //println(newBody)
                def shortName = noteInInbox.title[14..16]
                //def shortName = AbstractConfiguration.getConfig().getShortName(sender)

                Note n = new Note()
                n.title = newHeadline.trim()
                n.content = newBody
                n.notebookGuid = defaultNotebook.getSharedNotebook().notebookGuid
                def newNote = defaultNotebook.createNote(n) // get guid
                n.guid = newNote.guid

                // finally move the original to docs in case it needs to be fixed manually
                noteInInbox.title = noteInInbox.title.replace("NEU_KONTAKT", "Kontaktdaten, original")
                noteInInbox.title = noteInInbox.title.replace("RE:", "")
                noteInInbox.title = noteInInbox.title.replace("FW:", "")
                noteInInbox.title = noteInInbox.title.replace("AW:", "")
                noteInInbox.title = noteInInbox.title.replace("WG:", "").trim()
                noteInInbox.title += (" " + am.email)
                noteInInbox.notebookGuid = mailNotebook.getSharedNotebook().notebookGuid
                mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)

                // create a link to the new one, that points to the original and a todo to check new entry
                //String evernoteLink = inboxNotebook.getInternalLinkTo(noteInInbox, linkName + ", mail: " + mailAddr)
                String evernoteLink = mailNotebook.getInternalLinkTo(noteInInbox, "Original-Adressdaten für " + am.email)

                // n.getContent??
                ENHelper.addHistoryEntry(n, evernoteLink)

                (0..<matcher.count).each {
                    String todoStr = matcher[it][0]
                    todoStr = todoStr.replaceAll("^(todo|Todo|TODO):", " ")
                    ENHelper.addTodoEntry(n, todoStr)
                }

                String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date())
                ENHelper.addTodoEntry(n, "$shortName: $date NEUEN KONTAKT PRÜFEN")
                //SyncHandler.get().updateNoteImmediately(n)

                // finally, create a Link in case there is a duplicate hit for the email

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
                SyncHandler.get().updateNoteImmediately(n)



            } else if (noteInInbox.title.contains("(ADRESSE_NEU)")) {
                mailNotebook.getSharedNoteStore().updateNote(ENConnection.get().getBusinessAuthToken(), noteInInbox)
                if (!testMode) {
                   log.info("Neue Adresse - informiere Anna. " + noteInInbox.getTitle())
                    new ReadAndForwardExchangeMails().sentMailTo("anna@v-und-s.de", "(ADRESSE_NEU) in C__MAILS_DOCS :-)", noteInInbox.getTitle())
                }

                //def hitNotes = findNotesContaining()

                /*

                // 0.
                //FÜR NEUANLAGE: Titel ermitteln (email, titel, name, firma)
                String nameCompany
                def email = ENHelper.findFirstEmailAdress(noteInInbox.content)
                String names
                String domain
                if (email) {
                    names = email.split("@")
                    //nameCompany = email // just in case, nothing else is found...
                    def name = names[0]
                    def comp = names[1].split("\\.")[0].capitalize()
                    nameCompany = "$name ($comp)"
                    if (email =~ /[\w]{3,}\.[^@]{3,}@[\w]{3,}\.\w{2,3}/) { // vor.nach@name.yxr
                        def vorNach = names[0].split("\\.")
                        def vor = vorNach[0].capitalize()
                        def nach = vorNach[1].capitalize()
                        nameCompany = "$vor $nach ($comp)"
                    }

                }
                def rawText = ENHelper.getRawText(noteInInbox)
                AdressMagic magic = new AdressMagic(rawText)

                // DIE ARBEIT MUSS MAN SICH NUR MACHEN, WENN DIE EMAIL NICHT PASST (vorname.nachname@companyName.XYZ
                def titleTrimmed = noteInInbox.title.replace("ADRESSE_NEU:", "").trim()
                if (titleTrimmed =~ /[^(]{3,}\([^)]{3,}\)/) {
                    nameCompany = titleTrimmed
                }

                // Note zum verlinken finden
                def hit = false
                if(email) {
                    List<Note> listOfNotes = SyncHandler.get().allNotes
                    for (Note localNote : listOfNotes) {
                        if (localNote.content.contains(email)) {
                            hit = true
                            ENHelper.addHistoryEntry(localNote, )
                            break // only link to one...
                        }
                    }
                }
                if(!hit) { // sonst neue note erzeugen und verlinken
                    Note n = new Note()
                    n.title = "ADRESSE_NEU: " + nameCompany
                    n.content = ENHelper.createNoteFromEmailText("")

                    // LINK

                    n = defaultNotebook.createNote(n)
                    SyncHandler.get().updateNote(n)


                }
                */
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

    List<Note> findNotesContaining(List<String> stringsToSearchFor) {
        def result = []
        List<Note> listOfNotes = SyncHandler.get().allNotes
        listOfNotes.forEach { localNote ->
            stringsToSearchFor.forEach { searchString ->
                if (localNote.content.contains(searchString)) {
                    result << localNote
                }
            }
        }
        results

    }

    static def overview() {
        if(overviewCreator == null) {
            overviewCreator = new OverviewCreator()
        }
        overviewCreator.createOverviews()
    }

    static def syncAndGenerateOverviews() {
        // TODO: CHECK LAST OVERVIEW TIME - avoid too often
        SyncHandler.get().sync(null)
        overview()
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
            } catch (Exception e) {
                log.warn("Could not startup because connection failed. retry in 2 min...")
                sleep(1000 * 60 * 2)
            }
        }
        MainGUI.saveProperties()
    }


    static void main(String[] args) {
        //testSomeThings()
        //System.exit(0)

        try {
            println("starting CREAM deamon (crm@v-und-s.de and todo-lists)")

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
            //SyncHandler.get().sync()

            //noinspection GroovyInfiniteLoopStatement
            while (true) {
                try {
                    (0..15).each { // 15 minutes before sync
                        processMails()
                        processEvernoteInbox()
                        (1..60).each { // one minute sleeping between mail poll
                            print "."
                            sleep(1000) {
                                println "going to exit CREAM deamon"
                                System.exit(0)
                            }
/*
                                if ( System.in.available() != 0 ) {
                                    int c = System.in.read()
                                    println c
                                    char cc = (char) c
                                    println cc
                                    if (c == 'o') {
                                        overview()
                                    } else if (c == 'q') {
                                        System.exit(1)
                                    } else if (c == 's') {
                                        SyncHandler.get().sync()
                                    } else {
                                        println ""
                                        println "? --> o__verview, s__ync, q__uit"
                                    }
                                }
                                */

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
            }
        }
    }

    static boolean waitForRateLimitOver(Exception e) {
        if(e instanceof EDAMSystemException) {
            if (e.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
                log.info("\n\nWir müssen leider WARTEN. Evernote hat dicht gemacht... Dauer: " + Util.readableTime(e.rateLimitDuration * 1000))
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

     static void testSomeThings() {
        String raw = """




Dipl.-Wirtsch.-Ing. (FH)



Timo Seggelmann



Managing Director



+49 541 999646-0



t.seggelmann@salt-and-pepper.eu LINK:<mailto:t.seggelmann@salt-and-pepper.eu>



+49 152 09489209



www.salt-and-pepper.eu



SALTANDPEPPER Software GmbH & Co. KG



Kaffee-Partner-Allee 5



49090 0snabrück









todo: BEL nach WS Beratungsprojekt

{HIER war in Evernote ein BILD}












"""

         def regexTODO = /(?m)^(todo|Todo|TODO):.*$/ //complete line starting with todo
         //def regexTODO = /todo/
         def matcher = raw =~ regexTODO
         //println matcher.count
         //println matcher[0]
        (0..<matcher.count).each {
            def match = matcher[it][0]
            String todoStr = match.toString()
            todoStr = todoStr.replaceAll("^(todo|Todo|TODO):", " ")
            println todoStr
            //ENHelper.addTodoEntry(n, todoStr)
        }

    }
}
