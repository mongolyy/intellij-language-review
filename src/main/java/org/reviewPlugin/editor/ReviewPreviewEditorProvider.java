package org.reviewPlugin.editor;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.PossiblyDumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.reviewPlugin.ReviewLanguage;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class ReviewPreviewEditorProvider implements FileEditorProvider, PossiblyDumbAware {
    public static final String EDITOR_TYPE_ID = ReviewLanguage.LANGUAGE_NAME + "PreviewEditor";

    /**
     * Check wether this {@link FileEditorProvider} can create a valid {@link FileEditor} for the file.
     *
     * @param project the project context.
     * @param file    the file to be tested for acceptance. This parameter must never be <code>null</code>.
     * @return whether the provider can create a valid editor for the specified <code>file</code>.
     */
    @Override
    public boolean accept(@NotNull com.intellij.openapi.project.Project project, @NotNull VirtualFile file) {
        return ReviewLanguage.isReviewFile(project, file);
    }

    /**
     * Create a valid editor for the specified file.
     * <p/>
     * Should be called only if the provider has accepted this file.
     *
     * @param project the project context.
     * @param file    the file for which an editor must be created.
     * @return an editor for the specified file.
     * @see #accept(com.intellij.openapi.project.Project, com.intellij.openapi.vfs.VirtualFile)
     */
    @Override
    @NotNull
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new ReviewPreviewEditor(FileDocumentManager.getInstance().getDocument(file), project);
    }

    /**
     * Dispose the specified <code>editor</code>.
     *
     * @param editor editor to be disposed. This parameter must not be <code>null</code>.
     */
    @Override
    public void disposeEditor(@NotNull FileEditor editor) {
        editor.dispose();
    }

    @Override
    @NotNull
    public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file) {
        return FileEditorState.INSTANCE;
    }

    @Override
    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
    }

    @Override
    @NotNull
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override
    @NotNull
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }

    /**
     * Indicates the editor can be created while background indexing is running.
     *
     * @return {@code true}
     */
    @Override
    public boolean isDumbAware() {
        return true;
    }
}
