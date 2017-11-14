package yacc.entities;

import entity.NonTerminal;
import entity.Terminal;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuihua on 2017/11/15.
 *
 * 文法分析表
 */
public class ParsingTable {

    /**
     * ACTION 子表，对应终结符
     */
    private Map<Integer, Map<Terminal, Action>> actionMao;

    /**
     * GOTO 子表，对应非终结符
     */
    private Map<Integer, Map<NonTerminal, Action>> gotoMao;

    public ParsingTable() {
        actionMao = new HashMap<>();
        gotoMao = new HashMap<>();
    }

    public Map<Integer, Map<Terminal, Action>> getActionMao() {
        return actionMao;
    }

    public Map<Integer, Map<NonTerminal, Action>> getGotoMao() {
        return gotoMao;
    }
}
