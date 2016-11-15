package benchMarking;

import interfacepackage.ParallelPriorityQueue;

public class Consumer extends Thread{
	  int Id;
	  ParallelPriorityQueue queue;
	  int[] counts;
	  public Consumer(int Id, ParallelPriorityQueue queue, int[] counts) {
		this.Id = Id;
	    this.queue = queue;
	    this.counts = counts;
	    this.setPriority(MAX_PRIORITY);
	  }
	  public void run() {
		  while(!running);
//		  before = Instant.now().toEpochMilli();
//		  System.out.println(running);
		  while(running){
			  try{
				  queue.poll();
			  } catch (NullPointerException e) {
				  
			  }
			 
			  counts[Id] = counts[Id] + 1;
		  }
	  }
	  
}
