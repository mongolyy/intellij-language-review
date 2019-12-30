package org.reviewPlugin.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ReviewPreviewConfigurable implements SearchableConfigurable {
    @Nullable
    private ReviewPreviewSettingsForm myForm = null;
    @NotNull
    private ReviewApplicationSettings myReviewApplicationSettings;

    public ReviewPreviewConfigurable(@NotNull ReviewApplicationSettings reviewApplicationSettings) {
        myReviewApplicationSettings = reviewApplicationSettings;
    }

    @NotNull
    @Override
    public String getId() {
        return "Settings.Review.Preview";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Re:VIEW";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return getForm().getComponent();
    }

    @Override
    public boolean isModified() {
        return !getForm().getReviewPreviewSettings().equals(myReviewApplicationSettings.getReviewPreviewSettings());
    }

    @Override
    public void apply() throws ConfigurationException {
        myReviewApplicationSettings.setReviewPreviewSettings(getForm().getReviewPreviewSettings());
    }

    @Override
    public void reset() {
        getForm().setReviewPreviewSettings(myReviewApplicationSettings.getReviewPreviewSettings());
    }

    @Override
    public void disposeUIResources() {
        myForm = null;
    }

    @NotNull
    public ReviewPreviewSettingsForm getForm() {
        if (myForm == null) {
            myForm = new ReviewPreviewSettingsForm();
        }
        return myForm;
    }
}
