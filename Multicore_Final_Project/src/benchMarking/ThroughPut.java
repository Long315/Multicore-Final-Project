package benchMarking;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

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
	
	public ParallelPriorityQueue factoryMethod(Class<? extends ParallelPriorityQueue> queueClass, int c, int numThreads){
		
		try {
			ParallelPriorityQueue queue;
			if (c != 0)
				queue = queueClass.getConstructor(int.class, int.class).newInstance(c, numThreads);
			else
				queue = queueClass.getConstructor().newInstance();
			return queue;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
	
	public int addFixedNumThreadsTest(int numThreads, Class<? extends ParallelPriorityQueue> queueClass, int c) {
		int sum = 0;
		int loopCount = 10;
		
		for (int loop = 0; loop < loopCount; loop++) {
			try {
				
				// create a queue, may cause exceptions, hance try-catch
				ParallelPriorityQueue queue = factoryMethod(queueClass, c, numThreads);
				
				// operation counts for threads
				int[] ProducerCounts = new int[numThreads];
				
				// instantiate a list of all add threads.
				ArrayList<Producer> producers = new ArrayList<>();
				for( int i = 0; i < numThreads; i++) producers.add(new Producer(i, queue, ProducerCounts, valuelist));
				
				// let all threads running
				for (Producer producer : producers) producer.start();
				
				// let all threads start to add items to the queue
				Producer.sta();
				while (Producer.before < 0 || System.currentTimeMillis() - Producer.before < 1000);
				Producer.stp();
				
				// collect # of ops
				for( int i = 0; i < numThreads; i++) sum += (int)(ProducerCounts[i] / ((Producer.after - Producer.before) / 1000.0));
				
			} catch (RuntimeException e) {
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
	
	public int pollFixedNumThreadsTest(int numThreads, Class<? extends ParallelPriorityQueue> queueClass, int c) {
		int sum = 0;
		int loopCount = 10;
		int initElementNum = 400000;
		
		for (int loop = 0; loop < loopCount; loop++) {
			try {
				
				// create a queue, may cause exceptions, hance try-catch
				ParallelPriorityQueue queue = factoryMethod(queueClass, c, numThreads);
				
				for (int i = 0 ; i < initElementNum; i++) queue.add(i);
				
				// operation counts for threads
				int[] ConsumerCounts = new int[numThreads];
				
				// instantiate a list of all add threads.
				ArrayList<Consumer> consumers = new ArrayList<>();
				for( int i = 0; i < numThreads; i++) consumers.add(new Consumer(i, queue, ConsumerCounts));
				
				// let all threads running
				for (Consumer consumer : consumers) consumer.start();
				
				// let all threads start to add items to the queue
				Consumer.sta();
				while (Consumer.before < 0 || System.currentTimeMillis() - Consumer.before < 1000);
				Consumer.stp();
				
				// collect # of ops
				for( int i = 0; i < numThreads; i++) sum += (int)(ConsumerCounts[i] / ((Consumer.after - Consumer.before) / 1000.0));
				
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sum;
	}
	
	public int producerConsumer(int numOfProducers, int numOfConsumers, Class<? extends ParallelPriorityQueue> queueClass, int c){
		int sumP = 0;
		int sumC = 0;
		int loopCount = 10;
		int initElementNum = 30000;
		
		for (int loop = 0; loop < loopCount; loop++) {
			try {
				
				// create a queue, may cause exceptions, hance try-catch
				ParallelPriorityQueue queue = factoryMethod(queueClass, c, numOfProducers + numOfConsumers);
				
				// this part is to check whether the null pointer exception
				if (queue == null) {
					System.err.println("queue instantiate error");
				}
				
				for (int i = 0 ; i < initElementNum; i++) queue.add(i);
				
				// add tasks
				int[] ProducerCounts = new int[numOfProducers];
				int[] ConsumerCounts = new int[numOfConsumers];
				ArrayList<Producer> producers = new ArrayList<>();
				ArrayList<Consumer> consumers = new ArrayList<>();
				for( int i = 0; i < numOfProducers; i++) producers.add(new Producer(i, queue, ProducerCounts, valuelist));
				for( int i = 0; i < numOfConsumers; i++) consumers.add(new Consumer(i, queue, ConsumerCounts));

				// get tasks running
				for (Producer producer : producers) producer.start();
				for (Consumer consumer : consumers) consumer.start();
				
				// get tasks doing add and polls
				Producer.sta();
				Consumer.sta();
				while (Producer.before < 0 || Consumer.before < 0 || System.currentTimeMillis() - Producer.before < 1000 || System.currentTimeMillis() - Consumer.before < 1000);
				Producer.stp();
				Consumer.stp();
				
				// collect # of ops
				for( int i = 0; i < numOfProducers; i++) sumP += (int)(ProducerCounts[i] / ((Producer.after - Producer.before) / 1000.0));
				for( int i = 0; i < numOfConsumers; i++) sumC += (int)(ConsumerCounts[i] / ((Consumer.after - Consumer.before) / 1000.0));
				
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// see balance or not
		System.out.println(sumP / loopCount);
		System.out.println(sumC / loopCount);
		return (sumP + sumC) / loopCount;
	}
	
	public int[][] producerConsumerDiffNumThread(Class<? extends ParallelPriorityQueue> queueClass, int c) {
		
		int startpoint = 4;
		int expo = 4;
		int[][] datapoints = new int[2][10];
		datapoints[0][0] = startpoint;
		for (int i = 1; i < 10; i++) {
			datapoints[0][i] = datapoints[0][i - 1] + expo;
		}
		
		for (int i = 0; i < 10; i++) {
			int N = datapoints[0][i];
			datapoints[1][i] = producerConsumer(N - N / 2, N / 2, queueClass, c);
			System.out.format("%d, %d\n", N, datapoints[1][i]);
		}
		
		return datapoints;
	}
	
    public int[][] producerConsumerDiffProducerPortion(Class<? extends ParallelPriorityQueue> queueClass, int c) {
        
        int[][] datapoints = new int[2][15];
        for (int i = 0; i < 15; i++) {
            datapoints[0][i] = i + 1;
        }
        
        for (int i = 0; i < 15; i++) {
            int N = datapoints[0][i];
            //			System.out.println("points");
            datapoints[1][i] = producerConsumer(N, 16 - N, queueClass, c);
            System.out.format("%d, %d\n", N, datapoints[1][i]);
        }
        return datapoints;
    }
	
	public static void main(String args[]) {
		ThroughPut tp = new ThroughPut();
//		tp.addVariousNumThreadsTest(LockFreePriorityQueueWrapper.class, 0);
		for (int i = 1; i < 33; i++)
			System.out.println(tp.pollFixedNumThreadsTest(i, LockFreePriorityQueueWrapper.class, 0));
		
//		tp.producerConsumerDiffNumThread(SkipQueue.class, 0);
//		tp.producerConsumerDiffProducerPortion(LockFreePriorityQueueWrapper.class, 0);
	}
}
