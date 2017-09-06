package benchmark.ga.tetris.neat.network;

import benchmark.ga.tetris.TetrisGrid;
import benchmark.ga.tetris.TetrisScreen;
import io.vavr.Tuple2;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.function.Consumer;
import java.util.function.Function;

import static benchmark.ga.tetris.TetrisScreen.setupProperties;


/**
 * Created by Jonny on 9/3/17.
 */
public class Neat {

    private final NeuronTracker neuronTracker;
    private final Set<Neuron> inputNeurons;
    private final Map<Neuron, Consumer<TetrisGrid>> actionMapper;

    public Neat() {
        final TetrisGrid grid = new TetrisGrid();
        this.neuronTracker = new NeuronTracker();
        final Iterator<Consumer<TetrisGrid>> possibleActions =
                Iterator.of(
                        TetrisGrid::upButton,
                        TetrisGrid::downButton,
                        TetrisGrid::leftButton,
                        TetrisGrid::rightButton
                );
        this.actionMapper = possibleActions.toMap(
                action -> this.neuronTracker.createNewBlankInstance(),
                Function.identity()
        );

        this.inputNeurons = Stream.range(0, grid.getGridAsIterator().size())
                .map(i -> this.neuronTracker.createNewBlankInstance())
                .toSet();
    }

    public static void main(final String... args) throws SlickException {
        Log.setVerbose(false);
        setupProperties();
        final Neat neat = new Neat();
        final Network network = neat.generateBestNetwork(Integer.MAX_VALUE, 50, Duration.ofMinutes(15));
        final TetrisScreen screen = new TetrisScreen(network);
        System.out.println("Your network is ready!");
        screen.start();
    }

    private Network generateBestNetwork(final int generationLimit, final int populationSize, final Duration timeLimit) {
        //Create initial Population
        final Population initialPopulation = new Population(
                populationSize,
                this.neuronTracker,
                this.inputNeurons.toSortedSet(),
                this.actionMapper,
                new NeatStatistics(generationLimit, LocalTime.now().plus(timeLimit), -1)
        );

        LocalTime start = LocalTime.now();

        Tuple2<Long, Network> currentBest = initialPopulation.getBestNetwork();
        Population currentPopulation = initialPopulation;
        NeatStatistics currentStatistic;
        do {
            final Population evaluated = currentPopulation.evaluateParallel();

            final Tuple2<Long, Network> bestGenerationNetwork = evaluated.getBestNetwork();
            if (bestGenerationNetwork._1 >= currentBest._1) {
                currentBest = bestGenerationNetwork;
            }
            currentPopulation = evaluated.killAndBreed();

            currentStatistic = evaluated.getStatistics();
            if (Duration.between(start, LocalTime.now()).compareTo(Duration.ofSeconds(15)) >= 0) {
                System.out.println(currentStatistic);
                start = LocalTime.now();
            }
        } while (generationLimit > currentStatistic.getGeneration() && !Duration.between(LocalTime.now(), currentStatistic.getTimeLimit()).isNegative());
        System.out.println(currentStatistic);
        return currentBest._2;
    }

}
