package benchmark.sort;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * All of the sorting algorithms that the application supports.
 */
@Getter
@AllArgsConstructor
public enum Sorts {
    SELECTION_SORT(new SelectionSort()),
    INSERTION_SORT(new InsertionSort()),
    MERGE_SORT(new MergeSort()),
    QUICK_SORT(new QuickSort()),
    JAVA_ARRAYS_SORT(Arrays::sort);

    /**
     * Sorting algorithm the enum is associated with.
     */
    private final Sort sortingAlgorithm;


    /**
     * Returns the names of all of the sorting algorithms.
     *
     * @return names of all the sorting algorithms.
     */
    public static String[] getSortsName() {
        return Arrays.stream(values()).map(Sorts::name).collect(Collectors.toList()).toArray(new String[0]);
    }



}
