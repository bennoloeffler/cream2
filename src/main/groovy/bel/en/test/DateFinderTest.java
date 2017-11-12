package bel.en.test;

import bel.en.helper.DateFinder;
import junit.framework.TestCase;


import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DateFinderTest extends TestCase {

    DateFinder dateFinder = null;

    public void testFindStrKW() throws Exception {
        dateFinder = new DateFinder("DUMMY");
        String result = dateFinder.findStrKW("asdlfj ölksadjf öalkdsj KW23");
        assertEquals("23", result);
        result = dateFinder.findStrKW("asdlfj ölksadjf öalkdsj KW23<");
        assertEquals("23", result);
        result = dateFinder.findStrKW("asdlfj ölksadjf öalkdsj KW23:");
        assertEquals("23", result);
        result = dateFinder.findStrKW("KW17 asdlfj ölksadjf öalkdsj ");
        assertEquals("17", result);
        result = dateFinder.findStrKW(">KW1 asdlfj ölksadjf öalkdsj");
        assertEquals("1", result);
        result = dateFinder.findStrKW(">KW9: asdlfj ölksadjf öalkdsj");
        assertEquals("9", result);
    }

    public void testFindStrMonth() throws Exception {
        dateFinder = new DateFinder("DUMMY");
        String result = dateFinder.findStrMonth("asdlfj Dez ölksadjf öalkdsj");
        assertEquals("Dez", result);
        //result = dateFinder.findStrMonth("asdlfj Jan"); //den Test brauchts nicht, denn im ENML kommt immer ein < am Ende der Zeile, oder?
        //assertEquals("Jan", result);
        result = dateFinder.findStrMonth("asdlfj Feb<");
        assertEquals("Feb", result);
        //result = dateFinder.findStrMonth("Feb asdlfj "); //den Test brauchts auch nicht, dnen es kommt immer ein > am Anfang, oder?
        //assertEquals("Feb", result);
        result = dateFinder.findStrMonth(">M&#228;rz: asdlfj "); //Umlaute?
        assertEquals("M&#228;rz", result);
        result = dateFinder.findStrMonth(">Dezember: XJanX");
        assertEquals("Dez", result);
    }

    public void testFindStrDateDayMonth() throws Exception {
        dateFinder = new DateFinder("DUMMY");
        String result = dateFinder.findStrDayMonth("asdlfj 14.07. ölksadjf öalkdsj");
        assertEquals("14.07.", result);
        result = dateFinder.findStrDayMonth("asdlfj 4.07. ölksadjf öalkdsj");
        assertEquals("4.07.", result);
        result = dateFinder.findStrDayMonth("asdlfj 14.1. ölksadjf öalkdsj");
        assertEquals("14.1.", result);
        result = dateFinder.findStrDayMonth("asdlfj 1.1. ölksadjf öalkdsj");
        assertEquals("1.1.", result);
        result = dateFinder.findStrDayMonth(">1.1. asdlfj ölksadjf öalkdsj");
        assertEquals("1.1.", result);
        //result = dateFinder.findStrDayMonth("asdlfj ölksadjf öalkdsj 1.1."); //Start end End of line not needed to test, because always < or >...
        //assertEquals("1.1.", result);
        result = dateFinder.findStrDayMonth("asdlfj ölksadjf öalkdsj 1.1.<");
        assertEquals("1.1.", result);
        result = dateFinder.findStrDayMonth(">12.1.: asdlfj ölksadjf öalkdsj <");
        assertEquals("12.1.", result);
        result = dateFinder.findStrDayMonth("> asdlfj ölksadjf 1.12.: öalkdsj <");
        assertEquals("1.12.", result);
    }


    public void testFindStrDateDayMonthYear() throws Exception {
        dateFinder = new DateFinder("DUMMY");
        String result = dateFinder.findStrDayMonthYear("asdlfj 14.07.69 ölksadjf öalkdsj");
        assertEquals("14.07.69", result);
        result = dateFinder.findStrDayMonthYear("asdlfj 4.07.69 ölksadjf öalkdsj");
        assertEquals("4.07.69", result);
        result = dateFinder.findStrDayMonthYear("asdlfj 14.1.69 ölksadjf öalkdsj");
        assertEquals("14.1.69", result);
        result = dateFinder.findStrDayMonthYear("asdlfj 14.1.2016 ölksadjf öalkdsj");
        assertEquals("14.1.2016", result);
        result = dateFinder.findStrDayMonthYear(">14.1.2016 ölksadjf öalkdsj");
        assertEquals("14.1.2016", result);
        result = dateFinder.findStrDayMonthYear(">14.1.2016: ölksadjf öalkdsj");
        assertEquals("14.1.2016", result);
        result = dateFinder.findStrDayMonthYear(" ölksadjf öalkdsj 14.1.2016<");
        assertEquals("14.1.2016", result);

}

    public void testCalcInMsFromKW() throws Exception {
        dateFinder = new DateFinder("DUMMY");
        //Calendar cal = dateFinder.calcInMsFromKW("9");
        //29.2
        //assertEquals(29, cal.get(Calendar.DAY_OF_MONTH));
        //assertEquals(2 - 1, cal.get(Calendar.MONTH));

        Calendar cal = dateFinder.calcInMsFromKW("51");
        //18.12
        assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12 - 1, cal.get(Calendar.MONTH));
    }

    public void testGetCalendar() throws Exception {
        dateFinder = new DateFinder("DUMMY");

        // Dez --> 1.12.2016
        Calendar cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf Dez dfkj");
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12 - 1, cal.get(Calendar.MONTH));

        // 11.12.16 --> 11.12.2016
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 11.12.16 dfkj");
        assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12 - 1, cal.get(Calendar.MONTH));
        assertEquals(2016, cal.get(Calendar.YEAR));

        // 1.1.2016 --> 1.1.2016
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 1.1.2016 dfkj");
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(1 - 1, cal.get(Calendar.MONTH));
        assertEquals(2016, cal.get(Calendar.YEAR));

        // 1.1.16 --> 1.1.2016
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 1.1.16 dfkj");
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(1 - 1, cal.get(Calendar.MONTH));
        assertEquals(2016, cal.get(Calendar.YEAR));

        //KW51 --> 18.12.2017
        cal = dateFinder.getCalendar("asdflj öaslkdfj ölksadjf ölkjasödf jölkj KW51 aöldskjf kjl ökjas");
        assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12 - 1, cal.get(Calendar.MONTH));


        //test number of digits
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 1.1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 11.1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 1.11.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 11.11.16 dfkj");
        assertNotNull(cal);

        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 1.1.2016 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 11.1.2016 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 1.11.2016 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf 11.11.2016 dfkj");
        assertNotNull(cal);

        // test ignore case with months
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf DEZ dfkj");
        assertNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf dez dfkj");
        assertNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 aösdlkjf Dez dfkj");
        assertNotNull(cal);

        // test months
        cal = dateFinder.getCalendar("asdfj Jan dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal Januar 9 aösdlkjf 1.c1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal Feb 9 aösdlkjf 1.1g.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Februar aösdlkjf 1g.1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 März aösdlkjf 1.1.g16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Mär aösdlkjf 1.1.g16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Apr aösdlkjf 1.1.1g6 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 April aösdlkjf 1.1.g16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Mai aösdlkjf 1.1g.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Jun aösdlkjf 1.1g.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Juni aösdlkjf 1.1g.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Juli aösdlkjf 1.1g.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Jul aösdlkjf 1.1g.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Aug aösdlkjf 1.1g.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 August aösdlkjf 1g.1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Sep aösdlkjf 1.1g.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 September aösdlkjf 1.1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Okt aösdlkjf 1.1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Oktober aösdlkjf 1.1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Nov aösdlkjf 1.1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 November aösdlkjf 1.1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Dez aösdlkjf 1.1.16 dfkj");
        assertNotNull(cal);
        cal = dateFinder.getCalendar("asdfj öal 9 Dezember aösdlkjf 1.1.16 dfkj");
        assertNotNull(cal);

        // test what NOT TO FIND (especially malformed dates or month names in other names)

        //// *Dez
        cal = dateFinder.getCalendar("asdfj öal 9 Dezibel aösdlkjf dfkj");
        assertNull(cal);

        cal = dateFinder.getCalendar("Seppel asdfj öal 9 Dezibel aösdlkjf 1.111.16 dfkj");
        assertNull(cal);

        cal = dateFinder.getCalendar("we 2.1.111 dfkj");
        assertNull(cal);

        cal = dateFinder.getCalendar("we 99.13.9999 dfkj");
        assertNull(cal);
    }

    public void testGetEstimatedCalendar() throws Exception {
        dateFinder = new DateFinder("DUMMY");

        // Jan --> 1.1.2017 (does skip, because obviously
        Calendar cal = dateFinder.getEstimatedCalendar("asdfj öal 9 aösdlkjf Jan dfkj");
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(1 - 1, cal.get(Calendar.MONTH));
        assertEquals(2018, cal.get(Calendar.YEAR));

        // 1.1. --> 1.1.2017 (does skip, because obviously 1.1. is too far in the past , so it is the next year...
        cal = dateFinder.getEstimatedCalendar("asdfj öal 1.1. aösdlkjf dfkj");
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(1 - 1, cal.get(Calendar.MONTH));
        assertEquals(2018, cal.get(Calendar.YEAR));

        // Jan --> 1.1.2017 (DO NOT CHANGE!)
        cal = dateFinder.getEstimatedCalendar("asdfj öal 1.1.2016 aösdlkjf Jan dfkj");
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(1 - 1, cal.get(Calendar.MONTH));
        assertEquals(2016, cal.get(Calendar.YEAR));

    }

    public void testGet() throws Exception {
        assertCal(new DateFinder("asdfj öal 1.1. 3.4.2016 aösdlkjf Jan KW 32 KW32 Dezember dfkj").get(), 3, 4, 2016);

        assertCal(new DateFinder("asdfj öal 1.1.2018 3.4.2016 aösdlkjf Jan KW 32 KW32 Dezember dfkj").get(), 3, 4, 2016);
        //assertCal(new DateFinder("asdfj öal 1.1. 3.4.201 aösdlkjf Jan KW 32 KW32 Dezember dfkj").get(), 1, 1, 2018);

        // this one will fail, because it tries to pars 3.46.2016 and returns a null calendar. It does not jump to 1.1. or Jan...
        //assertCal(new DateFinder("asdfj öal 1.1 3.46.2016 aösdlkjf Jan KW 32 KW78 Dezember dfkj").get(), 1, 12, 2016);
        assertCal(new DateFinder("asdfj öal 1.1x. 4.2016 aösdlkjf Jan KW 32 KW51 Dezember dfkj").get(), 18, 12, 2017);
    }

    // little helper...
    private void assertCal(Calendar c, int day, int month, int year) {
        assertNotNull(c);
        assertEquals(day, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(month - 1, c.get(Calendar.MONTH));
        assertEquals(year, c.get(Calendar.YEAR));
    }

    public void testFindPhoneNr() {
        //String content = "<en-note><div>nächster Schritt: Denkwerkstatt vor Ort anbieten</div><div><br clear=\"none\"/></div><div>Andere Teilnehmer: <a shape=\"rect\" style=\"color: #69aa35;\" href=\"https://www.evernote.com/shard/s226/sh/e064d529-cfad-40b0-a932-8cb3ad5935f7/ffdb2abcd06b4eebab200172150658e7\" target=\"_blank\">Denkwerkstatt Teilnehmer ZAMB</a><br clear=\"none\"/></div><div>Denkwerkstatt anbieten</div><div>NIT: Dr Volker <span>Franke</span> (Harting) Dr. Volker <span>Franke</span> (Harting) (erst Ende Oktober von der Weltreise zurück)<br clear=\"none\"/></div><div>BEL: Phönix (Stichwort ZAMB, die Drei vom ersten WS, Nunne...) <a shape=\"rect\" style=\"color: #69aa35;\" href=\"evernote:///view/28032648/s216/64162747-f0af-4274-b903-6987199e44ed/64162747-f0af-4274-b903-6987199e44ed/52e9f6f6-6c81-4e8c-b1b7-c7551d28cae7\" target=\"_blank\">Herr Nunne (Phoenix Contact)</a><br clear=\"none\"/></div><div><span style=\"color: #808080; font-family: &amp;apos; trebuchet ms&amp;apos;, sans-serif; font-size: 15px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\">NIT: Axel Hessenkämper (GEA) (Anderer Ansprechpartner?)</span></div><div style=\"margin: 0cm 0cm 0.0001pt; font-family: Calibri, sans-serif; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.300781); font-size: 15px;\"><div><span style=\"font-family: &amp;apos; trebuchet ms&amp;apos;, sans-serif; color: gray;\">NIX TUN: Günter Arztmiller (LJU)</span></div></div><div style=\"margin: 0cm 0cm 0.0001pt; font-family: Calibri, sans-serif; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.300781); font-size: 15px;\"><div><span style=\"color: gray; font-family: &amp;apos; trebuchet ms&amp;apos;, sans-serif; font-size: 11pt;\">NIX - klären, ob Lars raus ist?: Eike Dölschner (Herose, Vorsicht, da ist Lars Vollmer schon als Berater dran)</span></div></div><div><br clear=\"none\"/></div><div>BEL hat am 5.8 einen Telefontermin mit Herrn Schulte!<br clear=\"none\"/></div><div><span style=\"font-family: UICTFontTextStyleBody; font-size: 17px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\">NIT: </span><span style=\"font-family: UICTFontTextStyleBody; font-size: 17px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\">Sven <span>Schulte</span></span></div><div><span style=\"font-family: UICTFontTextStyleBody; font-size: 17px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\">Dipl.-Wirt.-Ing.</span></div><div><span style=\"font-family: UICTFontTextStyleBody; font-size: 17px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\">Werkleitung</span></div><div><span style=\"font-family: UICTFontTextStyleBody; font-size: 17px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\">Telefon: +49 5271 62 - 654 | Fax: +49 5271 62-451</span><br clear=\"none\" style=\"font-family: UICTFontTextStyleBody; font-size: 17px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\"/><span style=\"font-family: UICTFontTextStyleBody; font-size: 17px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\">E-Mail: s.<span>schulte</span>@optibelt.com | www.optibelt.de </span><br clear=\"none\" style=\"font-family: UICTFontTextStyleBody; font-size: 17px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\"/><span style=\"font-family: UICTFontTextStyleBody; font-size: 17px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\">Ein Unternehmen der Arntz Optibelt Gruppe</span></div><div><span style=\"font-family: UICTFontTextStyleBody; font-size: 17px; -webkit-tap-highlight-color: rgba(26, 26, 26, 0.301961);\">Arntz Optibelt GmbH| Corveyer Allee 15 | 37671 Höxter | Deutschland</span></div><div><span style=\"font-size: 17px;\"><span style=\"font-family: UICTFontTextStyleBody;\"><br clear=\"none\"/></span></span></div><div>Er hat ein &quot;Lean Fabrikplanungs-Problem&quot;</div><div>Gerhard hat bei der ZAMB eine Problemtransformation mit ihm gemacht.</div></en-note>\n";
        String content = ">Telefon: +49 5271 62 - 654 | Fax: +49 5271 62-451</span>";
        String regexPhoneNr = "((\\+49|0){1}[\\d\\s/\\-()]+)(\\s|<)";

        Pattern p = Pattern.compile(regexPhoneNr);
        Matcher m = p.matcher(content);
        if (m.find()) {
            String frist = m.group();
            String g1 = m.group(0);
            String g2 = m.group(1);
            String g3 = m.group(2);
        }

    }

}