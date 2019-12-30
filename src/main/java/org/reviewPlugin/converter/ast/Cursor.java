package org.reviewPlugin.converter.ast;

public interface Cursor {

    /**
     * @return The line number where the owning block begins.
     */
    int getLineNumber();

    /**
     * @return Source file simple name, or <code>&lt;stdin&gt;</code> value when converting from a String.
     */
    String getPath();

    /**
     * @return Absolute path to the source file parent directory, or the execution path when converting from a String.
     */
    String getDir();

    /**
     * @return Absolute path to the source file, or <code>null</code> when converting from a String. These will point to the correct source file, even when it is included from another.
     */
    String getFile();

}
