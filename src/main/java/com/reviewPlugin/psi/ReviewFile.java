package com.reviewPlugin.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.reviewPlugin.ReviewFileType;
import com.reviewPlugin.ReviewLanguage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ReviewFile extends PsiFileBase {
    public ReviewFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, ReviewLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ReviewFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Review File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }
}
