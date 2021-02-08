package pushmanager.core.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import pushmanager.core.MqttClientWrapping;
import pushmanager.core.MqttConnectManager;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MessageDispatch {

    private static volatile MessageDispatch instance;

    private ScheduledExecutorService executorService;

    private ConcurrentLinkedQueue<MessageWrapping> queue;

    private AtomicInteger atomicInteger;
    private int wittingTime = 500;

    private MessageDispatch() {

        int corePoolSize = 1;
        String threadName = "dispatch-thread-pool";
        executorService = Executors.newScheduledThreadPool(corePoolSize, r -> {
            Thread thread = new Thread(r);
            thread.setName(threadName);
            return thread;
        });
        executorService.scheduleWithFixedDelay(new DispatchWorker(), 0, wittingTime, TimeUnit.MILLISECONDS);
        queue = new ConcurrentLinkedQueue<>();
        atomicInteger = new AtomicInteger(0);
    }


    public static MessageDispatch getInstance() {
        if (null == instance) {
            synchronized (MessageDispatch.class) {
                if (null == instance) {
                    instance = new MessageDispatch();
                }
            }
        }
        return instance;
    }


    public void addMessage(MessageWrapping messageWrapping) {
        int i = atomicInteger.incrementAndGet();
        queue.add(messageWrapping);
        if (i > 100)
            executorService.scheduleWithFixedDelay(new DispatchWorker(), 0, wittingTime, TimeUnit.MILLISECONDS);
    }


    class DispatchWorker implements Runnable {


        @Override
        public void run() {
            MessageWrapping messageWrapping = queue.poll();
            while (null != messageWrapping) {
                atomicInteger.decrementAndGet();
                Map<String, MqttClientWrapping> metricsStatusQueue = MqttConnectManager.getInstance().getMetricsStatusQueue();
                for (String key : metricsStatusQueue.keySet()) {
                    if (!key.equals(messageWrapping.getSourceUrl())) {
                        try {
                            metricsStatusQueue.get(key).publish(messageWrapping.getTopic(), messageWrapping.getMqttMessage());
                        } catch (MqttException e) {
                            log.error(e.getMessage());
                        }
                    }
                }
                messageWrapping = queue.poll();
            }
        }
    }

}
