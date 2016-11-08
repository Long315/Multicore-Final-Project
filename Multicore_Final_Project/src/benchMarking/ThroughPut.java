package benchMarking;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import interfacepackage.ParallelPriorityQueue;
import interfacepackage.multiqueue.MultiQueue;

public class ThroughPut {
    public int providerConsumer(int numOfProviders, int numOfConsumers, ParallelPriorityQueue<Integer> queue){
        int sum = 0;
        int loopCount = 50;
        for (int loop = 0; loop < loopCount; loop++) {
            int[] ProviderCounts = new int[numOfProviders];
            int[] ConsumerCounts = new int[numOfConsumers];
            ExecutorService threadPool = Executors.newFixedThreadPool(numOfProviders + numOfConsumers);
            ArrayList<Thread> tasks = new ArrayList<>();
            for( int i = 0; i < numOfProviders; i++) tasks.add(new Provider(i, queue, ProviderCounts));
            for( int i = 0; i < numOfConsumers; i++) tasks.add(new Consumer(i, queue, ConsumerCounts));
            int sum1 = 0;
            int sum2 = 0;
            try {
                long before = Instant.now().toEpochMilli();
                threadPool.invokeAll(tasks, 1000, TimeUnit.MILLISECONDS);
                long timegap = Instant.now().toEpochMilli() - before;
                for (int i = 0; i < numOfProviders; i++) sum1 += ProviderCounts[i];
                for (int i = 0; i < numOfConsumers; i++) sum1 += ConsumerCounts[i];
                for (int i = 0; i < numOfProviders; i++) sum2 += ProviderCounts[i];
                for (int i = 0; i < numOfConsumers; i++) sum2 += ConsumerCounts[i];
                tasks.stream().forEach(e -> e.stop());
                sum += (int) (sum1 - (sum2 - sum1) / 2) / (timegap / 1000.0);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                threadPool.shutdown();
            }
        }
        return sum / loopCount;
    }
    
    
    public static void main(String args[]) {
        ThroughPut tp = new ThroughPut();
        MultiQueue<Integer> queue = new MultiQueue<>(2, 16);
        System.out.println(tp.providerConsumer(8,8,queue));
    }
}