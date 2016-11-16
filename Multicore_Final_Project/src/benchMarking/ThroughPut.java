package benchMarking;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import interfacepackage.ParallelPriorityQueue;
import lockfreepriorityqueue.linearizable.LockFreePriorityQueueWrapper;
import lockfreepriorityqueue.nonlinearlizable.SkipQueue;
import multiqueue.MultiQueue;

public class ThroughPut {
	Random r = new Random();
	int[] valuelist = new int[5000000];
	
	ThreadPoolExecutor threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
	
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
				
				int[] ProducerCounts = new int[numThreads];
				ArrayList<Producer> producers = new ArrayList<>();

				// instantiate a list of all add threads.
				for( int i = 0; i < numThreads; i++) producers.add(new Producer(i, queue, ProducerCounts, valuelist));
				
				
				// let all threads running
				for (Producer producer : producers) threadPool.execute(producer);
				
				// let all threads start to add items to the queue
				Producer.sta();
				while (Producer.before < 0 || System.currentTimeMillis() - Producer.before < 1000);
				Producer.stp();
				
				// collect # of ops
				for( int i = 0; i < numThreads; i++) sum += (int)(ProducerCounts[i] / ((Producer.after - Producer.before) / 1000.0));
				
				Producer.after = -1;
				Producer.before = -1;
				
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sum / loopCount;
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
				for (Consumer consumer : consumers) threadPool.execute(consumer);
				
				// let all threads start to add items to the queue
				Consumer.sta();
				while (Consumer.before < 0 || System.currentTimeMillis() - Consumer.before < 1000);
				Consumer.stp();
				
				// collect # of ops
				for( int i = 0; i < numThreads; i++) sum += (int)(ConsumerCounts[i] / ((Consumer.after - Consumer.before) / 1000.0));
				
