package org.reviewPlugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.reviewPlugin.ReviewFileType;
import org.reviewPlugin.ReviewLanguage;

public abstract class ReviewAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent event) {
        PsiFile file = event.getData(LangDataKeys.PSI_FILE);
        boolean enabled = false;
        if (file != null) {
            if (file.getLanguage() == ReviewLanguage.INSTANCE || file.getName().endsWith(ReviewFileType.DOT_DEFAULT_EXTENSION)) {
                enabled = true;
            }
        }
        event.getPresentation().setEnabledAndVisible(enabled);
    }
}
