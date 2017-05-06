package benchmark;

import benchmark.cli.CLIParameters;
import benchmark.gui.BenchMarkFrame;
import com.beust.jcommander.JCommander;

import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 * Main entry point of the program
 */
public class BenchmarkMain {

    /**
     * Program entry point
     *
     * @param args the arguments from the command line.
     */
    public static void main(final String... args) {
        //Parse out the cli arguments
        final CLIParameters parameters = new CLIParameters();
        final JCommander jCommander = new JCommander(parameters);
        jCommander.parse(args);

        //Create and define the frame.
        BenchMarkFrame frame = new BenchMarkFrame(parameters);
        Dimension dimension = new Dimension(180, 200);
        frame.setPreferredSize(dimension);
        frame.pack();
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        //Tells the frame to put the window in the center of the screen
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
