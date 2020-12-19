package de.xbrowniecodez.verus;

public class Logger {

    /**
    * @author brownie
     * @time: 9 Nov 2020 23:18:05
    */
    public static void info(String s) {
        System.out.println("[*] " + s);
    }
    public static void error(String s) {
        System.out.println("[ERROR] " + s);
    }
}
