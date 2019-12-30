package org.reviewPlugin.psi;

import com.intellij.psi.tree.IElementType;
import org.reviewPlugin.ReviewLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ReviewTokenType extends IElementType {
    public ReviewTokenType(@NotNull @NonNls String debugName) {
        super(debugName, ReviewLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "ReviewTokenType." + super.toString();
    }
}
