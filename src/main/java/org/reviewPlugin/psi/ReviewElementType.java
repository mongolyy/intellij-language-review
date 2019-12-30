package org.reviewPlugin.psi;

import com.intellij.psi.tree.IElementType;
import org.reviewPlugin.ReviewLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ReviewElementType extends IElementType {
    public ReviewElementType(@NotNull @NonNls String debugName) {
        super(debugName, ReviewLanguage.INSTANCE);
    }
}
