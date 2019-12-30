package org.reviewPlugin;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ReviewFileType extends LanguageFileType {
    public static final ReviewFileType INSTANCE = new ReviewFileType();

    public static final String[] DEFAULT_ASSOCIATED_EXTENSIONS = {"re"};

    private ReviewFileType() {
        super(ReviewLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Re:VIEW file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Re:VIEW language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "Re:VIEW";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return ReviewIcons.FILE;
    }
}
