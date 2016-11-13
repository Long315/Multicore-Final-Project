package benchMarking;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import interfacepackage.ParallelPriorityQueue;
import lockfreepriorityqueue.linearizable.LockFreePriorityQueueWrapper;
import lockfreepriorityqueue.nonlinearlizable.SkipQueue;
import multiqueue.MultiQueue;

public class ThroughPut {
	Random r = new Random();
	int[] valuelist = new int[5000000];
	public ThroughPut(){
		for (int i = 0; i < 5000000; ++i) {
			valuelist[i] = r.nextInt();
		}
	}
	
	public int addFixedNumThreadsTest(int numThreads, Class<? extends ParallelPriorityQueue> queueClass, int c) {
		int sum = 0;
		int loopCount = 10;
		
		for (int loop = 0; loop < loopCount; loop++) {
			
			
			try {
				ParallelPriorityQueue queue;
				
				if (c != 0)
					queue = queueClass.getConstructor(int.class, int.class).newInstance(c, numThreads);
				else
					queue = queueClass.getConstructor().newInstance();
				
				int[] ProviderCounts = new int[numThreads];
				ArrayList<Provider> providers = new ArrayList<>();
				CyclicBarrier gate = new CyclicBarrier(numThreads);
				Provider.sta();
				
				for( int i = 0; i < numThreads; i++) providers.add(new Provider(i, queue, ProviderCounts, gate, valuelist));
				for (Provider provider : providers) provider.start();
//				System.out.println("threads started");
				while (Provider.before < 0 || Instant.now().toEpochMilli() - Provider.before < 1000){
//					System.out.println(Provider.before);
				};
				
				Provider.stp();
				
				for( int i = 0; i < numThreads; i++) sum += (int)(ProviderCounts[i] / ((Provider.after - Provider.before) / 1000.0));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		return sum;
	}
	
	public int[][] addVariousNumThreadsTest(Class<? extends ParallelPriorityQueue> queueClass, int c) {
		int[][] datapoints = new int[2][32];
		for (int i = 0; i < 32; i++) {
			datapoints[0][i] = i + 1;
		}
		
		for (int i = 0; i < 32; i++) {
			int N = datapoints[0][i];
			datapoints[1][i] = addFixedNumThreadsTest(N, queueClass, c);
			System.out.format("%d, %d\n", N, datapoints[1][i]);
		}
		return datapoints;
	}
	
	public int providerConsumer(int numOfProviders, int numOfConsumers, Class<? extends ParallelPriorityQueue> queueClass, int c){
		int sumP = 0;
		int sumC = 0;
		int loopCount = 10;
		int initElementNum = 30000;
		for (int loop = 0; loop < loopCount; loop++) {
//			System.out.println("loops");
			try {
				// init Queue
				ParallelPriorityQueue queue;
				if (c != 0)
					queue = queueClass.getConstructor(int.class, int.class).newInstance(c, numOfProviders + numOfConsumers);
				else
					queue = queueClass.getConstructor().newInstance();
				for (int i = 0 ; i < initElementNum; i++) queue.add(i);
//				System.out.println("queue inited");
				// add tasks
				Provider.sta();
				Consumer.sta();
				int[] ProviderCounts = new int[numOfProviders];
				int[] ConsumerCounts = new int[numOfConsumers];
//				ExecutorService threadPool = Executors.newFixedThreadPool(numOfProviders + numOfConsumers);
//				ArrayList<Thread> tasks = new ArrayList<>();
				ArrayList<Provider> providers = new ArrayList<>();
				ArrayList<Consumer> consumers = new ArrayList<>();
				CyclicBarrier gate = new CyclicBarrier(3);
				for( int i = 0; i < numOfProviders; i++) providers.add(new Provider(i, queue, ProviderCounts, gate, valuelist));
				for( int i = 0; i < numOfConsumers; i++) consumers.add(new Consumer(i, queue, ConsumerCounts, gate));
//				System.out.println("tasks created");
				for (Provider provider : providers) provider.start();
				for (Consumer consumer: consumers) consumer.start();
				
//				while (!Consumer.running || !Provider.running);
				while (Provider.before < 0 || Instant.now().toEpochMilli() - Provider.before < 1000);
				while(Consumer.before < 0 || Instant.now().toEpochMilli() - Consumer.before < 1000);
				Provider.stp();
				Consumer.stp();
				for( int i = 0; i < numOfProviders; i++) sumP += (int)(ProviderCounts[i] / ((Provider.after - Provider.before) / 1000.0));
				for( int i = 0; i < numOfConsumers; i++) sumC += (int)(ConsumerCounts[i] / ((Consumer.after - Consumer.before) / 1000.0));
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			
			
		}
		System.out.println(sumP / loopCount);
		System.out.println(sumC / loopCount);
		return (sumP + sumC) / loopCount;
	}
	
	public int[][] providerConsumerDiffNumThread(Class<? extends ParallelPriorityQueue> queueClass, int c) {
		
		int startpoint = 4;
		int expo = 4;
		int[][] datapoints = new int[2][10];
		datapoints[0][0] = startpoint;
		for (int i = 1; i < 10; i++) {
			datapoints[0][i] = datapoints[0][i - 1] + expo;
		}
		
		for (int i = 0; i < 10; i++) {
			int N = datapoints[0][i];
			datapoints[1][i] = providerConsumer(N - N / 2, N / 2, queueClass, c);
			System.out.format("%d, %d\n", N, datapoints[1][i]);
		}
		return datapoints;
	}
	
	public int[][] providerConsumerDiffProducerPortion(Class<? extends ParallelPriorityQueue> queueClass, int c) {

		int[][] datapoints = new int[2][16];
		for (int i = 0; i < 10; i++) {
			datapoints[0][i] = i + 22;
		}
		
		for (int i = 0; i < 10; i++) {
			int N = datapoints[0][i];
//			System.out.println("points");
			datapoints[1][i] = providerConsumer(N, 32 - N, queueClass, c);
			System.out.format("%d, %d\n", N, datapoints[1][i]);
		}
		return datapoints;
	}
	
	public static void main(String args[]) {
		ThroughPut tp = new ThroughPut();
		tp.addVariousNumThreadsTest(LockFreePriorityQueueWrapper.class, 0);
//		tp.providerConsumerDiffNumThread(LockFreePriorityQueueWrapper.class, 0);
//		tp.providerConsumerDiffProducerPortion(LockFreePriorityQueueWrapper.class, 0);
	}
}
