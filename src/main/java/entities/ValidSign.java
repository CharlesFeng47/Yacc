package entities;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * 合法字符
 */
public abstract class ValidSign {

    /**
     * 合法字符的表示
     */
    private String representation;

    public ValidSign(String representation) {
        this.representation = representation;
    }

    @Override
    public String toString() {
        return representation;
    }

    public String getRepresentation() {
        return representation;
    }
}
