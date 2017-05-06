package benchmark.sort;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test that {@link SelectionSort} is valid
 */
public class SelectionSortTest {
    @Test
    public void sort() throws Exception {
        SortTest.testSort(new SelectionSort());
    }

}