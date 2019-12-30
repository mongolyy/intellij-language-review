// This is a generated file. Not intended for manual editing.
package org.reviewPlugin.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.reviewPlugin.psi.*;
import org.reviewPlugin.psi.ReviewProperty;
import org.reviewPlugin.psi.ReviewVisitor;

public class ReviewPropertyImpl extends ASTWrapperPsiElement implements ReviewProperty {

  public ReviewPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ReviewVisitor visitor) {
    visitor.visitProperty(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ReviewVisitor) accept((ReviewVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public String getKey() {
    return ReviewPsiImplUtil.getKey(this);
  }

  @Override
  public String getValue() {
    return ReviewPsiImplUtil.getValue(this);
  }

}
