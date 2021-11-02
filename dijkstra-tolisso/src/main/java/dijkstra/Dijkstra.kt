package dijkstra

import kotlinx.atomicfu.atomic
import java.util.*
import java.util.concurrent.Phaser
import java.util.concurrent.ThreadLocalRandom
import kotlin.Comparator
import kotlin.concurrent.thread

private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> o1!!.distance.compareTo(o2!!.distance) }

class SoftPriorityQueue(workers : Int) {

    private val counter = atomic(0)

    private val size = workers
    private val queues = List(size) { PriorityQueue(NODE_DISTANCE_COMPARATOR) }

    fun decrease() {
        while (true) {
            val value = counter.value
            if (counter.compareAndSet(value, value - 1)) {
                break
            }
        }
    }

    private fun increase() {
        while (true) {
            val value = counter.value
            if (counter.compareAndSet(value, value + 1)) {
                break
            }
        }
    }

    fun get(): Node? {
        while (counter.value != 0) {
            var l = ThreadLocalRandom.current().nextInt(size)
            var r = ThreadLocalRandom.current().nextInt(size)
            if (l == r) {
                r = (l + 1) % size
            }
            if (r < l) {
                val tmp = r
                r = l
                l = tmp
            }

            synchronized(queues[l]) {
                synchronized(queues[r]) {
                    if (!(queues[l].isEmpty() && queues[r].isEmpty())) {
                        if (queues[l].isEmpty()) return queues[r].poll()
                        if (queues[r].isEmpty()) return queues[l].poll()
                        if (NODE_DISTANCE_COMPARATOR.compare(
                                queues[l].peek(),
                                queues[r].peek()
                            ) < 0)
                            return queues[l].poll()
                        return queues[r].poll()
                    }
                }
            }
        }
        return null
    }

    fun put(x : Node) {
        increase()
        val i = ThreadLocalRandom.current().nextInt(size)
        synchronized(queues[i]) {
            queues[i].add(x)
        }
    }
}
// Returns `Integer.MAX_VALUE` if a path has not been found.
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    // The distance to the start node is `0`
    start.distance = 0
    // Create a priority (by distance) queue and add the start node into it
    val q = SoftPriorityQueue(workers) // TODO replace me with a multi-queue based PQ!
    q.put(start)
    // Run worker threads and wait until the total work is done
    val onFinish = Phaser(workers + 1) // `arrive()` should be invoked at the end by each worker
    repeat(workers) {
        thread {
            while(true) {
                val cur: Node? = q.get()
                if (cur == null) break
                try {
                    for (e in cur.outgoingEdges) {
                        while (true) {
                            val newDist = cur.distance + e.weight
                            val oldDist = e.to.distance
                            if (oldDist > newDist) {
                                if (e.to.casDistance(oldDist, newDist)) {
                                    q.put(e.to)
                                    break
                                } else {
                                    continue
                                }
                            } else break
                        }
                    }
                } finally {
                    q.decrease()
                }
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}

