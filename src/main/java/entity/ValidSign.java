package entity;

/**
 * Created by cuihua on 2017/11/14.
 *
 * 合法字符
 * TODO 增加方法及实现 在分析表中遇到了该怎么做
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

    public void setRepresentation(String representation) {
        this.representation = representation;
    }
}
