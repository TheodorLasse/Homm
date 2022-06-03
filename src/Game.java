package src;

import src.data.GameComponent;
import src.data.GameMap;
import src.data.ImageLoader;
import src.data.Vector2D;
import src.data.time.DeltaTime;
import src.sprites.Sprite;
import src.sprites.SpriteHandler;
import src.sprites.SpriteLayer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

/**
 * Class where the entire game is implemented. Controlls the central update function which controls deltaTime and calls upon all other
 * updates etc. Also initializes the game, shows the window and the end screen.
 */

public class Game {
    private final Logger logger = Logger.getLogger("");
    private static final double MAX_FPS = 144;
    private static final Random RND = new Random();
    public static final ImageLoader imageLoader = new ImageLoader();
    private SpriteHandler spriteHandler = null;
    private GameMap gameMap = null;


    GameComponent gc;
    Dimension screenSize;

    public Game(){
        setUpLogger();

        // Try to load all images and audio.
        // If the loading fails, then exit the program because there is no point in running the game without these resources.
        try {
            imageLoader.loadImages();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString(), e);
            System.exit(1);
        }


        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        gc = new GameComponent(this);
        spriteHandler = new SpriteHandler();
        gameMap = new GameMap();
        show();
    }

    /**
     * Starts the game.
     */
    public void start() {

        long lastUpdate = System.nanoTime();

        // Minimum number of nanoseconds for each game loop
        final double minFrameTime = DeltaTime.NANO_SECONDS_IN_SECOND / MAX_FPS;

        while (true) {
            long startTime = System.nanoTime();
            long deltaTime = startTime - lastUpdate;
            lastUpdate = startTime;

            update(new DeltaTime(deltaTime));
            gc.repaint();

            long totalTime = System.nanoTime() - startTime;
            // To avoid using 100% of cpu core when not needed, sleep until the minimum frame time is met
            if (totalTime < minFrameTime) {
                try {
                    TimeUnit.NANOSECONDS.sleep((long) minFrameTime - totalTime);
                } catch (InterruptedException e) {}
            }
        }
    }

    /**
     * Updates the game.
     */
    private void update(DeltaTime deltaTime) {
        spriteHandler.update(deltaTime);
    }

    public int getWidth() {
        return screenSize.width;
    }

    public int getHeight() {
        return screenSize.height;
    }

    public Dimension getScreenDimension(){
        return screenSize;
    }

    /**
     * Returns an iterable var of all sprites. Basically merges all sprites into one list for gameComponent. The list's order matters,
     * sprites are drawn before entities, etc
     */
    public Iterable<Sprite> getSpriteIterator() {
        List<Sprite> sprites = new ArrayList<>();

        //param 1: top left corner pos, param 2: size of object in middle of screen, param 3: scope to draw
        sprites.addAll(gameMap.getIterator(new Vector2D(10, 20), new Vector2D(), new Vector2D(5, 5)));
        sprites.addAll(spriteHandler.getLayerIterator(SpriteLayer.FIRST));
        sprites.addAll(spriteHandler.getLayerIterator(SpriteLayer.LAST));

        return sprites;
    }

    private void addIterator(List<Sprite> sprites, Iterable<Sprite> it) {
        for (Sprite s : it) {
            sprites.add(s);
        }
    }

    /**
     * Creates the game window.
     */
    private void show() {
        JFrame frame = new JFrame("Game");
        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(gc);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Adds a new FileHandler to the logger. If the FileHandler fails, a ConsoleHandler is used as backup.
     */
    private void setUpLogger() {
        try {
            FileHandler fh = new FileHandler("LogFile.log");
            logger.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            // This catch clause triggers a "CatchFallthrough" warning in the automatic code inspection. The warning can be ignored because
            // the exception is handled in the catch clause (In this case by adding a ConsoleHandler instead of the FileHandler).

            logger.log(Level.SEVERE, e.toString(), e);

            // If the logger don't have any handlers, add a ConsoleHandler
            if (logger.getHandlers().length == 0) {
                ConsoleHandler ch = new ConsoleHandler();
                logger.addHandler(ch);
            }
        }
    }
}
