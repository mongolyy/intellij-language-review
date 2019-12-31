package org.reviewPlugin.converter.impl;

import org.reviewPlugin.converter.ReviewConverter;
import org.reviewPlugin.log.LogHandler;

import java.util.Map;

public class ReviewConverterImpl implements ReviewConverter {
    @Override
    public String convert(String content, Map<String, Object> options) {
        String htmlContent = content.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>");
        return htmlContent;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void registerLogHandler(LogHandler logHandler) {

    }

    @Override
    public void unregisterLogHandler(LogHandler logHandler) {

    }
}
