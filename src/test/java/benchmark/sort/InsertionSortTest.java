package benchmark.sort;

import org.junit.Test;

/**
 * Test that {@link InsertionSort} is valid
 */
public class InsertionSortTest {
    @Test
    public void testSort() throws Exception {
        SortTest.testSort(new InsertionSort());
    }
}