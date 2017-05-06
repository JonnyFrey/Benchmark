package benchmark.gui;

import benchmark.sort.Sort;
import benchmark.sort.Sorts;
import com.beust.jcommander.ParameterException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

import static benchmark.cli.PositiveNumberValidator.parsePositiveNumber;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * The code that runs when a component that is using this object has an action called to it. For example, when you push
 * a button a the GUI that has this add to it's Action listener's then the actionPerformed method is called. This runs
 * the current selected sorting algorithm a certain amount of time to get an average of how long it would take to sort a
 * certain sized array of integers.
 */
@AllArgsConstructor
@Log4j2
public class RunActionListener implements ActionListener {

    /**
     * The GUI to read the values from.
     */
    private final BenchMarkFrame frame;

    /**
     * {@inheritDoc}
     * <p>
     * This runs the current selected sorting algorithm a certain amount of time to get an average of how long it would
     * take to sort a certain sized array of integers.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        //Get the array size string from the GUI
        final String inputString = this.frame.getArraySizeInput().getText().trim();
        try {
            //Attempt to parse the int
            final int arraySize = parsePositiveNumber(inputString);

            //Attempt to determine which sorting array to use based off of the GUI.
            final String sortName = this.frame.getChooseSortMethod().getSelectedItem().toString();
            final Sort sortingAlgorithm = checkNotNull(
                    Optional.of(Sorts.valueOf(sortName))
                            .orElseThrow(() -> new IllegalStateException(format("Unknown Sorts enum. Found '%s'", sortName)))
                            .getSortingAlgorithm(),
                    "Sorting Algorithm not defined yet"
            );

            final int[] array = new int[arraySize];
            final int numberOfRuns = this.frame.getParameters().getNumOfRuns();

            //Run a new randomly generated array and build an average time over it.
            double totalTime = 0;
            for (int i = 0; i < numberOfRuns; i++) {
                fillArrayWithRandomNumbers(array);
                final long startTime = System.currentTimeMillis();
                sortingAlgorithm.sort(array);
                totalTime += System.currentTimeMillis() - startTime;
            }
            final double averageTime = totalTime / numberOfRuns;

            this.frame.getDisplay().setText(format(" Average time took %.2f", averageTime));
            this.frame.getArraySizeInput().selectAll();
            this.frame.getArraySizeInput().requestFocus();

            log.info(format("Array size = %d Runs = %d %s avg time: %s", arraySize, numberOfRuns, sortName, averageTime));
        } catch (final ParameterException ex) {
            frame.getDisplay().setText(" Invalid array size.");
            log.error("Failed to run sorting algorithm.", ex);
        } catch (final Exception ex) {
            frame.getDisplay().setText(" Critical error was thrown");
            log.error("Sorting algorithm threw an exception. ", ex);
        }

    }

    /**
     * Fills the array with random numbers
     *
     * @param array the array to be filled.
     */
    public static void fillArrayWithRandomNumbers(final int[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (int) (Math.random() * array.length);
        }
    }


}
