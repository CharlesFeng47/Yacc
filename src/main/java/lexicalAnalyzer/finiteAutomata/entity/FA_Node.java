package lexicalAnalyzer.finiteAutomata.entity;

import java.util.List;

/**
 * Created by cuihua on 2017/10/26.
 * <p>
 * 最小化 DFA 过程中形成的等价 FA_State 组成的集合
 */
public class FA_Node {

    /**
     * 等价 FA_State
     */
    private List<FA_State> states;

    public FA_Node(List<FA_State> states) {
        this.states = states;
    }

    public List<FA_State> getStates() {
        return states;
    }

    public void setStates(List<FA_State> states) {
        this.states = states;
    }
}
