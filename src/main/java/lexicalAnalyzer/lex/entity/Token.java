package lexicalAnalyzer.lex.entity;

/**
 * Created by cuihua on 2017/10/31.
 *
 * 词法单元
 */
public class Token {

    /**
     * 模式
     */
    private String patternType;

    /**
     * 属性值
     */
    private String attribute;

    public Token(String patternType, String attribute) {
        this.patternType = patternType;
        this.attribute = attribute;
    }

    public String getPatternType() {
        return patternType;
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
}
