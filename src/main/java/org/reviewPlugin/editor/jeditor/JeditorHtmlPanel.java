package org.reviewPlugin.editor.jeditor;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.reviewPlugin.editor.ReviewHtmlPanel;
import org.reviewPlugin.editor.ReviewPreviewEditor;
import org.reviewPlugin.settings.ReviewApplicationSettings;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import static org.reviewPlugin.util.UIUtil.loadStyleSheet;

final class JeditorHtmlPanel extends ReviewHtmlPanel {
    private Logger log = Logger.getInstance(JeditorHtmlPanel.class);

    @NotNull
    private final JEditorPane jEditorPane;
    @NotNull
    private final JBScrollPane scrollPane;
    @NotNull
    private String myLastRenderedHtml = "";

    JeditorHtmlPanel(Document document) {
        jEditorPane = new JEditorPane();
        scrollPane = new JBScrollPane(jEditorPane);
        // Setup the editor pane for rendering HTML.
        File baseDir = new File("");
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file != null) {
            VirtualFile parent = file.getParent();
            if (parent != null && parent.getCanonicalPath() != null) {
                // parent will be null if we use Language Injection and Fragment Editor
                baseDir = new File(parent.getCanonicalPath());
            }
        }
        final HTMLEditorKit kit = new ReviewEditorKit(baseDir);

        // Create an Review style, based on the default stylesheet supplied by UiUtil.getHTMLEditorKit()
        // since it contains fix for incorrect styling of tooltips
        final String cssFile = isDarcula() ? "darcula.css" : "preview.css";
        final StyleSheet customStyle = loadStyleSheet(JeditorHtmlPanel.class.getResource(cssFile));
        final StyleSheet style = UIUtil.getHTMLEditorKit().getStyleSheet();
        style.addStyleSheet(customStyle);
        kit.setStyleSheet(style);

        jEditorPane.setEditorKit(kit);
        jEditorPane.setEditable(false);
        // use this to prevent scrolling to the end of the pane on setText()
        ((DefaultCaret) jEditorPane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    }

    private boolean isDarcula() {
        final ReviewApplicationSettings settings = ReviewApplicationSettings.getInstance();
        switch (settings.getReviewPreviewSettings().getPreviewTheme()) {
            case INTELLIJ:
                return UIUtil.isUnderDarcula();
            case LIGHT:
                return false;
            case DARCULA:
                return true;
            default:
                return false;
        }
    }

    @Override
    @NotNull
    public JComponent getComponent() {
        return scrollPane;
    }

    @Override
    public void setHtml(@NotNull String html) {
        myLastRenderedHtml = html;
        EditorKit kit = jEditorPane.getEditorKit();
        javax.swing.text.Document doc = kit.createDefaultDocument();
        try {
            kit.read(new StringReader(html), doc, 0);
        } catch (IOException ex) {
            String message = "Error setting HTML: " + ex.getMessage();
            log.error(message, ex);
            Notification notification = ReviewPreviewEditor.NOTIFICATION_GROUP
                    .createNotification("Error rendering reviewConverter", message, NotificationType.ERROR, null);
            // increase event log counter
            notification.setImportant(true);
            Notifications.Bus.notify(notification);
        } catch (BadLocationException e) {
            log.error("bad location", e);
        }

        updatePreviewOnEDT(doc);
    }

    private void updatePreviewOnEDT(final javax.swing.text.Document doc) {
        /**
         * call jEditorPane.setDocument in the EDT to avoid flicker
         *
         * @see http://en.wikipedia.org/wiki/Event_dispatching_thread)
         */
        UIUtil.invokeAndWaitIfNeeded((Runnable) () -> {
            jEditorPane.setDocument(doc);
            Rectangle d = jEditorPane.getVisibleRect();
            jEditorPane.setSize((int) d.getWidth(), (int) jEditorPane.getSize().getHeight());
        });
    }

    @Override
    public void render() {
        setHtml(myLastRenderedHtml);
    }

    @Override
    public void scrollToLine(int line, int lineCount) {
        // NOOP
    }

    @Override
    public void dispose() {

    }
}
