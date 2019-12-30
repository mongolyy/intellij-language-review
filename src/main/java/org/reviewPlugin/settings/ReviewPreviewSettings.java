package org.reviewPlugin.settings;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reviewPlugin.editor.ReviewHtmlPanel;
import org.reviewPlugin.editor.ReviewHtmlPanelProvider;
import org.reviewPlugin.editor.jeditor.JeditorHtmlPanelProvider;
import org.reviewPlugin.ui.SplitFileEditor;

import java.util.*;

public class ReviewPreviewSettings {
    public static final ReviewPreviewSettings DEFAULT = new ReviewPreviewSettings();

    @Attribute("DefaultSplitLayout")
    @Nullable // can be returned as null when upgrading from an old release
    private SplitFileEditor.SplitEditorLayout mySplitEditorLayout = SplitFileEditor.SplitEditorLayout.SPLIT;

    @Tag("HtmlPanelProviderInfo")
    @Property(surroundWithTag = false)
    @NotNull
    private ReviewHtmlPanelProvider.ProviderInfo myHtmlPanelProviderInfo = JeditorHtmlPanelProvider.INFO;

    {
/*        final ReviewHtmlPanelProvider.AvailabilityInfo availabilityInfo = new JavaFxHtmlPanelProvider().isAvailable();
        if (availabilityInfo == ReviewHtmlPanelProvider.AvailabilityInfo.AVAILABLE) {
            myHtmlPanelProviderInfo = JavaFxHtmlPanelProvider.INFO;
        }*/
    }

    @Attribute("PreviewTheme")
    @Nullable // can be returned as null when upgrading from an old release
    private ReviewHtmlPanel.PreviewTheme myPreviewTheme = ReviewHtmlPanel.PreviewTheme.INTELLIJ;

    @Property(surroundWithTag = false)
    @MapAnnotation(surroundWithTag = false, entryTagName = "attribute")
    @NotNull
    private Map<String, String> attributes = new HashMap<>();

    @Attribute("VerticalSplit")
    private boolean myIsVerticalSplit = true;

    @Attribute("EditorFirst")
    private boolean myIsEditorFirst = true;

    @Attribute("EnableInjections")
    private boolean myEnableInjections = true;

    // can be disabled if it causes problems for a user. Option to disable it will be removed once the feature is stable
    @Attribute("InplacePreviewRefresh")
    private boolean myInplacePreviewRefresh = true;

    @Attribute("DisabledInjectionsByLanguage")
    @Nullable
    private String myDisabledInjectionsByLanguage;

    @Attribute("DefaultLanguageForPassthrough")
    @Nullable
    private String myLanguageForPassthrough = "html";

    @Attribute("ShowReviewWarningsAndErrorsInEditor")
    private boolean myShowReviewWarningsAndErrorsInEditor = true;

    @Attribute("EnabledKroki")
    private boolean myEnableKroki = false;

    @Attribute("KrokiUrl")
    private String myKrokiUrl;

