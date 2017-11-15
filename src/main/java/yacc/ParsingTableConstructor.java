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

import java.util.HashMap;
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
    private final List<ValidSign> validSigns;

    /**
     * ValidSign 的 String 形式对应其 FIRST 集合
     */
    private Map<String, List<Terminal>> firstMap;

    /**
     * 非终结符号与其 FOLLOW 集合
     */
    private Map<String, List<Terminal>> followMap;


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

    // TODO S->A->A，未测试
    private void initFirstMap() {
        this.firstMap = new HashMap<>();

        // 计算所有的终结符 FIRST，并将所有非终结符加入集合 needToHandle
        List<NonTerminal> needToHandle = new LinkedList<>();
        for (ValidSign vs : validSigns) {
            if (vs instanceof Terminal) {
                List<Terminal> result = new LinkedList<>();
                result.add((Terminal) vs);
                firstMap.put(vs.getRepresentation(), result);
            } else if (vs instanceof NonTerminal) {
                needToHandle.add((NonTerminal) vs);
            }
        }

        while (true) {
            if (needToHandle.size() == 0 && firstMap.size() == validSigns.size()) {
                break;
            }

            // 遍历每一个非终结符，每次都处理自己可以处理的（非循环的）
            while (true) {
                boolean noNewHandledNonTerminal = true;

                for (int i = 0; i < needToHandle.size(); ) {
                    List<Terminal> result = new LinkedList<>();

                    NonTerminal nt = needToHandle.get(i);
                    List<Production> relatedProduction = getRelatedProduction(nt);

                    // 检查该 FIRST 是否依赖于别的非终结符
                    boolean isDependent = false;
                    for (Production p : relatedProduction) {
                        if (p.getRight().get(0) instanceof NonTerminal) {
                            isDependent = true;
                            break;
                        }
                    }

                    if (isDependent) {
                        if (hasCycle(nt)) {
                            // 循环依赖，延后处理
                            i++;
                            continue;
                        }

                        // FIRST 依赖于别的非终结符，但不是循环依赖
                        for (Production p : relatedProduction) {
                            int toCheckIndex = 0;
                            ValidSign rightToCheckVS;

                            // 依次检查此字符是否可以推导出 ε
                            boolean boolCanDeriveToNull = false;
                            do {
                                rightToCheckVS = p.getRight().get(toCheckIndex);
                                if (rightToCheckVS instanceof NonTerminal) {
                                    List<Terminal> rightToCheckVSFollow = firstMap.get(rightToCheckVS.getRepresentation());

                                    if (rightToCheckVSFollow != null) {
                                        // 为空表示现在还不能计算非循环的依赖
                                        result.removeAll(rightToCheckVSFollow);
                                        result.addAll(rightToCheckVSFollow);

                                        // 根据能不能推出 ε ，决定要不要继续向后推，加入 FIRST
                                        boolCanDeriveToNull = canDeriveToNull(rightToCheckVS);
                                        if (boolCanDeriveToNull) toCheckIndex++;
                                    }
                                } else if (rightToCheckVS instanceof Terminal) {
                                    result.remove(rightToCheckVS);
                                    result.add((Terminal) rightToCheckVS);
                                }
                            } while (boolCanDeriveToNull);
                        }

                        if (result.size() > 0) {
                            firstMap.put(nt.getRepresentation(), result);
                            needToHandle.remove(nt);
                            noNewHandledNonTerminal = false;
                        } else {
                            // 延后计算
                            i++;
                        }
                    } else {
                        // FIRST 不依赖于别的非终结符，只依赖于终结符，直接处理
                        for (Production p : relatedProduction) {
                            if (!result.contains(p.getRight().get(0))) result.add((Terminal) p.getRight().get(0));
                        }
                        firstMap.put(nt.getRepresentation(), result);
                        needToHandle.remove(nt);
                        noNewHandledNonTerminal = false;
                    }
                }

                if (noNewHandledNonTerminal) break;
            }

            // 处理循环的非终结符，找到循环的两点，中间所有经过的非终结符 FIRST 都相同
            for (int i = 0; i < needToHandle.size(); ) {
                NonTerminal nt = needToHandle.get(i);

                List<Terminal> result = new LinkedList<>();
                List<NonTerminal> handling = new LinkedList<>();
                handling.add(nt);
                boolean noNewNonTerminal;
                do {
                    noNewNonTerminal = true;

                    List<Production> related = new LinkedList<>();
                    for (NonTerminal temp : handling) {
                        List<Production> curRelated = getRelatedProduction(temp);
                        related.removeAll(curRelated);
                        related.addAll(curRelated);
                    }

                    for (Production p : related) {
                        ValidSign rightFirst = p.getRight().get(0);
                        if (rightFirst instanceof Terminal) {
                            if (!result.contains(rightFirst)) result.add((Terminal) rightFirst);
                        } else if (rightFirst instanceof NonTerminal) {
                            if (!handling.contains(rightFirst)) {
                                handling.add((NonTerminal) rightFirst);
                                noNewNonTerminal = false;
                            }
                        }
                    }
                } while (!noNewNonTerminal);

                // handling 中的所有非终结符 FIRST 都相同
                for (NonTerminal temp : handling) {
                    firstMap.put(temp.getRepresentation(), result);
                    needToHandle.remove(temp);
                }
            }
        }
    }

    private void initFollowMap() {
        this.followMap = new HashMap<>();

        // 起始符的 FOLLOW  加入 $
        Terminal finalTerminal = new Terminal("$");
        List<Terminal> startNonTerminalFollow = new LinkedList<>();
        startNonTerminalFollow.add(finalTerminal);
        followMap.put(productions.get(0).getLeft().getRepresentation(), startNonTerminalFollow);

        // 计算所有的非终结符 FOLLOW
        for (ValidSign vs : validSigns) {
            if (vs instanceof NonTerminal) {
                List<Terminal> result = new LinkedList<>();
                for (Production p : getRelatedProduction((NonTerminal) vs)) {
                    List<ValidSign> right = p.getRight();
                    int index = containNT(right, (NonTerminal) vs);
                    if (index != -1) {
                        if (index == right.size() - 1) {
                            // ß = ε
                            List<Terminal> followA = getFollow((NonTerminal) right.get(index));
                            result.removeAll(followA);
                            result.addAll(followA);
                        } else {
                            // ß != ε
                            if (canDeriveToNull(right.get(index + 1))) {
                                // ß can derive ε

                            } else {
                                // ß cannot derive ε
                                List<Terminal> firstA = firstMap.get(vs.getRepresentation());
                                result.removeAll(firstA);
                                result.addAll(firstA);

                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * 检查非终结符 nt 在文法中是否存在循环依赖
     */
    private boolean hasCycle(NonTerminal nt) {
        List<NonTerminal> encountered = new LinkedList<>();
        encountered.add(nt);

        List<Production> toCheck = new LinkedList<>();
        toCheck.addAll(productions);
        boolean hasNewNT;
        do {
            hasNewNT = false;
            for (int i = 0; i < toCheck.size(); ) {
                Production p = toCheck.get(i);
                if (encountered.contains(p.getLeft())) {
                    ValidSign vs = p.getRight().get(0);
                    if (vs instanceof NonTerminal) {
                        if (encountered.contains(vs)) {
                            // 已包含，及存在循环
                            return true;
                        } else {
                            // 不包含，即为可以产生的，即加入 encountered
                            encountered.add((NonTerminal) vs);
                            hasNewNT = true;
                            toCheck.remove(p);
                            continue;
                        }
                    }
                }
                i++;
            }
        } while (hasNewNT);

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
     * @return 非终结符 nt 在 right 中，则返回 nt 在其中的序号，找不到则返回 -1
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
    private boolean canDeriveToNull(ValidSign vs) {
        for (Production production : productions) {
            List<ValidSign> right = production.getRight();
            if (production.getLeft().getRepresentation().equals(vs.getRepresentation())
                    && right.get(0).getRepresentation().equals("ε")) return true;
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