package yacc.entities;

import entities.NonTerminal;
import entities.Terminal;
import entities.ValidSign;
import layeredFA.entities.FA_State;
import layeredFA.entities.LayeredFA;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuihua on 2017/11/15.
 * <p>
 * 文法分析表
 */
public class ParsingTable {

    /**
     * ACTION 子表，对应终结符
     */
    private Map<Integer, Map<Terminal, Action>> actionMap;

    /**
     * GOTO 子表，对应非终结符
     */
    private Map<Integer, Map<NonTerminal, Integer>> gotoMap;

    public ParsingTable(LayeredFA fa) {
        for (FA_State state : fa.getStates()) {
            for (ValidSign vs : fa.getValidSign()) {
                if (vs instanceof NonTerminal) gotoMap.put(state.getStateID(), new HashMap<>());
                else if (vs instanceof Terminal) actionMap.put(state.getStateID(), new HashMap<>());
            }
        }

    }

    public Map<Integer, Map<Terminal, Action>> getActionMap() {
        return actionMap;
    }

    public Map<Integer, Map<NonTerminal, Integer>> getGotoMap() {
        return gotoMap;
    }
}
