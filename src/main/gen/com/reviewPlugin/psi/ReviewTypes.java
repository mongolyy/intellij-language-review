// This is a generated file. Not intended for manual editing.
package com.reviewPlugin.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.reviewPlugin.psi.impl.*;

public interface ReviewTypes {

  IElementType PROPERTY = new ReviewElementType("PROPERTY");

  IElementType COMMENT = new ReviewTokenType("COMMENT");
  IElementType CRLF = new ReviewTokenType("CRLF");
  IElementType KEY = new ReviewTokenType("KEY");
  IElementType SEPARATOR = new ReviewTokenType("SEPARATOR");
  IElementType VALUE = new ReviewTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == PROPERTY) {
        return new ReviewPropertyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
