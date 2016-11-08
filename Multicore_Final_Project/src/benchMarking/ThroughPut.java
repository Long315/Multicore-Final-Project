package finalProj.benchMarking;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import finalProj.ParallelPriorityQueue;
import finalProj.multiqueue.MultiQueue;

public class ThroughPut {
    public int providerConsumer(int numOfProviders, int numOfConsumers, ParallelPriorityQueue<Integer> queue){
        int[] ProviderCounts = new int[numOfProviders];
        int[] ConsumerCounts = new int[numOfConsumers];
        ExecutorService threadPool = Executors.newFixedThreadPool(numOfProviders + numOfConsumers);
        ArrayList<Thread> tasks = new ArrayList<>();
        for( int i = 0; i < numOfProviders; i++) tasks.add(new Provider(i, queue, ProviderCounts));
        for( int i = 0; i < numOfConsumers; i++) tasks.add(new Consumer(i, queue, ConsumerCounts));
        int sum = 0;
        try {
            long before = Instant.now().toEpochMilli();
            threadPool.invokeAll(tasks, 1, TimeUnit.SECONDS);
            System.out.println(Instant.now().toEpochMilli() - before);
            for (int i = 0; i < numOfProviders; i++) sum += ProviderCounts[i];
            for (int i = 0; i < numOfConsumers; i++) sum += ConsumerCounts[i];
            System.out.println(sum);
            sum = 0;
            for (int i = 0; i < numOfProviders; i++) sum += ProviderCounts[i];
            for (int i = 0; i < numOfConsumers; i++) sum += ConsumerCounts[i];
            tasks.stream().forEach(e -> e.stop());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
        return sum;
    }
    
    
    public static void main(String args[]) {
        ThroughPut tp = new ThroughPut();
        MultiQueue<Integer> queue = new MultiQueue<>(2, 20);
        System.out.println(tp.providerConsumer(10,10,queue));
    }
}
