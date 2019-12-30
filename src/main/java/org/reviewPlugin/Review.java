package org.reviewPlugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reviewPlugin.actions.ReviewAction;
import org.reviewPlugin.converter.*;
import org.reviewPlugin.converter.constants.Attributes;
import org.reviewPlugin.converter.constants.AttributesBuilder;
import org.reviewPlugin.converter.constants.Options;
import org.reviewPlugin.converter.constants.OptionsBuilder;
import org.reviewPlugin.editor.ReviewPreviewEditor;
import org.reviewPlugin.log.*;
import org.reviewPlugin.settings.ReviewApplicationSettings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.reviewPlugin.ReviewUtil.*;

public class Review {
    private static class MaxHashMap extends LinkedHashMap<String, ReviewConverter> {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ReviewConverter> eldest) {
            // cache up to three instances (for example: javafx, pdf, spring-restdocs)
            if (this.size() > 3) {
                eldest.getValue().shutdown();
                return true;
            } else {
                return false;
            }
        }
    }

    private static MaxHashMap instances = new MaxHashMap();

    private com.intellij.openapi.diagnostic.Logger log =
            com.intellij.openapi.diagnostic.Logger.getInstance(Review.class);

    /**
     * Base directory to look up includes.
     */
    private final File fileBaseDir;

    /**
     * Images directory.
     */
    private final Path imagesPath;
    private final String name;
    private final String projectBasePath;

    public Review(String projectBasePath, File fileBaseDir, Path imagesPath, String name) {
        this.projectBasePath = projectBasePath;
        this.fileBaseDir = fileBaseDir;
        this.imagesPath = imagesPath;
        this.name = name;
    }

    private ReviewConverter initWithExtensions(List<String> extensions, String format) {
        synchronized (Review.class) {
            ReviewApplicationSettings reviewApplicationSettings = ReviewApplicationSettings.getInstance();
            if (extensions.size() > 0) {
                reviewApplicationSettings.setExtensionsPresent(projectBasePath, true);
            }
            String md;
            if (Boolean.TRUE.equals(reviewApplicationSettings.getExtensionsEnabled(projectBasePath))) {
                md = calcMd(projectBasePath, extensions);
            } else {
                md = calcMd(projectBasePath, Collections.emptyList());
            }
            ReviewConverter reviewConverter = instances.get(md);
            if (reviewConverter == null) {
                ByteArrayOutputStream boasOut = new ByteArrayOutputStream();
                ByteArrayOutputStream boasErr = new ByteArrayOutputStream();
                LogHandler logHandler = new IntellijLogHandler("initialize");
                String oldEncoding = null;
                try {
                    reviewConverter = ReviewConverter.Factory.create();
                    reviewConverter.registerLogHandler(logHandler);
                    instances.put(md, reviewConverter);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    if (oldEncoding != null) {
                        System.setProperty("file.encoding", oldEncoding);
                    }
                    if (reviewConverter != null) {
                        reviewConverter.unregisterLogHandler(logHandler);
                    }
                    notify(boasOut, boasErr, Collections.emptyList());
                }
            }
            return reviewConverter;
        }
    }

    /**
     * Calculate a hash for the extensions.
     * Hash will change if the project has been changed, of the contents of files have changed.
     * This will also include all files in subdirectories of the extension when creating the hash.
     */
    private String calcMd(String projectBasePath, List<String> extensions) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(projectBasePath.getBytes(StandardCharsets.UTF_8));
            List<Path> folders = new ArrayList<>();
            for (String s : extensions) {
                try {
                    try (InputStream is = new FileInputStream(s)) {
                        md.update(IOUtils.toByteArray(is));
                    }
                    Path parent = FileSystems.getDefault().getPath(s).getParent();
                    if (!folders.contains(parent)) {
                        folders.add(parent);
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent, path -> Files.isDirectory(path))) {
                            for (Path p : stream) {
                                scanForRubyFiles(p, md);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("unable to read file", e);
                }
            }
            byte[] mdbytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("unknown hash", e);
        }
    }

    private void scanForRubyFiles(Path path, MessageDigest md) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) {
                    scanForRubyFiles(p, md);
                }
                if (Files.isRegularFile(p) && Files.isReadable(p)) {
                    try (InputStream is = Files.newInputStream(p)) {
                        md.update(IOUtils.toByteArray(is));
                    }
                }
            }
        }
    }

    private void notify(ByteArrayOutputStream boasOut, ByteArrayOutputStream boasErr, List<LogRecord> logRecords) {
        notify(boasOut, boasErr, logRecords,
                !ReviewApplicationSettings.getInstance().getReviewPreviewSettings().isShowReviewWarningsAndErrorsInEditor());
    }

    public void notifyAlways(ByteArrayOutputStream boasOut, ByteArrayOutputStream boasErr, List<LogRecord> logRecords) {
        notify(boasOut, boasErr, logRecords, true);
    }

    private void notify(ByteArrayOutputStream boasOut, ByteArrayOutputStream boasErr, List<LogRecord> logRecords,
                        boolean logAll) {
        String out = boasOut.toString();
        String err = boasErr.toString();
        if (logAll) {
            // logRecords will not be handled in the org.Review.intellij.annotator.ExternalAnnotator
            for (LogRecord logRecord : logRecords) {
                if (logRecord.getSeverity() == Severity.DEBUG) {
                    continue;
                }
                StringBuilder message = new StringBuilder();
                message.append("Error during rendering ").append(name).append("; ").append(logRecord.getSeverity().name()).append(" ");
                if (logRecord.getCursor() != null && logRecord.getCursor().getFile() != null) {
                    message.append(logRecord.getCursor().getFile()).append(":").append(logRecord.getCursor().getLineNumber());
                }
                message.append(" ").append(logRecord.getMessage());
                Notification notification = ReviewPreviewEditor.NOTIFICATION_GROUP.createNotification("Message during rendering " + name,
                        message.toString(), NotificationType.INFORMATION, null);
                notification.setImportant(true);
                Notifications.Bus.notify(notification);
            }
        }
        if (out.length() > 0) {
            Notification notification = ReviewPreviewEditor.NOTIFICATION_GROUP.createNotification("Message during rendering " + name, out,
                    NotificationType.INFORMATION, null);
            notification.setImportant(false);
            Notifications.Bus.notify(notification);
        }
        if (err.length() > 0) {
            Notification notification = ReviewPreviewEditor.NOTIFICATION_GROUP.createNotification("Error during rendering " + name, err,
                    NotificationType.INFORMATION, null);
            notification.setImportant(true);
            Notifications.Bus.notify(notification);
        }
    }

    @Nullable
    public static Path tempImagesPath() {
        Path tempImagesPath = null;
        try {
            tempImagesPath = Files.createTempDirectory("Review-intellij");
        } catch (IOException _ex) {
            String message = "Can't create temp folder to render images: " + _ex.getMessage();
            Notification notification = ReviewPreviewEditor.NOTIFICATION_GROUP
                    .createNotification("Error rendering Review", message, NotificationType.ERROR, null);
            // increase event log counter
            notification.setImportant(true);
            Notifications.Bus.notify(notification);
        }
        return tempImagesPath;
    }

    @NotNull
    public static String config(Document document, Project project) {
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
        StringBuilder tempContent = new StringBuilder();
        if (currentFile != null) {
            VirtualFile folder = currentFile.getParent();
            if (folder != null) {
                while (true) {
                    for (String configName : new String[]{".Reviewconfig", ".Reviewconfig.adoc"}) {
                        VirtualFile configFile = folder.findChild(configName);
                        if (configFile != null &&
                                !currentFile.equals(configFile)) {
                            Document config = FileDocumentManager.getInstance().getDocument(configFile);
                            if (config != null) {
                                // prepend the new config, followed by two newlines to avoid sticking-together content
                                tempContent.insert(0, "\n\n");
                                tempContent.insert(0, config.getText());
                                // prepend the location of the config file
                                tempContent.insert(0, ":Reviewconfigdir: " + folder.getCanonicalPath() + "\n\n");
                            }
                        }
                    }
                    if (folder.getPath().equals(project.getBasePath())) {
                        break;
                    }
                    folder = folder.getParent();
                    if (folder == null) {
                        break;
                    }
                }
            }
        }
        return tempContent.toString();
    }

    @NotNull
    public static List<String> getExtensions(Project project) {
        VirtualFile lib = project.getBaseDir().findChild(".Review");
        if (lib != null) {
            lib = lib.findChild("lib");
        }

        List<String> extensions = new ArrayList<>();
        if (lib != null) {
            for (VirtualFile vf : lib.getChildren()) {
                if ("rb".equals(vf.getExtension())) {
                    Document extension = FileDocumentManager.getInstance().getDocument(vf);
                    if (extension != null) {
                        extensions.add(vf.getCanonicalPath());
                    }
                }
            }
        }
        return extensions;
    }

    @FunctionalInterface
    public interface Notifier {
        void notify(ByteArrayOutputStream boasOut, ByteArrayOutputStream boasErr, List<LogRecord> logRecords);
    }

    public String render(String text, List<String> extensions) {
        return render(text, "", extensions, this::notify);
    }

    public String render(String text, String config, List<String> extensions) {
        return render(text, config, extensions, this::notify);
    }

    public String render(String text, String config, List<String> extensions, Notifier notifier) {
        return render(text, config, extensions, notifier, "javafx");
    }

    public String render(String text, String config, List<String> extensions, Notifier notifier, String format) {
        synchronized (Review.class) {
            CollectingLogHandler logHandler = new CollectingLogHandler();
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(ReviewAction.class.getClassLoader());
            ByteArrayOutputStream boasOut = new ByteArrayOutputStream();
            ByteArrayOutputStream boasErr = new ByteArrayOutputStream();
            VirtualFile antoraPartials = findAntoraPartials(
                    LocalFileSystem.getInstance().findFileByIoFile(new File(projectBasePath)),
                    LocalFileSystem.getInstance().findFileByIoFile(fileBaseDir)
            );
            String antoraImagesDir = findAntoraImagesDirRelative(
                    LocalFileSystem.getInstance().findFileByIoFile(new File(projectBasePath)),
                    LocalFileSystem.getInstance().findFileByIoFile(fileBaseDir)
            );
            String antoraAttachmentsDir = findAntoraAttachmentsDirRelative(
                    LocalFileSystem.getInstance().findFileByIoFile(new File(projectBasePath)),
                    LocalFileSystem.getInstance().findFileByIoFile(fileBaseDir)
            );
            VirtualFile antoraExamplesDir = findAntoraExamplesDir(
                    LocalFileSystem.getInstance().findFileByIoFile(new File(projectBasePath)),
                    LocalFileSystem.getInstance().findFileByIoFile(fileBaseDir)
            );
            try {
                ReviewConverter reviewConverter = initWithExtensions(extensions, format);
                reviewConverter.registerLogHandler(logHandler);
                // prependConfig.setConfig(config);
                try {
                    return "<div id=\"content\">\n" + reviewConverter.convert(text, getDefaultOptions("html5", antoraPartials, antoraImagesDir, antoraAttachmentsDir, antoraExamplesDir)) + "\n</div>";
                } finally {
                    // prependConfig.setConfig("");
                    reviewConverter.unregisterLogHandler(logHandler);
                }
            } catch (Exception | ServiceConfigurationError ex) {
                log.warn("unable to render Re:VIEW document", ex);
                logHandler.log(new LogRecord(Severity.FATAL, ex.getMessage()));
                StringBuilder response = new StringBuilder();
                response.append("unable to render Re:VIEW document");
                Throwable t = ex;
                do {
                    response.append("<p>").append(t.getClass().getCanonicalName()).append(": ").append(t.getMessage());
                    if (t.getMessage().startsWith("unknown encoding name")) {
                        response.append("<p>Either your local encoding is not supported by JRuby, or you passed an unrecognized value to the Java property 'file.encoding' either in the IntelliJ options file or via the JAVA_TOOL_OPTION environment variable.");
                        // String property = SafePropertyAccessor.getProperty("file.encoding", null);
                        // response.append("<p>encoding passed by system property 'file.encoding': ").append(property);
                        // response.append("<p>available encodings (excuding aliases): ");
                        //EncodingDB.getEncodings().forEach(entry -> response.append(entry.getEncoding().getCharsetName()).append(" "));
                    }
                    t = t.getCause();
                } while (t != null);
                response.append("<p>(the full exception stack trace is available in the IDE's log file. Visit menu item 'Help | Show Log in Explorer' to see the log)");
                return response.toString();
            } finally {
                notifier.notify(boasOut, boasErr, logHandler.getLogRecords());
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }

    public void convertTo(File file, String config, List<String> extensions, FileType format) {

        Notifier notifier = this::notifyAlways;
        synchronized (Review.class) {
            CollectingLogHandler logHandler = new CollectingLogHandler();
            ByteArrayOutputStream boasOut = new ByteArrayOutputStream();
            ByteArrayOutputStream boasErr = new ByteArrayOutputStream();
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(ReviewAction.class.getClassLoader());
            VirtualFile springRestDocsSnippets = findSpringRestDocSnippets(
                    LocalFileSystem.getInstance().findFileByIoFile(new File(projectBasePath)),
                    LocalFileSystem.getInstance().findFileByIoFile(fileBaseDir));
            try {
                ReviewConverter reviewConverter = initWithExtensions(extensions, format.toString());
                // prependConfig.setConfig(config);
                reviewConverter.registerLogHandler(logHandler);
            } catch (Exception | ServiceConfigurationError ex) {
                log.warn("unable to render Review document", ex);
                logHandler.log(new LogRecord(Severity.FATAL, ex.getMessage()));
                StringBuilder response = new StringBuilder();
                response.append("unable to render Re:VIEW document");
                Throwable t = ex;
                do {
                    response.append("<p>").append(t.getClass().getCanonicalName()).append(": ").append(t.getMessage());
                    if (t.getMessage().startsWith("unknown encoding name")) {
                        response.append("<p>Either your local encoding is not supported by JRuby, or you passed an unrecognized value to the Java property 'file.encoding' either in the IntelliJ options file or via the JAVA_TOOL_OPTION environment variable.");
                        // String property = SafePropertyAccessor.getProperty("file.encoding", null);
                        // response.append("<p>encoding passed by system property 'file.encoding': ").append(property);
                        // response.append("<p>available encodings (excuding aliases): ");
                        // EncodingDB.getEncodings().forEach(entry -> response.append(entry.getEncoding().getCharsetName()).append(" "));
                    }
                    t = t.getCause();
                } while (t != null);
                response.append("<p>(the full exception stack trace is available in the IDE's log file. Visit menu item 'Help | Show Log in Explorer' to see the log)");
                try {
                    boasErr.write(response.toString().getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException("Unable to write bytes");
                }
            } finally {
                notifier.notify(boasOut, boasErr, logHandler.getLogRecords());
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }

    public Map<String, Object> getExportOptions(Map<String, Object> options, FileType fileType) {
        if (fileType == FileType.HTML) {
            options.put(Options.HEADER_FOOTER, true);
        }
        return options;
    }

    private Map<String, Object> getDefaultOptions(String backend, VirtualFile antoraPartials, String antoraImagesDir, String antoraAttachmentsDir, VirtualFile antoraExamplesDir) {
        AttributesBuilder builder = AttributesBuilder.attributes()
                .showTitle(true)
                .backend(backend)
                .sourceHighlighter("coderay")
                .attribute("coderay-css", "style")
                .attribute("env", "idea")
                .attribute("env-idea");

        String graphvizDot = System.getenv("GRAPHVIZ_DOT");
        if (graphvizDot != null) {
            builder.attribute("graphvizdot", graphvizDot);
        }

        Attributes attrs = builder.get();

        final ReviewApplicationSettings settings = ReviewApplicationSettings.getInstance();
        if (imagesPath != null) {
/*            if (settings.getReviewPreviewSettings().getHtmlPanelProviderInfo().getClassName().equals(JavaFxHtmlPanelProvider.class.getName())) {
                attrs.setAttribute("outdir", imagesPath.toAbsolutePath().normalize().toString());
            }*/
        }

        settings.getReviewPreviewSettings().getAttributes().forEach(attrs::setAttribute);


        OptionsBuilder opts = OptionsBuilder.options().backend(backend).headerFooter(false)
                .attributes(attrs)
                .option("sourcemap", "true")
                .baseDir(fileBaseDir);

        return opts.asMap();
    }

    public enum FileType {
        PDF("pdf"),
        HTML("html5");

        private final String formatType;

        FileType(String formatType) {
            this.formatType = formatType;
        }

        @Override
        public String toString() {
            return formatType;
        }
    }
}
