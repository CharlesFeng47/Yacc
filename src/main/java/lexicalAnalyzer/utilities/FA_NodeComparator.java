package lexicalAnalyzer.utilities;

import lexicalAnalyzer.finiteAutomata.entity.FA_Node;
import lexicalAnalyzer.finiteAutomata.entity.FA_State;

import java.util.Comparator;
import java.util.List;

/**
 * Created by cuihua on 2017/10/28.
 * <p>
 * 对当前优化最小化 DFA 的结果集进行排序
 */
public class FA_NodeComparator implements Comparator<FA_Node> {


    @Override
    public int compare(FA_Node o1, FA_Node o2) {
        List<FA_State> states1 = o1.getStates();
        List<FA_State> states2 = o2.getStates();
        
        int size1 = states1.size();
        int size2 = states2.size();

        if (size1 < size2) return -1;
        else if (size1 > size2) return 1;
        else {
            // 状态数目相同，逐一比较
            for (int i = 0; i < size1; i++) {
                int state1 = states1.get(i).getStateID();
                int state2 = states2.get(i).getStateID();

                if (state1 < state2) return  -1;
                else if (state1 > state2) return 1;
            }

            // 每个状态都相同
            return 0;

        }
    }
}
