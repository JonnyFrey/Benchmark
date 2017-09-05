package benchmark.ga.tetris.neat.network;

import org.jenetics.internal.math.random;
import org.jenetics.util.RandomRegistry;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Jonny on 9/3/17.
 */
public class NeuronTracker {

    private AtomicLong masterId = new AtomicLong(0);

    public Neuron createNewInstance() {
        return new Neuron(this.masterId.getAndIncrement());
    }

    public Neuron createNewBlankInstance() {
        return new Neuron(this.masterId.getAndIncrement(), 0);
    }
}
