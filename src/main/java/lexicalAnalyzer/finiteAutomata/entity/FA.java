package lexicalAnalyzer.finiteAutomata.entity;

import java.util.List;

/**
 * Created by cuihua on 2017/10/24.
 * <p>
 * 表示有穷自动机
 */
public abstract class FA {

    /**
     * 开始状态
     */
    private FA_State start;

    /**
     * 所有状态
     */
    private List<FA_State> states;

    /**
     * 终止／接受态
     */
    private List<FA_State> terminatedStates;

    /**
     * 字母表
     */
    private List<Character> alphabet;

    public FA_State getStart() {
        return start;
    }

    public void setStart(FA_State start) {
        this.start = start;
    }

    public List<FA_State> getStates() {
        return states;
    }

    public void setStates(List<FA_State> states) {
        this.states = states;
    }

    public List<FA_State> getTerminatedStates() {
        return terminatedStates;
    }

    public void setTerminatedStates(List<FA_State> terminatedStates) {
        this.terminatedStates = terminatedStates;
    }

    public List<Character> getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(List<Character> alphabet) {
        this.alphabet = alphabet;
    }

    /**
     * @param lexeme 要检查的词素
     * @return 词素是否合法
     */
    public abstract boolean isValid(String lexeme);

}
