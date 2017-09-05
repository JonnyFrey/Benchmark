package benchmark.ga.tetris.neat.network;

import benchmark.ga.tetris.TetrisGrid;
import benchmark.ga.tetris.TetrisLoader;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.concurrent.Future;
import lombok.Value;
import org.jenetics.util.RandomRegistry;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

/**
 * Created by Jonny on 9/3/17.
 */
@Value
public class Population {

    private final List<Tuple2<Long, Network>> population;
    private final int size;

    private final NeuronTracker neuronTracker;
    private final Set<Neuron> inputNeurons;
    private final Map<Neuron, Consumer<TetrisGrid>> actionMapper;
    private final NeatStatistics statistics;

    public Population(
            final int size,
            final NeuronTracker neuronTracker,
            final Set<Neuron> inputNeurons,
            final Map<Neuron, Consumer<TetrisGrid>> actionMapper,
            final NeatStatistics statistics) {
        this.size = size;
        this.neuronTracker = neuronTracker;
        this.inputNeurons = inputNeurons;
        this.actionMapper = actionMapper;

        this.population = Stream.range(0, size)
                .map(i -> new Network(this.neuronTracker, this.inputNeurons, this.actionMapper))
                .map(Network::mutateNewConnection)
                .map(network -> Tuple.of(0L, network))
                .toList();
        this.statistics = statistics;
    }

    private Population(
            final List<Tuple2<Long, Network>> population,
            final int size,
            final NeuronTracker neuronTracker,
            final Set<Neuron> inputNeurons,
            final Map<Neuron, Consumer<TetrisGrid>> actionMapper,
            final NeatStatistics statistics) {
        this.population = population;
        this.size = size;
        this.neuronTracker = neuronTracker;
        this.inputNeurons = inputNeurons;
        this.actionMapper = actionMapper;
        this.statistics = statistics;
    }

    public Population evaluate() {
        final TetrisLoader loader = TetrisLoader.getInstance();
        final List<Tuple2<Long, Network>> newPopulation = this.population.map(Tuple2::_2)
                .map((Network network) -> this.simulate(network, this.statistics.getTimeLimit(), loader));
        final NeatStatistics newBlank = this.statistics.newBlank();
        newPopulation.map(Tuple2::_1).forEach(newBlank);
        return new Population(
                newPopulation,
                this.size,
                this.neuronTracker,
                this.inputNeurons,
                this.actionMapper,
                this.statistics.combine(newBlank)
        );
    }

    public Population evaluateParallel() {
        final TetrisLoader loader = TetrisLoader.getInstance();
        final List<Tuple2<Long, Network>> newPopulation = this.population.map(Tuple2::_2)
                .map((Network network) -> Future.of(ForkJoinPool.commonPool(), () -> this.simulate(network, this.statistics.getTimeLimit(), loader)))
                .map(Future::get);
        final NeatStatistics newBlank = this.statistics.newBlank();
        newPopulation.map(Tuple2::_1).forEach(newBlank);
        return new Population(
                newPopulation,
                this.size,
                this.neuronTracker,
                this.inputNeurons,
                this.actionMapper,
                this.statistics.combine(newBlank)
        );
    }

    private Tuple2<Long, Network> simulate(final Network network, final LocalTime timeLimit, final TetrisLoader loader) {
        final long seed = RandomRegistry.getRandom().nextLong();
        final TetrisGrid grid = new TetrisGrid(loader, new Random(seed));
        while (!grid.isGameOver() && Duration.between(timeLimit, LocalTime.now()).isNegative()) {
            network.reactToGrid(grid);
            grid.step();
        }
        return Tuple.of(grid.getScore(), network.withSeed(seed));
    }

    public Tuple2<Long, Network> getBestNetwork() {
        return this.population.sorted(Comparator.reverseOrder()).get();
    }

    public Population killAndBreed() {
        int keep = (int) (this.size * 0.15);
        int birth = this.size - keep;
        int numOfShuffles = -Math.floorDiv(-birth, keep) * 2;
        final List<Network> survivors = this.population.filter(pair -> this.statistics.scoreWithinBounds(pair._1))
                .sorted(Comparator.reverseOrder())
                .map(Tuple2::_2)
                .take(keep);
        final Stream<Network> babies = Stream.range(0, numOfShuffles)
                .map(i -> survivors.shuffle())
                .flatMap(List::iterator)
                .zipWithIndex()
                .partition(pair -> pair._2 % 2 == 0)
                .map((mom, dad) -> Tuple.of(mom.map(Tuple2::_1).take(birth), dad.map(Tuple2::_1).take(birth)))
                .apply(Stream::zip)
                .map(couple -> couple._1.mate(couple._2))
                .map(Network::mutate);
        if (birth != babies.size()) {
            System.out.println("Size " + this.size);
            System.out.println("Keep " + keep);
            System.out.println("Birth " + birth);
            System.out.println("Nums " + numOfShuffles);
            System.out.println("Actual Size " + babies.size());
            throw new IllegalStateException("Jon you've done your math wrong");
        }
        final List<Tuple2<Long, Network>> newPopulation = survivors
                .appendAll(babies)
                .map(network -> Tuple.of(0L, network));
        if (newPopulation.size() != this.size) {
            throw new IllegalStateException("Still Jon you've done your math wrong");
        }
        return new Population(
                newPopulation,
                this.size,
                this.neuronTracker,
                this.inputNeurons,
                this.actionMapper,
                this.statistics
        );
    }
}
