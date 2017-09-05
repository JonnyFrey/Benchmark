package benchmark.ga.tetris.neat.network;

import benchmark.ga.tetris.TetrisGrid;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import org.jenetics.internal.math.random;
import org.jenetics.util.RandomRegistry;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Jonny on 9/3/17.
 */
public class Network implements Comparable<Network> {

    private final NeuronTracker neuronTracker;
    private final Set<Neuron> inputNeurons;
    private final Set<Neuron> hiddenNeurons;
    private final Set<Neuron> outputNeurons;

    private final Map<Neuron, Consumer<TetrisGrid>> actionMapper;

    private final Set<Connection> connections;

    private final long seed;

    public Network(
            final NeuronTracker neuronTracker,
            final Set<Neuron> inputNeurons,
            final Map<Neuron, Consumer<TetrisGrid>> actionMapper
    ) {
        this(neuronTracker, inputNeurons, HashSet.empty(), actionMapper.keySet(), actionMapper, HashSet.empty(), 0);
    }

    public Network(
            final NeuronTracker neuronTracker,
            final Set<Neuron> inputNeurons,
            final Set<Neuron> hiddenNeurons,
            final Set<Neuron> outputNeurons,
            final Map<Neuron, Consumer<TetrisGrid>> actionMapper,
            final Set<Connection> connections,
            final long seed) {
        this.neuronTracker = neuronTracker;
        this.inputNeurons = inputNeurons.toLinkedSet();
        this.hiddenNeurons = hiddenNeurons;
        this.outputNeurons = outputNeurons;
        this.actionMapper = actionMapper;
        this.connections = connections;
        this.seed = seed;
    }

    public Map<Neuron, Double> evaluate(final Map<Neuron, Double> input) {
        final Map<Long, ? extends Set<Connection>> outputMapping = this.connections.groupBy(Connection::getOutputNeuron);
        final Map<Long, Neuron> neuronMapping = this.inputNeurons
                .addAll(this.hiddenNeurons)
                .addAll(this.outputNeurons)
                .toMap(Neuron::getId, Function.identity());
        final Set<Long> outputIds = this.outputNeurons.map(Neuron::getId);
        final Map<Neuron, Double> finalResult = this.evaluate(
                this.outputNeurons,
                outputMapping,
                neuronMapping,
                input.mapKeys(Neuron::getId)
        ).filter((id, result) -> outputIds.contains(id)).mapKeys(neuronMapping::apply);
        return finalResult;
    }

    public void reactToGrid(final TetrisGrid grid) {
        this.evaluate(
                this.inputNeurons
                        .zipAll(
                                grid.getGridAsIterator(),
                                new Neuron(-1),
                                0d
                        ).toMap(Tuple2::_1, Tuple2::_2)
        ).mapValues(value -> value > 0.5)
                .filter((neuron, apply) -> apply)
                .map(Tuple2::_1)
                .forEach(
                        neuron -> this.actionMapper
                                .get(neuron)
                                .getOrElseThrow(IllegalStateException::new)
                                .accept(grid)
                );
    }

    private Map<Long, Double> evaluate(
            final Set<Neuron> output,
            final Map<Long, ? extends Set<Connection>> outputMapping,
            final Map<Long, Neuron> neurons,
            final Map<Long, Double> input
    ) {
        Map<Long, Double> newInfo = input;
        for (Neuron outputNeuron : output) {
            newInfo = this.evaluate(outputNeuron, outputMapping, neurons, newInfo);
        }
        return newInfo;
    }

    private Map<Long, Double> evaluate(
            final Neuron neuron,
            final Map<Long, ? extends Set<Connection>> outputMapping,
            final Map<Long, Neuron> neurons,
            final Map<Long, Double> input
    ) {
        if (input.containsKey(neuron.getId())) {
            return input;
        }
        double sum = neuron.getBias();
        Map<Long, Double> newInfo = input;
        if (outputMapping.containsKey(neuron.getId())) {
            for (final Connection connection : outputMapping.apply(neuron.getId())) {
                if (connection.isEnabled()) {
                    if (!newInfo.containsKey(connection.getInputNeuron())) {
                        newInfo = this.evaluate(neurons.apply(connection.getInputNeuron()), outputMapping, neurons, newInfo.put(neuron.getId(), 0D));
                    }
                    sum += newInfo.apply(connection.getInputNeuron());
                }
            }
        }
        return newInfo.put(neuron.getId(), 1 / (1 + Math.pow(Math.E, -sum)));
    }

