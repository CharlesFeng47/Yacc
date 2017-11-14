package entity;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * 非终结符【大写】
 */
public class NonTerminal extends ValidSign {

    /**
     * 非终结符的表示，限定为单个字符
     */
    private char representation;

    public NonTerminal(char representation) {
        this.representation = representation;
    }

    public char getRepresentation() {
        return representation;
    }

    public void setRepresentation(char representation) {
        this.representation = representation;
    }
}
