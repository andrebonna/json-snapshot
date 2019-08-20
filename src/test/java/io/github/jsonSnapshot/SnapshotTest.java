package io.github.jsonSnapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SnapshotTest {

  private static final SnapshotConfig DEFAULT_CONFIG = new DefaultConfig();
  private static final String FILE_PATH = "src/test/java/anyFilePath";
  private static final String SNAPSHOT_NAME = "java.lang.String.toString=";
  private static final String SNAPSHOT = "java.lang.String.toString=[\n  \"anyObject\"\n]";

  private SnapshotFile snapshotFile;

  private Snapshot snapshot;

  @BeforeEach
  void setUp() throws NoSuchMethodException, IOException {
    snapshotFile = new SnapshotFile(DEFAULT_CONFIG.getFilePath(), "anyFilePath");
    snapshot =
        new Snapshot(
            new DefaultConfig(),
            snapshotFile,
            String.class,
            String.class.getDeclaredMethod("toString"),
            null,
            SnapshotMatcher.defaultJsonFunction(),
            "anyObject");
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.delete(Paths.get(FILE_PATH));
  }

  @Test
  void shouldGetSnapshotNameSuccessfully() {
    String snapshotName = snapshot.getSnapshotName();
    assertThat(snapshotName).isEqualTo(SNAPSHOT_NAME);
  }

  @Test
  void shouldMatchSnapshotSuccessfully() {
    snapshot.toMatchSnapshot();
    assertThat(snapshotFile.getRawSnapshots())
        .isEqualTo(Stream.of(SNAPSHOT).collect(Collectors.toCollection(TreeSet::new)));
  }

  @Test
  void shouldMatchSnapshotWithException() {
    snapshotFile.push(SNAPSHOT_NAME + "anyWrongSnapshot");

    assertThrows(SnapshotMatchException.class, snapshot::toMatchSnapshot);
  }

  @SneakyThrows
  @Test
  void shouldRenderScenarioNameWhenSupplied() {
    Snapshot snapshotWithScenario =
        new Snapshot(
            new DefaultConfig(),
            snapshotFile,
            String.class,
            String.class.getDeclaredMethod("toString"),
            "hello world",
            SnapshotMatcher.defaultJsonFunction(),
            "anyObject");
    assertThat(snapshotWithScenario.getSnapshotName())
        .isEqualTo("java.lang.String.toString[hello world]=");
  }

  @SneakyThrows
  @Test
  void shouldNotRenderScenarioNameWhenNull() {
    Snapshot snapshotWithoutScenario =
        new Snapshot(
            new DefaultConfig(),
            snapshotFile,
            String.class,
            String.class.getDeclaredMethod("toString"),
            null,
            SnapshotMatcher.defaultJsonFunction(),
            "anyObject");
    assertThat(snapshotWithoutScenario.getSnapshotName()).isEqualTo("java.lang.String.toString=");
  }

  @SneakyThrows
  @Test
  void shouldOverwriteSnapshotsWhenParamIsPassed() {
    SnapshotConfig mockConfig = Mockito.mock(SnapshotConfig.class);
    Mockito.when(mockConfig.shouldUpdateSnapshot()).thenReturn(true);
    SnapshotFile snapshotFile = Mockito.mock(SnapshotFile.class);
    Set<String> set = new HashSet<>();
    set.add("java.lang.String.toString[hello world]=[{" + "\"a\": \"b\"" + "}]");
    Mockito.when(snapshotFile.getRawSnapshots()).thenReturn(set);

    Snapshot snapshot =
        new Snapshot(
            mockConfig,
            snapshotFile,
            String.class,
            String.class.getDeclaredMethod("toString"),
            "hello world",
            SnapshotMatcher.defaultJsonFunction(),
            "anyObject");
    snapshot.toMatchSnapshot();
    Mockito.verify(snapshotFile)
        .push("java.lang.String.toString[hello world]=[\n" + "  \"anyObject\"\n" + "]");
  }
}
