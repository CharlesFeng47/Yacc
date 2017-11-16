package lexicalAnalyzer.utilities;

import lexicalAnalyzer.finiteAutomata.entity.FA_State;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by cuihua on 2017/10/24.
 * <p>
 * 优化查找为二分法，速度更快
 */
public class FA_StatesList extends LinkedList<FA_State> {

    public FA_StatesList() {
    }

    public FA_StatesList(Collection<? extends FA_State> c) {
        super(c);
    }

    @Override
    public int indexOf(Object o) {
        int i = ((FA_State) o).getStateID();

        int start = 0;
        int end = this.size() - 1;
        while (start <= end) {
            int middle = (start + end) / 2;
            if (i < get(middle).getStateID()) {
                end = middle - 1;
            } else if (i > get(middle).getStateID()) {
                start = middle + 1;
            } else {
                return middle;
            }
        }
        return -1;
    }
}
