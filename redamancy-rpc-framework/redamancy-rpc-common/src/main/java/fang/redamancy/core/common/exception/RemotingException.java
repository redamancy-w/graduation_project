package fang.redamancy.core.common.exception;

/**
 * @Author redamancy
 * @Date 2023/4/17 16:06
 * @Version 1.0
 */
public class RemotingException extends Exception {
    private static final long serialVersionUID = -3160452149606778709L;

    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}
