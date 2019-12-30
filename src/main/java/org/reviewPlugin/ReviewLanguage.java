package org.reviewPlugin;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class ReviewLanguage extends Language {
    public static final ReviewLanguage INSTANCE = new ReviewLanguage();

    public static final String LANGUAGE_NAME = "Re:VIEW";

    private ReviewLanguage() {
        super("Re:VIEW");
    }

    public static boolean isReviewFile(@NotNull Project project, @NotNull VirtualFile file) {
        if (file.isDirectory() || !file.exists()) {
            return false;
        }
        // when a project is already disposed due to a slow initialization, reject this file
        if (project.isDisposed()) {
            return false;
        }
        final FileViewProvider provider = PsiManager.getInstance(project).findViewProvider(file);
        return provider != null && provider.getBaseLanguage().isKindOf(INSTANCE);
    }
}
