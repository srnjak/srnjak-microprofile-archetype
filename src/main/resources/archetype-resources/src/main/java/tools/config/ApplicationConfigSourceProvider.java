package ${package}.tools.config;

import io.smallrye.config.source.yaml.YamlConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Config provider for aplication configurations in yaml format.
 */
public class ApplicationConfigSourceProvider implements ConfigSourceProvider {

    /**
     * Default ordinal value
     */
    private static final Integer DEFAULT_ORDINAL = 150;

    /**
     * Retrieves a config source from a yaml file.
     *
     * @param file  The yaml file
     * @param ordinal   Default ordinal value
     * @return The config source
     */
    private static Optional<ConfigSource> getConfigSource(
            File file, int ordinal) {

        return Optional.of(file)
                .filter(File::exists)
                .filter(File::canRead)
                .map(f -> {
                    try (InputStream is = new FileInputStream(f)) {
                        return new YamlConfigSource(
                                file.getAbsolutePath(), is, ordinal);
                    } catch (IOException e) {
                        return null;
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<ConfigSource> getConfigSources(
            ClassLoader classLoader) {

        var configPath = System.getProperty("config-path");

        var files = Optional.ofNullable(configPath)
                .map(File::new)
                .filter(File::exists)
                .filter(File::isDirectory)
                .map(File::listFiles)
                .map(Arrays::asList)
                .orElse(List.of());

        var counter = new AtomicInteger(DEFAULT_ORDINAL);

        return files.stream()
                .filter(File::isFile)
                .filter(file -> file.getName().endsWith("yml")
                        || file.getName().endsWith("yaml"))
                .sorted()
                .map(f -> getConfigSource(f, counter.addAndGet(10)))
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableList());
    }
}
