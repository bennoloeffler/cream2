package bel.en.usecases;

import bel.en.data.AbstractConfiguration;
import bel.en.email.ActionFailedException;
import bel.en.email.EmailData;
import bel.en.email.EmailDrain;
import bel.en.email.EmailInterpreter;
import bel.en.evernote.ENHelper;
import bel.en.localstore.SyncHandler;
import bel.util.Util;
import com.evernote.edam.type.Note;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static bel.en.evernote.ENHelper.createNoteFromEmailText;

/**
 * this is a use case for the EMailDispatcher...
 */
public class EmailToEvernoteInbox implements EmailInterpreter{

    public EmailToEvernoteInbox() {
    }

    @Override
    public boolean tryAction(EmailData email, EmailDrain drain) throws ActionFailedException {
        try  {

            Set<String> emailAdressToLinkTo = new HashSet<>();
            String[] to = email.getTo();
            for (int i = 0; i < to.length; i++) {
                String s = to[i];
                if( ! "crm@v-und-s.de".equals(s)) {
                    emailAdressToLinkTo.add(s);
                }
            }
            to = email.getCc();
            for (int i = 0; i < to.length; i++) {
                String s = to[i];
                if( ! "crm@v-und-s.de".equals(s)) {
                    emailAdressToLinkTo.add(s);
                }
            }

            String subject = email.getSubject();
            String emailAdressSubject = Util.extractEmail(subject);
            if (emailAdressSubject != null) {
                emailAdressToLinkTo.add(emailAdressSubject);
            }

            String text = email.getText();
            // TODO: scan frist line for an email adress at the very beginning...



            if(emailAdressToLinkTo.size() > 0) {

                // Create Note in the CRM-Email-Appendix-Staple
                Note emailNote  = new Note();
                emailNote.setTitle(subject);
                emailNote.setContent(createNoteFromEmailText(ENHelper.newLineToBR(ENHelper.escapeHTML(email.getText()))));
                //emailNote = mailsNotebook.createNote(emailNote);

                // Create Link
                //String subjectLink = mailsNotebook.getInternalLinkTo(emailNote);

                // Create History Entry: Date ShortName: EmailOfLink SubjectAsLink
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDate d = LocalDate.now();
                String dateStr = d.format(formatter);
                for(String emailAdress: emailAdressToLinkTo) {
                    Collection<Note> notes = SyncHandler.get().filterNotesWithEmail(emailAdress);
                    for (Note n : notes) {
                        String sender = AbstractConfiguration.getConfig().getShortName(email.getSender());
                        //ENHelper.addHistoryEntry(n, sender + ": " + dateStr + "  " + subjectLink);

                        // FIX TODO

                        SyncHandler.get().updateNoteImmediately(n);

                    }
                }
            }
            return true;
        } catch(Exception e) {
            throw new ActionFailedException("Konnte " + email.getSubject() + " nicht ablegen. Grund: " + e.toString());
        }
        //return false;
    }

    @Override
    public String getName() {
        return "Email an Evernote Historie anfügen";
    }

    @Override
    public String getHelp() {
        return "Wenn eine Email-Adresse am Begin der Betreffzeile oder eine Mail-Adresse in den Adressaten (to oder cc) findet, dann wird diese im CRM gesucht und die Email dort abgelegt. Wird keine Notiz gefunden, dann nicht...";
    }
}
