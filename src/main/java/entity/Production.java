package entity;

import java.util.List;

/**
 * Created by cuihua on 2017/11/14.
 *
 * 表示文法的一条产生式
 */
public class Production {

    /**
     * 产生式左边
     */
    private NonTerminal left;

    /**
     * 产生式右边
     */
    private List<ValidSign> right;

    /**
     * 标识小圆点现在的位置【最小为 0，最大为 right.size()】
     */
    private int indicator;

    public NonTerminal getLeft() {
        return left;
    }

    public void setLeft(NonTerminal left) {
        this.left = left;
    }

    public List<ValidSign> getRight() {
        return right;
    }

    public void setRight(List<ValidSign> right) {
        this.right = right;
    }
}
