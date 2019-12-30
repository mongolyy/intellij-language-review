package org.reviewPlugin.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.reviewPlugin.ui.SplitTextEditorProvider;

public class ReviewSplitEditorProvider extends SplitTextEditorProvider {
    public ReviewSplitEditorProvider() {
        super(new PsiAwareTextEditorProvider(), new ReviewPreviewEditorProvider());
    }

    @Override
    protected FileEditor createSplitEditor(@NotNull final FileEditor firstEditor, @NotNull FileEditor secondEditor) {
        if (!(firstEditor instanceof TextEditor) || !(secondEditor instanceof ReviewPreviewEditor)) {
            throw new IllegalArgumentException("Main editor should be TextEditor");
        }
        ReviewPreviewEditor reviewPreviewEditor = (ReviewPreviewEditor) secondEditor;
        reviewPreviewEditor.setEditor(((TextEditor) firstEditor).getEditor());
        return new ReviewSplitEditor(((TextEditor) firstEditor), ((ReviewPreviewEditor) secondEditor));
    }

    @Override
    public void disposeEditor(@NotNull FileEditor fileEditor) {
        // default -- needed for IntelliJ IDEA 15 compatibility
        Disposer.dispose(fileEditor);
    }
}
