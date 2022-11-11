package util;

/**
 * @Author redamancy
 * @Date 2022/11/10 16:45
 * @Version 1.0
 */
public class RuntimeUtil {

    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
