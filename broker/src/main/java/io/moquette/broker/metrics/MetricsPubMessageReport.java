package io.moquette.broker.metrics;

import com.alibaba.fastjson.JSON;
import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import io.moquette.contants.ConstantsTopics;
import io.moquette.broker.PostOffice;
import io.netty.buffer.Unpooled;
import io.handler.codec.mqtt.*;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MetricsPubMessageReport extends ScheduledReporter {
    private PostOffice postOffice;
    private OperatingSystemMXBean systemMXBean ;
    private static final Runtime runtime = Runtime.getRuntime();


    protected MetricsPubMessageReport(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit, ScheduledExecutorService executor, boolean shutdownExecutorOnStop, Set<MetricAttribute> disabledMetricAttributes, PostOffice postOffice) {
        super(registry, name, filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, disabledMetricAttributes);
        this.postOffice = postOffice;
        this.systemMXBean = ManagementFactory.getOperatingSystemMXBean();
    }


    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

        if (!gauges.isEmpty()) {
            postOffice.internalPublish(wrappMessage(gauges));
        }

        if (!counters.isEmpty()) {
            postOffice.internalPublish(wrappMessage(counters));
        }

        if (!histograms.isEmpty()) {
            postOffice.internalPublish(wrappMessage(histograms));
        }

        if (!meters.isEmpty()) {
            postOffice.internalPublish(wrappMessage(meters));
        }

        if (!timers.isEmpty()) {
            postOffice.internalPublish(wrappMessage(timers));
        }
    }


    public MqttPublishMessage wrappMessage(SortedMap metricSortedMap) {
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(ConstantsTopics.$SYS_METRICS, ThreadLocalRandom.current().nextInt(10000));
        byte[] bytes1 = JSON.toJSONString(variableHeader).getBytes();
        byte[] bytes = JSON.toJSONString(metricSortedMap).getBytes();
        MqttFixedHeader header = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_MOST_ONCE, false, bytes1.length + bytes.length);
        return new MqttPublishMessage(header, variableHeader, Unpooled.copiedBuffer(bytes));
    }




    private MqttPublishMessage jvmInfo(){
        return null;
    }


    public static MetricsPubMessageReport.Builder forRegistry(MetricRegistry registry) {
        return new MetricsPubMessageReport.Builder(registry);
    }

    public static class Builder {
        private final MetricRegistry registry;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private String name;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.name = "MetricsPubMessageReport";
            this.shutdownExecutorOnStop = true;
            this.disabledMetricAttributes = Collections.emptySet();
        }

        public MetricsPubMessageReport.Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        public MetricsPubMessageReport.Builder scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }


        public MetricsPubMessageReport.Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public MetricsPubMessageReport.Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public MetricsPubMessageReport.Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public MetricsPubMessageReport.Builder reName(String name) {
            this.name = name;
            return this;
        }

        public MetricsPubMessageReport.Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }

        public MetricsPubMessageReport build(PostOffice postOffice) {
            return new MetricsPubMessageReport(this.registry, this.name, this.filter, this.rateUnit, this.durationUnit, this.executor, this.shutdownExecutorOnStop, this.disabledMetricAttributes, postOffice);
        }
    }
}
