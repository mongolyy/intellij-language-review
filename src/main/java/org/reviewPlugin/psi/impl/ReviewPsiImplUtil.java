package org.reviewPlugin.psi.impl;

import com.intellij.lang.ASTNode;
import org.reviewPlugin.psi.ReviewProperty;
import org.reviewPlugin.psi.ReviewTypes;

public class ReviewPsiImplUtil {
    public static String getKey(ReviewProperty element) {
        ASTNode keyNode = element.getNode().findChildByType(ReviewTypes.KEY);
        if (keyNode != null) {
            // IMPORTANT: Convert embedded escaped spaces to simple spaces
            return keyNode.getText().replaceAll("\\\\ ", " ");
        } else {
            return null;
        }
    }

    public static String getValue(ReviewProperty element) {
        ASTNode valueNode = element.getNode().findChildByType(ReviewTypes.VALUE);
        if (valueNode != null) {
            return valueNode.getText();
        } else {
            return null;
        }
    }
}
