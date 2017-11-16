package lexicalAnalyzer.utilties;

import lexicalAnalyzer.finiteAutomata.entity.FA_State;

import java.util.Comparator;

/**
 * Created by cuihua on 2017/10/24.
 * <p>
 * 对 ε 闭包的集合进行排序
 */
public class FA_StateComparator implements Comparator<FA_State> {


    public int compare(FA_State o1, FA_State o2) {
        if (o1.getStateID() < o2.getStateID()) return -1;
        else if (o1.getStateID() == o2.getStateID()) return 0;
        else return 1;
    }
}
