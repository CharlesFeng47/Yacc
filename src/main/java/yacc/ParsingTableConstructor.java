package yacc;

import entity.NonTerminal;
import entity.Production;
import entity.Terminal;
import entity.ValidSign;
import exceptions.ParsingTableConflictException;
import layeredFA.FA_Constructor;
import layeredFA.entities.FA_Edge;
import layeredFA.entities.FA_State;
import layeredFA.entities.LayeredFA;
import org.apache.log4j.Logger;
import utilities.ActionType;
import yacc.entities.Action;
import yacc.entities.ParsingTable;

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
    private final List<Production> productions;

    /**
     * 此文法的所有起始产生式 String，不含 indicator
     */
    private final List<String> simpleProductions;

    /**
     * 所有的合法标记
     */
    private List<ValidSign> validSigns;

    /**
     * ValidSign 的 String 形式对应其 FIRST 集合
     */
    private Map<String, List<Terminal>> firstMap;

    /**
     * 非终结符号与其 FOLLOW 集合
     */
    private Map<NonTerminal, List<Terminal>> followMap;


    public ParsingTableConstructor(List<Production> productions) {
        if (!isSingleStartLeft(productions)) {
            // 新增开始符 & ，统领全局
            NonTerminal newStartProLeft = new NonTerminal("&");

            List<ValidSign> newStartProRight = new LinkedList<>();
            newStartProRight.add(productions.get(0).getLeft());
            Production newStartPro = new Production(newStartProLeft, newStartProRight);
            productions.add(0, newStartPro);
        }

        this.productions = productions;

        simpleProductions = new LinkedList<>();
        for (Production p : this.productions) {
            simpleProductions.add(p.toSimpleString());
        }

        validSigns = getAllValidSigns();
        initMap();
    }

    /**
     * 初始化 FIRST FOLLOW 集合
     */
    private void initMap() {
        initFirstMap();
        initFollowMap();
    }

    private void initFirstMap() {
        // 计算所有的终结符 FIRST
        for (ValidSign vs : validSigns) {
            if (vs instanceof Terminal) {
                List<Terminal> result = new LinkedList<>();
                result.add((Terminal) vs);
                firstMap.put(vs.getRepresentation(), result);
            }
        }

        // 计算所有的非终结符 FIRST
        List<NonTerminal> needToHandle = new LinkedList<>();
        for (ValidSign vs : validSigns) {
            if (vs instanceof NonTerminal) {
                needToHandle.add((NonTerminal) vs);
            }
        }
        while (true) {
            if (needToHandle.size() == 0 && firstMap.size() == validSigns.size()) {
                break;
            }

            // 遍历每一个非终结符，每次都处理自己可以处理的
            // TODO 需解决循环 FIRST
            for (NonTerminal nt : needToHandle) {
                List<Production> relatedProduction = getRelatedProduction(nt);

                boolean isIndependent = true;
                for (Production p : relatedProduction) {
                    ValidSign rightFirst = p.getRight().get(0);
                    if (rightFirst instanceof NonTerminal && firstMap.get(rightFirst.getRepresentation()) == null) {
                        // 需要依赖于别的 FIRST，之后再处理
                        isIndependent = false;
                        break;
                    }
                }

                if (!isIndependent) continue;

                // 不依赖于别的 FIRST，直接处理
                List<Terminal> result = new LinkedList<>();
                for (Production p : relatedProduction) {
                    result.add((Terminal) p.getRight().get(0));
                }
                firstMap.put(nt.getRepresentation(), result);
            }

        }
    }

    private void initFollowMap() {
        // 起始符的 FOLLOW  加入 $

        // 计算所有的非终结符 FOLLOW
//        for (:
//             ) {
//
//        }
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
        logger.debug("ValidSigns Size：" + result.size());
        return result;
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
     * 基于使用的产生式生成预测分析表
     */
    public ParsingTable getParsingTable() throws ParsingTableConflictException {
        FA_Constructor faConstructor = new FA_Constructor(productions, validSigns);
        LayeredFA fa = faConstructor.parse();

        ParsingTable pt = new ParsingTable(fa);

        // 根据状态间填写非终结符的 ACTION SHIFT 和终结符的 GOTO
        for (FA_State state : fa.getStates()) {
            for (FA_Edge edge : state.getFollows()) {
                ValidSign vs = edge.getLabel();
                if (vs instanceof NonTerminal) {
                    // GOTO
                    Map<NonTerminal, Integer> curGotoMap = pt.getGotoMap().get(state.getStateID());
                    curGotoMap.put((NonTerminal) vs, edge.getPointTo().getStateID());
                } else if (vs instanceof Terminal) {
                    // ACTION SHIFT
                    Map<Terminal, Action> curActionMap = pt.getActionMap().get(state.getStateID());
                    curActionMap.put((Terminal) vs, new Action(ActionType.SHIFT, edge.getPointTo().getStateID()));
                }
            }
        }


        // 根据状态内的可归约项归约产生式，填写非终结符的 ACTION REDUCTION
        for (FA_State state : fa.getStates()) {
            for (Production p : state.getProductions()) {
                // 检查当前产生式是否可归约
                if (isReducible(p)) {
                    // 可归约则计算 FOLLOW
                    List<Terminal> follow = getFollow(p.getLeft());

                    // 将 FOLLOW 中的终结符填入 ACTION REDUCTION
                    for (Terminal t : follow) {
                        Action curAction = new Action(ActionType.REDUCTION, getProductionNum(p));

                        Map<Terminal, Action> curActionMap = pt.getActionMap().get(state.getStateID());
                        Action preAction = curActionMap.get(t);
                        if (preAction != null) throw new ParsingTableConflictException(preAction, curAction);
                        else curActionMap.put(t, curAction);
                    }
                }
            }
        }

        return pt;
    }

    /**
     * 计算 production 是否可归约
     */
    private boolean isReducible(Production production) {
        return production.getIndicator() == production.getRight().size();
    }

    /**
     * B = ∂Aß，计算 FOLLOW(B)
     */
    private List<Terminal> getFollow(NonTerminal nt) {
        List<Terminal> result = new LinkedList<>();
        for (Production p : productions) {
            List<ValidSign> right = p.getRight();
            int index = containNT(right, nt);
            if (index != -1) {
                if (index == right.size() - 1) {
                    // ß = ε
                    // TODO 应该不可以这样比较，需要展开比String
                    List<Terminal> followA = getFollow((NonTerminal) right.get(index));
                    result.removeAll(followA);
                    result.addAll(followA);
                } else {
                    // ß != ε
                    if (deriveToNull(right.get(index + 1))) {
                        // ß can derive ε

                    } else {
                        // ß cannot derive ε
                        List<Terminal> firstA = getFirst(nt);
                        result.removeAll(firstA);
                        result.addAll(firstA);

                    }

                }
            }
        }
        return result;
    }

    /**
     * 计算 FIRST(B)
     */
    private List<Terminal> getFirst(NonTerminal nt) {
        List<Terminal> result = new LinkedList<>();
        for (Production p : productions) {

        }

        return result;
    }

    /**
     * @return nt 在 right 中，则返回 nt 在其中的序号，找不到则返回 -1
     */
    private int containNT(List<ValidSign> right, NonTerminal nt) {
        for (int i = 0; i < right.size(); i++) {
            ValidSign vs = right.get(i);
            if (vs.getRepresentation().equals(nt.getRepresentation())) return i;
        }
        return -1;
    }

    /**
     * @return 检验 vs -> ε 是否存在
     */
    private boolean deriveToNull(ValidSign vs) {
        for (Production production : productions) {
            List<ValidSign> right = production.getRight();
            if (right.size() == 1 && right.get(0).getRepresentation().equals("ε")) return true;
        }
        return false;
    }

    /**
     * 计算产生式的序号
     */
    private int getProductionNum(Production production) {
        String pString = production.toSimpleString();
        for (int i = 0; i < simpleProductions.size(); i++) {
            if (simpleProductions.get(i).equals(pString)) return i;
        }
        return -1;
    }

}