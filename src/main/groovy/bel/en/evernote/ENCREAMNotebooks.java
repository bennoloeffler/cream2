package bel.en.evernote;


import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
@Log4j2
public class ENCREAMNotebooks implements Serializable {

    static final long serialVersionUID = 1L;

    //private static ENCREAMNotebooks singleton = null;

    private String defaultNotebook;
    private String mailsNotebook;
    private String overviewNotebook;
    private String inboxNotebook;
    private ArrayList<String> groupNotebooks = new ArrayList<>();

    private Map<String, String> guidMap = new HashMap<>();

    /*
    static ENCREAMNotebooks get() {
        if(singleton == null) {
            singleton = new ENCREAMNotebooks();
        }
        return singleton;
    }*/

    ENCREAMNotebooks() {
    }

    public void createMapping(ENSharedNotebookGroup enSharedNotebookGroup) {
        for(ENSharedNotebook notebook: enSharedNotebookGroup.getAllNotebooks()) {
            guidMap.put(notebook.getSharedNotebook().getNotebookGuid(), notebook.getLinkToSharedNotebook().getShareName() );
        }
    }

    public boolean isALocalNotebook(String notebookGuid) {
            return guidMap.containsKey(notebookGuid);
    }

    public String getNotebookGuidForName(String notebookName) {
        for(Map.Entry<String, String> n: guidMap.entrySet()) {

            if(notebookName.equals(n.getValue())) {
                return n.getKey();
            }
        }

        assert(false);
        return null;
    }

    public String getNameForNotebookGuid(String newNotebookGuid) {
        String name = guidMap.get(newNotebookGuid);
        if(name == null) {
            log.debug("NO LOCAL NOTEBOOK FOR GUID: " + newNotebookGuid);
        }
        return name;
    }


    /**
    public ENCREAMNotebooks(String defaultNotebook, String mailNotebook, String overviewNotebook, String[] groupNotebooks) {
        this.defaultNotebook = defaultNotebook;
        this.mailNotebook = mailNotebook;
        this.overviewNotebook = overviewNotebook;
        this.groupNotebooks = groupNotebooks;
    }*/

    /*
    public String getDefaultNotebook() {
        return defaultNotebook;
    }
    public String getMailsNotebook() {
        return mailsNotebook;
    }
    public String getOverviewNotebook() {
        return overviewNotebook;
    }
    public ArrayList<String> getGroupNotebooks() {
        return groupNotebooks;
    }
*/
}
