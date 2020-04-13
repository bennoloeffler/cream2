package bel.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tries to find correct contact data from a text (e.g. email signature).
 * Heuristics:
 * 1 find email adresses. (remove those lines from the first pass)
 * 2 find mobile numbers. (remove those lines from the first pass)
 * 3 find normal phone numbers. (remove line)
 * 4 zip and city (remove line)
 * in the remaining lines, assume, that the first or the second line6 Name (Ähnlichkeit mit String vor @ in Mail)
 * is the company name and the completeString / person name and the function.
 * 5 in the remaining lines, check the last name from the mail (with unsharp search)
 * 6 the remaining should be the first name and the completeString (check for Dr. or Prof.) (remove line)
 * 7 in the remaining there should be the company webpage. Check from the domain of the email and for link. (remove)
 * 8 in the remaining, there should be company name. Check from the domain. Check for AG, GmbH, etc
 * 9 the remainging probably is the funktion/department (Projektleiter, Geschäftsführer, Einkauf, Prokurist, ...)
 * <p>
 * 5 Firmenname (AG, gmbh...)
 * <p>
 * Das Resultat ist also eine von hinten nach vorne sortierte Liste von MatchResults (value start end
 * <p>
 * 6 Funktion / Abt
 */
@Getter
public class AdressMagic {

    // original
    String text;

    //
    // extracted fields
    //

    String mrMrs = ""; // no magic yet
    String title = "";
    /**
     * if Dr. or Prof. it is set to that with an railing space
     */
    String titleInName = "";
    String christianNames = "";
    String surName = "";
    String mobile = "";
    String phone = "";
    String fax = "";
    String email = "";
    String functionDepartment ="";

    String company = "";
    String streetAndNr = "";
    String postbox = "";
    String zipCode = "";
    String town = "";
    String www = "";

    List<String> unrecognized;

    //
    // working data
    //

    private final String[] titles = {"Dr.-Ing. ", "Dr. ", "Prof. ", "Dipl.-Wirtsch.-Ing. ", "Dipl.-Wi.-Ing. ", "Dipl.-Wirt.-Ing. ", "B.A. ", "BBA ", "B.Sc. ", "LL.B. ", "B.Ed. ", "B.Eng. ", "B.F.A. ", "B.Mus. ", "B.M.A ", "M.A. ", "M.Sc. ", "M.Eng. ", "LL.M. ", "M.F.A. ", "M.Mus. ", "M.Ed. "}; // https://de.wikipedia.org/wiki/Liste_akademischer_Grade_(Deutschland)
    private final String[] departments = {"CEO", "CTO", "Vertrieb", "Konstruktion", "Software", "Entwicklung", "Innovation", "Logistik", "Materialwirtschaft", "Projektleiter", "Projektmanagement", "Einkauf", "Personal", "Produktion", "Geschäftsführer", "Leiter", "Teamleiter", "Gruppenleiter", "Head of", "Geschäftsführ", "Geschäftsleit"};
    private final String[] rechtsformen = {" GmbH", " AG", " GbR"};

    List<String> lines; // put the ext to lines

    List<String> allMails;
    ArrayList<String> allMobiles;
    ArrayList<String> allPhoneFax;

    public AdressMagic(String text) {
        this.text = text;
        doTheMagic();
    }

    private void doTheMagic() {
        //lines = Arrays.asList(text.split(",|\\R+"));

        lines = new ArrayList<>(Arrays.asList(text.split(",|\\R+")));

        // remove emty lines
        List<String> empty = new ArrayList<>();
        empty.add("");
        lines.removeAll(empty);
        // remove the TODOs:
        List<String> toRemove = new ArrayList<>();
        for (String line: lines) {
            if (line.matches("^(todo|Todo|TODO):.*")) {
                toRemove.add(line);
            }
        }
        lines.removeAll(toRemove);



        //
        // find email
        //

        List<String> mailLines = lines.stream().filter(str -> hasMail(str)).collect(Collectors.toList());
        lines.removeAll(mailLines);

        allMails = new ArrayList();
        for (String mailLine : mailLines) {
            allMails.addAll(RegexUtils.findEmailAdress(mailLine));
        }
        allMails = allMails.stream().distinct().collect(Collectors.toList());


        //
        // find mobile
        //

        List<String> mobileLines = lines.stream().filter(str -> hasMobile(str)).collect(Collectors.toList());
        lines.removeAll(mobileLines);

        allMobiles = new ArrayList();
        for (String mobilesLine : mobileLines) {
            allMobiles.addAll(RegexUtils.findGermanMobileNumbers(mobilesLine));
        }


        //
        // find phone fax
        //

        List<String> phoneFaxLines = lines.stream().filter(str -> hasPhoneFax(str)).collect(Collectors.toList());
        lines.removeAll(phoneFaxLines);

        // SPLIT, based on the usual marker (Fax, T: phone, ...)
        allPhoneFax = new ArrayList();
        for (String phoneFaxLine : phoneFaxLines) {
            allPhoneFax.addAll(RegexUtils.findGermanPhoneFaxNumbers(phoneFaxLine));
        }


        //
        // find zip city
        //

        List<String> zipCountryLines = lines.stream().filter(str -> hasZipCountry(str)).collect(Collectors.toList());
        if(zipCountryLines.size() > 0) lines.remove(zipCountryLines.get(0));
        if(zipCountryLines.size() > 0) { // ignore the rest...
            String zipTown = RegexUtils.findZipAndTown(zipCountryLines.get(0)).get(0);
            zipCode = zipTown.substring(0,5);
            town = zipTown.substring(6, zipTown.length());
        }

        //
        // guess all the rest based on email or heuristics
        //

        String companyWWWGuess = guessCompanyWWWByMailDomain(lines, mailLines);
        lines.remove(companyWWWGuess);

        String personNameGuess = guessPersonNameByMailName(lines, mailLines); // Dr.-Ing. Hugo von der Vogelweide
        lines.remove(personNameGuess);

        String companyNameGuess = guessCompanyNameByMailDomain(lines, mailLines); // Bosch GmbH <-- (hugo.vd.vogelweide@bosch.com)
        lines.remove(companyNameGuess);

        String streetGuess = guessStreetAndNr(lines);
        lines.remove(streetGuess);

        String functionDepartmentGuess = guessFunctionDepartment(lines);
        lines.remove(functionDepartmentGuess);


        // just remember the unrecognized rest - because that could be useful for the user.
        unrecognized = lines;


        //
        // put all the data together
        //

        // do emails
        for (String m : allMails) {
            if (email != "") {
                email += ", ";
            }
            email += m;
        }

        // do mobiles
        for (String m : allMobiles) {
            if (mobile != "") {
                mobile += ", ";
            }
            mobile += m;
        }

        // do PhoneFax
        // fist is phone, second is fax...
        if(allPhoneFax.size()>0) {phone = allPhoneFax.get(0);}
        if(allPhoneFax.size()>1) {fax = allPhoneFax.get(1);}
        if(allPhoneFax.size()>2) {
            System.out.println("IGNORING additional phoneFax...");
        }
        /*
        for (String m : allPhoneFax) {
            if (phone != "") {
                phone += ", ";
            }
            phone += m;
        }*/

        mrMrs="Herr"; // just guess - because most...

    }

    private String guessStreetAndNr(List<String> lines) {
        for(String streetCandidate: lines) {
            if(streetCandidate.toLowerCase().matches("[-öäüÖÄÜßa-zA-Z ]*(str\\.|straße|strasse|allee|gasse|weg|steige)[\\s-\\d]{0,9}[^\\d]{0,4}")){
                streetAndNr = streetCandidate.trim();
                return streetCandidate;
            }
            if(streetCandidate.toLowerCase().matches("[-öäüÖÄÜßa-zA-Z ]*[-\\d]{1,6}.*")){
                streetAndNr = streetCandidate.trim();
                return streetCandidate;
            }
        }
        return "";
    }


    private String guessCompanyWWWByMailDomain(List<String> lines, List<String> mailLines) {
        if (allMails.size() > 0) {
            String domain = "";
            for (String mailAdress : allMails) {
                String[] split = mailAdress.split("@");
                assert (split.length == 2);
                domain = split[1];
                for (String remainingLine : lines) {
                    if (remainingLine.contains(domain)) {
                        //assume that this is "the www line"
                        try {
                            www = RegexUtils.findWithRegex(remainingLine, "([\\w-]+\\.)+[a-z]{2,}", 0).get(0);
                        } catch (Exception e) {
                            // ignore...
                        }
                        //if (www == null || www == "") {
                            www = domain;
                        //}
                        return remainingLine;
                    }
                }
            }

            // guess the first emails domain, if no other line found...
            String[] split = allMails.get(0).split("@");
            www = split.length == 2 ? split[1] : "";
            return www;
        }

        for(String line: lines) {
            // make @domain.com notation possible
            try {
                www = RegexUtils.findWithRegex(line, "@(([\\w-]+\\.)+[a-z]{2,})", 1).get(0);
            } catch (Exception e) {
                // ignore...
            }
            if (www != null && !www.equals("")) {
                //www = line;
                return line;
            }
        }
        return "";
    }


    private String guessPersonNameByMailName(List<String> lines, List<String> mailLines) {

        if (allMails.size() > 0) {
            for (String mailAdress : allMails) {
                String[] split = mailAdress.split("@");
                assert (split.length == 2);
                String name = split[0];
                String mostSimilar = "";
                double similarity = -1;
                for (String remainingLine : lines) {
                    if (!"".equals(remainingLine)) {
/*                    boolean containsTitle = false;
                    for (int i = 0; i < titles.length; i++) {
                        if(remainingLine.contains(titles[i])) {
                            containsTitle = true;
                            break;
                        }
                    }
                    if (containsTitle) {
                        splitTitleAndAllNames(remainingLine);
                        return remainingLine;
                    } else {
*/
                        String bestFit = StringSimilarity.findBestSimilarityByEqualLenthMatch(name.toLowerCase(), remainingLine.toLowerCase());
                        double currentSimilarity = StringSimilarity.bestLastSimilarity;
                        //System.out.println("distance: " + currentSimilarity+ " " + companyName + " <--> " + remainingLine);
                        if (currentSimilarity > similarity) {
                            mostSimilar = remainingLine;
                            similarity = currentSimilarity;
                            //System.out.println("MOST: " + mostSimilar);
                        }
                        // }
                    }
                }
                if (similarity > 0.3) {
                    splitTitleAndAllNames(mostSimilar);
                    return mostSimilar;
                }
            }
        }

        // take something that is near to a name...
        String fullName = null;
        for(String line:lines) {
            try {
                fullName = RegexUtils.findWithRegex(line, "^[^1-9@]+", 0).get(0);
            } catch (Exception e) {
                // ignore...
            }
            if(line.equals(fullName) && line.length() > 10 && lines.size() < 2){
                splitTitleAndAllNames(fullName);
                return fullName;
            }
        }

        return "";
    }

    private void splitTitleAndAllNames(String remainingLine) {
        title = "";
        for (int i = 0; i < titles.length; i++) {
            String s = titles[i];
            if (remainingLine.contains(s)) {
                title += s;
                remainingLine = remainingLine.replace(s, ""); // remove it
            }
        }
        title = title.trim();
        if(title.startsWith("Dr.")){
            titleInName = "Dr. ";
        }
        if(title.startsWith("Prof.")){
            titleInName = "Prof. ";
        }

        String[] split = remainingLine.split(" ");
        if (split.length == 0) {
            christianNames = "";
            surName = "";
        } else if (split.length == 1) {
            christianNames = "";
            surName = split[0];
        } else if (split.length == 2) {
            christianNames = split[0];
            surName = split[1];
        } else {
            if(remainingLine.contains(" von ")) {
                int trennenAb = remainingLine.indexOf(" von ");
                christianNames = remainingLine.substring(0, trennenAb);
                surName = remainingLine.substring(trennenAb+1, remainingLine.length());
            } else if(remainingLine.contains(" de ")) {
                int trennenAb = remainingLine.indexOf(" de ");
                christianNames = remainingLine.substring(0, trennenAb);
                surName = remainingLine.substring(trennenAb+1, remainingLine.length());
            } else { // just take the last as surname and all the rest as first names
                surName = split[split.length-1];
                for (int i = 0; i < split.length-1; i++) {
                    String s = split[i];
                    christianNames = christianNames + " " + s;
                }
            }

            // handle all the double names... Probalby, there are only some cases:
            // 1 von der... de... etc. Adelstitel im NAchnamen
            // 2 Nachnamen mit Bindestrich (nicht vorgeschrieben aber normal)
            // 3 Vornamen mit Leerzeichen
            //christianNames = "not yet implemented";
            //surName = "not yet implemented";
        }
    }


    private String guessCompanyNameByMailDomain(List<String> lines, List<String> mailLines) {
        if (allMails.size() > 0) {
            for (String mailAdress : allMails) {
                String[] split = mailAdress.split("@");
                assert (split.length == 2);
                String domain = split[1];
                // TODO: make save
                String companyName = domain.split("\\.")[0];
                String mostSimilar = "";
                double similarity = -1;
                for (String remainingLine : lines) {
                    if(!"".equals(remainingLine)) {
                        String companyNameTmp = companyName;
                        // if there is a position like CEO or company name AES - don't match, because of one fitting one character = 33%
                        while(remainingLine.length() < companyNameTmp.length()) {
                            remainingLine += "#";
                        }
                        while(companyNameTmp.length() < remainingLine.length() ) {
                            companyNameTmp += "#";
                        }
                        StringSimilarity.findBestSimilarityByEqualLenthMatch(companyNameTmp.toLowerCase(), remainingLine.toLowerCase());
                        double currentSimilarity = StringSimilarity.bestLastSimilarity;
                        //System.out.println("distance: " + currentSimilarity+ " " + companyName + " <--> " + remainingLine);
                        if (currentSimilarity > similarity) {
                            mostSimilar = remainingLine;
                            similarity = currentSimilarity;
                            //System.out.println("MOST: " + mostSimilar);

                        }
                    }
                }
                if (similarity > 0.3) {
                    company = mostSimilar;
                    return mostSimilar;
                }

                // if that did not work, try "GmbH, etc."
                for (String remainingLine : lines) {
                    for (int i = 0; i < rechtsformen.length; i++) {
                        String r = rechtsformen[i];
                        if (remainingLine.contains(r)) {
                            company = remainingLine;
                            return remainingLine;
                        }
                    }
                }
            }
        }
        return "";
    }


    @Override
    public String toString() {
        return "AdressMagic{" +
                "mrMrs='" + mrMrs + '\'' +
                ", completeString='" + title + '\'' +
                ", christianNames='" + christianNames + '\'' +
                ", surName='" + surName + '\'' +
                ", mobile='" + mobile + '\'' +
                ", phone='" + phone + '\'' +
                ", fax='" + fax + '\'' +
                ", email='" + email + '\'' +
                ", functionDepartment='" + functionDepartment + '\'' +
                ", company='" + company + '\'' +
                ", streetAndNr='" + streetAndNr + '\'' +
                ", postbox='" + postbox + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", town='" + town + '\'' +
                ", www='" + www + '\'' +
                ", unrecognized=" + unrecognized +
                '}';
    }

    private String guessFunctionDepartment(List<String> lines) {
        // based on similarity to common department or function names...
        if(lines.size() == 1) {
            functionDepartment = lines.get(0);
            return lines.get(0);
        } else if (lines.size() == 0) {
            return "";
        } else {
            String bestFittedLine = "";
            double bestFit = -1;
            for (String line: lines) {
                if (!"".equals(line)) {

                    for (int i = 0; i < departments.length; i++) {
                        String department = departments[i];
                        String fit = StringSimilarity.findBestSimilarityByEqualLenthMatch(department, line);
                        if (StringSimilarity.bestLastSimilarity > bestFit) {
                            bestFit = StringSimilarity.bestLastSimilarity;
                            bestFittedLine = line;
                        }
                    }
                }
            }
            if(bestFit>0.7) {
                functionDepartment = bestFittedLine;
                return  bestFittedLine;
            }
        }
        return "";
    }

    private boolean hasZipCountry(String str) {
        return RegexUtils.findZipAndTown(str).size() > 0;
    }

    private boolean hasPhoneFax(String str) {
        return RegexUtils.findGermanPhoneFaxNumbers(str).size() > 0;
    }

    private boolean hasMobile(String str) {
        return RegexUtils.findGermanMobileNumbers(str).size() > 0;
    }

    private boolean hasMail(String str) {
        return RegexUtils.findEmailAdress(str).size() > 0;
    }
}
