package layeredFA;

import entities.NonTerminal;
import entities.Production;
import entities.ValidSign;
import layeredFA.entities.FA_Edge;
import layeredFA.entities.FA_State;
import layeredFA.entities.LayeredFA;
import org.apache.log4j.Logger;
import utilities.ClosureRecursionHandler;

import java.util.*;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * 根据文法构建相应的有穷自动机
 */
public class FA_Constructor {

    private static final Logger logger = Logger.getLogger(FA_Constructor.class);

    /**
     * 此文法的所有起始产生式（indicator 为 0）
     */
    private final List<Production> productions;

    /**
     * 所有的合法标记
     */
    private List<ValidSign> validSigns;

    public FA_Constructor(List<Production> productions, List<ValidSign> validSign) {
        this.productions = productions;
        this.validSigns = validSign;
    }

    /**
     * 基于现有的文法产生式，生成对应的状态机
     */
    public LayeredFA parse() {

        List<Production> startProduction = new LinkedList<>();
        startProduction.add(productions.get(0));

        // 以开始符的闭包集合开始
        FA_State startState = new FA_State(startProduction);
        startState.setProductions(closureProduction(startState.getProductions()));
        // 清理当前节点计算 innerStateExtension 时的递归现场
        ClosureRecursionHandler.reset();

        LayeredFA resultFA = new LayeredFA(startState, validSigns);

        // <产生式闭包, 闭包 FA_State>
        Map<List<String>, FA_State> productionStateMap = new HashMap<>();

        // dStates为<闭包 FA_State, 已标记>，LinkedHashMap保证为顺序而不是 hash 过的
        Map<FA_State, Boolean> dStates = new LinkedHashMap<>();
        dStates.put(resultFA.getStart(), false);
        while (true) {
            // dStates中是否还有未标记的状态，并对未标记的状态进行处理
            boolean hasStopped = true;
            FA_State unhandled = null;
            for (Map.Entry<FA_State, Boolean> entry : dStates.entrySet()) {
                if (!entry.getValue()) {
                    hasStopped = false;
                    entry.setValue(true);
                    unhandled = entry.getKey();
                    break;
                }
            }

            // 循环的终止条件
            if (hasStopped) break;

            // 处理此时的标记
            for (ValidSign vs : resultFA.getValidSign()) {
                List<Production> curFollowingPros = move(unhandled.getProductions(), vs);
                int curFollowingSize = curFollowingPros.size();

                if (curFollowingSize != 0) {
                    // 否则此状态此字符上无后继状态

                    // 保存当前要计算闭包的核
                    curFollowingPros = closureProduction(curFollowingPros);

                    // 清理当前节点计算 innerStateExtension 时的递归现场
                    ClosureRecursionHandler.reset();

                    // 判断此集合是都在 dStates 中，如不在则需更进一步处理
                    if (!isInDSates(dStates, curFollowingPros)) {
                        FA_State nextState = new FA_State(curFollowingPros);
                        dStates.put(nextState, false);
                        resultFA.getStates().add(nextState);

                        // 将此 FA_State 加入与产生式的映射
                        productionStateMap.put(convertProductionsToStrings(curFollowingPros), nextState);

                        // 加入链接
                        FA_Edge edge = new FA_Edge(vs, nextState);
                        unhandled.getFollows().add(edge);
                    } else {
                        // 直接加入链接
                        FA_State representState = productionStateMap.get(convertProductionsToStrings(curFollowingPros));
                        FA_Edge edge = new FA_Edge(vs, representState);
                        unhandled.getFollows().add(edge);
                    }
                }
            }
        }
        logger.debug("产生式拓展之后状态数目：" + resultFA.getStates().size());
        return resultFA;
    }

    /**
     * 对产生式进行闭包
     */
    private List<Production> closureProduction(List<Production> productions) {
        List<Production> result = new LinkedList<>();
        result.addAll(productions);

        ClosureRecursionHandler.addAllProductions(productions);

        for (Production p : productions) {
            // 小圆点到达最后，没有闭包
            if (p.getIndicator() == p.getRight().size()) continue;

            ValidSign nextSign = p.getRight().get(p.getIndicator());
            if (nextSign instanceof NonTerminal) {
                // 非终结符继续拓展
                List<Production> closure = getRelatedProduction((NonTerminal) nextSign);
                for (Production cp : closure) {
                    if (!ClosureRecursionHandler.contain(cp)) {
                        List<Production> core = new LinkedList<>();
                        core.add(cp);
                        result.addAll(closureProduction(core));
                    }
                }
            }
        }
        for (Production p : result) {
            logger.debug(p.toString());
        }
        logger.debug("\n");
        return result;
    }

    /**
     * 将此表达式集合以 vs 后移
     */
    private List<Production> move(List<Production> curPro, ValidSign vs) {
        String label = vs.getRepresentation();

        List<Production> resultProduction = new LinkedList<>();
        for (Production p : curPro) {
            // 表示此条表达式已到达末尾，不能再后继
            if (p.getIndicator() == p.getRight().size()) continue;

            ValidSign nextSign = p.getRight().get(p.getIndicator());
            if (nextSign.getRepresentation().equals(label)) {
                resultProduction.add(p.moveForward());
            }
        }

        logger.info("move 后继中产生式个数：" + resultProduction.size());
        for (Production p : resultProduction) {
            logger.info(p.toString());
        }
        return resultProduction;
    }

    /**
     * 判断 pros 是否已经在 dStates 中某个 FA_State 的表达式列表中了
     */
    private boolean isInDSates(Map<FA_State, Boolean> dStates, List<Production> toTestProductions) {
        for (Map.Entry<FA_State, Boolean> entry : dStates.entrySet()) {
            List<Production> curProductions = entry.getKey().getProductions();
            if (curProductions.size() == toTestProductions.size()) {
                List<String> curProductionsString = convertProductionsToStrings(curProductions);
                List<String> toTestProductionsString = convertProductionsToStrings(toTestProductions);

                List<String> newList = new LinkedList<>();
                newList.addAll(toTestProductionsString);
                newList.removeAll(curProductionsString);

                // 找到已经存在的状态
                if (newList.size() == 0) return true;
            }
        }
        return false;
    }

    /**
     * 将表达式转换为 String 格式便于比较
     */
    private List<String> convertProductionsToStrings(List<Production> ps) {
        List<String> result = new LinkedList<>();
        for (Production p : ps) {
            result.add(p.toString());
        }
        return result;
    }

    /**
     * 找寻以该非终结符 nonTerminal 开头的所有产生式
     */
    private List<Production> getRelatedProduction(NonTerminal nonTerminal) {
        List<Production> result = new LinkedList<>();
        for (Production p : productions) {
            if (p.getLeft().equals(nonTerminal)) {
                result.add(p);
            }
        }
        return result;
    }
}

