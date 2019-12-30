package org.reviewPlugin.editor.jeditor;

import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.io.File;
import java.net.MalformedURLException;

public class ReviewEditorKit extends HTMLEditorKit {
    private File baseDir;

    @Override
    public Document createDefaultDocument() {
        HTMLDocument document = (HTMLDocument) super.createDefaultDocument();
        try {
            document.setBase(baseDir.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return document;
    }

    public ReviewEditorKit(File baseDir) {
        this.baseDir = baseDir;
    }
}
