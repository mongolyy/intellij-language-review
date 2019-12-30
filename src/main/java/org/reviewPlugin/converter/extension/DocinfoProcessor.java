package org.reviewPlugin.converter.extension;

import org.reviewPlugin.converter.ast.Document;

import java.util.HashMap;
import java.util.Map;

public abstract class DocinfoProcessor extends BaseProcessor {

    public DocinfoProcessor() {
        super(new HashMap<>());
    }

    public DocinfoProcessor(Map<String, Object> config) {
        super(config);
    }

    public abstract String process(Document document);
}
