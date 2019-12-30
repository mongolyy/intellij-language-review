package org.reviewPlugin;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reviewPlugin.psi.ReviewBlockId;
import org.reviewPlugin.psi.ReviewFile;
import org.reviewPlugin.psi.ReviewProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReviewUtil {
    public static VirtualFile findAntoraPartials(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
        VirtualFile dir = fileBaseDir;
        while (dir != null) {
            if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
                    dir.getParent().getParent().findChild("antora.yml") != null) {
                VirtualFile antoraPartials = dir.findChild("partials");
                if (antoraPartials != null) {
                    return antoraPartials;
                }
                VirtualFile antoraPages = dir.findChild("pages");
                if (antoraPages != null) {
                    VirtualFile antoraPagePartials = antoraPages.findChild("_partials");
                    if (antoraPagePartials != null) {
                        return antoraPagePartials;
                    }
                }
            }
            if (projectBasePath.equals(dir)) {
                break;
            }
            dir = dir.getParent();
        }
        return null;
    }

    public static String findAntoraImagesDirRelative(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
        VirtualFile dir = fileBaseDir;
        String imagesDir = "";
        while (dir != null) {
            if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
                    dir.getParent().getParent().findChild("antora.yml") != null) {
                VirtualFile assets = dir.findChild("assets");
                if (assets != null) {
                    VirtualFile images = assets.findChild("images");
                    if (images != null) {
                        return imagesDir + "assets/images";
                    }
                }
            }
            if (projectBasePath.equals(dir)) {
                break;
            }
            dir = dir.getParent();
            imagesDir = "../" + imagesDir;
        }
        return null;
    }

    public static String findAntoraAttachmentsDirRelative(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
        VirtualFile dir = fileBaseDir;
        String attachmentsDir = "";
        while (dir != null) {
            if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
                    dir.getParent().getParent().findChild("antora.yml") != null) {
                VirtualFile assets = dir.findChild("assets");
                if (assets != null) {
                    VirtualFile attachments = assets.findChild("attachments");
                    if (attachments != null) {
                        return attachmentsDir + "assets/attachments";
                    }
                }
            }
            if (projectBasePath.equals(dir)) {
                break;
            }
            dir = dir.getParent();
            attachmentsDir = "../" + attachmentsDir;
        }
        return null;
    }

    public static VirtualFile findAntoraExamplesDir(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
        VirtualFile dir = fileBaseDir;
        while (dir != null) {
            if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
                    dir.getParent().getParent().findChild("antora.yml") != null) {
                VirtualFile examples = dir.findChild("examples");
                if (examples != null) {
                    return examples;
                }
            }
            if (projectBasePath.equals(dir)) {
                break;
            }
            dir = dir.getParent();
        }
        return null;
    }

    public static VirtualFile findSpringRestDocSnippets(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
        VirtualFile dir = fileBaseDir;
        while (dir != null) {
            VirtualFile pom = dir.findChild("pom.xml");
            if (pom != null) {
                VirtualFile targetDir = dir.findChild("target");
                if (targetDir != null) {
                    VirtualFile snippets = targetDir.findChild("generated-snippets");
                    if (snippets != null) {
                        return snippets;
                    }
                }
            }
            VirtualFile buildGradle = dir.findChild("build.gradle");
            if (buildGradle != null) {
                VirtualFile buildDir = dir.findChild("build");
                if (buildDir != null) {
                    VirtualFile snippets = buildDir.findChild("generated-snippets");
                    if (snippets != null) {
                        return snippets;
                    }
                }
            }
            VirtualFile buildGradleKts = dir.findChild("build.gradle.kts");
            if (buildGradleKts != null) {
                VirtualFile buildDir = dir.findChild("build");
                if (buildDir != null) {
                    VirtualFile snippets = buildDir.findChild("generated-snippets");
                    if (snippets != null) {
                        return snippets;
                    }
                }
            }
            if (projectBasePath.equals(dir)) {
                break;
            }
            dir = dir.getParent();
        }

        return null;
    }
}
