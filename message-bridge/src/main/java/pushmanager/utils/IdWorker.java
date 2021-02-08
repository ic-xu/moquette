package pushmanager.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author chenxu
 * @apiNote 唯一Id生成器
 * @date 19-6-19 - 下午1:58
 */

public class IdWorker {
    /**
     * @apiNote 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动）
     */
    private final static long TWEPOCH = 1610952399000L;
    // 机器标识位数
    private final static long WORKER_ID_BITS = 5L;
    // 数据中心标识位数
    private final static long DATACENTER_ID_BITS = 5L;
    // 机器ID最大值
    private final static long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
    // 数据中心ID最大值
    private final static long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_ID_BITS);
    // 毫秒内自增位
    private final static long SEQUENCE_BITS = 30L;
    // 机器ID偏左移12位
    private final static long WORKER_ID_SHIFT = SEQUENCE_BITS;
    // 数据中心ID左移17位
    private final static long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    // 时间毫秒左移22位
    private final static long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;


    private final static long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);
    /* 上次生产id时间戳 */
    private static long lastTimestamp = -1L;

    // 0，并发控制
    private long sequence = 0L;


    private final long workerId;
    // 数据标识id部分
    private final long datacenterId;


    private IdWorker() {
        this.datacenterId = getDatacenterId(MAX_DATACENTER_ID);
        this.workerId = getMaxWorkerId(datacenterId, MAX_WORKER_ID);
    }

    private static IdWorker idWorker;

    public static IdWorker getInstance() {
        if (null == idWorker) {
            synchronized (IdWorker.class) {
                if (null == idWorker) {
                    idWorker = new IdWorker();
                }
            }
        }
        return idWorker;
    }

    /**
     * 获取下一个ID
     *
     * @return
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }


        if (lastTimestamp == timestamp) {
            // 当前毫秒内，则+1
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // 当前毫秒内计数满了，则等待下一秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        // ID偏移组合生成最终的ID，并返回ID
        long nextId = ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT) | sequence;


//        return getRandomString(5) + nextId;
        return nextId;
    }


    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }


    private long timeGen() {
        return System.currentTimeMillis();
    }


    /**
     * <p>
     * 获取 maxWorkerId
     * </p>
     */
    protected static long getMaxWorkerId(long datacenterId, long maxWorkerId) {
        StringBuffer mpid = new StringBuffer();
        mpid.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (!name.isEmpty()) {
            /*
             * GET jvmPid
             */
            mpid.append(name.split("@")[0]);
        }
        /*
         * MAC + PID 的 hashcode 获取16个低位
         */
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }


    /**
     * <p>
     * 数据标识id部分
     * </p>
     */
    protected static long getDatacenterId(long maxDatacenterId) {
        long id = 0L;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                id = ((0x000000FF & (long) mac[mac.length - 1])
                        | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
                id = id % (maxDatacenterId + 1);
            }
        } catch (Exception e) {
            System.out.println(" getDatacenterId: " + e.getMessage());
        }
        return id;
    }


    /**
     * 获取指定长度随机字符串
     *
     * @param length
     * @return
     */
    public String getRandomString(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(3);
            long result;
            switch (number) {
                case 0:
                    result = Math.round(Math.random() * 25 + 65);
                    sb.append((char) result);
                    break;
                case 1:
                    result = Math.round(Math.random() * 25 + 97);
                    sb.append((char) result);
                    break;
                case 2:
                    sb.append(random.nextInt(10));
                    break;
                default:
            }
        }
        return sb.toString();
    }


    public static void main(String[] args) {

        HashSet<Long> objects = new HashSet<>();
        synchronized (IdWorker.class){
            for (int i = 0; i < 10000000; i++) {
                objects.add(IdWorker.getInstance().nextId());
                System.out.println(i);
                System.err.println(objects.size());
            }
            System.err.println(objects.size());
        }

    }

}
