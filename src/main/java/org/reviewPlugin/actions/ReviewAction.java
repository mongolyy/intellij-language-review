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
            if (file.getLanguage() == ReviewLanguage.INSTANCE) {
                enabled = true;
            } else {
                for (String ext : ReviewFileType.DEFAULT_ASSOCIATED_EXTENSIONS) {
                    if (file.getName().endsWith("." + ext)) {
                        enabled = true;
                        break;
                    }
                }
            }
        }
        event.getPresentation().setEnabledAndVisible(enabled);
    }
}
