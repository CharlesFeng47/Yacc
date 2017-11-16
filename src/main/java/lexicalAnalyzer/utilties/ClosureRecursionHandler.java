package lexicalAnalyzer.utilties;

import lexicalAnalyzer.finiteAutomata.entity.FA_State;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by cuihua on 2017/10/27.
 * <p>
 * 解决递归 closure 时循环处理的问题
 */
public class ClosureRecursionHandler {

    private static Logger logger = Logger.getLogger(ClosureRecursionHandler.class);

    private static List<FA_State> states = new FA_StatesList();
    private static FA_StateComparator comparator = new FA_StateComparator();

    private ClosureRecursionHandler() {
    }

    /**
     * 清理当前处理的现场
     */
    public static void reset() {
        states = new FA_StatesList();
        logger.debug("Already reset the ClosureRecursionHandler");
    }

    /**
     * 增加一个 state，需保证整个 list 是排好序的，才能被复写的二分法找到
     */
    public static void addState(FA_State state) {
        states.add(state);
        states.sort(comparator);
    }

    /**
     * 增加一堆 state list，需保证整个 list 是排好序的，才能被复写的二分法找到
     */
    public static void addAllState(List<FA_State> newStates) {
        states.addAll(newStates);
        states.sort(comparator);
    }

    /**
     * 检测 states 中是否含有参数 state
     */
    public static boolean contain(FA_State state) {
        boolean result = states.contains(state);
        logger.debug("State " + state.getStateID() + " is contained: " + result);
        return result;
    }
}
