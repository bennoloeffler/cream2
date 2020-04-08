package bel.en.evernote;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created 15.10.2017.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
@lombok.AllArgsConstructor
public class Angebot {

    LocalDate date; // from when
    long euros;
    String completeString;
    String responsible;
    long probability;

    public String toString() {
        String dateStr = getDateStr();
        String eurStr = getEurStr();
        String probStr = getProbStr();
        StringBuffer result = new StringBuffer();
        result.append(dateStr);
        appendEmpties(11, result);
        result.append(probStr);
        appendEmpties(16, result);
        appendEmpties(16 + 11 - eurStr.length(), result);
        result.append(eurStr);
        appendEmpties(31, result);
        result.append(getResponsibleStr());
        //appendEmpties(36, result);
        result.append("       -->       " + completeString);
        return result.toString();
    }

    public String getResponsibleStr() {
        return responsible != null ? responsible : "???";
    }

    public String getProbStr() {
        String probStr = ".?%";
        if (probability > 0) {
            probStr = "" + probability + "%";
        }
        return probStr;
    }

    public String getEurStr() {
        String eurStr = "EUR ....?";
        if (euros > 0) {
            eurStr = "EUR " + euros;
        }
        return eurStr;
    }

    public String getDateStr() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.YYYY");

        String dateStr = "yyyy-mm-dd";
        if (date != null) {
            dateStr = date.format(dateFormat);
        }
        return dateStr;
    }

    public String getCompleteString() {
        return completeString;
    }

    private void appendEmpties(int until, StringBuffer data) {
        while (data.length() < until) {
            data.append("_");
        }
    }

}
