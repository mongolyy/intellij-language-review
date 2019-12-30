package org.reviewPlugin;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;

public class ReviewLexerAdapter extends FlexAdapter {
    public ReviewLexerAdapter() {
        super(new ReviewLexer((Reader) null));
    }
}
