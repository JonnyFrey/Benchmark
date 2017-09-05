package benchmark.ga.tetris.neat.network;

import lombok.Value;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jenetics.internal.math.random;
import org.jenetics.util.RandomRegistry;

/**
 * Created by Jonny on 9/3/17.
 */
@Value
public class Connection {

    private long inputNeuron;
    private long outputNeuron;
    private float weight;
    private boolean enabled;

    public Connection(final long inputNeuron, final long outputNeuron) {
        this(inputNeuron, outputNeuron, true);

    }

    public Connection(final long inputNeuron, final long outputNeuron, final boolean enabled) {
        this(inputNeuron, outputNeuron, random.nextFloat(RandomRegistry.getRandom(), -5, 5), enabled);
    }

    public Connection(final long inputNeuron, final long outputNeuron, final float weight, final boolean enabled) {
        this.inputNeuron = inputNeuron;
        this.outputNeuron = outputNeuron;
        this.weight = weight;
        this.enabled = enabled;
    }

    public Connection enable(final boolean enabled) {
        return new Connection(this.inputNeuron, this.outputNeuron, this.weight, enabled);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Connection that = (Connection) o;

        return new EqualsBuilder()
                .append(Math.min(this.inputNeuron, this.outputNeuron), Math.min(that.inputNeuron, that.outputNeuron))
                .append(Math.max(this.inputNeuron, this.outputNeuron), Math.max(that.inputNeuron, that.outputNeuron))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(Math.min(this.inputNeuron, this.outputNeuron))
                .append(Math.max(this.inputNeuron, this.outputNeuron))
                .toHashCode();
    }

    public static void main(String[] args) {
        Connection connection = new Connection(1, 2, 1, true);
        Connection connection1 = new Connection(1, 2, 2, false);
        System.out.println(connection.equals(connection1));
        System.out.println(String.format("%s %s", connection.hashCode(), connection1.hashCode()));
    }
}
