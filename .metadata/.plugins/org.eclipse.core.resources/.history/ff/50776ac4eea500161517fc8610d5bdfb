package benchMarking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Interface.ParallelPriorityQueue;
import multiqueue.MultiQueue;

public class ThroughPut {
	public int providerConsumer(int numOfProviders, int numOfConsumers, ParallelPriorityQueue<Integer> queue){
		int[] ProviderCounts = new int[numOfProviders];
		int[] ConsumerCounts = new int[numOfConsumers];
		ExecutorService threadPool = Executors.newFixedThreadPool(numOfProviders + numOfConsumers);
		ArrayList<Callable<Integer>> tasks = new ArrayList<>();
		for( int i = 0; i < numOfProviders; i++) tasks.add(new Provider<Integer>(i, queue, ProviderCounts));
		for( int i = 0; i < numOfConsumers; i++) tasks.add(new Consumer<Integer>(i, queue, ConsumerCounts));
		try {
			threadPool.invokeAll(tasks, 1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			threadPool.shutdown();
		}
		int sum = 0;
		for (int i = 0; i < numOfProviders; i++) sum += ProviderCounts[i];
		for (int i = 0; i < numOfConsumers; i++) sum += ConsumerCounts[i];
		return sum;
	}
	
	
	public static void main(String args[]) {
		ThroughPut tp = new ThroughPut();
		MultiQueue<Integer> queue = new MultiQueue<>(2, 20);
		System.out.println(tp.providerConsumer(10,10,queue));
	}
}
