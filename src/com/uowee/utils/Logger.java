package com.uowee.utils;

public class Logger {

    private static String prefix = "[MINICAP-LOGGER]";
    private static String suffix = "[/MINICAP-LOGGER]";

    public static void debug(final String msg) {
        System.out.println(Logger.prefix + " [debug] " + msg + Logger.suffix);
    }

    public static void error(final String msg) {
        System.out.println(Logger.prefix + " [error] " + msg + Logger.suffix);
    }

    public static void info(final String msg) {
        System.out.println(Logger.prefix + " [info] " + msg + Logger.suffix);
    }
}
