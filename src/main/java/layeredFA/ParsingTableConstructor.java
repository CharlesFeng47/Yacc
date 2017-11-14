package layeredFA;

import entity.NonTerminal;
import entity.Production;
import entity.ValidSign;
import layeredFA.entities.FA_State;
import layeredFA.entities.LayeredFA;
import org.apache.log4j.Logger;
import utilities.ClosureRecursionHandler;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * 构造此文法的文法分析表
 */
public class ParsingTableConstructor {

    private static final Logger logger = Logger.getLogger(ParsingTableConstructor.class);

    /**
     * 此文法的所有起始产生式（indicator 为 0）
     */
    final private List<Production> productions;

    public ParsingTableConstructor(List<Production> productions) {
        if (isSingleStartLeft(productions)) {
            // 新增开始符 & ，统领全局
            NonTerminal newStartProLeft = new NonTerminal("&");

            List<ValidSign> newStartProRight = new LinkedList<>();
            newStartProRight.add(productions.get(0).getLeft());
            Production newStartPro = new Production(newStartProLeft, newStartProRight);
            productions.add(0, newStartPro);
        }

        this.productions = productions;
    }

    /**
     * 检查 productions 中是不是开始符只出现了一次
     */
    private boolean isSingleStartLeft(List<Production> productions) {
        NonTerminal startNT = productions.get(0).getLeft();
        String startSign = startNT.getRepresentation();

        int count = 0;
        for (Production p : productions) {
            if (p.getLeft().getRepresentation().equals(startSign)) count++;
        }

        return count == 1;
    }

    /**
     * 基于现有的文法产生式，生成对应的状态机
     */
    public LayeredFA parse() {

        List<Production> startProduction = new LinkedList<>();
        startProduction.add(productions.get(0));

        FA_State startState = new FA_State(startProduction);

        LayeredFA resultFA = new LayeredFA(startState, getAllValidSigns());

        // dStates为<闭包, 已标记>，LinkedHashMap保证为顺序而不是 hash 过的
        Map<FA_State, Boolean> dStates = new LinkedHashMap<>();
        dStates.put(innerStateExtension(resultFA.getStart()), false);

        // 清理当前节点计算 innerStateExtension 时的递归现场
        ClosureRecursionHandler.reset();

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
                FA_State curFollowing = move(unhandled, vs);
                int curFollowingSize = curFollowing.getProductions().size();

                if (curFollowingSize != 0) {
                    // 否则此状态此字符上无后继状态

                    // 保存当前要计算闭包的核
                    curFollowing = innerStateExtension(curFollowing);

                    // 清理当前节点计算 innerStateExtension 时的递归现场
                    ClosureRecursionHandler.reset();

                    // 排序后对比，判断此集合是都在dStates中
                    if (!isInDSates(dStates, curFollowing)) {
                        dStates.put(curFollowing, false);
                    }
                }
            }
        }

        return null;
    }

    /**
     * 计算当前节点的ε闭包 ε-innerStateExtension inner state extension 之后的节点
     */
    private FA_State innerStateExtension(FA_State nowState) {
        nowState.getProductions().addAll(closureProduction(nowState.getProductions()));
        return nowState;
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
     * 将此状态以 vs 后移
     */
    private FA_State move(FA_State cur, ValidSign vs) {
        String label = vs.getRepresentation();
        List<Production> resultProduction = new LinkedList<>();
        for (Production p : cur.getProductions()) {
            ValidSign nextSign = p.getRight().get(p.getIndicator());
            // null 表示此条表达式已到达末尾，不能再后继
            if (nextSign == null) continue;
            if (nextSign.getRepresentation().equals(label)) {
                resultProduction.add(p.moveForward());
            }
        }
        FA_State result = new FA_State(resultProduction);
        logger.info("move 后继中产生式个数：" + result.getProductions().size());
        for (Production p : resultProduction) {
            logger.info(p.toString());
        }
        return result;
    }

    /**
     * 判断 state 是否已经在 dStates 中了
     */
    private boolean isInDSates(Map<FA_State, Boolean> dStates, FA_State state) {
        for (Map.Entry<FA_State, Boolean> entry : dStates.entrySet()) {
            List<Production> curProductions = entry.getKey().getProductions();
            if (curProductions.size() == state.getProductions().size()) {
                List<Production> newList = new LinkedList<>();
                newList.addAll(state.getProductions());
                newList.removeAll(curProductions);

                // 找到已经存在的状态
                if (newList.size() == 0) return true;
            }
        }
        return false;
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

    /**
     * 获取所有的合法字符，相当于字母表
     */
    private List<ValidSign> getAllValidSigns() {
        List<ValidSign> result = new LinkedList<>();
        for (Production p : productions) {
            for (ValidSign vs : p.getRight()) {
                if (!result.contains(vs)) result.add(vs);
            }
        }
        return result;
    }
}
