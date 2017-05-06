package benchmark.sort;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test that {@link MergeSort} is valid
 */
public class MergeSortTest {
    @Test
    public void testSort() throws Exception {
        SortTest.testSort(new MergeSort());
    }

}