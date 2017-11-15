package exceptions;

/**
 * Created by cuihua on 2017/11/16.
 * <p>
 * 语法分析的 ERROR 错误
 */
public class SyntaxException extends Exception {

    public SyntaxException(String message) {
        super(message);
    }
}
