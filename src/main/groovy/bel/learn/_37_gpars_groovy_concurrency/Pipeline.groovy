package bel.learn._37_gpars_groovy_concurrency

import bel.learn._14_timingExecution.RunTimer
import bel.util.StringSimilarity
import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.DataflowReadChannel

import static groovyx.gpars.GParsPool.withPool

/**
 * there are textfiles.
 * they will be read.
 * they will be parsed for a text (Benno Löffler) (regex) and for similarity to that String (StringSimilarity).
 * if there is a hit, there will a checksum
 * finally, there will be a checksum of all.
 *
 */

def readFile = { String fileName ->
    //println Thread.currentThread()
    def f = new File(fileName)
    f.text
}

def regexAE = {String content ->
    //println Thread.currentThread()
    def m = (content =~ /(?s)(?m)Anfang.*Ende/)
    m?m[0]:""
}

def similarBL = {String content ->
    //println Thread.currentThread()
    StringSimilarity.findBestSimilarityByEqualLenthMatch(content, "Benno Löffler")
    StringSimilarity.bestLastSimilarity > 0.8?content:""
}

def checksum = {String content ->
    //println Thread.currentThread()
    def cs = 0
    for(n in content) {
        cs += n.hashCode()
    }
    cs
}




def test = {
    assert "Benno Loffler" == readFile("test/test.txt")
    assert "Anfang sdfsdf Ende" == regexAE("sdfsdf Anfang sdfsdf Ende asdfasdf")
    assert "" == regexAE("abc benno loeffler abc")
    assert similarBL("Benno Laffler")
    assert !similarBL("Beumopo Laffler")
    assert checksum("sdfsdf") > 0
    assert checksum("sdfsdf") != checksum("sdfsda")
    assert checksum("sdfsda") == checksum("sdfsda")
}

//test()

def singlethreadedPipeline =  { String fn ->
    def c = readFile(fn)
    String r = regexAE(c)
    similarBL(r)
}

def fl = ["a", "b", "c"]//, "d", "e", "f", "a", "b", "c", "d", "e"]
def resultList =[]
def fileName(f) {
    "test/"+f+".txt"
}

//--------------- single thread ----------------------

RunTimer rt = new RunTimer()
for(f in fl) {
    resultList.add(singlethreadedPipeline(fileName(f)))
}
rt.stop("singlethreaded took")
//println resultList

//--------------- pipeline multi thread ----------------------

final startQueue = new DataflowQueue()
final DataflowReadChannel resultChannel = startQueue | readFile | regexAE | similarBL


    rt.go()
    for (f in fl) {
        print " $f"
        startQueue << fileName(f)
    }

    resultList = []
    for (f in fl) {
        resultList.add(resultChannel.val)
        //i++
    }
    rt.stop("concurrent took")
