package bel.en.gui;

import bel.en.data.AbstractConfiguration;
import bel.en.data.CreamUserData;
import bel.en.evernote.ENConnection;
import bel.en.evernote.ENHelper;
import bel.en.evernote.ENSharedNotebook;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.User;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.thrift.TException;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created 14.04.2017.
 */
@Log4j2
public class DebugForm {
    @Getter
    private JPanel panel;
    @Getter
    private JTextPane debugTextPane;
    @Getter
    private JButton aktualisierenButton;

    public DebugForm() {
        aktualisierenButton.addActionListener(e -> writeDiagnosisData());
    }

    /*
    Replaced that by interface of JDK: java.util.function.Supplier<T> {T get();}
    @FunctionalInterface interface StringGetter {
        String get();
    }*/

    private void writeDiagnosisData() {
        debugTextPane.setText("");
        CreamUserData cu = AbstractConfiguration.getConfig().getCurrentUser();

        addText("\n--- local data ---\n\n");
        addText("CREAM User: " + cu.getCompleteName() + ", " + cu.getShortName() + ", " + cu.getEmail());
        //addText("EN authToken: " + ENAuth.get().getAuthToken());

        ENConnection c = ENConnection.get();
        addText(() -> {
            boolean conOK = c.connect();
            return String.format("Connection Test: %s", conOK ? "OK" : "failed");
        });


        if (c.connect()) {
            addText("--- get evernote user data ---", () -> {
                try {
                    User u = c.getUserStoreClient().getUser();
                    return u.toString();
                    /*
                    UserAttributes att = u.getAttributes();
                    BusinessUserInfo bus = u.getBusinessUserInfo();
                    Accounting acc = u.getAccounting();
                    r = "EN User Info: " + u.getName() + "\n" +
                            "  mail: " + u.getEmail() + "\n" +
                            "  username: " + u.getUsername() + "\n" +
                            "  attribute: " + (att == null ? "???" : att.toString()) + "\n" +
                            "  accounting: " + (acc == null ? "???" : acc.toString()) + "\n";

                    if (u != null) {
                        r += "BUSINESS ACCOUNT: YES\n" +
                                "  business name: " + bus.getBusinessName() + "\n" +
                                "  business mail: " + bus.getEmail() + "\n";
                    } else {
                        r += "BUSINESS ACCOUNT: NO\n";
                    }*/
                } catch (EDAMUserException e) {
                    log.catching(e);
                    return e.getLocalizedMessage();
                } catch (EDAMSystemException e) {
                    log.catching(e);
                    return e.getLocalizedMessage();
                } catch (TException e) {
                    log.catching(e);
                    return e.getMessage();
                }
            });

            // notebooks
            addText("--- get private NOTBOOKS: ---", () -> {
                try {
                    java.util.List<Notebook> notebooks = c.getNoteStoreClient().listNotebooks();
                    return notebooks.stream().map(n -> n.getName()).collect(Collectors.joining("\n"));
                } catch (EDAMUserException e) {
                    log.catching(e);
                    return e.getMessage();
                } catch (EDAMSystemException e) {
                    log.catching(e);
                    return e.getMessage();
                } catch (TException e) {
                    log.catching(e);
                    return e.getMessage();
                }
            });


            // notebooks
            addText("--- get linked NOTBOOKS: --- ", () -> {
                try {
                    java.util.List<LinkedNotebook> linkedNotebooks = c.getNoteStoreClient().listLinkedNotebooks();
                    return linkedNotebooks.stream().map(n -> n.getShareName() + " (" + n.getUsername() + ")").collect(Collectors.joining("\n"));
                } catch (EDAMUserException e) {
                    log.catching(e);
                    return e.getMessage();
                } catch (EDAMSystemException e) {
                    log.catching(e);
                    return e.getMessage();
                } catch (TException e) {
                    log.catching(e);
                    return e.getMessage();
                } catch (EDAMNotFoundException e) {
                    log.catching(e);
                    return e.getMessage();
                }
            });

            //AuthenticationResult ar;

            addText("--- evernote BUSINESS authenticate Test  ---", () -> {

                try {
                    AuthenticationResult ar = c.getUserStoreClient().authenticateToBusiness();
                    return ar.toString();
                    //return "NO BUSINESS auth";
                    //ar.getCurrentTime();
                    //ar.getExpiration();
                    //ar.getPublicUserInfo();
                    //ar.

                } catch (EDAMUserException e) {
                    log.catching(e);
                    return e.getMessage();
                } catch (EDAMSystemException e) {
                    log.catching(e);
                    return e.getMessage();
                } catch (TException e) {
                    log.catching(e);
                    return e.getMessage();
                }
            });

            //bAuthResult = userStore.authenticateToBusiness(authToken)
            addText("--- evernote BUSINESS connect and write a note to notebook C__TEST  ---", () -> {

                ENSharedNotebook todos = null;
                try {
                    todos = new ENSharedNotebook(c, "C__TEST");
                } catch (Exception e) {
                    log.catching(e);
                    return "Failed to connect to C__TEST. Reason: " + e.getMessage();
                }
                String title = "Test " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"));
                if (todos != null) {
                    Note n = new Note();
                    n.setTitle(title);
                    String content = ENHelper.createValidEmptyContentWithEmptyDataBlock();
                    n.setContent(content);
                    ENHelper.addHistoryEntry(n, "Zufall (millis): " + System.currentTimeMillis());
                    //n.setContent();
                    try {
                        Note note = todos.createNote(n);
                    } catch (Exception e) {
                        log.catching(e);
                        if (e instanceof EDAMUserException) {
                            EDAMUserException ue = (EDAMUserException) e;
                            EDAMErrorCode errorCode = ue.getErrorCode();
                            return "Failed to create Note to C__TEST. Reason: " + errorCode.toString();
                        }

                        return "Failed to create Note to C__TEST. Reason: " + e.getMessage();
                    }
                }
                return "SUCCESS: connected and wrote Test-Note to C__TEST with title: " + title;
            });


        }


    }

    private void addText(String t) {
        addText(t, null);
    }

    private void addText(Supplier<String> g) {
        addText(null, g);
    }

    private void addText(String t, Supplier<String> g) {
        SwingUtilities.invokeLater(() -> {
            String text = "";
            if (t != null && g != null) {
                text = "\n\n\n" + t + "\n\n" + g.get();
            } else if (g != null) {
                text = g.get();
            } else if (t != null) {
                text = t;
            } else {
                throw new RuntimeException("both null");
            }
            String current = debugTextPane.getText();
            debugTextPane.setText(current + "\n" + text);
        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        debugTextPane = new JTextPane();
        scrollPane1.setViewportView(debugTextPane);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        aktualisierenButton = new JButton();
        aktualisierenButton.setText("aktualisieren");
        panel1.add(aktualisierenButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}
