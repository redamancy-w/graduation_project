package fang.redamancy.core.common.util;

/**
 * @Author redamancy
 * @Date 2023/1/8 17:11
 * @Version 1.0
 */


public class Holder<T> {
    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;

    }
}
