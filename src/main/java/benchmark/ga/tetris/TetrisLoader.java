package benchmark.ga.tetris;

import org.jenetics.util.RandomRegistry;
import org.newdawn.slick.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Jonny on 8/31/17.
 */
public class TetrisLoader {

    //[piece][dimention][x][y]
    private static TetrisLoader TETRIS_LOADER;
    private boolean[][][][] pieceMap;
    private Color[] pieceColor;

    private TetrisPiece[] allPieces;

    private TetrisLoader(final String resource) {
        try(final BufferedReader reader = Files.newBufferedReader(Paths.get("src/main/resources/", resource))) {
            final int piecesCount = Integer.parseInt(reader.readLine());
            this.pieceColor = new Color[piecesCount];
            this.pieceMap = new boolean[piecesCount][][][];
            for (int pieceIndex = 0; pieceIndex < piecesCount; pieceIndex++) {
                final int[] infoBits = Arrays.stream(reader.readLine().split(" "))
                        .map(Integer::parseInt)
                        .mapToInt(Integer::intValue)
                        .toArray();
                this.pieceMap[pieceIndex] = new boolean[infoBits[0]][][];
                this.pieceColor[pieceIndex] = new Color(infoBits[3], infoBits[4], infoBits[5]);
                for (int dir = 0; dir < this.pieceMap[pieceIndex].length; dir++) {
                    this.pieceMap[pieceIndex][dir] = new boolean[infoBits[1]][infoBits[2]];
                    for (int row = 0; row < this.pieceMap[pieceIndex][dir].length; row++) {
                        final Boolean[] line = reader.readLine()
                                .chars()
                                .mapToObj(value -> value == 'X')
                                .toArray(Boolean[]::new);
                        for (int col = 0; col < this.pieceMap[pieceIndex][dir][row].length; col++) {
                            this.pieceMap[pieceIndex][dir][row][col] = line[col];
                        }
                    }
                    reader.readLine();
                }

            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        this.allPieces = new TetrisPiece[this.pieceMap.length];
        for (int i = 0; i < this.allPieces.length; i++) {
            this.allPieces[i] = this.createPiece(i);
        }
    }

    public TetrisPiece createPiece() {
        return this.createPiece(RandomRegistry.getRandom());
    }

    public TetrisPiece createPiece(final Random random) {
        return new TetrisPiece(this.allPieces[random.nextInt(this.pieceMap.length)]);
    }

    public int getNumPieces() {
        return this.allPieces.length;
    }

    public TetrisPiece createPiece(final int type) {
        return new TetrisPiece(clonePortion(type + 1), type + 1);
    }

    private boolean[][][] clonePortion(final int type) {
        final int dirSize = this.pieceMap[type - 1].length;
        boolean[][][] newArray = new boolean[dirSize][][];
        for (int dir = 0; dir < dirSize; dir++) {
            final int rowSize = this.pieceMap[type - 1][dir].length;
            newArray[dir] = new boolean[rowSize][];
            for (int row = 0; row < rowSize; row++) {
                final int colSize = this.pieceMap[type - 1][dir][row].length;
                newArray[dir][row] = new boolean[colSize];
                System.arraycopy(this.pieceMap[type - 1][dir][row], 0, newArray[dir][row], 0, colSize);
            }
        }
        return newArray;
    }

    public Color getPieceColor(final int type) {
        return pieceColor[type - 1];
    }

    public static TetrisLoader getInstance() {
        if (TETRIS_LOADER == null) {
            TETRIS_LOADER = new TetrisLoader("pieces");
        }
        return TETRIS_LOADER;
    }


}
