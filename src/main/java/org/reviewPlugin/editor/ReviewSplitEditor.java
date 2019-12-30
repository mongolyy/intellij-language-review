package org.reviewPlugin.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretAdapter;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.reviewPlugin.ui.SplitFileEditor;

public class ReviewSplitEditor extends SplitFileEditor<TextEditor, ReviewPreviewEditor> implements TextEditor {
    public ReviewSplitEditor(@NotNull TextEditor mainEditor,
                               @NotNull ReviewPreviewEditor secondEditor) {
        super(mainEditor, secondEditor);

        mainEditor.getEditor().getCaretModel().addCaretListener(new MyCaretListener(secondEditor));
    }

    @Override
    protected void adjustEditorsVisibility() {
        super.adjustEditorsVisibility();
        getSecondEditor().renderIfVisible();
    }

    @NotNull
    @Override
    public String getName() {
        return "AsciiDoc split editor";
    }

    @NotNull
    @Override
    public Editor getEditor() {
        return getMainEditor().getEditor();
    }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return getMainEditor().canNavigateTo(navigatable);
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {
        getMainEditor().navigateTo(navigatable);
    }

    private static class MyCaretListener extends CaretAdapter {
        @NotNull
        private final ReviewPreviewEditor myPreviewFileEditor;

        MyCaretListener(@NotNull ReviewPreviewEditor previewFileEditor) {
            myPreviewFileEditor = previewFileEditor;
        }

        @Override
        public void caretPositionChanged(CaretEvent e) {
            final Editor editor = e.getEditor();
            if (editor.getCaretModel().getCaretCount() != 1) {
                return;
            }

            myPreviewFileEditor.scrollToLine(e.getNewPosition().line);
        }
    }
}
