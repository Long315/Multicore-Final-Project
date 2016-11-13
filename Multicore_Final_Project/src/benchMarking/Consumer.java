package benchMarking;

import java.time.Instant;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import interfacepackage.ParallelPriorityQueue;

public class Consumer extends Thread{
	  int Id;
	  ParallelPriorityQueue queue;
	  int[] counts;
	  CyclicBarrier gate;
	  public Consumer(int Id, ParallelPriorityQueue queue, int[] counts, CyclicBarrier gate) {
		this.Id = Id;
	    this.queue = queue;
	    this.counts = counts;
	    this.gate = gate;
	  }
	  public void run() {
		  try {
				gate.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  before = Instant.now().toEpochMilli();
//		  System.out.println(running);
		  while(running){
			  queue.poll();
			  counts[Id] = counts[Id] + 1;
		  }
	  }
	  
}