    public Network mutateNeuron() {
        final List<Neuron> allOutputNeurons = this.hiddenNeurons.toList().shuffle();
        if (!allOutputNeurons.isEmpty()) {
            final Neuron oldNeuron = allOutputNeurons.get();
            Set<Neuron> newHiddenNeurons = this.hiddenNeurons.remove(oldNeuron).add(oldNeuron.mutate());
            return new Network(
                    this.neuronTracker,
                    this.inputNeurons,
                    newHiddenNeurons,
                    this.outputNeurons,
                    this.actionMapper,
                    this.connections,
                    this.seed
            );
        }
        return this;
    }

    public Network mutateNewConnection() {
        final Set<Long> inputIds = this.inputNeurons.map(Neuron::getId);
        final Set<Long> hiddenIds = this.hiddenNeurons.map(Neuron::getId);
        final Set<Long> outputIds = this.outputNeurons.map(Neuron::getId);

        final List<Long> possibleInputNeurons = hiddenIds.addAll(inputIds).toList().shuffle();
        final List<Long> possibleOutputNeurons = hiddenIds.addAll(outputIds).toList().shuffle();

        for (final Long input : possibleInputNeurons) {
            for (final Long output : possibleOutputNeurons) {
                if (!input.equals(output)) {
                    final Connection connection = new Connection(input, output);
                    if (!this.connections.contains(connection)) {
                        return new Network(
                                this.neuronTracker,
                                this.inputNeurons,
                                this.hiddenNeurons,
                                this.outputNeurons,
                                this.actionMapper,
                                this.connections.add(connection),
                                this.seed
                        );
                    }
                }
            }
        }
        return this;
    }

    public Network mutateNewNeuron() {
        final List<Connection> shuffledConnections = this.connections.toList().shuffle();
        if (!shuffledConnections.isEmpty()) {
            final Neuron newNeuron = this.neuronTracker.createNewInstance();
            final Connection connectionToBreak = shuffledConnections.get();
            final Connection newInputConnection = new Connection(connectionToBreak.getInputNeuron(), newNeuron.getId());
            final Connection newOutputConnection = new Connection(newNeuron.getId(), connectionToBreak.getOutputNeuron());

            return new Network(
                    this.neuronTracker,
                    this.inputNeurons,
                    this.hiddenNeurons.add(newNeuron),
                    this.outputNeurons,
                    this.actionMapper,
                    this.connections.remove(connectionToBreak)
                            .add(connectionToBreak.enable(false))
                            .add(newInputConnection)
                            .add(newOutputConnection),
                    this.seed
            );
        }
        return this;
    }

    public Network mate(final Network network) {
        final Map<Integer, Connection> thisConMap = this.connections.toMap(Object::hashCode, Function.identity());
        final Map<Integer, Connection> otherConMap = network.connections.toMap(Object::hashCode, Function.identity());
        final Set<Connection> newConnection = thisConMap.merge(
                otherConMap,
                (connection, connection2) -> {
                    final Connection choosenOne = RandomRegistry.getRandom().nextBoolean() ? connection : connection2;
                    if (!choosenOne.isEnabled() && random.nextFloat(RandomRegistry.getRandom(), 0, 1) >= .75) {
                        return choosenOne.enable(true);
                    }
                    return choosenOne;
                }
        ).values().toSet();
        final Set<Neuron> newHiddenNeurons = this.hiddenNeurons.addAll(network.hiddenNeurons);
        return new Network(
                this.neuronTracker,
                this.inputNeurons,
                newHiddenNeurons,
                this.outputNeurons,
                this.actionMapper,
                newConnection,
                this.seed
        );
    }


    public Network mutate() {
        final Random rand = RandomRegistry.getRandom();
        if (random.nextFloat(rand, 0, 1) < 0.1) {
            float mutation = random.nextFloat(rand, 0, 1);
            if (mutation < 0.33) {
                return this.mutateNeuron();
            } else if (mutation < .66) {
                return this.mutateNewNeuron();
            }
            return this.mutateNewConnection();
        }
        return this;
    }

    public Network withSeed(long seed) {
        return new Network(
                this.neuronTracker,
                this.inputNeurons,
                this.hiddenNeurons,
                this.outputNeurons,
                this.actionMapper,
                this.connections,
                seed
        );
    }

    @Override
    public int compareTo(final Network o) {
        return Integer.compare(this.hashCode(), o.hashCode());
    }

    public long getSeed() {
        return seed;
    }
}
