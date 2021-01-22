package io.moquette.broker.metrics;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

public class SystemMessage {
    public static void main(String[] args) throws UnknownHostException {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor processor = hal.getProcessor();  //获取cpu信息
        setCpuInfo(processor);
        System.out.println("===============================================================");
        GlobalMemory memory = hal.getMemory();  //获取内存信息
        setMemInfo(memory);
        System.out.println("===============================================================");
        setSysInfo(); //服务器信息
        System.out.println("===============================================================");
        setJvmInfo(); //jvm信息
        System.out.println("===============================================================");
        OperatingSystem op = si.getOperatingSystem();
        setSysFiles(op); //磁盘信息
        System.out.println("===============================================================");

    }

    /**
     * cpu信息
     * @param processor
     */
    private static void setCpuInfo(CentralProcessor processor) {   // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        System.out.println("cpu信息===>>>" + processor);
        System.out.println("核心数===>>>" + processor.getLogicalProcessorCount());
        System.out.println("CPU总的使用率totalCpu===>>>" + totalCpu);
        System.out.println("CPU系统使用率cSys===>>>" + cSys);
        System.out.println("CPU用户使用率user===>>>" + user);
        System.out.println("CPU当前等待率iowait===>>>" + iowait);
        System.out.println("CPU当前空闲率idle===>>>" + idle);
    }

    /**
     * 内存信息
     */
    private static void setMemInfo(GlobalMemory memory) {
        System.out.println("内存大小字节KB=====>>>"+memory.getTotal());
        System.out.println("已使用内存大小=====>>>"+(memory.getTotal() - memory.getAvailable()));
        System.out.println("剩余内存大小G=====>>>"+memory.getAvailable()/1024/1024/1024); //G
    }

    /**
     * 服务器信息
     */
    private static void setSysInfo() {
        Properties props = System.getProperties();
        props.getProperty("os.name");
        props.getProperty("os.arch");
        props.getProperty("user.dir");
        System.out.println("系统版本======>>"+ props.getProperty("os.name"));
        System.out.println("位数======>>"+props.getProperty("os.arch"));
        System.out.println("项目地址======>>"+props.getProperty("user.dir"));
    }


    /**
     * Java虚拟机
     */
    private  static  void setJvmInfo() {
        Properties props = System.getProperties();
        System.out.println("当前JVM占用的内存总数(M)=====>>>>"+div(Runtime.getRuntime().totalMemory(),1024*1024,100));
        System.out.println("JVM最大可用内存总数(M)=====>>>>"+div(Runtime.getRuntime().maxMemory(),1024*1024,100));
        System.out.println("JVM空闲内存(M)=====>>>>"+div(Runtime.getRuntime().freeMemory(),1024*1024,100));
        System.out.println("JDK版本=====>>>>"+props.getProperty("java.version"));
        System.out.println("JDK路径=====>>>>"+props.getProperty("java.home"));
    }

    /**
     * 设置磁盘信息
     */
    private static void setSysFiles(OperatingSystem os) {
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        for (OSFileStore fs : fileStores) {
            long free = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            long used = total - free;
            System.out.println("盘符路径======>>>>"+fs.getMount());
            System.out.println("盘符类型======>>>>"+fs.getType());
            System.out.println("文件类型======>>>>"+fs.getName());
            System.out.println("总大小======>>>>"+convertFileSize(total));
            System.out.println("剩余大小======>>>>"+convertFileSize(free));
            System.out.println("已经使用量======>>>>"+convertFileSize(used));
            System.out.println("资源的使用率======>>>>"+div(used, total, 4));
            System.out.println("=======================================");
        }
    }

    /**
     * 字节转换
     *
     * @param size 字节大小
     * @return 转换后值
     */
    public  static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double v1, double v2, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        if (b1.compareTo(BigDecimal.ZERO) == 0)
        {
            return BigDecimal.ZERO.doubleValue();
        }
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }
}
