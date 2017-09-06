package benchmark.ga.tetris.neat.network;

import lombok.Value;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * @author Jonny
 * @since 9/5/17
 */
@Value
public class Pixel {

    private int x;
    private int y;
    private int color;
    private boolean full;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final Pixel pixel = (Pixel) o;

        return new EqualsBuilder()
                .append(this.x, pixel.x)
                .append(this.y, pixel.y)
                .append(this.color, pixel.color)
                .append(this.full, pixel.full)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(this.x)
                .append(this.y)
                .append(this.color)
                .append(this.full)
                .toHashCode();
    }
}
