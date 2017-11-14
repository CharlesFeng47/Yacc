package entity;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * 终结符【小写】
 */
public class Terminal extends ValidSign {

    /**
     * 终结符的表示，可能存在多个字符（如 if ）
     */
    private String representation;

    public Terminal(String representation) {
        this.representation = representation;
    }

    public String getRepresentation() {
        return representation;
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }
}
