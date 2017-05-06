package benchmark.sort;

import org.junit.Test;

/**
 * Test that {@link QuickSort} is valid
 */
public class QuickSortTest {
    @Test
    public void testSort() throws Exception {
        SortTest.testSort(new QuickSort());
    }

}