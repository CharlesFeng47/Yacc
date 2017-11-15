package layeredFA.entities;

import entity.ValidSign;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cuihua on 2017/11/14.
 *
 * 有穷自动机
 */
public class LayeredFA {

    /**
     * 开始状态
     */
    private FA_State start;

    /**
     * 所有状态
     */
    private List<FA_State> states;

    /**
     * 所有的合法标记
     */
    private List<ValidSign> validSign;

    public LayeredFA(FA_State start, List<ValidSign> validSign) {
        this.start = start;
        this.validSign = validSign;

        this.states = new LinkedList<>();
        states.add(start);
    }

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

    public List<ValidSign> getValidSign() {
        return validSign;
    }

    public void setValidSign(List<ValidSign> validSign) {
        this.validSign = validSign;
    }
}
