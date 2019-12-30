package org.reviewPlugin.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.Alarm;
import com.intellij.util.FileContentUtil;
import com.intellij.util.messages.MessageBusConnection;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reviewPlugin.Review;
import org.reviewPlugin.settings.ReviewApplicationSettings;
import org.reviewPlugin.settings.ReviewPreviewSettings;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ReviewPreviewEditor extends UserDataHolderBase implements FileEditor {
    public static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Re:VIEW", NotificationDisplayType.NONE, true);

    private Logger log = Logger.getInstance(ReviewPreviewEditor.class);

    /**
     * single threaded with one task queue (one for each editor window).
     */
    private final LazyApplicationPoolExecutor lazyExecutor = new LazyApplicationPoolExecutor();

    /**
     * Indicates whether the HTML preview is obsolete and should regenerated from the Review {@link #document}.
     */
    private transient String currentContent = null;

    private transient int targetLineNo = 0;
    private transient int currentLineNo = 0;

    /**
     * The {@link Document} previewed in this editor.
     */
    private final Document document;
    private Project project;

    /**
     * The directory which holds the temporary images.
     */
    private final Path tempImagesPath;

    @NotNull
    private final JPanel myHtmlPanelWrapper;

    @NotNull
    private volatile ReviewHtmlPanel myPanel;

    @NotNull
    private final Alarm mySwingAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

    /**
     * .
     */
    private FutureTask<Review> review = new FutureTask<>(new Callable<Review>() {
        @Override
        public Review call() {
            File fileBaseDir = new File("");
            VirtualFile file = FileDocumentManager.getInstance().getFile(document);
            String name = "unkown";
            if (file != null) {
                name = file.getName();
                VirtualFile parent = file.getParent();
                if (parent != null && parent.getCanonicalPath() != null) {
                    // parent will be null if we use Language Injection and Fragment Editor
                    fileBaseDir = new File(parent.getCanonicalPath());
                }
            }
            return new Review(project.getBasePath(), fileBaseDir, tempImagesPath, name);
        }
    });

    private void render() {
        final String config = Review.config(document, project);
        final String content = document.getText();
        List<String> extensions = Review.getExtensions(project);

        lazyExecutor.execute(() -> {
            try {
                if (!(config + content).equals(currentContent)) {
                    currentContent = config + content;
                    String markup = review.get().render(content, config, extensions);
                    if (markup != null) {
                        myPanel.setHtml(markup);
                    }
                }
                if (currentLineNo != targetLineNo) {
                    currentLineNo = targetLineNo;
                    myPanel.scrollToLine(targetLineNo, document.getLineCount());
                }
                ApplicationManager.getApplication().invokeLater(myHtmlPanelWrapper::repaint);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                String message = "Error rendering preview: " + ex.getMessage();
                log.error(message, ex);
                Notification notification = NOTIFICATION_GROUP.createNotification("Error rendering Reviewtor", message,
                        NotificationType.ERROR, null);
                // increase event log counter
                notification.setImportant(true);
                Notifications.Bus.notify(notification);
            }
        });
    }

    void renderIfVisible() {
        if (getComponent().isVisible()) {
            render();
        }
    }

    @Nullable("Null means leave current panel")
    private ReviewHtmlPanelProvider retrievePanelProvider(@NotNull ReviewApplicationSettings settings) {
        final ReviewHtmlPanelProvider.ProviderInfo providerInfo = settings.getReviewPreviewSettings().getHtmlPanelProviderInfo();

        ReviewHtmlPanelProvider provider = ReviewHtmlPanelProvider.createFromInfo(providerInfo);

        if (provider.isAvailable() != ReviewHtmlPanelProvider.AvailabilityInfo.AVAILABLE) {
            settings.setReviewPreviewSettings(new ReviewPreviewSettings(settings.getReviewPreviewSettings().getSplitEditorLayout(),
                    ReviewPreviewSettings.DEFAULT.getHtmlPanelProviderInfo(), settings.getReviewPreviewSettings().getPreviewTheme(),
                    settings.getReviewPreviewSettings().getAttributes(), settings.getReviewPreviewSettings().isVerticalSplit(),
                    settings.getReviewPreviewSettings().isEditorFirst(), settings.getReviewPreviewSettings().isEnabledInjections(),
                    settings.getReviewPreviewSettings().getLanguageForPassthrough(),
                    settings.getReviewPreviewSettings().getDisabledInjectionsByLanguage(),
                    settings.getReviewPreviewSettings().isShowReviewWarningsAndErrorsInEditor(),
                    settings.getReviewPreviewSettings().isInplacePreviewRefresh(),
                    settings.getReviewPreviewSettings().isKrokiEnabled(),
                    settings.getReviewPreviewSettings().getKrokiUrl()));

      /* the following will not work, IntellIJ will show the error "parent must be showing" when this is
         tiggered during startup. */
      /*
      Messages.showMessageDialog(
          myHtmlPanelWrapper,
          "Tried to use preview panel provider (" + providerInfo.getName() + "), but it is unavailable. Reverting to default.",
          CommonBundle.getErrorTitle(),
          Messages.getErrorIcon()
      );
      */

            provider = ReviewHtmlPanelProvider.getProviders()[0];
        }

        return provider;
    }

    public ReviewPreviewEditor(final Document document, Project project) {

        this.document = document;
        this.project = project;

        this.tempImagesPath = Review.tempImagesPath();

        myHtmlPanelWrapper = new JPanel(new BorderLayout());

        final ReviewApplicationSettings settings = ReviewApplicationSettings.getInstance();

        myPanel = detachOldPanelAndCreateAndAttachNewOne(document, tempImagesPath, myHtmlPanelWrapper, null, retrievePanelProvider(settings));

        MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        ReviewApplicationSettings.SettingsChangedListener settingsChangedListener = new MyUpdatePanelOnSettingsChangedListener();
        settingsConnection.subscribe(ReviewApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);

        MyEditorColorsListener editorColorsListener = new MyEditorColorsListener();
        settingsConnection.subscribe(EditorColorsManager.TOPIC, editorColorsListener);

        // Get Review asynchronously
        new Thread(() -> review.run()).start();

        // Listen to the document modifications.
        this.document.addDocumentListener(new DocumentAdapter() {
            @Override
            public void documentChanged(DocumentEvent e) {
                renderIfVisible();
            }
        }, this);
    }

    @Contract("_, _, _, null, null -> fail")
    @NotNull
    private static ReviewHtmlPanel detachOldPanelAndCreateAndAttachNewOne(Document document, Path imagesDir, @NotNull JPanel panelWrapper,
                                                                            @Nullable ReviewHtmlPanel oldPanel,
                                                                            @Nullable ReviewHtmlPanelProvider newPanelProvider) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (oldPanel == null && newPanelProvider == null) {
            throw new IllegalArgumentException("Either create new one or leave the old");
        }
        if (newPanelProvider == null) {
            return oldPanel;
        }
        if (oldPanel != null) {
            panelWrapper.remove(oldPanel.getComponent());
            Disposer.dispose(oldPanel);
        }

        final ReviewHtmlPanel newPanel = newPanelProvider.createHtmlPanel(document, imagesDir);
        if (oldPanel != null) {
            newPanel.setEditor(oldPanel.getEditor());
        }
        panelWrapper.add(newPanel.getComponent(), BorderLayout.CENTER);

        return newPanel;
    }

    /**
     * Get the {@link java.awt.Component} to display as this editor's UI.
     */
    @Override
    @NotNull
    public JComponent getComponent() {
        return myHtmlPanelWrapper;
    }

    /**
     * Get the component to be focused when the editor is opened.
     */
    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return myHtmlPanelWrapper;
    }

    /**
     * Get the editor displayable name.
     *
     * @return <code>Review</code>
     */
    @Override
    @NotNull
    @NonNls
    public String getName() {
        return "Preview";
    }

    /**
     * Get the state of the editor.
     * <p/>
     * Just returns {@link FileEditorState#INSTANCE} as {@link ReviewPreviewEditor} is stateless.
     *
     * @param level the level.
     * @return {@link FileEditorState#INSTANCE}
     * @see #setState(com.intellij.openapi.fileEditor.FileEditorState)
     */
    @Override
    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return FileEditorState.INSTANCE;
    }

    /**
     * Set the state of the editor.
     * <p/>
     * Does not do anything as {@link ReviewPreviewEditor} is stateless.
     *
     * @param state the new state.
     * @see #getState(com.intellij.openapi.fileEditor.FileEditorStateLevel)
     */
    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    /**
     * Indicates whether the document content is modified compared to its file.
     *
     * @return {@code false} as {@link ReviewPreviewEditor} is read-only.
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * Indicates whether the editor is valid.
     *
     * @return {@code true} if {@link #document} content is readable.
     */
    @Override
    public boolean isValid() {
        return document.getText() != null;
    }

    /**
     * Invoked when the editor is selected.
     * <p/>
     * Refresh view on select (as dependent elements might have changed).
     */
    @Override
    public void selectNotify() {
        myHtmlPanelWrapper.repaint();
        ApplicationManager.getApplication().invokeLater(() -> {
            ApplicationManager.getApplication().runWriteAction(() -> {
                // project might be already closed (yes, this really happens when you work in multiple projects opened in separate windows)
                if (!project.isDisposed()) {
                    currentContent = null; // force a refresh of the preview by resetting the current memorized content
                    reprocessAnnotations();
                    // save the content in all other editors as their content might be referenced in preview
                    ApplicationManager.getApplication().saveAll();
                    renderIfVisible();
                }
            });
        }, ModalityState.NON_MODAL);
    }

    private void reprocessAnnotations() {
        PsiDocumentManager pm = PsiDocumentManager.getInstance(project);
        if (pm != null) {
            PsiFile psiFile = pm.getPsiFile(document);
            if (psiFile != null) {
                DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
            }
        }
    }

    /**
     * Invoked when the editor is deselected (it does not mean that it is not visible).
     * <p/>
     * Does nothing.
     */
    @Override
    public void deselectNotify() {
    }

    /**
     * Add specified listener.
     * <p/>
     * Does nothing.
     *
     * @param listener the listener.
     */
    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    /**
     * Remove specified listener.
     * <p/>
     * Does nothing.
     *
     * @param listener the listener.
     */
    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    /**
     * Get the background editor highlighter.
     *
     * @return {@code null} as {@link ReviewPreviewEditor} does not require highlighting.
     */
    @Override
    @Nullable
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    /**
     * Get the current location.
     *
     * @return {@code null} as {@link ReviewPreviewEditor} is not navigable.
     */
    @Override
    @Nullable
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    /**
     * Get the structure view builder.
     *
     * @return TODO {@code null} as parsing/PSI is not implemented.
     */
    @Override
    @Nullable
    public StructureViewBuilder getStructureViewBuilder() {
        return null;
    }

    /**
     * Dispose the editor.
     */
    @Override
    public void dispose() {
        Disposer.dispose(this);
        if (tempImagesPath != null) {
            try {
                FileUtils.deleteDirectory(tempImagesPath.toFile());
            } catch (IOException _ex) {
                Logger.getInstance(ReviewPreviewEditor.class).warn("could not remove temp folder", _ex);
            }
        }
    }

    void scrollToLine(int line) {
        targetLineNo = line;
        renderIfVisible();
    }

    private class MyUpdatePanelOnSettingsChangedListener implements ReviewApplicationSettings.SettingsChangedListener {
        @Override
        public void onSettingsChange(@NotNull ReviewApplicationSettings settings) {
            reprocessAnnotations();

            // trigger re-parsing of content as language injection might have changed
            // TODO - doesn't work reliably yet when switching back-and-forth
            VirtualFile file = FileDocumentManager.getInstance().getFile(document);
            if (file != null) {
                FileContentUtil.reparseFiles(file);
            }

            final ReviewHtmlPanelProvider newPanelProvider = retrievePanelProvider(settings);
            mySwingAlarm.addRequest(() -> {
                synchronized (this) {
                    myPanel = detachOldPanelAndCreateAndAttachNewOne(document, tempImagesPath, myHtmlPanelWrapper, myPanel, newPanelProvider);
                }
                currentContent = null; // force a refresh of the preview by resetting the current memorized content
                renderIfVisible();
            }, 0, ModalityState.stateForComponent(getComponent()));
        }
    }

    public Editor getEditor() {
        return myPanel.getEditor();
    }

    public void setEditor(Editor editor) {
        myPanel.setEditor(editor);
    }

    private class MyEditorColorsListener implements EditorColorsListener {
        @Override
        public void globalSchemeChange(@Nullable EditorColorsScheme scheme) {
            final ReviewApplicationSettings settings = ReviewApplicationSettings.getInstance();
            // reset contents in preview with latest CSS headers
            if (settings.getReviewPreviewSettings().getPreviewTheme() == ReviewHtmlPanel.PreviewTheme.INTELLIJ) {
                currentContent = null;
                myPanel.setHtml("");
                renderIfVisible();
            }
        }
    }
}
