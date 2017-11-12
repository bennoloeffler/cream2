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
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.YYYY");

        String dateStr = "????-??-??";
        if (date != null) {
            dateStr = date.format(dateFormat);
        }
        //String eur = NumberFormat.getCurrencyInstance().format(euros);
        String eur = "EUR ?????";
        if (euros > 0) {
            eur = "EUR " + euros;
        }
        String prob = "??%";
        if (probability > 0) {
            prob = "" + probability + "%";
        }
        StringBuffer result = new StringBuffer();
        result.append(dateStr);
        appendEmpties(11, result);
        result.append(prob);
        appendEmpties(16, result);
        appendEmpties(16 + 11 - eur.length(), result);
        result.append(eur);
        appendEmpties(31, result);
        result.append(responsible != null ? responsible : "???");
        //appendEmpties(36, result);
        result.append("       -->       " + completeString);
        return result.toString();
    }

    private void appendEmpties(int until, StringBuffer data) {
        while (data.length() < until) {
            data.append("_");
        }
    }

}
