package org.reviewPlugin.log;

public class LogRecord {
    private final Severity severity;

    private final String message;

    private String sourceFileName;

    private String sourceMethodName;

    public LogRecord(Severity severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    /**
     * @return Severity level of the current record.
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * @return Descriptive message about the event.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return The value <code>&lt;script&gt;</code>. For the source filename use obtained with the method.
     */
    public String getSourceFileName() {
        return sourceFileName;
    }

    /**
     * @return The Asciidoctor Ruby engine method used to convert the file; <code>convertFile</code> or <code>convert</code> whether you are converting a File or a String.
     */
    public String getSourceMethodName() {
        return sourceMethodName;
    }
}
