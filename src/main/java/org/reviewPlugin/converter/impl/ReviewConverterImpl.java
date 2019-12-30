package org.reviewPlugin.converter.impl;

import org.reviewPlugin.converter.constants.Options;
import org.reviewPlugin.converter.constants.OptionsBuilder;
import org.reviewPlugin.converter.ReviewConverter;
import org.reviewPlugin.converter.ast.Document;
import org.reviewPlugin.converter.ast.DocumentHeader;
import org.reviewPlugin.log.LogHandler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

public class ReviewConverterImpl implements ReviewConverter {
    @Override
    public String convert(String content, Map<String, Object> options) {
        String htmlContent = content.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>");
        return htmlContent;
    }

    @Override
    public <T> T convert(String content, Map<String, Object> options, Class<T> expectedResult) {
        return null;
    }

    @Override
    public String convert(String content, Options options) {
        String htmlContent = content.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>");
        return htmlContent;
    }

    @Override
    public <T> T convert(String content, Options options, Class<T> expectedResult) {
        return null;
    }

    @Override
    public String convert(String content, OptionsBuilder options) {
        return content;
    }

    @Override
    public <T> T convert(String content, OptionsBuilder options, Class<T> expectedResult) {
        return null;
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, Map<String, Object> options) throws IOException {

    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, Options options) throws IOException {

    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, OptionsBuilder options) throws IOException {

    }

    @Override
    public String convertFile(File file, Map<String, Object> options) {
        return null;
    }

    @Override
    public <T> T convertFile(File file, Map<String, Object> options, Class<T> expectedResult) {
        return null;
    }

    @Override
    public String convertFile(File file, Options options) {
        return null;
    }

    @Override
    public <T> T convertFile(File file, Options options, Class<T> expectedResult) {
        return null;
    }

    @Override
    public String convertFile(File file, OptionsBuilder options) {
        return null;
    }

    @Override
    public <T> T convertFile(File file, OptionsBuilder options, Class<T> expectedResult) {
        return null;
    }

    @Override
    public String[] convertDirectory(Iterable<File> directoryWalker, Map<String, Object> options) {
        return new String[0];
    }

    @Override
    public String[] convertDirectory(Iterable<File> directoryWalker, Options options) {
        return new String[0];
    }

    @Override
    public String[] convertDirectory(Iterable<File> directoryWalker, OptionsBuilder options) {
        return new String[0];
    }

    @Override
    public String[] convertFiles(Collection<File> files, Map<String, Object> options) {
        return new String[0];
    }

    @Override
    public String[] convertFiles(Collection<File> reviewConverterFiles, Options options) {
        return new String[0];
    }

    @Override
    public String[] convertFiles(Collection<File> files, OptionsBuilder options) {
        return new String[0];
    }

    @Override
    public void requireLibrary(String... requiredLibraries) {

    }

    @Override
    public void requireLibraries(Collection<String> requiredLibraries) {

    }

    @Override
    public DocumentHeader readDocumentHeader(File file) {
        return null;
    }

    @Override
    public DocumentHeader readDocumentHeader(String content) {
        return null;
    }

    @Override
    public DocumentHeader readDocumentHeader(Reader contentReader) {
        return null;
    }

    @Override
    public void unregisterAllExtensions() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String reviewConverterVersion() {
        return null;
    }

    @Override
    public Document load(String content, Map<String, Object> options) {
        return null;
    }

    @Override
    public Document loadFile(File file, Map<String, Object> options) {
        return null;
    }

    @Override
    public void registerLogHandler(LogHandler logHandler) {

    }

    @Override
    public void unregisterLogHandler(LogHandler logHandler) {

    }
}
