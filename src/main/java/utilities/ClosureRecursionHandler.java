package utilities;

import entities.Production;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * 解决递归 closure 时循环处理的问题
 */
public class ClosureRecursionHandler {

    private static Logger logger = Logger.getLogger(ClosureRecursionHandler.class);

    private static List<Production> productions = new LinkedList<>();

    private ClosureRecursionHandler() {
    }

    /**
     * 清理当前处理的现场
     */
    public static void reset() {
        productions = new LinkedList<>();
        logger.debug("Already reset the ClosureRecursionHandler");
    }

    /**
     * 增加一个 state
     */
    public static void addProduction(Production p) {
        productions.add(p);
    }

    /**
     * 增加一堆 state list
     */
    public static void addAllProductions(List<Production> ps) {
        productions.addAll(ps);
    }

    /**
     * 检测 states 中是否含有参数 state
     */
    public static boolean contain(Production production) {
        boolean result = productions.contains(production);
        logger.debug("Production " + production.toString() + " is contained: " + result);
        return result;
    }
}
