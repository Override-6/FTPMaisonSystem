/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can USE it as you want.
 * You can download this source code, and modify it ONLY FOR PRIVATE USE but you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.engine.internal.concurrency.pool

import fr.linkit.api.internal.concurrency.Worker
import fr.linkit.api.internal.concurrency.pool.ClosedWorkerPool

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

class SimpleClosedWorkerPool(initialThreadCount: Int, name: String) extends AbstractWorkerPool(name) with ClosedWorkerPool {
    
    if (initialThreadCount < 0)
        throw new IllegalArgumentException(s"initialThreadCount < 0")
    
    //The extracted workQueue of the executor which contains all the tasks to execute
    private val workQueue                         = new LinkedBlockingQueue[Runnable]()
    private val workerFactory: Runnable => Worker = target => {
        val worker = new BusyWorkerThread(target, this, threadCount + 1)
        addWorker(worker)
        worker
    }
    setThreadCount(initialThreadCount)
    
    override def nextTaskCount: Int = super.nextTaskCount
    
    override protected def countRemainingTasks: Int = workQueue.size()
    
    override protected def pollTask: Runnable = workQueue.poll()
    override protected def takeTask: Runnable = workQueue.take()
    
    override def setThreadCount(newCount: Int): Unit = {
        if (workers.size > newCount)
            throw new IllegalArgumentException(s"newCount < workers.size ($newCount < ${workers.size})")
        for (_ <- 0 until newCount - workers.size) {
            val worker = workerFactory(() => waitingRoom())
            worker.thread.start()
        }
    }
    
    override def close(): Unit = {
        super.close()
        val workerCount = workers.size
        workers.clear()
        for (_ <- 0 to workerCount)
        //all threads waiting to execute another tasks will see a null task was submit & that super.closed = true so they'll stop executing
            workQueue.add(null)
    }
    
    override protected def post(runnable: Runnable): Unit = workQueue.offer(runnable)
    
    private def waitingRoom(): Unit = {
        val self = EngineWorkerPools.currentWorker.asInstanceOf[BusyWorkerThread]
        self.execWhileCurrentTaskPaused(workQueue.take(), !closed)(_.run())
    }
}
