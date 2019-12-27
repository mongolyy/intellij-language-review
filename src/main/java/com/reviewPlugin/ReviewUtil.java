package com.reviewPlugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.reviewPlugin.psi.ReviewFile;
import com.reviewPlugin.psi.ReviewProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReviewUtil {
    public static List<ReviewProperty> findProperties(Project project, String key) {
        List<ReviewProperty> result = null;
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(ReviewFileType.INSTANCE, GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            ReviewFile reviewFile = (ReviewFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (reviewFile != null) {
                ReviewProperty[] properties = PsiTreeUtil.getChildrenOfType(reviewFile, ReviewProperty.class);
                if (properties != null) {
                    for (ReviewProperty property : properties) {
                        if (key.equals(property.getKey())) {
                            if (result == null) {
                                result = new ArrayList<ReviewProperty>();
                            }
                            result.add(property);
                        }
                    }
                }
            }
        }
        return result != null ? result : Collections.<ReviewProperty>emptyList();
    }

    public static List<ReviewProperty> findProperties(Project project) {
        List<ReviewProperty> result = new ArrayList<ReviewProperty>();
        Collection<VirtualFile> virtualFiles =
                FileTypeIndex.getFiles(ReviewFileType.INSTANCE, GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            ReviewFile reviewFile = (ReviewFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (reviewFile != null) {
                ReviewProperty[] properties = PsiTreeUtil.getChildrenOfType(reviewFile, ReviewProperty.class);
                if (properties != null) {
                    Collections.addAll(result, properties);
                }
            }
        }
        return result;
    }
}
