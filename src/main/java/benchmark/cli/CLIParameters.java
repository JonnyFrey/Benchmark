package benchmark.cli;

import com.beust.jcommander.Parameter;
import lombok.Getter;

/**
 * The object that represents and holds values from the CLI
 */
@Getter
public class CLIParameters {

    /**
     * The number of runs the programs initially starts with.
     */
    @Parameter(names = {"--run", "-r"}, validateWith = PositiveNumberValidator.class)
    private int numOfRuns = 20;

}
