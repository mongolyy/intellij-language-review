package org.reviewPlugin.editor;

import com.intellij.CommonBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.nio.file.Path;

public abstract class ReviewHtmlPanelProvider {
    public static final ExtensionPointName<ReviewHtmlPanelProvider> EP_NAME = ExtensionPointName.create("org.reviewPlugin.html.panel.provider");

    private static ReviewHtmlPanelProvider[] ourProviders = null;

    @NotNull
    public abstract ReviewHtmlPanel createHtmlPanel(Document document, Path imagesPath);

    @NotNull
    public abstract AvailabilityInfo isAvailable();

    @NotNull
    public abstract ProviderInfo getProviderInfo();

    @NotNull
    public static ReviewHtmlPanelProvider[] getProviders() {
        if (ourProviders == null) {
            ourProviders = EP_NAME.getExtensions();
        }
        return ourProviders;
    }

    @NotNull
    public static ReviewHtmlPanelProvider createFromInfo(@NotNull ProviderInfo providerInfo) {
        try {
            return Class.forName(providerInfo.getClassName()).asSubclass(ReviewHtmlPanelProvider.class)
                    .getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            Messages.showMessageDialog(
                    "Cannot set preview panel provider (" + providerInfo.getName() + "):\n" + e.getMessage(),
                    CommonBundle.getErrorTitle(),
                    Messages.getErrorIcon()
            );
            Logger.getInstance(ReviewHtmlPanelProvider.class).error(e);
            return getProviders()[0];
        }
    }

    public static class ProviderInfo {
        @NotNull
        @Attribute("name")
        private String myName;
        @NotNull
        @Attribute("className")
        private String className;

        @SuppressWarnings("unused")
        private ProviderInfo() {
            myName = "";
            className = "";
        }

        public ProviderInfo(@NotNull String name, @NotNull String className) {
            myName = name;
            this.className = className;
        }

        @NotNull
        public String getName() {
            return myName;
        }

        @NotNull
        public String getClassName() {
            return className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ProviderInfo)) {
                return false;
            }

            ProviderInfo info = (ProviderInfo) o;

            if (!myName.equals(info.myName)) {
                return false;
            }
            if (!className.equals(info.className)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = myName.hashCode();
            result = 31 * result + className.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return myName;
        }
    }

    public abstract static class AvailabilityInfo {
        public static final AvailabilityInfo AVAILABLE = new AvailabilityInfo() {
            @Override
            public boolean checkAvailability(@NotNull JComponent parentComponent) {
                return true;
            }
        };

        public static final AvailabilityInfo UNAVAILABLE = new AvailabilityInfo() {
            @Override
            public boolean checkAvailability(@NotNull JComponent parentComponent) {
                return false;
            }
        };

        public abstract boolean checkAvailability(@NotNull JComponent parentComponent);
    }
}
