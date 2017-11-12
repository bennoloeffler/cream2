package bel.en.helper;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.*;

/**
 * Made for finding different types of date descriptions in natural text - especially in Notes written by Nicole :-)
 * The parsing is unprecise in nature, eg. "Summer 2016", or "End of December".
 * There may be even assumptions, e. g. regarding the year in "end of February" - if it is July, probably the
 * February of the next year is meant, not this year.
 * Therefore, parsing and heuristics are used, to determine a "mostly probable assumed date" for action.
 * A "precision assumption" is not made (end of december is far less precise than "31.12.2016 00:00:00",
 * since we can assume the year (but we have to guess) and there is no sec, no min, no hour, no day.
 * Precision is always "day".
 *
 * Those formats are recognized:
 * KW32
 * 14.3
 * 13.5.2017
 * Dezember
 * Dez (exactly 3 first letters, first letter big, other too small)
 *
 * This is not possible:
 * in 5 Wochen
 * Ende 2016
 * Sommer 2016
 * Anfang Q1
 * Ende Q2
 * Q3
 * nach LF
 *
 * Falls mehrere Termine in String stehen: Erst der genaueste dd.MM.yy, then dd.MM, then KW, then Month
 */
public class DateFinder {
    private boolean estimating;
    private Calendar calEstimated;

    private DateFinder() {
    }

    public DateFinder(String stringWithDate) {
        calEstimated = getEstimatedCalendar(stringWithDate);
    }

    public Calendar get(){
        return calEstimated;
    }

    /**
     *
     * @param s string, to search for the date
     * @return a calendar, that has an estimated year (e.g Jan jumps to the next year, if we are in July)
     */
    public Calendar getEstimatedCalendar(String s) {
        long WEEK_IN_MS = 1000*60*60*24*7;
        Calendar cal = getCalendar(s);
        if(cal != null) {
            if(estimating) {
                long calInMS = cal.getTimeInMillis();
                long now = new GregorianCalendar(Locale.GERMAN).getTimeInMillis();
                if (calInMS < now - 8 * WEEK_IN_MS) {
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
                }
            }
        }
        return cal;
    }

    // get Date and DONT estimate Year! But set the marker for the getEstimatedCalendar function
    public Calendar getCalendar(String stringToParse) {
        String found = null;
        Calendar cal;

        // 1: normal date tt.mm.yy MOST PRECISE first
        found = findStrDayMonthYear(stringToParse);
        if(found != null) {
            // DO NOT ESTIMATE...
            estimating = false;
            cal = calcInMsFromTTMMYY(found);
        } else {
            // 2: normal date tt.mm. Potentially not precise... therefore estimate
            found = findStrDayMonth(stringToParse);
            if (found != null) {
                estimating = true;
                cal = calcInMsFromTTMM(found);
            } else {
                // 3: KW this may be although imprecise. Estimate.
                found = findStrKW(stringToParse);
                if (found != null) {
                    estimating = true;
                    cal = calcInMsFromKW(found);
                } else {
                    // 4: Month. Most imprecise. Estimate.
                    found = findStrMonth(stringToParse);
                    if (found != null) {
                        estimating = true; // Attention: if year is available, estimating true will be set back!
                        cal = calcInMsFromMonth(found);
                    } else {
                        return null;
                    }
                }
            }
        }
        return cal;
    }