				Consumer.after = -1;
				Consumer.before = -1;
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sum / loopCount;
	}
	
	public int[] producerConsumer(int numOfProducers, int numOfConsumers, Class<? extends ParallelPriorityQueue> queueClass, int c){
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
				ArrayList<Future> producerFuture = new ArrayList<>(numOfProducers);
				ArrayList<Future> consumerFuture = new ArrayList<>(numOfConsumers);
				for (Producer producer : producers) threadPool.execute(producer);
				for (Consumer consumer : consumers) threadPool.execute(consumer);
				
//				System.out.println("submitted");
				// get tasks doing add and polls
				Producer.sta();
				Consumer.sta();
				while (Producer.before < 0 || Consumer.before < 0 || System.currentTimeMillis() - Producer.before < 1000 || System.currentTimeMillis() - Consumer.before < 1000);
				Producer.stp();
				Consumer.stp();
				
//				System.out.println("got");
				// collect # of ops
				for( int i = 0; i < numOfProducers; i++) sumP += (int)(ProducerCounts[i] / ((Producer.after - Producer.before) / 1000.0));
				for( int i = 0; i < numOfConsumers; i++) sumC += (int)(ConsumerCounts[i] / ((Consumer.after - Consumer.before) / 1000.0));
				
				Producer.after = -1;
				Producer.before = -1;
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
		// see balance or not
//		System.out.println(sumP / loopCount);
//		System.out.println(sumC / loopCount);
		return new int[]{sumP / loopCount, sumC / loopCount, (sumP + sumC) / loopCount};
	}
	
	public int[][] producerConsumerDiffNumThread(double portion, Class<? extends ParallelPriorityQueue> queueClass, int c) {
		
		int startpoint = Math.max((int)(1 / portion), (int)(1 / (1 - portion) + 1));
		int expo = 2;
		int[][] datapoints = new int[4][10];
		datapoints[0][0] = startpoint;
		for (int i = 1; i < 10; i++) {
			datapoints[0][i] = datapoints[0][i - 1] + expo;
		}
		
		for (int i = 0; i < 10; i++) {
			int N = datapoints[0][i];
			int p = (int)(N * portion);
			int[] result = producerConsumer(p, N - p, queueClass, c);
			datapoints[1][i] = result[0];
			datapoints[2][i] = result[1];
			datapoints[3][i] = result[2];
			System.out.println(i);
//			if (i > 1) break;
		}
		
		return datapoints;
	}
	
	public int[][] producerConsumerDiffProducerPortion(Class<? extends ParallelPriorityQueue> queueClass, int c) {

		int[][] datapoints = new int[4][15];
		for (int i = 0; i < 15; i++) {
			datapoints[0][i] = i + 1;
		}
		
		for (int i = 0; i < 15; i++) {
			int N = datapoints[0][i];
//			System.out.println("points");
			int[] result = producerConsumer(N, 16 - N, queueClass, c);
			datapoints[1][i] = result[0];
			datapoints[2][i] = result[1];
			datapoints[3][i] = result[2];
			System.out.println(i);
		}
		return datapoints;
	}
	
	public int alternateFixedThreads(int numThreads, Class<? extends ParallelPriorityQueue> queueClass, int c) {
		int sum = 0;
		int loopCount = 10;
		int initElementNum = 300;
		
		for (int loop = 0; loop < loopCount; loop++) {
			try {
				
				// create a queue, may cause exceptions, hance try-catch
				ParallelPriorityQueue queue = factoryMethod(queueClass, c, numThreads);
				
				for (int i = 0 ; i < initElementNum; i++) queue.add(i);
				
				// operation counts for threads
				int[] Counts = new int[numThreads];
				
				// instantiate a list of all add threads.
				ArrayList<Alternate> alternates = new ArrayList<>();
				for( int i = 0; i < numThreads; i++) alternates.add(new Alternate(i, queue, Counts, valuelist));
				
				// let all threads running
				for (Alternate alternate : alternates)  threadPool.execute(alternate);
				
				// let all threads start to add items to the queue
				Alternate.sta();
				while (Alternate.before < 0 || System.currentTimeMillis() - Alternate.before < 1000);
				Alternate.stp();
				
				// collect # of ops
				for( int i = 0; i < numThreads; i++) sum += (int)(Counts[i] / ((Alternate.after - Alternate.before) / 1000.0));
				
				Alternate.after = -1;
				Alternate.before = -1;
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}
		return sum / loopCount;
	}
	
	public int[][] alternateDiffNumThreads(Class<? extends ParallelPriorityQueue> queueClass, int c) {
		int[][] datapoints = new int[2][32];
		for (int i = 0; i < 32; i++) {
			datapoints[0][i] = i + 1;
		}
		
		for (int i = 0; i < 32; i++) {
			int N = datapoints[0][i];
			datapoints[1][i] = alternateFixedThreads(N, queueClass, c);
			System.out.format("%d, %d\n", N, datapoints[1][i]);
		}
		return datapoints;
	}
	
	public static void write2file(int[][] data, String filename) {
	    
		try (PrintWriter writer = new PrintWriter(filename + ".txt", "UTF-8");) {
			int n = data.length;
			int m = data[0].length;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					writer.print(data[i][j]);
					writer.print(", ");
				}
				writer.print('\n');
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String args[]) {
		String postfix = Long.toString(System.currentTimeMillis() / 1000 % 10000);
		ThroughPut tp = new ThroughPut();
		
		String arg = args[0];
		
		int s = Integer.valueOf(arg);
		
		try{
			
			switch(s) {
			case 1: 
				write2file(tp.producerConsumerDiffNumThread(0.25, SkipQueue.class, 0), "SkipQueue_diffThread_0.25_" + postfix);
				break;
			case 2: 
				write2file(tp.producerConsumerDiffNumThread(0.5, SkipQueue.class, 0), "SkipQueue_diffThread_0.50_" + postfix);
				break;
			case 3:
				write2file(tp.producerConsumerDiffNumThread(0.75, SkipQueue.class, 0), "SkipQueue_diffThread_0.75_" + postfix);
				break;
			case 4:
				write2file(tp.producerConsumerDiffNumThread(0.25, LockFreePriorityQueueWrapper.class, 0), "LockFreePriorityQueueWrapper_diffThread_0.25_" + postfix);
				break;
			case 5:
				write2file(tp.producerConsumerDiffNumThread(0.5, LockFreePriorityQueueWrapper.class, 0), "LockFreePriorityQueueWrapper_diffThread_0.50_" + postfix);
				break;
			case 6:
				write2file(tp.producerConsumerDiffNumThread(0.75, LockFreePriorityQueueWrapper.class, 0), "LockFreePriorityQueueWrapper_diffThread_0.75_" + postfix);
				break;
			case 7:
				write2file(tp.producerConsumerDiffNumThread(0.25, MultiQueue.class, 4), "MultiQueue_diffThread_0.25_c4_" + postfix);
				break;
			case 8:
				write2file(tp.producerConsumerDiffNumThread(0.5, MultiQueue.class, 4), "MultiQueue_diffThread_0.50_c4_" + postfix);
				break;
			case 9:
				write2file(tp.producerConsumerDiffNumThread(0.75, MultiQueue.class, 4), "MultiQueue_diffThread_0.75_c4_" + postfix);
				break;
			case 10:
				write2file(tp.producerConsumerDiffNumThread(0.25, MultiQueue.class, 2), "MultiQueue_diffThread_0.25_c2_" + postfix);
				break;
			case 11:
				write2file(tp.producerConsumerDiffNumThread(0.5, MultiQueue.class, 2), "MultiQueue_diffThread_0.50_c2_" + postfix);
				break;
			case 12:
				write2file(tp.producerConsumerDiffNumThread(0.75, MultiQueue.class, 2), "MultiQueue_diffThread_0.75_c2_" + postfix);
				break;
			case 13:
				write2file(tp.producerConsumerDiffProducerPortion(SkipQueue.class, 0), "SkipQueue_diffProducerPortion_" + postfix);
				break;
			case 14:
				write2file(tp.producerConsumerDiffProducerPortion(MultiQueue.class, 2), "MultiQueue_diffProducerPortion_c2_" + postfix);
				break;
			case 15:
				write2file(tp.producerConsumerDiffProducerPortion(MultiQueue.class, 4), "MultiQueue_diffProducerPortion_c4_" + postfix);
				break;
			case 16:
				write2file(tp.producerConsumerDiffProducerPortion(LockFreePriorityQueueWrapper.class, 0), "LockFreePriorityQueueWrapper_diffProducerPortion_" + postfix);
				break;
			case 17:
				write2file(tp.alternateDiffNumThreads(MultiQueue.class, 2), "MultiQueue_PoshPop_c2_" + postfix);
				break;
			case 18:
				write2file(tp.alternateDiffNumThreads(MultiQueue.class, 4), "MultiQueue_PoshPop_c4_" + postfix);
				break;
			case 19:
				write2file(tp.alternateDiffNumThreads(SkipQueue.class, 0), "SkipQueue_PoshPop_" + postfix);
				break;
			case 20:
				write2file(tp.alternateDiffNumThreads(LockFreePriorityQueueWrapper.class, 0), "LockFreePriorityQueueWrapper_PoshPop_" + postfix);
				break;
			case 21:
				write2file(tp.alternateDiffNumThreads(MultiQueue.class, 8), "MultiQueue_PoshPop_c8_" + postfix);
				break;
			case 22:
				write2file(tp.producerConsumerDiffNumThread(0.25, MultiQueue.class, 8), "MultiQueue_diffThread_0.25_c8_" + postfix);
				break;
			case 23:
				write2file(tp.producerConsumerDiffNumThread(0.5, MultiQueue.class, 8), "MultiQueue_diffThread_0.50_c8_" + postfix);
				break;
			case 24:
				write2file(tp.producerConsumerDiffNumThread(0.75, MultiQueue.class, 8), "MultiQueue_diffThread_0.75_c8_" + postfix);
				break;
			default:
				System.out.println("Invalid Argument! Argument should be a number between 1 to 24 inclusive.");
				break;
			}
			
		} finally {
			tp.threadPool.shutdown();
			System.exit(0);
		}
		
	}
}
