package io.moquette.utils;

import io.moquette.broker.subscriptions.Topic;

public class TopicUtils {

    public static String[] getTopicLevelArr(String value) {
        String[] split;
        if (value.startsWith("/") && value.endsWith("/")) {
            value = "aa" + value + "aa";
            split = value.split("/");
            split[0] = "/";
            split[split.length - 1] = "/";
        } else if (value.startsWith("/")) {
            value = "aa" + value;
            split = value.split("/");
            split[0] = "/";
        } else if (value.endsWith("/")) {

            value = value + "aa";
            split = value.split("/");
            split[split.length - 1] = "/";
        } else {
            split = value.split("/");
        }
        return split;
    }

    public static String formatTopics(Topic topic) {
        return topic.getValue().replace("in topics", "").trim().replace("'", "")
            .replace("\"", "").replaceAll("\\s+", " ").trim();
    }
}
