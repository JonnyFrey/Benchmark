package benchmark.ga.tetris;

import benchmark.ga.tetris.neat.network.Network;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.slick2d.NiftyOverlayGame;
import lombok.Getter;
import lombok.Setter;
import org.jenetics.util.RandomRegistry;
import org.newdawn.slick.*;
import org.newdawn.slick.util.Log;

import javax.annotation.Nonnull;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Created by Jonny on 8/31/17.
 */
public class TetrisScreen extends NiftyOverlayGame {

    private static final int boxSize = 70;

    private TetrisInput input;
    private TetrisGrid grid;
    private int total = 0;

    private final AppGameContainer container;
    private int screenWidth;
    private int screenHeight;

    private int stepTime = 1000;

    private Network network;

    @Setter @Getter
    private boolean pause = true;

    public TetrisScreen() throws SlickException {
        super();
        this.grid = new TetrisGrid(new Random());
        this.input = new TetrisInput(this.grid, this);
        this.container = new AppGameContainer(this);
        this.container.setShowFPS(false);

        this.screenWidth = 40 * this.grid.getMaxCols();
        this.screenHeight = 40 * this.grid.getMaxRows();

        this.container.setDisplayMode(this.screenWidth, this.screenHeight, false);
        this.container.setVSync(true);
    }

    public TetrisScreen(final Network network) throws SlickException {
        this();
        this.grid = new TetrisGrid(new Random(network.getSeed()));
        this.network = network;
        this.stepTime = 100;
    }

    public void start() throws SlickException {
        this.container.start();
    }

    public static void main(final String... args) throws SlickException {
        Log.setVerbose(false);
        setupProperties();
        TetrisScreen screen = new TetrisScreen();
        screen.start();
    }

    @Override
    protected void initGameAndGUI(@Nonnull final GameContainer container) {
        this.initNifty(container);
        container.getInput().addKeyListener(input);
    }

    @Override
    protected void renderGame(@Nonnull final GameContainer container, @Nonnull final Graphics g) {
        g.clear();
        try {
            final Image tetrisImage = new Image(boxSize * this.grid.getMaxCols(), boxSize * this.grid.getMaxRows());
            final Graphics tetrisGraphic = tetrisImage.getGraphics();
            tetrisGraphic.setColor(new Color(0xFFDAB9));
            tetrisGraphic.fillRect(0, 0, tetrisImage.getWidth(), tetrisImage.getHeight());
            for (int row = 0; row < this.grid.getMaxRows(); row++) {
                for (int col = 0; col < this.grid.getMaxCols(); col++) {
                    if (this.grid.getGrid()[row][col] != 0) {
                        tetrisGraphic.setColor(TetrisLoader.getInstance().getPieceColor(this.grid.getGrid()[row][col]));
                        tetrisGraphic.fillRect(col * boxSize, row * boxSize, boxSize, boxSize);
                        tetrisGraphic.setColor(Color.black);
                        tetrisGraphic.drawRect(col * boxSize, row * boxSize, boxSize, boxSize);
                    }
                }
            }
            g.drawImage(
                    tetrisImage,
                    0,
                    0,
                    container.getWidth(),
                    container.getHeight(),
                    0,
                    0,
                    tetrisImage.getWidth(),
                    tetrisImage.getHeight()
            );

            final Image scoreImage = new Image(200, 200);
            final Graphics scoreGraphics = scoreImage.getGraphics();
            scoreGraphics.setColor(Color.black);
            scoreGraphics.drawString("Score: " + this.grid.getScore(), 0, 0);

            g.drawImage(scoreImage, 0, 0);

            scoreImage.destroy();
            tetrisImage.destroy();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateGame(@Nonnull final GameContainer container, final int delta) {
        this.total += delta;
        if (!this.pause && !this.grid.isGameOver() && total >= this.stepTime) {
            if (this.network != null) {
                this.network.reactToGrid(this.grid);
            }
            this.grid.step();
            this.total = 0;
        }
        if (this.grid.isGameOver()) {
            this.input.setAcceptInput(false);
        }
    }

    @Override
    protected void prepareNifty(@Nonnull final Nifty nifty) {

    }

    @Override
    public boolean closeRequested() {
        return true;
    }

    @Override
    public String getTitle() {
        return "Tetris AI";
    }

    /**
     * Sets the appropriate System Properties to set up lwjgl. Must be called before the using lwjgl.
     */
    public static void setupProperties() {
        final String libraryNatives = Paths.get("natives/").toFile().getAbsolutePath();
        System.setProperty("org.lwjgl.librarypath", libraryNatives);
        System.setProperty("net.java.games.input.librarypath", libraryNatives);
    }

}
