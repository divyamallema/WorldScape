package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

public class Engine {
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private Random rand;

    private int xpos;

    private int ypos;

    private Long seed;

    private boolean isFloor = false;

    private TETile[][] world = new TETile[WIDTH][HEIGHT];

    private TERenderer ter = new TERenderer();

    private File prevSeedFile;

    private String avatarName;

    private boolean gameOver = false;

    private boolean gameWin = false;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        // INITIALIZING WORLD
        ter.initialize(WIDTH, HEIGHT);
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        // Start Game
        startGame();
        rand = new Random(seed);
        // SPLITTING AREAS AND CREATING WORLD
        Area area = new Area(rand);
        area.split();
        area.createRooms(world);
        area.makeHallways(world);
        makeWalls();
        area.createLights(world);

        while (!isFloor) { //set avatar to a random tile that is a floor tile
            xpos = RandomUtils.uniform(rand, WIDTH);
            ypos = RandomUtils.uniform(rand, HEIGHT);
            if (world[xpos][ypos] == Tileset.FLOOR) {
                isFloor = true;
            }
        }
        System.out.println("xpos: " + xpos + " ypos: " + ypos);
        world[xpos][ypos] = Tileset.AVATAR;
        String validCommands = "wasd:qQrlo";
        ter.renderFrame(world);
        String inputStr = "";
        while (true) {
            hud();
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            if (gameOver) {
                drawFrame("GAME OVER");
                StdDraw.pause(5000);
                System.exit(0);
            }
            if (gameWin) {
                drawFrame("CONGRATS! YOU'VE WON THE GAME!");
                StdDraw.pause(5000);
                System.exit(0);
            }
            char c = StdDraw.nextKeyTyped();
            inputStr += String.valueOf(c);
            if (validCommands.indexOf(c) == -1) {
                System.out.println("Invalid Command");
            } else {
                if (validCommands.indexOf(c) == 0) {
                    //up
                    if (checkValidity(xpos, ypos + 1)) {
                        swap(xpos, ypos, xpos, ypos + 1);
                        ypos++;
                        ter.renderFrame(world);
                    }
                } else if (validCommands.indexOf(c) == 1) {
                    //left
                    if (checkValidity(xpos - 1, ypos)) {
                        swap(xpos, ypos, xpos - 1, ypos);
                        xpos--;
                        ter.renderFrame(world);
                    }
                } else if (validCommands.indexOf(c) == 2) {
                    //down
                    if (checkValidity(xpos, ypos - 1)) {
                        swap(xpos, ypos, xpos, ypos - 1);
                        ypos--;
                        ter.renderFrame(world);
                    }
                } else if (validCommands.indexOf(c) == 3) {
                    //right
                    if (checkValidity(xpos + 1, ypos)) {
                        swap(xpos, ypos, xpos + 1, ypos);
                        xpos++;
                        ter.renderFrame(world);
                    }
                } else if (inputStr.contains(":q") || inputStr.contains(":Q")) {
                    String x = Integer.toString(this.xpos);
                    String y = Integer.toString(this.ypos);
                    Utils.writeContents(prevSeedFile, seed.toString(), " ", x, " ", y);
                    System.exit(0);
                } else if (inputStr.contains("r")) {
                    interactWithKeyboard();
                } else if (inputStr.contains("l")) {
                    // background color for all floor tiles is black
                    if (area.getLightOn()) {
                        area.toggleLight(world);
                    } else {
                        area.createLights(world);
                    }
                }
            }
        }
    }

    public boolean checkValidity(int x, int y) {
        //checks if coordinate is wall or black space or out of bounds
        if (x > WIDTH || y > HEIGHT || x < 0 || y < 0) {
            return false;
        } else if (world[x][y] == Tileset.WALL) {
            return false;
        } else if (world[x][y] == Tileset.LIGHT || world[x][y] == Tileset.LIGHTTWO
                || world[x][y] == Tileset.LIGHTTHREE) {
            return false;
        }
        if (world[x][y] == Tileset.SAND) {
            gameOver = true;
            return false;
        }
        if (world[x][y] == Tileset.MOUNTAIN) {
            gameWin = true;
            return false;
        }
        return true;
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        try {
            input = input.toUpperCase();
            prevSeedFile = new File("prevWorldFile.txt");
            if (!prevSeedFile.exists()) {
                prevSeedFile.createNewFile();
            }
            String seedString = "";
            for (int x = 0; x < WIDTH; x += 1) {
                for (int y = 0; y < HEIGHT; y += 1) {
                    world[x][y] = Tileset.NOTHING;
                }
            }
            ter.initialize(WIDTH, HEIGHT);
            char c = ' ';
            // CREATING NEW WORLD WITH GIVEN INPUT
            if (input.contains("N")) {
                for (int i = 1; i < input.length(); i++) {
                    for (int j = 1; j < input.indexOf("S"); j++) {
                        seedString += input.charAt(j);
                    }
                    seed = Long.parseLong(seedString);
                    rand = new Random(seed);
                    createArea();
                    for (int k = input.indexOf("S") + 1; k < input.length(); k++) {
                        c = input.charAt(k);
                        moveAvatar(c);
                    }
                    ter.renderFrame(world);
                    break;
                }
            } else if (input.contains("L")) {
                // LOADING PREV WORLD FROM FILE
                String contents = Utils.readContentsAsString(prevSeedFile);
                int count = 0;
                char xChar;
                String x = "";
                String y = "";
                for (int j = 0; j < contents.length(); j++) {
                    xChar = contents.charAt(j);
                    if (xChar == ' ') {
                        count += 1;
                        continue;
                    }
                    if (count == 0) {
                        seedString += xChar;
                    } else if (count == 1) {
                        x += xChar;
                    } else if (count == 2) {
                        y += xChar;
                    }
                }
                seed = Long.parseLong(seedString);
                xpos = Integer.parseInt(x);
                ypos = Integer.parseInt(y);
                isFloor = true;
                rand = new Random(seed);
                createArea();
                for (int k = input.indexOf("L") + 1; k < input.length(); k++) {
                    c = input.charAt(k);
                    moveAvatar(c);
                }
                ter.renderFrame(world);
            }
            // QUITTING AND SAVING PREV WORLD
            if (input.contains(":q") || input.contains(":Q")) {
                System.out.println("Saving world...");
                Utils.writeContents(prevSeedFile, this.seed.toString(), " ",
                        Integer.toString(this.xpos), " ", Integer.toString(this.ypos));
                System.exit(0);
            }
            return world;
        } catch (IOException e) {
            return world;
        }
    }

    public void createArea() {
        Random random = new Random(seed);
        Area area = new Area(random);
        area.split();
        area.createRooms(world);
        area.makeHallways(world);
        makeWalls();
    }

    public void moveAvatar(char c) {
        while (!isFloor) { //set avatar to a random tile that is a floor tile
            xpos = RandomUtils.uniform(rand, WIDTH);
            ypos = RandomUtils.uniform(rand, HEIGHT);
            if (checkValidity(xpos, ypos)) {
                isFloor = true;
            }
        }

//        System.out.println("xpos: " + xpos + " ypos: " + ypos);
        world[xpos][ypos] = Tileset.AVATAR;
        String validCommands = "WASD:qQ";
        if (validCommands.indexOf(c) == -1) {
            System.out.println(c);
            System.out.println("Invalid Command");
        } else {
            if (validCommands.indexOf(c) == 0) {
                //up
                if (checkValidity(xpos, ypos + 1)) {
                    swap(xpos, ypos, xpos, ypos + 1);
                    ypos++;
                    ter.renderFrame(world);
                }
            } else if (validCommands.indexOf(c) == 1) {
                //left
                if (checkValidity(xpos - 1, ypos)) {
                    swap(xpos, ypos, xpos - 1, ypos);
                    xpos--;
                    ter.renderFrame(world);
                }
            } else if (validCommands.indexOf(c) == 2) {
                //down
                if (checkValidity(xpos, ypos - 1)) {
                    swap(xpos, ypos, xpos, ypos - 1);
                    ypos--;
                    ter.renderFrame(world);
                }
            } else if (validCommands.indexOf(c) == 3) {
                //right
                if (checkValidity(xpos + 1, ypos)) {
                    swap(xpos, ypos, xpos + 1, ypos);
                    xpos++;
                    ter.renderFrame(world);
                }
            }
        }
    }
    public void startGame() {
        try {
            prevSeedFile = new File("prevWorldFile.txt");
            if (!prevSeedFile.exists()) {
                prevSeedFile.createNewFile();
            }
            char c;
            // SHOW MAIN MENU AND WAIT FOR INPUT
            String input = "";
            String validCommands = "NnLlQq";
            while (input.length() == 0) {
                mainMenu();
                if (!StdDraw.hasNextKeyTyped()) {
                    continue;
                }
                c = StdDraw.nextKeyTyped();
                if (validCommands.indexOf(c) == -1) {
                    System.out.println("Invalid Command in start game");
                }
                input += String.valueOf(c);
            }
            // CHECK INPUT AND WAIT FOR SEED INPUT OR READ INPUT FROM FILE
            if (input.equals("N") || input.equals("n")) {
                // ENTER AVATAR NAME
                drawFrame("Enter Avatar Name (press '.' when done): ");
                String currSeed = "";
                String name = "";
                Boolean bool = true;
                Boolean bool1 = true;
                Boolean bool2 = true;
                while (bool) {
                    if (!StdDraw.hasNextKeyTyped()) {
                        continue;
                    }
                    c = StdDraw.nextKeyTyped();
                    if (!(c >= 48 && c <= 57)) {
                        name += String.valueOf(c);
                    }
                    if (c != '.') {
                        drawFrame("Enter Avatar Name (press '.' when done): " + name);
                    } else {
                        avatarName = name;
                        drawFrame("Enter Game Seed: " + currSeed);
                        bool = false;
                    }
                }
                // ENTER SEED
                while (bool2) {
                    if (!StdDraw.hasNextKeyTyped()) {
                        continue;
                    }
                    c = StdDraw.nextKeyTyped();
                    if (c >= 48 && c <= 57) {
                        currSeed += String.valueOf(c);
                    }
                    if (c != 's') {
                        drawFrame("Enter Game Seed: " + currSeed);
                    } else {
                        Utils.writeContents(prevSeedFile, currSeed);
                        seed = Long.parseLong(currSeed);
                        bool2 = false;
                    }
                }
            } else if (input.equals("L") || input.equals("l")) {
                // READ STRING FROM FILE AND SEED = THAT STRING
                String contents = Utils.readContentsAsString(prevSeedFile);
                System.out.println("Contents: " + contents);
                int count = 0;
                char xChar;
                String seedStr = "";
                String x = "";
                String y = "";
                for (int i = 0; i < contents.length(); i++) {
                    xChar = contents.charAt(i);
                    if (xChar == ' ') {
                        count += 1;
                        continue;
                    }
                    if (count == 0) {
                        seedStr += xChar;
                    } else if (count == 1) {
                        x += xChar;
                    } else if (count == 2) {
                        y += xChar;
                    }
                }
                seed = Long.parseLong(seedStr);
                xpos = Integer.parseInt(x);
                ypos = Integer.parseInt(y);
                isFloor = true;

            } else if (input.equals("Q") || input.equals("q") || input.contains(":Q")
            || input.contains(":q")) {
                System.exit(0);
            } else {
                startGame();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public void makeWalls() {
        for (int i = 0; i < WIDTH; i += 1) {
            for (int j = 0; j < HEIGHT; j += 1) {
                if (world[i][j] == Tileset.FLOOR || world[i][j] == Tileset.SAND) {
                    if (world[i - 1][j] == Tileset.NOTHING) {
                        world[i - 1][j] = Tileset.WALL;
                    }
                    if (world[i + 1][j] == Tileset.NOTHING) {
                        world[i + 1][j] = Tileset.WALL;
                    }
                    if (world[i][j - 1] == Tileset.NOTHING) {
                        world[i][j - 1] = Tileset.WALL;
                    }
                    if (world[i][j + 1] == Tileset.NOTHING) {
                        world[i][j + 1] = Tileset.WALL;
                    }
                }
                if (world[i][j] == Tileset.FLOOR || world[i][j] == Tileset.SAND) {
                    if (world[i - 1][j] == Tileset.WALL && world[i][j - 1] == Tileset.WALL) {
                        world[i - 1][j - 1] = Tileset.WALL;
                    }
                    if (world[i + 1][j] == Tileset.WALL && world[i][j - 1] == Tileset.WALL) {
                        world[i + 1][j - 1] = Tileset.WALL;
                    }
                    if (world[i + 1][j] == Tileset.WALL && world[i][j + 1] == Tileset.WALL) {
                        world[i + 1][j + 1] = Tileset.WALL;
                    }
                    if (world[i - 1][j] == Tileset.WALL && world[i][j + 1] == Tileset.WALL) {
                        world[i - 1][j + 1] = Tileset.WALL;
                    }
                }
            }
        }
        boolean notFound = true;
        int i = 0;
        int j = 0;
        while (notFound && i < WIDTH && j < HEIGHT) {
            if (world[i][j] == Tileset.WALL) {
                System.out.println("i: " + i + " j: " + j);
                world[i][j] = Tileset.LOCKED_DOOR;
                notFound = false;
            }
            i++;
            j++;
        }
    }

    public void swap(int x, int y, int x2, int y2) {
        TETile temp = world[x][y];
        world[x][y] = world[x2][y2];
        world[x2][y2] = temp;
    }

    public void hud() {
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 10);
        StdDraw.setFont(fontBig);
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        StdDraw.textLeft(1, HEIGHT - 4, formatter.format(date));
        StdDraw.textLeft(1, HEIGHT - 5, "Avatar Name: " + avatarName);
        if (x >= WIDTH || y >= HEIGHT || x < 0 || y < 0) {
            x = 0;
            y = 0;
        }
        if (world[x][y] == Tileset.FLOOR) {
            StdDraw.textLeft(1, HEIGHT - 2, Tileset.FLOOR.description());
            StdDraw.textLeft(1, HEIGHT - 3, "Looks like I can walk on this.");
        } else if (world[x][y] == Tileset.WALL) {
            StdDraw.textLeft(1, HEIGHT - 2, Tileset.WALL.description());
            StdDraw.textLeft(1, HEIGHT - 3, "This place is fortified.");
        } else if (world[x][y] == Tileset.NOTHING) {
            StdDraw.textLeft(1, HEIGHT - 2, Tileset.NOTHING.description());
            StdDraw.textLeft(1, HEIGHT - 3, "Nothing but deep space for miles.");
        } else if (world[x][y] == Tileset.AVATAR) {
            StdDraw.textLeft(1, HEIGHT - 2, Tileset.AVATAR.description());
            StdDraw.textLeft(1, HEIGHT - 3, "Hey look, that's me!");
        }
        StdDraw.show();
        StdDraw.pause(100);
        StdDraw.clear(Color.black);
        ter.renderFrame(world);
    }

    public void mainMenu() {
        /* Take the input string S and display it at the center of the screen,
         * with the pen settings given below. */

        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT - 10, "CS 61BL: The Game");
        StdDraw.text(WIDTH / 2, HEIGHT - 15, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT - 17.5, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT - 20, "Quit (Q)");
        StdDraw.text(WIDTH / 2, HEIGHT - 22.5, "Click 'r' while exploring to restart game");
        StdDraw.show();
    }

    public void drawFrame(String s) {
        /* Take the input string S and display it at the center of the screen,
         * with the pen settings given below. */
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT - 10, s);
        StdDraw.show();
    }
}
