package byow.Core;

import java.io.IOException;

/** This is the main entry point for the program. This class simply parses
 *  the command line inputs, and lets the byow.Core.Engine class take over
 *  in either keyboard or input string mode.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 2) {
            System.out.println("Can only have one arguments - input string");
            System.exit(0);
        } else if (args.length == 1) {
            Engine engine = new Engine();
            engine.interactWithInputString(args[0]);
        // DO NOT CHANGE THESE LINES YET ;)
        } else {
            Engine engine = new Engine();
            engine.interactWithKeyboard();
        }
    }
}
