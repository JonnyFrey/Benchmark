package benchmark.ga.tetris.neat.network;

import lombok.Value;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jenetics.internal.math.random;
import org.jenetics.util.RandomRegistry;

import javax.annotation.Nonnull;

/**
 * Created by Jonny on 9/3/17.
 */
@Value
public class Neuron implements Comparable<Neuron> {

    private long id;
    private float bias;

    public Neuron(final long id) {
        this.id = id;
        this.bias = random.nextFloat(RandomRegistry.getRandom(), -5, 5);
    }

    public Neuron(final long id, final float bias) {
        this.id = id;
        this.bias = bias;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Neuron neuron = (Neuron) o;

        return new EqualsBuilder()
                .append(id, neuron.id)
                .isEquals();
    }

    public Neuron mutate() {
        return new Neuron(this.id, random.nextFloat(RandomRegistry.getRandom(), -5, 5));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }

    @Override
    public int compareTo(@Nonnull final Neuron neuron) {
        return Long.compare(this.id, neuron.id);
    }
}
