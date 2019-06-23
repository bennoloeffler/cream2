package bel.learn._37_gpars_groovy_concurrency

import groovyx.gpars.dataflow.DataflowVariable
import static groovyx.gpars.dataflow.Dataflow.task
import static groovyx.gpars.GParsPool.withPool
final def x = new DataflowVariable()
final def y = new DataflowVariable()
final def z = new DataflowVariable()


x.whenBound {println "bound x: $it"}  //Asynchronous waiting
println "starting threads"
//withPool { // scheint auch ohne das hier die Actor Threads aus dem Threadpool zu nehmen.
    task {
        println "${Thread.currentThread()} starting task for result z"
        //def sum = x.val + y.val
        //println "sum delivered"
        z << x.val + y.val
    }

    task {
        println "${Thread.currentThread()} starting task for x"
        sleep 1000
        println "finished task for x"
        x << 10
    }

    task {
        println "${Thread.currentThread()} starting task for y"
        sleep 3000
        println "finished task for y"
        y << 5
    }



    //task {
        println "${Thread.currentThread()}  Result: ${z.val}" // synchronous waiting
    //}.join()

//}