    private Calendar calcInMsFromTTMMYY(String found) {
        LocalDate date = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        try {
            date = LocalDate.parse(found, formatter);
        } catch (Exception e) {
            try {
                formatter = DateTimeFormatter.ofPattern("d.MM.yy");
                date = LocalDate.parse(found, formatter);
            } catch (Exception e1) {
                try {
                    formatter = DateTimeFormatter.ofPattern("dd.M.yy");
                    date = LocalDate.parse(found, formatter);
                } catch (Exception e2) {
                    try {
                        formatter = DateTimeFormatter.ofPattern("d.M.yy");
                        date = LocalDate.parse(found, formatter);
                    } catch (Exception e3) {
                        formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                        try {
                            date = LocalDate.parse(found, formatter);
                        } catch (Exception e4) {
                            try {
                                formatter = DateTimeFormatter.ofPattern("d.MM.yyyy");
                                date = LocalDate.parse(found, formatter);
                            } catch (Exception e5) {
                                try {
                                    formatter = DateTimeFormatter.ofPattern("dd.M.yyyy");
                                    date = LocalDate.parse(found, formatter);
                                } catch (Exception e6) {
                                    try {
                                        formatter = DateTimeFormatter.ofPattern("d.M.yyyy");
                                        date = LocalDate.parse(found, formatter);
                                    } catch (Exception e7) {
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new GregorianCalendar(date.getYear(),date.getMonth().getValue()-1,date.getDayOfMonth() );
    }

    private Calendar calcInMsFromTTMM(String found) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.");
        // append the current year to the end of the string - otherwise, it can't be parsed... Ugly workaround
        Calendar now = new GregorianCalendar(Locale.GERMAN);
        long year = now.get(Calendar.YEAR);
        String yearStr = Long.toString(year);
        found = found + yearStr;
        return calcInMsFromTTMMYY(found);
    }

    // may be Dez or Dezember or Dez 2016 or Dezember 2016
    private Calendar calcInMsFromMonth(String found) {
        Calendar cal = new GregorianCalendar(Locale.GERMAN);
        int year = cal.get(Calendar.YEAR);
        int month = -1;
        String[] split = found.split(" ");
        if(split.length == 1) {
            // no year appended...
           // System.out.println("split: " + split[0]);
        } else if(split.length == 2)  {
            year = Integer.parseInt(split[1]);
            found = split[0];
            estimating = false;
        } else {
            System.out.println("error!");
        }

        if("Jan".equalsIgnoreCase(found) || "Januar".equalsIgnoreCase(found)) {
            month = Calendar.JANUARY;
        } else if ("Feb".equals(found) || "Februar".equals(found)) {
            month = Calendar.FEBRUARY;
        } else if("Mär".equalsIgnoreCase(found) || "M&#228;rz".equalsIgnoreCase(found) || "März".equalsIgnoreCase(found) || "Mae".equalsIgnoreCase(found) || "Maer".equalsIgnoreCase(found) || "Maerz".equalsIgnoreCase(found)) {
            month = Calendar.MARCH;
        } else if("Apr".equalsIgnoreCase(found) || "April".equalsIgnoreCase(found)) {
            month = Calendar.APRIL;
        } else if("Mai".equalsIgnoreCase(found) || "Mai".equalsIgnoreCase(found)) {
            month = Calendar.MAY;
        } else if("Jun".equalsIgnoreCase(found) || "Juni".equalsIgnoreCase(found)) {
            month = Calendar.JUNE;
        } else if("Jul".equalsIgnoreCase(found) || "Juli".equalsIgnoreCase(found)) {
            month = Calendar.JULY;
        } else if("Aug".equalsIgnoreCase(found) || "August".equalsIgnoreCase(found)) {
            month = Calendar.AUGUST;
        } else if("Sep".equalsIgnoreCase(found) || "September".equalsIgnoreCase(found)) {
            month = Calendar.SEPTEMBER;
        } else if("Okt".equalsIgnoreCase(found) || "Oktober".equalsIgnoreCase(found)) {
            month = Calendar.OCTOBER;
        } else if("Nov".equalsIgnoreCase(found) || "November".equalsIgnoreCase(found)) {
            month = Calendar.NOVEMBER;
        } else if("Dez".equalsIgnoreCase(found) || "Dezember".equalsIgnoreCase(found)) {
            month = Calendar.DECEMBER;
        }
        cal.clear();
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        return cal;
    }

    // found needs to be an int...
    public Calendar calcInMsFromKW(String found) {
        int kwi = Integer.parseInt(found);
        CalendarWeek kw = new CalendarWeek(kwi, Locale.GERMANY);
        Calendar cal = new GregorianCalendar(Locale.GERMANY);
        cal.clear();
        cal.setTimeInMillis(kw.getStart().getTime());
        return cal;
    }

    // just return the digit of the KWxy String - the xy
    public String findStrKW(String s) {

        // find KWxy
        Pattern p = Pattern.compile(".*(KW ?[0-9]+).*");
        Matcher m = p.matcher(s);

        if (m.matches()) {
            String kw = m.group(1);
            p = Pattern.compile(".*([0-9]+).*");
            m = p.matcher(kw);
            if (m.matches()) {
                String number = m.group();
                number = number.substring(2); // just skip the KW
                number = number.trim();
                return number;
            }
        }
        return null;
    }

    public String findStrMonth(String s) {
        //find Months
        Pattern p = Pattern.compile(".*(\\s|>)((Jan|Januar|Feb|Februar|M&#228;rz|Maer|Mae|Maerz|Mär|März|Apr|April|Mai|Jun|Juni|Jul|Juli|Aug|August|Sep|Sept|September|Okt|Oktober|Nov|November|Dez|Dezember)( \\d\\d\\d\\d)?)(\\s|<|:)?.*");
        Matcher m = p.matcher(s);
        if (m.matches()) {
            return m.group(2);
        } else {
            return null;
        }
    }

    public String findStrDayMonth(String s) {
        //find Days.Months
        Pattern p1 = Pattern.compile(".*(\\s|>)([0-9][0-9]\\.[0-9][0-9]\\.)(\\s|<|:|&)?.*");
        Pattern p2 = Pattern.compile(".*(\\s|>)([0-9]\\.[0-9][0-9]\\.)(\\s|<|:|&)?.*");
        Pattern p3 = Pattern.compile(".*(\\s|>)([0-9][0-9]\\.[0-9]\\.)(\\s|<|:|&)?.*");
        Pattern p4 = Pattern.compile(".*(\\s|>)([0-9]\\.[0-9]\\.)(\\s|<|:|&)?.*");
        Matcher m1 = p1.matcher(s);
        Matcher m2 = p2.matcher(s);
        Matcher m3 = p3.matcher(s);
        Matcher m4 = p4.matcher(s);
        if (m1.matches()) {
            String match = m1.group(2);
            return match;
        } else if (m2.matches()) {
            String match = m2.group(2);
            return match;
        } else if (m3.matches()) {
            String match = m3.group(2);
            return match;
        } else if (m4.matches()) {
            String match = m4.group(2);
            return match;
        } else   {
            return null;
        }
    }

    public String findStrDayMonthYear(String s) {
        //find Days.Months
        /*
        Pattern p1 = Pattern.compile(".*([0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9][0-9]*)\\s.*");
        Pattern p2 = Pattern.compile(".*([0-9]\\.[0-9][0-9]\\.[0-9][0-9][0-9]*)\\s.*");
        Pattern p3 = Pattern.compile(".*([0-9][0-9]\\.[0-9]\\.[0-9][0-9][0-9]*)\\s.*");
        Pattern p4 = Pattern.compile(".*([0-9]\\.[0-9]\\.[0-9][0-9][0-9]*)\\s.*");
        */
        Pattern p1 = Pattern.compile(".*([0-9][0-9]\\.[0-9][0-9]\\.([0-9][0-9][0-9][0-9]|[0-9][0-9]))(\\s|<|:|&)?.*");
        Pattern p2 = Pattern.compile(".*([0-9]\\.[0-9][0-9]\\.([0-9][0-9][0-9][0-9]|[0-9][0-9]))(\\s|<|:|&)?.*");
        Pattern p3 = Pattern.compile(".*([0-9][0-9]\\.[0-9]\\.([0-9][0-9][0-9][0-9]|[0-9][0-9]))(\\s|<|:|&)?.*");
        Pattern p4 = Pattern.compile(".*([0-9]\\.[0-9]\\.([0-9][0-9][0-9][0-9]|[0-9][0-9]))(\\s|<|:|&)?.*");

        Matcher m1 = p1.matcher(s);
        Matcher m2 = p2.matcher(s);
        Matcher m3 = p3.matcher(s);
        Matcher m4 = p4.matcher(s);
        if (m1.matches()) {
            String match = m1.group(1);
            return match;
        } else if (m2.matches()) {
            String match = m2.group(1);
            return match;
        } else if (m3.matches()) {
            String match = m3.group(1);
            return match;
        } else if (m4.matches()) {
            String match = m4.group(1);
            return match;
        } else   {
            return null;
        }
    }


    static class CalendarWeek {
        int weekOfYear;

        Date start;

        Date end;

        public CalendarWeek(final int weekOfYear,final Locale locale) {
            this.weekOfYear = weekOfYear;

            final GregorianCalendar calendar = new GregorianCalendar(locale);
            final int CURRENT_YEAR = calendar.get(Calendar.YEAR);
            calendar.clear();
            calendar.set(Calendar.YEAR,CURRENT_YEAR);
            calendar.set(Calendar.WEEK_OF_YEAR, this.weekOfYear);

            this.start = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 6);
            this.end = calendar.getTime();
        }

        public Date getEnd() {
            return end;
        }

        public Date getStart() {
            return start;
        }

        public int getWeekOfYear() {
            return weekOfYear;
        }
    }
}


/**


 [ ] NIT: KW52 TErmin für BEL, der ruft Michael Steinbach zum 3.0 WS an Tel.: 09771-68877-175    ( Michael + Torsten Steinbach (Steinbach) )
 [ ] NIT: wieder nachfassen Jan 2017    ( Herr Ritter (GROZ-Beckert) )
 [ ] AZA:&nbsp;KW4 ruft AZA ihn wieder an    ( Bayer, Reiner Heise (FRIMO Lotte) )
 [ ]  NIT: Termin Östereich mit Oliver mit BEL machen (erst KW48 dazu anrufen - s. Mail)    ( Oliver Eller (BOS) )
 [ ] NIT: wieder anfragen Jan 2017    ( Marco Schaible (Bürkle) (früher Horst Wickl) )
 [ ] &nbsp;BEL: 01.02.2017 Aufgeräumt?    ( Dr. Hanrath (Leitz) )
 [ ] NIT: Termin bestätigen lassen KW3    ( Gerhard Bolz (Liebherr) )
 [ ] NIT: Jan 2017 wieder nachfragen    ( Ralf Winkler (SCHUNK) )
 [ ] &nbsp;BEL: 31.1.2017 7P gelesen?    ( Hr Groth (Berliner Luft) )
 [ ] BEL: 27.2.17 Entlassungen durch? Angebot jetzt beauftragen?    ( Uwe Schwanke, Jochen Gaßmann (BMA) )
 [ ] BEL: Nov 2016 EMail an Hr. Fingerloos 07351-571720&nbsp;(Thema Bosch/Kapp Durchlaufzeitreduzierung / LF)    ( Frank Fingerloos (Vollmer) )
 [ ] NIT: Gastgeberrolle angefragt am 19.10, er ist bereit, aber erst nächstes Jahr. Feb 2017 wieder anfragen    ( Frank Fingerloos (Vollmer) )
 [ ] BEL: auf Wunsch von Hr. Körner Treffen am 21.12. in Hannover, wie dann weiter?    ( Ingo Körner (Broetje Automation) )
 [ ] BEL: Termin am 04.12. dort vor Ort, wie dann weiter?    ( Dr. Enkrich (Physik Instrumente (PI)) )
 [ ] MIK BEB: 2.12.16 Anruf nachfassen Frau Pöppe    ( Pöppe, Marlies (Hermann Paus Maschinenfabrik GmbH) )
 [ ] MIK: Treffen mit Sundermann und Jörg Priese     ( Sundermann, Frank (Durch Denken Vorne Consult GmbH) )
 [ ] MIK NIT: 28.11.16 Nachfassen&nbsp;    ( Möller, Godehard (Venjakob )
 [ ] MIK NIT: 18.1.17 nachfassen und Termin für Denkwerkstatt mit relevantem Problem vereinbaren    ( Nauen, Andreas (KTR Kupplungstechnik GmbH) )
 [ ] BEL: 20.12. Termin mit Herrn Schulte, wie dann weiter?    ( KW41, DENKWERKSTATT vor Ort (ZAMB) )
 [ ] NIT: Juli 2017 mal wieder nachfragen    ( Ingo Arendt (ENERCON) )
 [ ] BEL: zu Wittenstein &nbsp;mit LAV sprechen    ( Werner Wind, Anna-Katherina Wittenstein (Wittenstein SE) )
 [ ] NIT: Gastgeberrolle AM angefragt, wurde abgelehnt. 01.07.17 wieder nachfragen    ( Werner Wind, Anna-Katherina Wittenstein (Wittenstein SE) )
 [ ] NIT: restlichen 25% im Dez16 abrechnen?    ( Köhler, Sven (VEMAG GmbH) )
 [ ] MIK: 07.11.16 ggf. Anruf Herr Köhler &nbsp;wegen Eskalation    ( Köhler, Sven (VEMAG GmbH) )
 [ ] BEL: Gastgeberrolle Agiler Maschinenbau wolltest du anfragen, KW45 (gerne vorab telef und die Details schicke ich dann per Mail)    ( Börne Rensing (Franz Kessler) )
 [ ] BEL: Kessler könnte was für den Lieferanten-Tag sein    ( Börne Rensing (Franz Kessler) )
 [ ] NIT: 15.01.17 wieder&nbsp;nachfragen, s.u.    ( Börne Rensing (Franz Kessler) )
 [ ] NIT: hat er sich gemeldet? KW1    ( Herr Dormann (KLÖCKNER DESMA) )
 [ ] NIT: bei Alfred nachfragen, KW2    ( Hr. Sirmann (harmonicdrive) )
 [ ] NIT: wann ist BEL in der CH? KW1 wieder schauen    ( Sergio Galante (nicht mehr bei Jossi, wieder bei Bosch!) )
 [ ] NIT: ist MAW wieder im Boot? Sonst mit BEL besprechen wie weiter? zurück an Vertrieb? KW50    ( Kirsten Hüttmann (Vossloh Locomotives) )
 [ ] NIT: Telefonat für Jan organisieren KW1    ( Barwan (BÖWE Systec) )
 [ ] BEL:&nbsp;Folgeprojekt abgesichert? 1.5.2017    ( Edgar Mörtl (Rohwedder) )
 [ ] NIT:&nbsp;zu Veranstaltungen einladen, KW2 - gibt es wieder welche?    ( Jürgen Feyerherd (Optima Lifescience) )
 [ ] NIT: wieder per Mail nachfragen, KW04    ( Matthias Winterberg (seca) )
 [ ] NIT: Interesse an einem Gespräch? KW48    ( Latusseck (SIHI) )
 [ ] NIT: mit Frau Worms neuen Termin suchen, 09.01.    ( Hr. Tepper (ELHA-Maschinenbau) )
 [ ] NIT: KW02 mal wieder nachfragen. Vortrag Kulturveränderungen Hr. Mäußler    ( Bernard Fenner (Hitachi Zosen, ex-AXPO) )
 [ ] NIT: jetzt mal wieder&nbsp;Telefonat&nbsp;mit BEL? Jan 2017 nachfragen und Gastgeberrolle nachfassen, dazu Mail geschrieben Ende 2016    ( Andrea Billig (HERKULES) )
 [ ] BEL: 3.12. Herr Reinhard wg. Shopfloor-M mit Stephan,&nbsp;07151 1262310    ( Martin Schwarz / Hr Reinhard (STIHL) )
 [ ] BEL: mit Hr. Schwarz zum weiteren Vorgehen telefonieren? Nov 2016    ( Martin Schwarz / Hr Reinhard (STIHL) )
 [ ] BEL: Verhandlung MAJ am 20.12. Ergebnis an Stihl kommunizieren    ( Martin Schwarz / Hr Reinhard (STIHL) )
 [ ] NIT: 25.11. wieder anrufen und Termin für BEL machen    ( Rolf Schepers (OSMA Aufzüge) )
 [ ] NIT: hat Herr Ehlert mit Frau Heine gesprochen und die Folie geschickt? KW48    ( Birgit Heine und Uwe Endruschat (Heraeus) )
 [ ] NIT: Termin nachfassen 31.12.    ( Brahms, Muckli, Kübler (Gleason Pfauter) )
 [ ] NIT: Angebot angenommen? KW48    ( Lauckner, Helmut Dr.-Ing. (Mafell) )
 [ ]     ( Martin Kapp (KAPP-Niles) )
 [ ] MIK / BEL: (Marie)&nbsp;Offerte Coaching und Führung KW 50    ( Martin Kapp (KAPP-Niles) )
 [ ] BEL: KW 4/2017 Orga-Anpassung anbieten (wenn Marie Signal gibt; s.u.)    ( Martin Kapp (KAPP-Niles) )
 [ ] MIK: KW 47 Disruptions-Workshop vorschlagen    ( Martin Kapp (KAPP-Niles) )
 [ ] MIK: KW 50&nbsp;Lieferantentag mit Heller organisieren    ( Martin Kapp (KAPP-Niles) )
 [ ] MIK / STS: Blitz-Kaizen / Kata: Kontakt herstellen in KW 47    ( Martin Kapp (KAPP-Niles) )
 [ ] MIK / BEL: M2-Termin am 13.01.17    ( Linnewedel (MB Petroleum Deutschland GmbH) )
 [ ] NIT: 23.11. erneuter Anruf Herr Geiger zum Angebot Denkwerkstatt    ( Geiger, Thomas (LEWA GmbH) )
 [ ] NIT: Bestellung nachfassen, wenn Agenda geklärt KW47 - ANS angeschrieben dazu    ( Brand, Alexander (Blefa) )
 [ ] BEL, NIT: 14.12. Auswahlverfahren beendet. Wie ist es für uns ausgegangen?    ( Herr Hilbrath (Fa. Kampf) )
 [ ] NIT: jetzt etwas entschieden? wie geht es weiter? 20.12.    ( Hr Böhnisch (Leistritz Pumpen) )
 [ ] BEL: Freitag Telefontermin. 25.11. 15:00    ( Hr Mirtschink (ATN Hölzel) )
 [ ] BEL: Juli 2017 Nachhaken    ( Herr Nunne (Phoenix  Contact) )
 [ ] BEL: 7.12. vorsichtig nachfragen... Gibts was neues?    ( Hr. Sponner, (Borsig ZM Compression GmbH) )
 [ ] NIT: Denkwerkstatt Feb durch AZA&nbsp;eingeladen? 06.01. AZA FRAGEN    ( Hr Lässing (FOCKE) )
 [ ] NIT:&nbsp;Termin vor Ort am 25.01. geht es weiter? sonst hier verschieben    ( Stefan Oswald (EOS GmbH Electro Optical Systems) )
 [ ] NIT: BEL: 5.12. Nachhaken. 2h Termin mit COO und uns (BEL und ggf. Gerhard Wohland?)    ( Dr. Volker Franke (Harting) )
 [ ] BEL: NIT: ANS: Angebot geschreiben. Dringend Termin machen zum Vorstellen. Macht ANS.    ( Hr. Metzger (Knoll) )
 [ ] BEL: Herrn Nagel (07033+60522) in Schweiz AGILE PA Ort einladen. KW10    ( Johannes Lintner (Nagel) )
 [ ] BEL: NIT: Referenzbesuch stattgefunden? KW50, nachfassen    ( Kleppel/Bartle (ARKU) )
 [ ] MIK: Termin vor Ort mit MRM am 19.01. wie dann weiter?    ( Hr Sprenger (NEUHAUS NEOTEC Maschinen- und Anlagenbau) )
 [ ] &nbsp;NIT: hat er ein Go &quot;vom Chef&quot; eingeholt? 05.12. nachfragen, dann Termin machen mit Alfred zusammen    ( Christof Siebert / Reiner Koettgen / Frau Buchfink  (Trumpf) )
 [ ] BEL: 01.01.&nbsp;Bruno Mendler Mail schreiben... 2 x sind Leute mitten im Prozess gegangen...    ( Daniel Laubscher, Bruno Mendler (BÜHLER) )
 [ ] BEL: Denkwerkstatt wird neu terminiert, hinterher Hr. Wassermann anrufen, Strategie-Workshop? Operations-Mann: Wann kommt er? KW1    ( Wassermann (MAN Roland) )
 [ ] BEL:     ( Albeck, Dr. König, Weis, Röpke, Fischer, ... (Bosch PA) )
 [ ] NIT: Termin abstimmen mit Herrn Waldmann, April 2017    ( Albeck, Dr. König, Weis, Röpke, Fischer, ... (Bosch PA) )
 [ ] BEL: nachhaken 1.2.2017    ( Florian Fischer (MAAG) )





 **/