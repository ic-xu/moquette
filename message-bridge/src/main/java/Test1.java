import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class Test1 {

// -XX:+UseConcMarkSweepGC
    public static void main(String[] args) throws InterruptedException {


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

        thread();

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