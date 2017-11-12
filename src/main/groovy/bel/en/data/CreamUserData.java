package bel.en.data;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

/**
 * User for the CREAM CRM
 */
@Data
public class CreamUserData implements Serializable {
    static final long serialVersionUID = 1L;
    @NonNull final private String shortName;
    @NonNull final private String email;
    @NonNull final private String completeName;
    @NonNull final private String privileges;
    @NonNull final private String diffMail;

    public CreamUserData(String shortName, String email, String completeName, String privileges, String diffMail) {
        this.shortName = shortName.trim();
        this.email = email.trim();
        this.completeName = completeName.trim();
        this.privileges = privileges.trim();
        this.diffMail = diffMail.trim();
    }

    /*
    public String getShortName() {
        return shortName;
    }

    public String getEmail() {
        return email;
    }

    public String getCompleteName() {
        return completeName;
    }
*/
    public boolean isAdmin() {
        return privileges.equals("admin");
    }

    public boolean isDiffMail() { return diffMail.startsWith("diffMail");}

    public boolean isDiffMailOncePerDay() { return diffMail.endsWith("Daily");}

    /*public boolean canWrite() {
        return !privileges.equals("read");
    }*/
}
