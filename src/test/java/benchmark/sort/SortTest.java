package benchmark.sort;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;

import java.util.Arrays;

import static benchmark.gui.RunActionListener.fillArrayWithRandomNumbers;
import static java.lang.String.format;

/**
 * A general test to test all sorting algorithms.
 */
@Log4j2
class SortTest {

    static void testSort(Sort sort) {
        int[] array = new int[10];
        for (int i = 0; i < 3; i++) {
            fillArrayWithRandomNumbers(array);
            int[] copy = Arrays.copyOf(array, array.length);
            log.info(format("Testing %s", sort.getClass().getSimpleName()));
            Arrays.sort(copy);
            log.info("Initial array = " + Arrays.toString(array));
            log.info("Sorted array = " + Arrays.toString(copy));
            sort.sort(array);
            log.info("Implemented Sorted array = " + Arrays.toString(array));
            Assert.assertArrayEquals("Sorting algorithm doesn't sort appropriately", copy, array);
        }
    }

}
