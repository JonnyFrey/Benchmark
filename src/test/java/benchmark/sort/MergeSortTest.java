package benchmark.sort;

import org.junit.Test;

/**
 * Test that {@link MergeSort} is valid
 */
public class MergeSortTest {
    @Test
    public void testSort() throws Exception {
        SortTest.testSort(new MergeSort());
    }

}