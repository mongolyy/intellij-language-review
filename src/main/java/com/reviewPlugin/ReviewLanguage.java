package com.reviewPlugin;

import com.intellij.lang.Language;

public class ReviewLanguage extends Language {
    public static final ReviewLanguage INSTANCE = new ReviewLanguage();

    private ReviewLanguage() {
        super("Re:VIEW");
    }
}
