package bel.learn._37_gpars_groovy_concurrency

import groovyx.gpars.dataflow.DataflowQueue

import static groovyx.gpars.dataflow.Dataflow.task

import static groovyx.gpars.GParsPool.withPool


def words = ['Groovy', 'fantastic', 'concurrency', 'fun', 'enjoy', 'safe', 'GPars', 'data', 'flow', 1]
final def buffer = new DataflowQueue()
final def syncerToUpper = new DataflowQueue()
final def syncerPlain = new DataflowQueue()

withPool {

    task {
        for (String word in words) {
            syncerPlain.val
            buffer << word  //add to the buffer PLAIN
            syncerToUpper << "ok"
        }
    }

    task {
        syncerToUpper << "ok"
        for (String word in words) {
            syncerToUpper.val
            buffer << word.toUpperCase()  //add to the buffer UPPER CASE
            syncerPlain << "ok"
        }
    }

    task {
        while (true) {
            println buffer.val  //read from the buffer in a loop
            //if (buffer.val == 1) return
        }
    }

}

//sleep(10000)
