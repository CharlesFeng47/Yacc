package lexicalAnalyzer.finiteAutomata.entity;

import java.util.Map;

/**
 * Created by cuihua on 2017/10/24.
 * <p>
 * Deterministic FA，确定的有穷自动机
 */
public class DFA extends FA {

    /**
     * DFA 中各状态之间的转换关系
     * 第一个 state(FA_State) 通过 label(Character) 到达第二个 state(FA_State)
     */
    private Map<FA_State, Map<Character, FA_State>> move;


    public Map<FA_State, Map<Character, FA_State>> getMove() {
        return move;
    }

    public void setMove(Map<FA_State, Map<Character, FA_State>> move) {
        this.move = move;
    }


    @Override
    public boolean isValid(String lexeme) {
        FA_State curState = getStart();

        for (char c : lexeme.toCharArray()) {
            boolean canFind = false;
            for (FA_Edge curEdge : curState.getFollows()) {
                if (curEdge.getLabel() == c) {
                    curState = curEdge.getPointTo();
                    canFind = true;
                    break;
                }
            }
            if (!canFind) return false;
        }
        return getTerminatedStates().contains(curState);
    }
}
