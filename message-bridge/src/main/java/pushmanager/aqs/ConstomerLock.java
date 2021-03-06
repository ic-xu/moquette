package pushmanager.aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

public class ConstomerLock implements Lock {


    /***
     * status 的状态为-1 表示有责任去唤醒后续的节点
     */
    class MySync extends AbstractQueuedSynchronizer {

        @Override
        protected boolean tryAcquire(int arg) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }


        @Override
        protected boolean tryRelease(int arg) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        /**
         * 是否是独占锁
         *
         * @return
         */
        @Override
        protected boolean isHeldExclusively() {
            return getState()==1;
        }

        public Condition newCondition() {
            return new ConditionObject();
        }
    }


    private MySync sync = new MySync();



    private ReentrantLock reentrantLock = new ReentrantLock(true);


    void ff() throws InterruptedException {
        Condition condition = reentrantLock.newCondition();
        condition.signalAll();

        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        readLock.unlock();


        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        writeLock.unlock();

    }
    /**
     * 无限等待加锁
     */
    @Override
    public void lock() {
        sync.tryAcquire(1);
    }

    /**
     * 可打断的加锁
     *
     * @throws InterruptedException
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    /**
     * 尝试加一次锁
     *
     * @return
     */
    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1,unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }
}
