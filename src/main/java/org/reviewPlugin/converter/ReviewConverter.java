package org.reviewPlugin.converter;

import org.reviewPlugin.log.LogHandler;

import java.util.*;
import java.util.stream.Collectors;

public interface ReviewConverter extends AutoCloseable {
    /**
     * Parse the AsciiDoc source input into an Document and
     * render it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content the AsciiDoc source as String.
     * @param options a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    String convert(String content, Map<String, Object> options);

    /**
     * This method frees all resources consumed by asciidoctorJ module. Keep in mind that if this method is called, instance becomes unusable and you should create another instance.
     */
    void shutdown();

    /**
     * Factory for creating a new instance of Asciidoctor interface.
     */
    final class Factory {

        private Factory() {
        }

        /**
         * Creates a new instance of Asciidoctor.
         *
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         * Ruby calls.
         */
        public static ReviewConverter create() {
            ServiceLoader<ReviewConverter> reviewConverterImpls = ServiceLoader.load(ReviewConverter.class);
            Iterator<ReviewConverter> iterator = reviewConverterImpls.iterator();
            if (!iterator.hasNext()) {
                reviewConverterImpls = ServiceLoader.load(ReviewConverter.class, Factory.class.getClassLoader());
                iterator = reviewConverterImpls.iterator();
            }
            if (iterator.hasNext()) {
                ReviewConverter impl = iterator.next();
                List<ReviewConverter> remainingImpls = new ArrayList<>();
                while (iterator.hasNext()) {
                    remainingImpls.add(iterator.next());
                }
                if (!remainingImpls.isEmpty()) {
                    remainingImpls.add(0, impl);
                    String remainingImplNames = remainingImpls
                            .stream()
                            .map(reviewConverter -> reviewConverter.getClass().getName())
                            .collect(Collectors.joining(",", "[", "]"));
                    throw new RuntimeException(String.format("Found multiple Re:VIEW implementations in the classpath: %s", remainingImplNames));
                }
                return impl;
            } else {
                throw new RuntimeException("Unable to find an implementation of ReviewConverter in the classpath (using ServiceLoader)");
            }
        }
    }

    void registerLogHandler(LogHandler logHandler);

    void unregisterLogHandler(LogHandler logHandler);

    default <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(getClass())) {
            return clazz.cast(this);
        }
        throw new IllegalArgumentException("Cannot unwrap to " + clazz.getName());
    }

    @Override
    default void close() {
        // no-op
    }
}
