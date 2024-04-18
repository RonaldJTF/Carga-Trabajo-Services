package co.edu.unipamplona.ciadti.cargatrabajo.services.util;

import lombok.extern.log4j.Log4j2;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class Trace {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_UNDERLINE = "\u001B[4m";
    public static final String ANSI_BG_RED = "\u001B[41m";
    public static final String ANSI_BG_GREEN = "\u001B[42m";
    public static final String ANSI_BG_YELLOW = "\u001B[43m";
    public static final String ANSI_BG_BLUE = "\u001B[44m";
    public static final String ANSI_BG_PURPLE = "\u001B[45m";
    public static final String ANSI_BG_CYAN = "\u001B[46m";
    public static final String ANSI_BG_WHITE = "\u001B[47m";

    private static final String LABEL_EXCEPTION = ANSI_BOLD + ANSI_RED + "Exception: " + ANSI_RESET;
    private static final String LABEL_CLASS = ANSI_BOLD + ANSI_YELLOW + "Class: " + ANSI_RESET;
    private static final String LABEL_METHOD = ANSI_BOLD + ANSI_BLUE + "Method: " + ANSI_RESET;
    private static final String LABEL_MESSAGE = ANSI_BOLD + ANSI_GREEN + "Message: " + ANSI_RESET;

    public static String format(String className, String methodName, Exception e) {
        return (LABEL_EXCEPTION + e.getClass().getSimpleName() + " " +
                LABEL_CLASS + className + " " +
                LABEL_METHOD + methodName + " " +
                LABEL_MESSAGE + e.getMessage());
    }

    public static String format(Exception e) {
        return (LABEL_EXCEPTION + e.getClass().getSimpleName() + " " + LABEL_MESSAGE + e.getMessage());
    }

    public static String format(String message) {
        return (LABEL_MESSAGE + message);
    }

    public static void logError(String className, String methodName, Exception e) {
        log.error(Trace.format(className, methodName, e));
    }

    public static void logError(Exception e) {
        log.error(Trace.format(e));
    }

    public static void logError(String message) {
        log.error(Trace.format(message));
    }

    public static void print(Object object, String ansi) {
        if (isFullyAnsiColored(ansi)){
            System.out.print(ansi  + object + ANSI_RESET);
        }else {
            System.out.print(object);
        }
    }
    public static void print(String label, Object object, String labelAnsi) {
        if (isFullyAnsiColored(labelAnsi)){
            System.out.print(labelAnsi  + label + ": " + ANSI_RESET + object);
        }else {
            System.out.print(ANSI_BOLD  + label + ": " + ANSI_RESET + object);
        }
    }

    public static void println(String message, String ansi) {
        if (isFullyAnsiColored(ansi)){
            System.out.println(ansi  + message + ANSI_RESET);
        }else {
            System.out.println(message);
        }
    }

    public static void println(String label, Object object, String labelAnsi) {
        if (isFullyAnsiColored(labelAnsi)){
            System.out.println(labelAnsi  + label + ": " + ANSI_RESET + object);
        }else {
            System.out.print(ANSI_BOLD  + label + ": " + ANSI_RESET + object);
        }
    }

    public static boolean isFullyAnsiColored(String text) {
        Pattern pattern = Pattern.compile("^\\u001B\\[[0-9;]*m$");
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }
}
