package org.reviewPlugin.editor.jeditor;

import com.intellij.openapi.editor.Document;
import org.jetbrains.annotations.NotNull;
import org.reviewPlugin.editor.ReviewHtmlPanel;
import org.reviewPlugin.editor.ReviewHtmlPanelProvider;

import java.nio.file.Path;

public class JeditorHtmlPanelProvider extends ReviewHtmlPanelProvider {
    public static final ProviderInfo INFO = new ProviderInfo("Swing", JeditorHtmlPanelProvider.class.getName());

    @NotNull
    @Override
    public ReviewHtmlPanel createHtmlPanel(Document document, Path imagesPath) {
        return new JeditorHtmlPanel(document);
    }

    @NotNull
    @Override
    public AvailabilityInfo isAvailable() {
        return AvailabilityInfo.AVAILABLE;
    }

    @NotNull
    @Override
    public ProviderInfo getProviderInfo() {
        return INFO;
    }
}
