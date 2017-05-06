package benchmark.gui;

import benchmark.cli.CLIParameters;
import benchmark.sort.Sorts;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import static java.awt.Color.YELLOW;

/**
 * The window the user will interact with. This class leverages java's Swing package to display a GUI in which the user
 * will interact with
 */
@Getter
public class BenchMarkFrame extends JFrame {

    /**
     * An input text box that contains the size of the array to run with.
     */
    private final JTextField arraySizeInput;
    /**
     * The display box that will display how any messages like results or errors.
     */
    private final JTextField display;

    /**
     * The Combo box that will store which sorting algorthmn to use
     */
    private final JComboBox<String> chooseSortMethod;

    /**
     * CLI parameters to set inital states of the application if desired.
     */
    private final CLIParameters parameters;

    /**
     * Constructs a GUI for the user to interact with.
     *
     * @param parameters cli parameters to set the inital defaults within the application.
     */
    public BenchMarkFrame(final CLIParameters parameters) {
        super("Benchmark");

        this.parameters = parameters;

        this.arraySizeInput = new JTextField(4);
        this.chooseSortMethod = new JComboBox<>(Sorts.getSortsName());

        this.display = new JTextField("   Ready");
        this.display.setBackground(YELLOW);
        this.display.setEditable(false);

        this.setupFrame();
    }

    /**
     * Takes the the frame and injects the approprate components into it for the users to interact with. This should be
     * called once by the constructor
     */
    private void setupFrame() {
        //This is the contain
        final Container container = this.getContentPane();

        container.setLayout(new GridLayout(6, 1));

        container.add(new JLabel(" Array Size: "));
        container.add(arraySizeInput);
        container.add(chooseSortMethod);

        final JButton run = new JButton("Run");
        ActionListener runActionListener = new RunActionListener(this);
        run.addActionListener(runActionListener);
        container.add(run);

        container.add(this.display);
    }

}
