package exceptions;

import yacc.entities.Action;

/**
 * Created by cuihua on 2017/11/15.
 * <p>
 * 填入文法分析表时，移入-归约冲突／归约-归约冲突
 */
public class ParsingTableConflictException extends Exception {

    private Action action1;

    private Action action2;

    public ParsingTableConflictException(Action action1, Action action2) {
        this.action1 = action1;
        this.action2 = action2;
    }

    @Override
    public String getMessage() {
        return action1.toString() + " 和 " + action2.toString() + "发生了冲突";
    }
}
