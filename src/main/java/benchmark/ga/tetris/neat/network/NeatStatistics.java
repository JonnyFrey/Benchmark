package benchmark.ga.tetris.neat.network;

import com.google.common.collect.Range;
import lombok.Value;
import org.jenetics.stat.LongMomentStatistics;

import java.time.Duration;
import java.time.LocalTime;
import java.util.function.Consumer;

import static java.lang.Math.sqrt;
import static java.lang.String.format;


/**
 * Created by Jonny on 9/5/17.
 */
@Value
public class NeatStatistics implements Consumer<Long> {

    private static final String cpattern = "| %22s %-51s|\n";
    private static final String spattern = "| %27s %-46s|\n";

    private final int generationLimit;
    private final LocalTime timeLimit;
    private final LongMomentStatistics statistics;
    private final int generation;
    private final double std;
    private final Range<Double> stdRange;
    private final double meanVelocity;

    private final int window;

    public NeatStatistics(final int generationLimit, final LocalTime timeLimit, final int window) {
        this.generationLimit = generationLimit;
        this.timeLimit = timeLimit;
        this.statistics = new LongMomentStatistics();
        this.generation = 1;
        this.std = sqrt(this.statistics.getVariance());
        this.stdRange = Range.closed(statistics.getMean() - (std * 2), statistics.getMean() + (std * 2));
        this.meanVelocity = 0;
        this.window = window;
    }

    private NeatStatistics(
            final int generationLimit,
            final LocalTime timeLimit,
            final LongMomentStatistics statistics,
            final int generation,
            final double meanVelocity,
            final int window
    ) {
        this.generationLimit = generationLimit;
        this.timeLimit = timeLimit;
        this.statistics = statistics;
        this.generation = generation;
        this.std = sqrt(this.statistics.getVariance());
        this.stdRange = Range.closed(statistics.getMean() - (std * 2), statistics.getMean() + (std * 2));
        this.meanVelocity = meanVelocity;
        this.window = window;
    }

    public NeatStatistics newBlank() {
        return new NeatStatistics(
                this.generationLimit,
                this.timeLimit,
                this.window
        );
    }

    public NeatStatistics window() {
        if (this.window < 0 && this.generation % this.window != 0) {
            return this;
        }
        return new NeatStatistics(
                this.generationLimit,
                this.timeLimit,
                new LongMomentStatistics(),
                this.generation,
                0,
                this.window
        );
    }

    @Override
    public String toString() {
        return "+---------------------------------------------------------------------------+\n" +
                "|  Simulation Info                                                          |\n" +
                "+---------------------------------------------------------------------------+\n" +
                format(cpattern, "General Info:      ", "") +
                format(spattern, "Current Generation =", this.generation) +
                format(spattern, "Current Window =", this.generation / this.window) +
                format(spattern, "Window index =", this.generation % this.window) +
                format(spattern, "Current Time       =", formatLocalTime(LocalTime.now())) +
                format(spattern, "Generation Limit   =", this.generationLimit) +
                format(spattern, "Time Limit         =", formatLocalTime(this.timeLimit)) +
                format(cpattern, "Remaining Counters:", "") +
                format(spattern, "Generations =", (this.generationLimit - this.generation)) +
                format(spattern, "Time        =", formatDuration(Duration.between(LocalTime.now(), this.timeLimit))) +
                "+---------------------------------------------------------------------------+\n" +
                "|  Population Statistics                                                    |\n" +
                "+---------------------------------------------------------------------------+\n" +
                format(cpattern, "Fitness:", "") +
                format(spattern, "min       =", d(this.statistics.getMin())) +
                format(spattern, "max       =", d(this.statistics.getMax())) +
                format(spattern, "mean      =", d(this.statistics.getMean())) +
                format(spattern, "var       =", d(this.statistics.getVariance())) +
                format(spattern, "std       =", d(this.std)) +
                format(spattern, "high std  =", d(this.stdRange.lowerEndpoint())) +
                format(spattern, "low std   =", d(this.stdRange.upperEndpoint())) +
                format(spattern, "mean vel  =", d(this.meanVelocity)) +
                "+---------------------------------------------------------------------------+\n";
    }

    public static String formatDuration(final Duration duration) {
        long seconds = Math.abs(duration.getSeconds());
        String positive = String.format(
                "%d Hours: %02d Minutes: %02d Seconds",
                seconds / 3600,
                (seconds % 3600) / 60,
                seconds % 60
        );
        return duration.getSeconds() < 0 ? "-" + positive : positive;
    }

    public static String formatLocalTime(final LocalTime time) {
        StringBuilder buf = new StringBuilder(18);
        int hourValue = time.getHour();
        int minuteValue = time.getMinute();
        int secondValue = time.getSecond();
        buf.append(hourValue < 10 ? "0" : "").append(hourValue)
                .append(minuteValue < 10 ? ":0" : ":").append(minuteValue)
                .append(secondValue < 10 ? ":0" : ":").append(secondValue);
        return buf.toString();
    }

    private static String d(final double value) {
        return format("%2.4f", value);
    }

    @Override
    public void accept(final Long fitnessScore) {
        this.statistics.accept(fitnessScore);
    }

    public NeatStatistics combine(final NeatStatistics statistics) {
        return new NeatStatistics(
                this.generationLimit,
                this.timeLimit,
                this.statistics.combine(statistics.statistics),
                this.generation + statistics.generation,
                statistics.statistics.getMean() - this.statistics.getMean(),
                this.window
        );
    }

    public boolean scoreWithinBounds(final Long score) {
        return this.stdRange.contains(Double.valueOf(score));
    }

}
