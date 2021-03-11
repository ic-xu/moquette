import io.client.mqttv3.*;
import pushmanager.core.MqttClientWrapping;
import pushmanager.core.dispatch.MessageDispatch;
import pushmanager.core.dispatch.MessageWrapping;
import pushmanager.utils.IPStringUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class Test1 {

    // -XX:+UseConcMarkSweepGC
    public static void main(String[] args) throws InterruptedException, MqttException {


//        CountDownLatch countDownLatch = new CountDownLatch(2);
//
//        countDownLatch.countDown();
//
//        countDownLatch.await();
////        ArrayList<String> stringArrayList = new ArrayList<>();
////
////        for (;;) {
////            stringArrayList.add("fffff");
////        }

//        thread();

        MqttClient client = new MqttClient("tcp://192.168.42.194:1883", "88888888888888");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("admin");
        options.setPassword("passwd".toCharArray());
        options.setConnectionTimeout(5000); // 设置超时时间
        options.setCleanSession(true);
        options.setKeepAliveInterval(3000);// 设置会话心跳时间
        options.setAutomaticReconnect(true); // 自动重连
        int[] Qos = {2, 2};
        String[] registerTopics = {"$SYS_METRICS", "$SYS_BROADCASTING"};
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println(cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                String msg = new String(message.getPayload(), Charset.forName("UTF-8"));
                System.out.println("messageArrived() topic:" + topic);
                System.out.println(msg);

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
//                try {
//                    client.subscribe(registerTopics,Qos);
//                } catch (MqttException e) {
//                    e.printStackTrace();
//                }
            }
        });
//        client.connect(options);
        new Thread(()-> {
            try {
                client.connect(options);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while (true){
            String s = scanner.nextLine();
            client.sendCustomerMessage(s.getBytes(),1,false);

        }



    }

    private static void thread() {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2, () -> {
            System.out.println("*********** all end *******");
        });


        ExecutorService service = Executors.newFixedThreadPool(3);
        service.submit(() -> {
            try {
                System.out.println("----1-----begin---------");
                TimeUnit.SECONDS.sleep(2);
                cyclicBarrier.await();
                System.out.println("----1-----end---------");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });


        service.submit(() -> {
            try {
                System.out.println("----2-----begin---------");
                TimeUnit.SECONDS.sleep(1);
                cyclicBarrier.await();
                System.out.println("----2-----end---------");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });


        service.submit(() -> {
            try {
                System.out.println("----3-----begin---------");
                TimeUnit.SECONDS.sleep(5);
                cyclicBarrier.await();
                System.out.println("-----3----end---------");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });

        service.submit(() -> {
            try {
                System.out.println("----3-----begin---------");
                TimeUnit.SECONDS.sleep(5);
                cyclicBarrier.await();
                System.out.println("-----3----end---------");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });


//        StampedLock lock = new StampedLock();
//        long l1 = lock.tryOptimisticRead();
//
//        long l = lock.readLock();
//
//        lock.unlockRead(l);

        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock(false);
        ReentrantReadWriteLock.WriteLock readLock = reentrantReadWriteLock.writeLock();
        readLock.lock();


        Semaphore semaphore = new Semaphore(2);
    }


    private static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            return null;
        }
    }
}
