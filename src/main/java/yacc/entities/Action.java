package yacc.entities;

import utilities.ActionType;

/**
 * Created by cuihua on 2017/11/15.
 * <p>
 * 文法分析表中的动作
 */
public class Action {

    /**
     * 动作类型
     */
    private ActionType type;

    /**
     * 移入的下一个状态／归约使用的产生式编号
     */
    private int operand;

    public Action(ActionType type, int operand) {
        this.type = type;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return type + " " + String.valueOf(operand);
    }

    public ActionType getType() {
        return type;
    }

    public int getOperand() {
        return operand;
    }
}
