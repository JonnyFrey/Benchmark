package benchmark.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

/**
 * A JCommander validator that checks to see if the the value is a valid positive non-zero integer.
 */
public class PositiveNumberValidator implements IParameterValidator {

    /**
     * {@inheritDoc}
     *
     * @throws ParameterException if the value is not a positive non-zero integer.
     */
    @Override
    public void validate(final String name, final String value) throws ParameterException {
        PositiveNumberValidator.parsePositiveNumber(value);
    }

    /**
     * Attempts to parse the String into a positive non-zero integer.
     *
     * @param value string to be parsed into an integer.
     * @return the parsed integer.
     * @throws ParameterException if the value is not a positive non-zero integer.
     */
    public static int parsePositiveNumber(final String value) {
        try {
            final int parsedValue = parseInt(value);
            if (parsedValue <= 0) {
                throw new ParameterException(format(
                        "Value '%s' must be a positive non-zero integer",
                        parsedValue
                ));
            }
            return parsedValue;
        } catch (final Exception e) {
            throw new ParameterException(format("Unable to parse the value:'%s'", value), e);
        }
    }

}
