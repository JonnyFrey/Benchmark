package benchmark.ga.tetris;

import lombok.Setter;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;

/**
 * Created by Jonny on 9/1/17.
 */
public class TetrisInput implements KeyListener {

    private final TetrisGrid grid;
    private final TetrisScreen tetrisScreen;
    @Setter
    private boolean acceptInput = true;
    private boolean down;

    public TetrisInput(final TetrisGrid grid, final TetrisScreen tetrisScreen) {
        this.grid = grid;
        this.tetrisScreen = tetrisScreen;
    }


    @Override
    public void keyPressed(final int key, final char c) {
        switch (key) {
            case Input.KEY_DOWN:
                this.grid.downButton();
                this.down = true;
                break;
        }
    }

    @Override
    public void keyReleased(final int key, final char c) {
        switch (key) {
            case Input.KEY_UP:
                this.grid.upButton();
                break;
            case Input.KEY_DOWN:
                this.down = false;
                break;
            case Input.KEY_LEFT:
                this.grid.leftButton();
                break;
            case Input.KEY_RIGHT:
                this.grid.rightButton();
                break;
            case Input.KEY_SPACE:
                this.tetrisScreen.setPause(!this.tetrisScreen.isPause());
        }
    }

    private void step() {
        if (this.down) {
            this.grid.downButton();
        }
    }

    @Override
    public void setInput(final Input input) {

    }

    @Override
    public boolean isAcceptingInput() {
        return this.acceptInput;
    }

    @Override
    public void inputEnded() {

    }

    @Override
    public void inputStarted() {

    }
}
