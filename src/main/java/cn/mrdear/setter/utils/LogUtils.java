package cn.mrdear.setter.utils;

/**
 * 本地开发时,使用的日志记录其,生产环境不会开放
 * @author quding
 * @since 2022/2/7
 */
public final class LogUtils {

    private static final Boolean LOG = Boolean.valueOf(System.getProperty("LOG"));


    public static void debug(String message, Object... args) {
        if (LOG) {
            System.out.printf((message) + "%n", args);
        }
    }

    public static void error(String message, Object... args) {
        if (LOG) {
            System.err.printf((message) + "%n", args);
        }
    }

}
