
package bel.learn._37_gpars_groovy_concurrency

import groovyx.gpars.GParsPool
import groovyx.gpars.ParallelEnhancer
import static groovyx.gpars.GParsPool.withPool



//@Grab(group='org.codehaus.gpars', module='gpars', version='1.2.1')

    def main() {
        sendEmails(1..5)
        enhancer()
        transparentConcurrent()
    }

    def sendEmails(numbers) {
        withPool { // otherwise, eachParallel is not available...
            numbers.eachParallel { number ->
                def rand = new Random().nextDouble() * 1000
                def wait = (long) rand
                println "${Thread.currentThread()} in closure $number waiting $wait"
                sleep wait
            }
        }

    }

    def enhancer() {
        println("enhancer")
        def list = 1..10
        ParallelEnhancer.enhanceInstance(list)
        def mod3
        //withPool { seem to work without... The enhancer does it...
            mod3 = list.collectParallel {
                println "${Thread.currentThread()}"
                it * 3
            }
        //}
        print mod3
    }

    def transparentConcurrent() {
        println("transparentConcurrent")
        withPool { // needed...
            def l = [1, 2, 3, 4, 6, 8, 9, 10].makeConcurrent()
            l.each {
                def rand = new Random().nextDouble() * 1000
                def wait = (long) rand
                println "${Thread.currentThread()} in closure $it waiting $wait"
                sleep wait
            }
        }
    }

main()
