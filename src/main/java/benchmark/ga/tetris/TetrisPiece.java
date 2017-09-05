package benchmark.ga.tetris;

import java.util.Arrays;

/**
 * Created by Jonny on 8/31/17.
 */
public class TetrisPiece {
    private final boolean[][][] piece;
    private final int type;
    private int position;

    private final int totalRow;
    private final int totalCol;

    public TetrisPiece(final boolean[][][] piece, final int type) {
        this.piece = piece;
        this.type = type;
        this.position = 0;
        this.totalRow = Arrays.stream(this.piece)
                .mapToInt(value -> value.length)
                .max()
                .orElseThrow(IllegalStateException::new);
        this.totalCol = Arrays.stream(this.piece)
                .flatMap(Arrays::stream)
                .mapToInt(value -> value.length)
                .max()
                .orElseThrow(IllegalStateException::new);
    }

    public TetrisPiece(final TetrisPiece originalPiece) {
        this(originalPiece.piece, originalPiece.type    );
    }

    public int getType() {
        return this.type;
    }

    public boolean[][] getPiece() {
        return this.piece[this.position];
    }

    public void rotate() {
        this.position++;
        if (this.position >= this.piece.length) {
            this.position = 0;
        }
    }

    public boolean[][] getNextRotation() {
        final int pos = this.position == this.piece.length - 1 ? 0 : this.position + 1;
        return this.piece[pos];
    }

    public int getTotalRow() {
        return totalRow;
    }

    public int getTotalCol() {
        return totalCol;
    }
}
