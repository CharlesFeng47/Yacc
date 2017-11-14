package entity;

import java.util.List;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
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

    public Production(NonTerminal left, List<ValidSign> right) {
        this.left = left;
        this.right = right;
        this.indicator = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(left.getRepresentation()).append(" -> ");
        for (ValidSign sign : right) {
            sb.append(sign.getRepresentation()).append(" ");
        }
        return sb.toString();
    }

    public Production moveForward(){
        // 在最后就直接返回空
        int curIndicator = this.getIndicator();
        if (curIndicator == this.getRight().size()) return null;

        Production result = new Production(this.left, this.right);
        result.setIndicator(++curIndicator);
        return result;
    }

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

    public int getIndicator() {
        return indicator;
    }

    public void setIndicator(int indicator) {
        this.indicator = indicator;
    }
}
