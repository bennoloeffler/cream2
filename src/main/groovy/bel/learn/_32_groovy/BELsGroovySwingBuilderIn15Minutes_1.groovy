package bel

import javax.swing.*
import java.awt.*

println "http://docs.groovy-lang.org/latest/html/gapi/groovy/swing/SwingBuilder.html"
println "SEE: https://de.m.wikibooks.org/wiki/Groovy:_Swing-GUI"

// TODO: das tut noch GAAAAR NICHT...
/*

sb = new groovy.swing.SwingBuilder()
anzeigeAktion = sb.action(closure: { sb.ausgabe.text="$it.source.text"})
frame = sb.frame(title:"Klicken", size:[200,100],
        defaultCloseOperation:WindowConstants.EXIT_ON_CLOSE) {
    panel() {
        gridBagLayout()
        button ("Rot",foreground:Color.RED, action:anzeigeAktion)
        button ("Blau",foreground:Color.BLUE, action:anzeigeAktion)
        textField(id:'ausgabe', constraints:new GridBagConstraints(gridx:0,gridy:1,gridwidth:2,fill:1))
    }
}
frame.visible = true
*/

sb = new groovy.swing.SwingBuilder()
frame = sb.frame(title:"Klicken", size:[200,100],
        defaultCloseOperation:WindowConstants.EXIT_ON_CLOSE) {
    panel() {
        button ("Rot",
                foreground:Color.RED,
                actionPerformed: {println "Rot gedrückt"})
        button ("Blau",foreground:Color.BLUE,
                actionPerformed: {println "Blau gedrückt"})
    }
}

frame.visible = true