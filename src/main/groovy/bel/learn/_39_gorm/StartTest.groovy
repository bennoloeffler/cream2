package bel.learn._39_gorm


startItOther{
    println "running startIt"
}


def startItOther(Closure exec) {
    println "running startIt(Closure exec) exec= ${exec.toString()}"
    exec()
}