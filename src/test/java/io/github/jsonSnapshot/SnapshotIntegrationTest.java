package io.github.jsonSnapshot;

import static io.github.jsonSnapshot.SnapshotMatcher.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SnapshotIntegrationTest {

  @BeforeAll
  static void beforeAll() {
    start();
  }

  @AfterAll
  static void afterAll() {
    validateSnapshots();
  }

  @Test
  void shouldMatchSnapshotOne() {
    expect(FakeObject.builder().id("anyId1").value(1).name("anyName1").build()).toMatchSnapshot();
  }

  @Test
  void shouldMatchSnapshotTwo() {
    expect(FakeObject.builder().id("anyId2").value(2).name("anyName2").build()).toMatchSnapshot();
  }

  @Test
  void shouldMatchSnapshotThree() {
    expect(FakeObject.builder().id("anyId3").value(3).name("anyName3").build()).toMatchSnapshot();
  }

  @Test
  void shouldMatchSnapshotFour() {
    expect(FakeObject.builder().id("anyId4").value(4).name("any\n\n\nName4").build())
        .toMatchSnapshot();
  }

  @Test
  void shouldMatchSnapshotInsidePrivateMethod() {
    matchInsidePrivate();
  }

  private void matchInsidePrivate() {
    expect(FakeObject.builder().id("anyPrivate").value(5).name("anyPrivate").build())
        .toMatchSnapshot();
  }

  @Test
  void shouldThrowSnapshotMatchException() {
    assertThrows(
        SnapshotMatchException.class,
        expect(FakeObject.builder().id("anyId5").value(6).name("anyName5").build())
            ::toMatchSnapshot,
        "Error on: \n"
            + "io.github.jsonSnapshot.SnapshotIntegrationTest.shouldThrowSnapshotMatchException=[");
  }

  @Test
  void shouldThrowStackOverflowError() {
    // Create cycle JSON
    FakeObject fakeObject1 = FakeObject.builder().id("anyId1").value(1).name("anyName1").build();
    FakeObject fakeObject2 = FakeObject.builder().id("anyId2").value(2).name("anyName2").build();
    fakeObject1.setFakeObject(fakeObject2);
    fakeObject2.setFakeObject(fakeObject1);

    assertThrows(SnapshotMatchException.class, () -> expect(fakeObject1).toMatchSnapshot());
  }

    @Test
    public void shouldMatchSnapshotAndIgnoreTopLevelFieldMethod() {
        expect(FakeObject.builder().id("anyId6").value(6).name("anyName6").build())
                .ignoring("value")
                .toMatchSnapshot();
    }

    @Test
    public void shouldMatchSnapshotAndIgnoreSeveralNestedFieldsMethod() {
        FakeObject grandChild = FakeObject.builder().id("grandChild7").value(7).build();
        FakeObject child = FakeObject.builder().id("child7").value(7).fakeObject(grandChild).build();
        expect(FakeObject.builder().id("anyId7").value(7).name("anyName7").fakeObject(child).build())
                .ignoring("value", "fakeObject.id", "fakeObject.fakeObject")
                .toMatchSnapshot();
    }

    @Test
    public void shouldMatchSnapshotAndIgnoreEverythingMethod() {
        FakeObject child = FakeObject.builder().id("child8").value(8).build();
        expect(FakeObject.builder().id("anyId8").value(8).name("anyName8").fakeObject(child).build())
                .ignoring("$..*")
                .toMatchSnapshot();
    }
}
