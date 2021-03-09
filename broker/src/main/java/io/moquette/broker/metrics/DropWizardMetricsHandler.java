package io.moquette.broker.metrics;

import com.codahale.metrics.*;
import io.moquette.broker.PostOffice;
import io.moquette.broker.config.IConfig;
import io.moquette.utils.NettyUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.handler.codec.mqtt.MqttMessage;
import io.handler.codec.mqtt.MqttMessageType;

import java.util.concurrent.TimeUnit;

import static io.moquette.contants.BrokerConstants.*;
import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Pipeline handler use to track some MQTT metrics.
 */
@Sharable
public final class DropWizardMetricsHandler extends ChannelInboundHandlerAdapter {
    private MetricRegistry metrics;
    private Meter publishesMetrics;
    private Meter subscribeMetrics;
    private Counter connectedClientsMetrics;
    private PostOffice postOffice;


    public DropWizardMetricsHandler(PostOffice postOffice) {
        this.postOffice = postOffice;
    }

    public void init(IConfig props) {
        this.metrics = new MetricRegistry();
        this.publishesMetrics = metrics.meter("publish.requests");
        this.subscribeMetrics = metrics.meter("subscribe.requests");
        this.connectedClientsMetrics = metrics.counter("connect.num_clients");

//        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
//            .convertRatesTo(TimeUnit.SECONDS)
//            .convertDurationsTo(TimeUnit.MILLISECONDS)
//            .build();
//        reporter.start(1, TimeUnit.MINUTES);
//
//        final String email = props.getProperty(METRICS_LIBRATO_EMAIL_PROPERTY_NAME);
//        final String token = props.getProperty(METRICS_LIBRATO_TOKEN_PROPERTY_NAME);
        final String source = props.getProperty(METRICS_LIBRATO_SOURCE_PROPERTY_NAME);

        MetricsPubMessageReport metricsPubMessageReport = MetricsPubMessageReport
            .forRegistry(metrics)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build(postOffice);

        metricsPubMessageReport.start(1, TimeUnit.MINUTES);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        MqttMessage msg = (MqttMessage) message;
        MqttMessageType messageType = msg.fixedHeader().messageType();
        switch (messageType) {
            case PUBLISH:
                this.publishesMetrics.mark();
                break;
            case SUBSCRIBE:
                this.subscribeMetrics.mark();
                break;
            case CONNECT:
                this.connectedClientsMetrics.inc();
                break;
            case DISCONNECT:
//                this.connectedClientsMetrics.dec();
                break;
            default:
                break;
        }
        ctx.fireChannelRead(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String clientID = NettyUtils.clientID(ctx.channel());
        if (clientID != null && !clientID.isEmpty()) {
            this.connectedClientsMetrics.dec();
        }
        ctx.fireChannelInactive();
    }

}
