package lexicalAnalyzer.utilties;

import lexicalAnalyzer.finiteAutomata.entity.FA_Node;

import java.util.LinkedList;

/**
 * Created by cuihua on 2017/10/28.
 * <p>
 * 优化 DFA 时使用的数据结构
 * 复写二分法（根据叶节点中 FA_State 的状态数目而定）
 */
public class FA_NodesList extends LinkedList<FA_Node> {

    @Override
    public int indexOf(Object o) {
        int i = ((FA_Node) o).getStates().size();

        int start = 0;
        int end = this.size() - 1;
        while (start <= end) {
            int middle = (start + end) / 2;
            if (i < get(middle).getStates().size()) {
                end = middle - 1;
            } else if (i > get(middle).getStates().size()) {
                start = middle + 1;
            } else {
                return middle;
            }
        }
        return -1;
    }
}
