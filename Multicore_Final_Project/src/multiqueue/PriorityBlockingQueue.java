/*
 */
package multiqueue;

import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityBlockingQueue<T> extends PriorityQueue<T>{
    private ReentrantLock r;
	private int versionID;
	private int lastPeekID;
    private static final long serialVersionUID = 42L;
	public PriorityBlockingQueue(){
		super();
    	versionID = 0;
		r = new ReentrantLock();
	}
	public boolean add(T t){
		if (r.tryLock()){
			super.add(t);
			++versionID;
			r.unlock();
			return true;
		}
		return false;
	}
	public T poll(){
		if (r.tryLock()){
			T result = super.poll();
			++versionID;
			r.unlock();
			return result;
		}
		return null;
	}
    public T peek(){
		if (r.tryLock()){
			T result = super.peek();
			lastPeekID = versionID;
			r.unlock();
			return result;
		}
		return null;
	}
	public T pollAfterPeek(){
		if (versionID == lastPeekID && r.tryLock()){
			T result = super.poll();
			++versionID;
			r.unlock();
			return result;
		}
		return null;
	}
}