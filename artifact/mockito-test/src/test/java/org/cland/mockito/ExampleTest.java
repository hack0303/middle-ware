package org.cland.mockito;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class ExampleTest {

    @Test
    public void testMockito() {
        // Create a mock object
        List<String> mockedList = mock(List.class);

        // Use the mock object
        mockedList.add("one");
        mockedList.clear();

        // Verify interactions
        verify(mockedList).add("one");
        verify(mockedList).clear();
    }
}