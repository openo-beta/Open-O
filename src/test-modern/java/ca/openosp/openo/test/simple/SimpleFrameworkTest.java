package ca.openosp.openo.test.simple;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify JUnit 5 framework is working
 */
@DisplayName("Simple Framework Test")
public class SimpleFrameworkTest {

    @Test
    @DisplayName("Test that JUnit 5 is working")
    public void testFrameworkWorks() {
        assertTrue(true, "Basic assertion should work");
        assertEquals(2, 1 + 1, "Math should work");
    }

    @Test
    @DisplayName("Test string operations")
    public void testStringOperations() {
        String hello = "Hello";
        String world = "World";
        String combined = hello + " " + world;
        
        assertEquals("Hello World", combined);
        assertNotNull(combined);
        assertTrue(combined.contains("Hello"));
        assertTrue(combined.contains("World"));
    }

    @Test
    @DisplayName("Test exception handling")
    public void testExceptions() {
        assertThrows(ArithmeticException.class, () -> {
            int result = 10 / 0;
        });
    }
}