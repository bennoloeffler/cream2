package bel.en.email;

import java.io.*;
import java.util.logging.Logger;

/**
 * Stores one email in a file with counter as name.
 */
public class LocalEmailStoreFileBased implements LocalEmailStore {
    private static final boolean debug = "on".equals(System.getProperty("DEBUG"));
    private static final Logger LOGGER = Logger.getLogger(LocalEmailStoreFileBased.class.getName());
    private final String EMAIL_SUFFIX = ".email";
    private final File FOLDER;
    private final File FOLDER_ARCHIVE;
    private PersistentCounter counter = null;
    private File lastAccessed;

    /**
     * create base folder and counter, if not yet exist
     */
    public void init() {
        if(!FOLDER.exists()) {
            FOLDER.mkdirs();
        }
        if(!FOLDER_ARCHIVE.exists()) {
            FOLDER_ARCHIVE.mkdirs();
        }
        //assert(FOLDER.canWrite());
        counter = new PersistentCounter(FOLDER.getAbsolutePath() + "/counter.txt");
    }

    /**
     * @return if there is an email available
     */
    public boolean hasData() {
        File[] files = getEmailFiles();
        return files.length > 0;
    }

    /**
     * Ctor
     * @param folderName where to store emails and counter
     */
    public LocalEmailStoreFileBased(String folderName) {
        FOLDER = new File(folderName);
        FOLDER_ARCHIVE = new File(folderName+"_archive_human_readable");
    }

    /**
     * @return all email data files available
     */
    private File[] getEmailFiles() {
        File[] files = FOLDER.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(EMAIL_SUFFIX);
            }
        });
        if(files == null) files = new File[0];
        return files;
    }

    /**
     * @return the next email data available
     */
    public EmailData getEmailData() {
        ObjectInputStream ois = null;
        try {
            File[] files = getEmailFiles();
            File f = files[0];
            ois = new ObjectInputStream(new FileInputStream(f));
            EmailData result = (EmailData) ois.readObject();
            lastAccessed = f;
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if(ois != null)ois.close();
            } catch (IOException e) {
                // ignore...
            }
        }
    }

    /**
     * delete the last email data, that was accessed (either written or read)
     */
    public void releaseLastAccessedEmail() {
        lastAccessed.delete();
    }

    /**
     * write an email data to store
     * @param d
     */
    public void storeEmailData(EmailData d) {
        counter.incrementCurrentCount();
        String fileName = FOLDER.getAbsolutePath() + "/mail_" + counter.getCurrentCount() + EMAIL_SUFFIX;
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(fileName));
            oos.writeObject(d);
            lastAccessed = new File(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if(oos != null) oos.close();
            } catch (IOException e) {
                // ignore
            }
        }

        writeEmailToHistory(d,  null);
    }

    private void writeEmailToHistory(EmailData d, String specialName) {
        //
        // now write it to archive (new sub-folder)
        //

        // create folder for the email parts
        boolean successCreatingFolder = false;
        long currentTime = System.currentTimeMillis();
        String archiveFolderName = null;
        while(!successCreatingFolder) {
            if(specialName == null) {
                archiveFolderName = FOLDER_ARCHIVE.getAbsolutePath() + "/mail_" + counter.getCurrentCount() + EMAIL_SUFFIX + "_" + currentTime;
            } else {
                archiveFolderName = FOLDER_ARCHIVE.getAbsolutePath() + "/mail_" + specialName + EMAIL_SUFFIX + "_" + currentTime;
            }
            File archiveFolder = new File(archiveFolderName);

            // if exists, append a counter and try another name
            if(!archiveFolder.exists()) {
                archiveFolder.mkdirs();
                successCreatingFolder = true;
            }
        }

        // print email as text
        PrintWriter p=null;
        try {
            p = new PrintWriter(archiveFolderName + "/message_plain.txt");
            p.println("Sender: " + d.getSender());
            p.println("Date: " + d.getDate());
            p.println("Subject: " + d.getSubject());
            p.println("Attachments: " + (d.getAttachments()==null?0:d.getAttachments().length));
            p.println("");
            p.println("Text:");
            p.println(d.getText());

        } catch (FileNotFoundException e) {
            LOGGER.severe("could not write archive mail. Reason: " + e);
        }
        if(p!=null) p.close();

        // print the attachements
        Attachment[] attachments = d.getAttachments();
        if(attachments != null){
            for (int i = 0; i < attachments.length; i++) {
                Attachment attachment = attachments[i];
                FileOutputStream f = null;
                try {
                    f = new FileOutputStream(archiveFolderName + "/" + attachment.getFileName());
                    f.write(attachment.getData());
                    f.close();
                } catch (IOException e) {
                    LOGGER.severe("could not write attachments of mail. Reason: " + e);
                }
            }
        }
    }

    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("usage: java -cp moa.jar LocalEmailStoreFileBased pathToLocalEmailStore nameOfMailToMoveToHistory archiveFolderName");
            System.exit(-1);
        }
        String pathToLocalEmailStore = args[0];
        String nameOfMailToMoveToHistory = args[1];
        String archiveFolderName = args[2];
        System.out.println("Local email store path: " + pathToLocalEmailStore);
        System.out.println("Email to move to archive: " + pathToLocalEmailStore + "/" + nameOfMailToMoveToHistory);
        System.out.println("Archive folder name: " + archiveFolderName);
        LocalEmailStoreFileBased store = new LocalEmailStoreFileBased(pathToLocalEmailStore);
        File[] files = store.getEmailFiles();
        if(files.length == 0) {
            System.out.println("Email store has no files. Check path to store!");
            System.exit(0);
        }
        File foundFile = null;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file.getName().equals(nameOfMailToMoveToHistory)) {
                foundFile = file;
            }
        }
        if(foundFile == null) {
            System.out.println("Email was not found in store. Check name of email!");
            System.exit(0);
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(foundFile));
            EmailData d = (EmailData) ois.readObject();
            store.writeEmailToHistory(d, archiveFolderName);
        } catch (IOException e) {
            System.out.println("Email could not be converted. Reason:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Email could not be converted. Reason:");
            e.printStackTrace();
        }

    }
}

