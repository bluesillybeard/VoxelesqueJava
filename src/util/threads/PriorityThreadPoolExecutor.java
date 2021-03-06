package util.threads;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

//since I need the priority to dynamically update when the player moves.
// I mean, when any elements are added - since the player moving is guaranteed to add more tasks
// to load/render chunks, it's basically guaranteed to update when the player moves.
public class PriorityThreadPoolExecutor<R extends Runnable> {
    private final List<R> tasks;
    private final KillablePoolThread[] runners; //Curse you java and your idiotic Generics system.
    private final Comparator<R> comparator;
    private boolean paused;

    public PriorityThreadPoolExecutor(Comparator<R> comparator, int threads){
        tasks = Collections.synchronizedList(new LinkedList<>());
        runners = new KillablePoolThread[threads];
        this.comparator = comparator;
        for(int i=0; i<threads; ++i){
            runners[i] = new KillablePoolThread<>(tasks);
            runners[i].start();
        }
    }

    public void submit(R task){
        tasks.add(task);
        tasks.sort(comparator);
    }

    public void stop(){
        tasks.clear();
        for(Thread t: runners){
            t.interrupt();
        }
    }

    /**
     * pauses the excecutor.
     * note: tasks that have already started won't be paused;
     * all this does is it stops the threads from taking up new tasks.
     * @param paused weather or not the threads will continue executing tasks
     */
    public void setPaused(boolean paused){
        this.paused = paused;
    }

    public boolean isEmpty(){
        for(KillablePoolThread<R> thread: runners){
            if(!thread.getIdle()){
                return false;
            }
        }
        return getTasks().isEmpty();
    }

    public List<R> getTasks (){
        return tasks;
    }


    private class KillablePoolThread<T extends Runnable> extends Thread{
        private final AtomicBoolean running;
        private final List<T> queue;
        private boolean idle;
        public KillablePoolThread(List<T> queue){
            this.queue = queue;
            running = new AtomicBoolean(false);
        }

        public void interrupt(){
            running.set(false);
            super.interrupt();
        }

        public boolean getIdle(){
            return idle;
        }

        /**
         * When an object implementing interface {@code Runnable} is used
         * to create a thread, starting the thread causes the object's
         * {@code run} method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method {@code run} is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            running.set(true);
            do {
                try {
                    if (!queue.isEmpty() && !paused) {
                        idle = false;
                        queue.remove(0).run();
                    } else {
                        idle = true;
                        Thread.sleep(100);
                    }
                } catch (Exception ignored){}
            } while (!Thread.interrupted() && running.get());
            running.set(false);
        }
    }
}
