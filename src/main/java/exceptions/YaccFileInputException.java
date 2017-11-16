package exceptions;

/**
 * Created by cuihua on 2017/11/16.
 * <p>
 * Yacc .y 文件中输入错误
 */
public class YaccFileInputException extends Exception {

    public YaccFileInputException(String message) {
        super(message);
    }
}
