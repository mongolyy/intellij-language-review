// This is a generated file. Not intended for manual editing.
package com.reviewPlugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.reviewPlugin.psi.ReviewTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.reviewPlugin.psi.*;

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