    public ReviewPreviewSettings() {
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public ReviewPreviewSettings(@NotNull SplitFileEditor.SplitEditorLayout splitEditorLayout,
                                   @NotNull ReviewHtmlPanelProvider.ProviderInfo htmlPanelProviderInfo,
                                   @NotNull ReviewHtmlPanel.PreviewTheme previewTheme,
                                   @NotNull Map<String, String> attributes,
                                   boolean verticalSplit, boolean editorFirst,
                                   boolean enableInjections,  @Nullable String languageForPassthrough,
                                   @Nullable String disabledInjectionsByLanguage,
                                   boolean showReviewWarningsAndErrorsInEditor,
                                   boolean inplacePreviewRefresh,
                                   boolean enableKroki,
                                   String krokiUrl) {
        mySplitEditorLayout = splitEditorLayout;
        myHtmlPanelProviderInfo = htmlPanelProviderInfo;
        myPreviewTheme = previewTheme;
        this.attributes = attributes;
        myIsVerticalSplit = verticalSplit;
        myIsEditorFirst = editorFirst;
        myEnableInjections = enableInjections;
        myLanguageForPassthrough = languageForPassthrough;
        myDisabledInjectionsByLanguage = disabledInjectionsByLanguage;
        myShowReviewWarningsAndErrorsInEditor = showReviewWarningsAndErrorsInEditor;
        myInplacePreviewRefresh = inplacePreviewRefresh;
        myEnableKroki = enableKroki;
        myKrokiUrl = krokiUrl;
    }

    @NotNull
    public SplitFileEditor.SplitEditorLayout getSplitEditorLayout() {
        if (mySplitEditorLayout == null) {
            return SplitFileEditor.SplitEditorLayout.SPLIT;
        }
        return mySplitEditorLayout;
    }

    @NotNull
    public ReviewHtmlPanel.PreviewTheme getPreviewTheme() {
        if (myPreviewTheme == null) {
            return ReviewHtmlPanel.PreviewTheme.INTELLIJ;
        }
        return myPreviewTheme;
    }

    @NotNull
    public ReviewHtmlPanelProvider.ProviderInfo getHtmlPanelProviderInfo() {
        return myHtmlPanelProviderInfo;
    }

    @NotNull
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public boolean isVerticalSplit() {
        return myIsVerticalSplit;
    }

    public boolean isEditorFirst() {
        return myIsEditorFirst;
    }

    public boolean isEnabledInjections() {
        return myEnableInjections;
    }

    public boolean isInplacePreviewRefresh() {
        return myInplacePreviewRefresh;
    }

    public String getDisabledInjectionsByLanguage() {
        return myDisabledInjectionsByLanguage;
    }

    public boolean isKrokiEnabled() {
        return myEnableKroki;
    }

    public String getKrokiUrl() {
        return myKrokiUrl;
    }

    public List<String> getDisabledInjectionsByLanguageAsList() {
        List<String> list = new ArrayList<>();
        if (myDisabledInjectionsByLanguage != null) {
            Arrays.asList(myDisabledInjectionsByLanguage.split(";")).forEach(
                    entry -> list.add(entry.trim().toLowerCase(Locale.US))
            );
        }
        return list;
    }

    public boolean isShowReviewWarningsAndErrorsInEditor() {
        return myShowReviewWarningsAndErrorsInEditor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReviewPreviewSettings that = (ReviewPreviewSettings) o;

        if (mySplitEditorLayout != that.mySplitEditorLayout) {
            return false;
        }
        if (!myHtmlPanelProviderInfo.equals(that.myHtmlPanelProviderInfo)) {
            return false;
        }
        if (myPreviewTheme != that.myPreviewTheme) {
            return false;
        }
        if (myIsVerticalSplit != that.myIsVerticalSplit) {
            return false;
        }
        if (myIsEditorFirst != that.myIsEditorFirst) {
            return false;
        }
        if (myEnableInjections != that.myEnableInjections) {
            return false;
        }
        if (!Objects.equals(myLanguageForPassthrough, that.myLanguageForPassthrough)) {
            return false;
        }
        if (!Objects.equals(myDisabledInjectionsByLanguage, that.myDisabledInjectionsByLanguage)) {
            return false;
        }
        if (myShowReviewWarningsAndErrorsInEditor != that.myShowReviewWarningsAndErrorsInEditor) {
            return false;
        }
        if (myEnableKroki != that.myEnableKroki) {
            return false;
        }
        if (!Objects.equals(myKrokiUrl, that.myKrokiUrl)) {
            return false;
        }
        return attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(mySplitEditorLayout);
        result = 31 * result + Objects.hashCode(myHtmlPanelProviderInfo);
        result = 31 * result + Objects.hashCode(myPreviewTheme);
        result = 31 * result + attributes.hashCode();
        result = 31 * result + (myIsVerticalSplit ? 1 : 0);
        result = 31 * result + (myIsEditorFirst ? 1 : 0);
        result = 31 * result + (myEnableInjections ? 1 : 0);
        result = 31 * result + Objects.hashCode(myLanguageForPassthrough);
        result = 31 * result + Objects.hashCode(myDisabledInjectionsByLanguage);
        result = 31 * result + (myShowReviewWarningsAndErrorsInEditor ? 1 : 0);
        result = 31 * result + (myInplacePreviewRefresh ? 1 : 0);
        result = 31 * result + (myEnableKroki ? 1 : 0);
        result = 31 * result + Objects.hashCode(myKrokiUrl);
        return result;
    }

    public String getLanguageForPassthrough() {
        // any old setting of null (not set) will default to 'html'
        return myLanguageForPassthrough == null ? "html" : myLanguageForPassthrough;
    }

    public interface Holder {
        void setReviewPreviewSettings(@NotNull ReviewPreviewSettings settings);

        @NotNull
        ReviewPreviewSettings getReviewPreviewSettings();
    }

}
