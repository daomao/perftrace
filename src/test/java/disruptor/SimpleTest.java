/**
 * 
 */
package disruptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;

import disruptor.WorkPoolTest.WPWorkPoolHandler;

/**
 * @author zhongfeng
 * 
 */
public class SimpleTest {

	/**
	 * 
	 */
	public SimpleTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExecutorService exec = Executors.newCachedThreadPool();
		Disruptor<ValueEvent> disruptor = new Disruptor<ValueEvent>(
				ValueEvent.EVENT_FACTORY, 1024, Executors.newCachedThreadPool());
		final EventHandler<ValueEvent> handler = new EventHandler<ValueEvent>() {

			@Override
			public void onEvent(ValueEvent event, long sequence,
					boolean endOfBatch) throws Exception {
				System.out.println("Sequence: " + sequence + "   ValueEvent: "
						+ event.getValue());
			}
		};

		disruptor.handleEventsWith(handler,handler);
		disruptor.after(handler,handler);
		//disruptor.handleEventsWithWorkerPool( new WPWorkPoolHandler("T1"), new WPWorkPoolHandler("T2"));
		RingBuffer<ValueEvent> ringBuffer = disruptor.start();
		for (long i = 10; i < 15; i++) {
			String uuid = String.valueOf(i);
			long seq = ringBuffer.next();
			ValueEvent valueEvent = ringBuffer.get(seq);
			valueEvent.setValue(uuid);
			ringBuffer.publish(seq);
		}
		disruptor.shutdown();
		exec.shutdown();
	}
	
	
	public static class ValueEvent{
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return "ValueEvent [value=" + value + "]";
		}

		public final static EventFactory<ValueEvent> EVENT_FACTORY = new EventFactory<ValueEvent>() {

			@Override
			public ValueEvent newInstance() {
				return new ValueEvent();
			}
		};
	}
}
