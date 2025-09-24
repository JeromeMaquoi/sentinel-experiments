package be.unamur.snail.spoon.constructor_instrumentation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class StackTraceHelperTest {
    private final StackTraceProvider mockProvider = Mockito.mock(StackTraceProvider.class);

    @AfterEach
    void tearDown() {
        System.clearProperty("packagePrefix");
    }

    @Test
    void shouldThrowWhenPrefixNotSetTest() {
        StackTraceHelper helper = new StackTraceHelper(mockProvider);
        assertThrows(IllegalArgumentException.class, helper::getFilteredStackTrace);
    }

    @Test
    void shouldThrowWhenPrefixIsEmptyTest() {
        System.setProperty("packagePrefix", "");
        StackTraceHelper helper = new StackTraceHelper(mockProvider);
        assertThrows(IllegalArgumentException.class, helper::getFilteredStackTrace);
    }

    @Test
    void shouldFilterUsingSystemPropertyPrefixTest() {
        System.setProperty("packagePrefix", "com.example");
        StackTraceElement matching = new StackTraceElement("com.example.Myclass", "testMethod", "testClass.java", 1);
        StackTraceElement nonMatching = new StackTraceElement("com.other.OtherClass", "testMethod", "testClass.java", 1);

        when(mockProvider.getStackTrace()).thenReturn(new StackTraceElement[]{matching, nonMatching});

        StackTraceHelper helper = new StackTraceHelper(mockProvider);
        List<StackTraceElement> result = helper.getFilteredStackTrace();

        assertEquals(1, result.size());
        assertEquals(matching, result.get(0));
    }
}