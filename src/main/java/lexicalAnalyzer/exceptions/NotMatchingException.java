package lexicalAnalyzer.exceptions;

/**
 * Created by cuihua on 2017/11/2.
 * <p>
 * 用户要分析的字符串和 .l 正则定义不匹配
 */
public class NotMatchingException extends Exception {

    /**
     * 不合法的词素
     */
    private String lexeme;

    public NotMatchingException(String lexeme) {
        this.lexeme = lexeme;
    }

    @Override
    public String getMessage() {
        return "转换生成的 DFA 中对词素 " + lexeme +  " 无匹配状态";
    }
}
