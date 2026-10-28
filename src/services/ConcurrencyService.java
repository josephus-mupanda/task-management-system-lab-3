package services;

import models.Task;

import java.util.ArrayList;
import java.util.List;

// simulates several users updating tasks at the same time (Feature 4).
// Threads and a parallel stream are both here, so the two ways can be compared on the same tasks.
public class ConcurrencyService {

    // each update waits a little, so we can see the threads working at the same time
    private static final int WORK_TIME_MS = 300;

    private int updatedCount;

    /** Starts one thread per task (Thread-1, Thread-2...) and waits until all the threads are finished. */
    public void updateWithThreads(List<Task> tasks) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            Runnable work = () -> updateTask(task);
            threads.add(new Thread(work, "Thread-" + (i + 1)));
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /** Updates the tasks with a parallel stream, Java decides how many threads are used. */
    public void updateWithParallelStream(List<Task> tasks) {
        tasks.parallelStream().forEach(this::updateTask);
    }

    /** Returns how many tasks were updated since the service was created. */
    public synchronized int getUpdatedCount() {
        return updatedCount;
    }

    // called by several threads at once, so the shared counter is only changed through a synchronized method
    private void updateTask(Task task) {
        Task.Status newStatus = task.getStatusType().next();
        System.out.println(Thread.currentThread().getName() + " updating " + task.getId() + " -> " + newStatus);
        try {
            Thread.sleep(WORK_TIME_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        task.setStatus(newStatus);
        countUpdate();
    }

    // updatedCount++ is not one single step, without synchronized two threads could lose an update
    private synchronized void countUpdate() {
        updatedCount++;
    }
}
