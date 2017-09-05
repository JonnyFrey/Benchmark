package benchmark.ga.tetris;

import com.google.common.collect.Range;
import io.vavr.collection.Iterator;
import lombok.Getter;
import org.jenetics.internal.math.random;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by Jonny on 8/31/17.
 */
public class TetrisGrid {

    public static int DEFAULT_ROWS = 18;
    public static int DEFAULT_COLS = 10;

    private final int[][] grid;
    private final int maxRows;
    private final int maxCols;
    private final ThreadLocal<Random> internalRandom;
    private final TetrisLoader tetrisLoader;
    private TetrisPiece piece;
    @Getter
    private int row, col;

    private boolean gameOver;
    private long score = 0;

    private long pieces = 0;

    public TetrisGrid(final Random random) {
        this(TetrisLoader.getInstance(), random);
    }

    public TetrisGrid(final TetrisLoader loader, final Random random) {
        this(DEFAULT_ROWS, DEFAULT_COLS, loader, random);
    }

    public TetrisGrid(final int rows, final int cols, final TetrisLoader tetrisLoader, final Random random) {
        this.grid = new int[rows][cols];
        this.maxRows = rows;
        this.maxCols = cols;
        this.gameOver = false;
        this.tetrisLoader = tetrisLoader;
        this.internalRandom = ThreadLocal.withInitial(() -> random);
    }

    public void clear() {
        for (int i = 0; i < this.grid.length; i++) {
            for (int j = 0; j < this.grid[i].length; j++) {
                this.grid[i][j] = 0;
            }
        }
    }

    public void step() {
        if (this.gameOver) {
            return;
        }

        //Piece doesn't exist
        if (piece == null) {
            //Create it already then
            this.spawnPiece();
        } else {
            //Try to move the piece down
            if (this.canPut(this.row + 1, this.col)) {
                this.movePieceDown();
            } else {
                //Check end game status
                if (isEndGame()) {
                    this.gameOver = true;
                    return;
                }
                //Check if row can be removed
                for (int rowRemove = 0; rowRemove < this.maxRows; rowRemove++) {
                    if (!Arrays.stream(this.grid[rowRemove]).parallel().filter(value -> value == 0).findAny().isPresent()) {
                        this.emptyRow(rowRemove);
                        this.score += 2000;
                        //score
                    }
                }
                //Spawn new piece
                this.spawnPiece();
            }
        }
    }

    private void movePieceDown() {
        //Remove Ones self from the grid
        this.put(this.piece.getPiece(), this.row, this.col, 0);
        this.row++;
        this.put(this.piece.getPiece(), this.row, this.col, this.piece.getType());
    }

    private void emptyRow(final int row) {
        final boolean[][] map = new boolean[1][this.maxCols];
        for (int i = 0; i < this.maxCols; i++) {
            map[0][i] = true;
        }
        this.put(map, row, 0, 0);
        for (int i = row - 1; i >= 0; i--) {
            for (int j = 0; j < this.maxCols; j++) {
                this.grid[i + 1][j] = this.grid[i][j];
            }
        }
    }

    private void put(final boolean[][] map, final int putRow, final int putCol, final int value) {
        for (int mapRow = 0; mapRow < map.length; mapRow++) {
            for (int mapCol = 0; mapCol < map[mapRow].length; mapCol++) {
                if (map[mapRow][mapCol] &&
                        Range.closedOpen(0, this.maxRows).contains(putRow + mapRow) &&
                        Range.closedOpen(0, this.maxCols).contains(putCol + mapCol)) {
                    this.grid[putRow + mapRow][putCol + mapCol] = value;
                }
            }
        }
    }

    private boolean withinGridBounds(final int rowCheck, final int colCheck) {
        return Range.closedOpen(0, this.maxRows).contains(rowCheck) &&
                Range.closedOpen(0, this.maxCols).contains(colCheck);
    }

    private boolean canPut(final int afterRow, final int afterCol) {
        return this.canPut(this.piece.getPiece(), this.row, this.col, afterRow, afterCol, this.piece.getType());
    }

    private boolean canPut(final boolean[][] map, final int beforeRow, final int beforeCol, final int afterRow, final int afterCol, final int value) {
        this.put(map, beforeRow, beforeCol, 0);
        final boolean result = this.canPut(map, afterRow, afterCol);
        this.put(map, beforeRow, beforeCol, value);
        return result;
    }

    private boolean canPut(final boolean[][] map, final int afterRow, final int afterCol) {
        for (int rowCheck = 0; rowCheck < map.length; rowCheck++) {
            for (int colCheck = 0; colCheck < map[rowCheck].length; colCheck++) {
                if (map[rowCheck][colCheck]) {
                    //Case we hit the boundaries, if it's out of the top bounder it's fine since it only falls down
                    if (!this.withinGridBounds(Math.max(rowCheck + afterRow, 0), colCheck + afterCol)) {
                        return false;
                    }
                    //Case there something underneath it
                    if (this.withinGridBounds(rowCheck + afterRow, colCheck + afterCol) &&
                            this.grid[rowCheck + afterRow][colCheck + afterCol] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void spawnPiece() {
        this.pieces++;
        //Randomly create a piece
        this.piece = this.tetrisLoader.createPiece(this.internalRandom.get());
        //Randomly place this piece above the screen
        this.row = 0 - this.piece.getTotalRow();
        this.col = random.nextInt(this.internalRandom.get(), 0, this.maxCols - this.piece.getTotalCol());
    }

    private boolean isEndGame() {
        for (int checkRow = 0; checkRow < this.piece.getPiece().length; checkRow++) {
            for (int checkCol = 0; checkCol < this.piece.getPiece()[checkRow].length; checkCol++) {
                if (this.piece.getPiece()[checkRow][checkCol] && this.row + checkRow < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void leftButton() {
        if (this.piece != null && this.canPut(this.row, this.col - 1)) {
            this.put(this.piece.getPiece(), this.row, this.col, 0);
            this.col--;
            this.put(this.piece.getPiece(), this.row, this.col, this.piece.getType());
        }
    }

    public void rightButton() {
        if (this.piece != null && this.canPut(this.row, this.col + 1)) {
            this.put(this.piece.getPiece(), this.row, this.col, 0);
            this.col++;
            this.put(this.piece.getPiece(), this.row, this.col, this.piece.getType());
        }
    }

    public void downButton() {
        if (this.piece != null && this.canPut(this.row + 1, this.col)) {
            this.movePieceDown();
            this.score += 10;
        }
    }

    public void upButton() {
        if (this.piece == null) {
            return;
        }
        this.put(this.piece.getPiece(), this.row, this.col, 0);
        if (this.canPut(this.piece.getNextRotation(), this.row, this.col)) {
            this.piece.rotate();
        }
        this.put(this.piece.getPiece(), this.row, this.col, this.piece.getType());
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int[][] getGrid() {
        return grid;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public int getMaxCols() {
        return maxCols;
    }

    public long getScore() {
        return score + (pieces * 100);
    }

    public Iterator<Double> getGridAsIterator() {
        return Iterator.of(this.grid)
                .map(Iterator::ofAll)
                .flatMap(Iterator::iterator)
                .map(Integer::doubleValue)
                .map(value -> value == 0 ? 0 : (value)/this.tetrisLoader.getNumPieces());
    }

}
