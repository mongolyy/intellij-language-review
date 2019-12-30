package org.reviewPlugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.messages.Topic;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@State(
        name = "ReviewApplicationSettings",
        storages = @Storage("review.xml")
)
public class ReviewApplicationSettings implements PersistentStateComponent<ReviewApplicationSettings.State>, ReviewPreviewSettings.Holder {
    private State myState = new State();

    private Map<String, Boolean> extensionsEnabled = new ConcurrentHashMap<>();
    private Map<String, Boolean> extensionsPresent = new ConcurrentHashMap<>();

    @NotNull
    public static ReviewApplicationSettings getInstance() {
        return ServiceManager.getService(ReviewApplicationSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }

    @Override
    public void setReviewPreviewSettings(@NotNull ReviewPreviewSettings settings) {
        myState.myPreviewSettings = settings;

        ApplicationManager.getApplication().getMessageBus().syncPublisher(SettingsChangedListener.TOPIC).onSettingsChange(this);
    }

    @NotNull
    @Override
    public ReviewPreviewSettings getReviewPreviewSettings() {
        return myState.myPreviewSettings;
    }

    public void setExtensionsEnabled(String projectBasePath, boolean extensionsEnabled) {
        this.extensionsEnabled.put(projectBasePath, extensionsEnabled);
        ApplicationManager.getApplication().getMessageBus().syncPublisher(SettingsChangedListener.TOPIC).onSettingsChange(this);
    }

    public Boolean getExtensionsEnabled(String projectBasePath) {
        return this.extensionsEnabled.get(projectBasePath);
    }

    public void setExtensionsPresent(String projectBasePath, boolean extensionsPresent) {
        if (!Boolean.valueOf(extensionsPresent).equals(this.extensionsPresent.get(projectBasePath))) {
            this.extensionsPresent.put(projectBasePath, extensionsPresent);
            EditorNotifications.updateAll();
        }
    }

    public Boolean getExtensionsPresent(String projectBasePath) {
        return this.extensionsPresent.get(projectBasePath);
    }

    public static class State {
        @Property(surroundWithTag = false)
        @NotNull
        private ReviewPreviewSettings myPreviewSettings = ReviewPreviewSettings.DEFAULT;
    }

    public interface SettingsChangedListener {
        Topic<SettingsChangedListener> TOPIC = Topic.create("ReviewApplicationSettingsChanged", SettingsChangedListener.class);

        void onSettingsChange(@NotNull ReviewApplicationSettings settings);
    }
}
