package bel.util.enml

import bel.en.evernote.ENHelper
import spock.lang.Specification
/**
 *
 *
 *
 */
class DELETEDHtmlToEnmlTest extends Specification {

    def getXhtmlFromTitleAndBody(title, body) {
        """
        <?xml version="1.0" encoding="UTF-8"?><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <title>$title</title>
            </head>
            <body>
                $body
            </body>
        </html>
        """
    }



    def html1 = """
                    <?xml version="1.0" encoding="UTF-8"?><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                    <html xmlns="http://www.w3.org/1999/xhtml">
                        <head>
                            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta>
                            <meta attribName="exporter-version" content="ENML4J 1.1.0"></meta>
                            <title>Test5  (Test5)</title>
                        </head>
                        <body>
                            <div>ANGEBOT: BEL: 22.5.2017 EUR 120.000 EUR  Analyse, Soll und erste Schritte für 4 Monate</div>
                        </body>
                    </html>
                """

    void setup() {
        // <?xml version="1.0" encoding="UTF-8"?><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"><html xmlns="http://www.w3.org/1999/xhtml"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta><meta attribName="exporter-version" content="ENML4J 1.1.0"></meta><title>Oliver Eller (BOS)</title></head><body><div><input type="checkbox"></input>NIT: Termin vor Ort im Oktober? alle SkypeTermine festgelegt? KW33</div><div>27.07.17: lt. BEL geht es im September einfach wie gehabt weiter, Aug fällt aus</div><div>24.07.16: Im August ist kein Termin vor Ort gewünscht.</div><div><input checked="" type="checkbox"></input>NIT: neue Termine an Frau Sobian geschickt, Juni nicht machbar, dafür Juli 2 und Aug und Sept. Ausgesucht? KW23</div><div><input checked="" type="checkbox"></input>NIT: zweites Projekt "kollegiale Führung" startet im Juni, 29.05.. Frau Sobian gehört zur Veränderungstruppe und trifft die Kollegen am 29.5. Danach soll ich Termine für 1x pro Monat BEL vor Ort, in den anderen Wochen Skype 1 Std. absprechen. Vorschläge sind am 23.5 an Frau Sobian gegangen</div><div><br></br></div><div>21.03.17: erster Tag mit BEL ist abgestimmt (5.4), STS hat auch zwei Tage in der Fertigung vereinbart und bleibt hier selbst dran</div><div>20.03.17: wieder Mail geschrieben, telefonisch ne</div><div>15.03.17: Oliver hat den ersten Schritt die 15.000 EUR beauftragt. Habe ihm eine Mail zu den Terminen geschrieben</div><div><input checked="" type="checkbox"></input>NIT: Oliver nächsten Termin abstimmen, nach dem 5.04. mit BEL klären</div><div><input checked="" type="checkbox"></input>BEL: 10.3. Angebot ok?<br></br></div><div>BEL: 10.3. AB "Angebot ok?" bitte um Feedback.</div><div><br></br></div><div>PROBLEM:</div><div>ok: 3 Schichten Durchlaufzeit</div><div>ok: Qualität</div><div>Problem: Produktivität</div><div><br></br></div><div>Problem in Emsdetten: Produktivität fällt von 90% auf 82%</div><div>ARbeit ist da. Der Durchsatz ist zu klein um alle Arbeit durchzubringen.</div><div>- die Maschinen scheinen nicht das Problem zu sein.</div><div>- Mitarbeiter scheinen eher das Problem zu sein.</div><div><span style="-webkit-text-size-adjust: 100%;">- Bestands-Reduktion bis zum Abriss (Logistik muss voll da sein).</span></div><div><span style="-webkit-text-size-adjust: 100%;">- Röhmheld: Abriss-Toleranz</span></div><div>- 10 Schwerpunkt-Überlast-Anlagen.</div><div><span style="-webkit-text-size-adjust: 100%;">- 100% Termintreue. System nicht überlasten. </span></div><div>- Eine Organisation wächst im Grenzbereich. </div><div>- Brummschicht mit minimal-Bestand</div><div><br></br></div><div><input checked="" type="checkbox"></input> NIT: Termin Östereich mit Oliver mit BEL machen KW13 wieder nachfassen (im März Klärung" hat Oliver geschrieben)</div><div><input checked="" type="checkbox"></input>NIT: Termin mit BEL und STS in Emsdetten, Wunsch Oliver nach Telefonart mit BEL. Und insights abfragen. (Mail geschrieben 10.02.)</div><div><br></br></div><div>13.02.17: Termin mit BEL/STS am 3.3. in Emsdetten. insights will sich Oliver bei Gelegenheit melden</div><div>10.02.17: Oliver Termin für STS und BEL angeboten. Insights möchte er sich nach seinem Skiurlaub ab KW8 zu melden</div><div>13.12.16: Winterschool nachgefragt per Mail (Oliver hat seinen besten Mann angemeldet und überlegt selbst auch noch) - er will sehr kurzfristig entscheiden, habe ok gesagt</div><div>07.12.16: Mail an Oliver</div><div><br></br></div><div><input checked="" type="checkbox"></input>BEL: Kontakt zu Herr Rackerseder bzgl. Referenzbesuch herstellen</div><div>BEL: 20.9. Habe Rackerseder per mail angefragt - NIT am 20.9 Kontaktdaten an Oliver gegeben. Herr Rackerseder ist einverstanden!</div><div><br></br></div><div><br></br></div><div>19.09.16: Oliver zur Terminabstimmung angeschrieben</div><div>01.08.16:Olivers Antwort:<span style="color: #1f497d;">ja ich komme zum Lagerfeuer am 9.9.16</span> <span style="font-family: Wingdings; color: #1f497d;">J</span><p><span style="color: #1f497d;">Dann will ich auch die 2 anderen Angebote (neuer Maschinenbau, Denkwerkstatt) mit Benno und Matthias durchsprechen.</span></p></div><div><br clear="none"></br></div><div>MAW hat ihm eine Denkwerkstatt angeboten. Vielleicht mit Gerhard. Ziel: August</div><div>02.06.16: MAW nach dem Stand gefragt</div><div>26.05.16: Einladung SCRUM in Aurich geschickt</div><div>24.05. Termin mit Oliver war gut.</div><div>MAW wird ihm eine Denkwerkstatt anbieten. Vielleicht mit Gerhard. Ziel: Juni</div><div><br clear="none"></br></div><div>03.05.16: Termin am 23.5 mit MAW und BEL steht. LF und SSNM überlegt er sich....</div><div>02.05.16: Oliver hat sich noch nicht gemeldet, nachgefragt</div><div>22.04.16: mit MAWabgesprochen - 23.5 Oliver angeboten</div><div><br clear="none"></br></div><div>nächste Aktion: Termin mit MAW bei ihm. Habe Terminoptionen an MAW geschickt, warte auf Antwort</div><div><br></br></div><div>Oliver: <table cellspacing="0"><tbody><tr><td><br></br></td><td><div><table border="0" cellpadding="0" cellspacing="0" style="display: block;"><tbody><tr><td><a title="+40 (0) 173 257 22 40"></a><a href="tel:+40%20173%20257%2022%2040" dir="ltr">+40 (0) 173 257 22 40</a></td><td><br></br></td></tr></tbody></table></div></td></tr></tbody></table></div><div><br></br></div></body></html>
        //<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"><html xmlns="http://www.w3.org/1999/xhtml"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta><meta attribName="exporter-version" content="ENML4J 1.1.0"></meta><title>Test5  (Test5)</title></head><body><div>ANGEBOT: BEL: 22.5.2017 EUR 120.000 EUR  Analyse, Soll und erste Schritte für 4 Monate</div><div><br></br></div><div>***FIRMA***</div><div>Firmenname: Test5 GmbH</div><div>Domain: test5.de</div><div>firmaxy.de: </div><div>Tel. Zentrale: </div><div>Mitarbeiter: </div><div>Umsatz: </div><div>Straße: Adlerweg 17</div><div>PLZ: 70176</div><div>Ort: Stuttgart</div><div>Land: </div><div>Notizen: </div><div>Postfach: </div><div>Tags: </div><div>+++FIRMA+++</div><div><br></br></div><div>***PERSON***</div><div>Anrede: </div><div>Titel: </div><div>Vorname: Test5</div><div>Nachname: NachnameNeu</div><div>Funktion: </div><div>Emails: </div><div>Festnetz: </div><div>Mobil: </div><div>LinkedIn: </div><div>Xing: </div><div>angelegt: </div><div>bekannt mit: </div><div>Sekretariat: </div><div>Abteilung: </div><div>Notizen: </div><div>Fax: </div><div>Tags: LF_2018,  LF,  LF_2016</div><div>+++PERSON+++</div><div><br></br></div><div>***PERSON***</div><div>Anrede: </div><div>Titel: </div><div>Vorname: Benno</div><div>Nachname: Löffler</div><div>Funktion: Abt </div><div>Emails: ben</div><div>Festnetz: whendö</div><div>Mobil: </div><div>LinkedIn: </div><div>Xing: </div><div>angelegt: </div><div>bekannt mit: </div><div>Sekretariat: </div><div>Abteilung: </div><div>Notizen: </div><div>Fax: </div><div>Tags: </div><div>+++PERSON+++</div><div>***END-DATA+++</div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div>***ABO*** BEL: +++ABO+++</body></html>

    }

    void cleanup() {
    }

    def "Transform empty html to empty enml"() {
        setup:
            def enmlEmpty = ENHelper.createNoteFromEmailText("")
            def htmlEmpty = getXhtmlFromTitleAndBody("", "")

        when:
            def enmlConverted = DELETED_HtmlToEnml.instance.transform(htmlEmpty)

        then:
            enmlConverted == enmlEmpty
    }

    def "Transform simple tags"() {
        setup:
            def enml = ENHelper.createNoteFromEmailText(
                    """ignore... 
                            <div> the text body </div>
                            ignore also
                            <div>second line</div>
                            ignore this, too
                         """)
            def html = getXhtmlFromTitleAndBody("the title",
                    """ignore... 
                            <div> the text body </div>
                            ignore also
                            <div>second line</div>
                            ignore this, too
                         """)
        when:
            def enmlConverted = DELETED_HtmlToEnml.instance.transform(html)

        then:
            enml == enmlConverted
    }

}
