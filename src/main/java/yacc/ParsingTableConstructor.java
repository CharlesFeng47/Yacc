package yacc;

import entities.NonTerminal;
import entities.Production;
import entities.Terminal;
import entities.ValidSign;
import exceptions.ParsingTableConflictException;
import layeredFA.FA_Constructor;
import layeredFA.entities.FA_Edge;
import layeredFA.entities.FA_State;
import layeredFA.entities.LayeredFA;
import org.apache.log4j.Logger;
import utilities.ActionType;
import utilities.FirstOrFollowCycle;
import yacc.entities.Action;
import yacc.entities.ParsingTable;

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
        this.firstMap = new LinkedHashMap<>();

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
                    List<Production> relatedProduction = getRelatedProductionWithLeft(nt);

                    // 检查该 FIRST 是否依赖于别的非终结符
                    boolean isDependent = false;
                    for (Production p : relatedProduction) {
                        if (p.getRight().get(0) instanceof NonTerminal) {
                            isDependent = true;
                            break;
                        }
                    }

                    if (isDependent) {
                        if (firstCycle(nt) != null) {
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
                            Terminal first = (Terminal) p.getRight().get(0);
                            if (!result.contains(first)) result.add(first);
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

                FirstOrFollowCycle cycle = firstCycle(nt);
                if (cycle != null) {
                    // 循环的需要进行处理
                    // handling 中的所有非终结符 FIRST 都相同
                    List<Terminal> result = cycle.getCycleValue();
                    for (NonTerminal temp : cycle.getCycleBody()) {
                        firstMap.put(temp.getRepresentation(), result);
                        needToHandle.remove(temp);
                    }
                }
            }
        }
    }

    /**
     * B = ∂Aß，计算 FOLLOW(B)
     */
    private void initFollowMap() {
        this.followMap = new LinkedHashMap<>();

        // 起始符的 FOLLOW  加入 $
        Terminal finalTerminal = new Terminal("$");
        List<Terminal> startNonTerminalFollow = new LinkedList<>();
        startNonTerminalFollow.add(finalTerminal);
        followMap.put(productions.get(0).getLeft().getRepresentation(), startNonTerminalFollow);

        // 计算所有的非终结符 FOLLOW
        List<NonTerminal> needToHandle = new LinkedList<>();
        for (ValidSign vs : validSigns) {
            if (vs instanceof NonTerminal) needToHandle.add((NonTerminal) vs);
        }

        while (true) {
            boolean hasNewHandledTerminalWithCycle = false;

            // 遍历每一个非终结符，每次都处理自己可以处理的（非循环的）
            while (true) {
                boolean hasNewHandledTerminalWithoutCycle = false;
                for (int i = 0; i < needToHandle.size(); i++) {

                    List<Terminal> result = new LinkedList<>();

                    NonTerminal nt = needToHandle.get(i);
                    List<Production> relatedProduction = getRelatedProductionWithRight(nt);

                    // 检查该 FOLLOW 是否依赖于别的非终结符
                    boolean isDependent = false;
                    for (Production p : relatedProduction) {
                        int ntIndex = containNT(p.getRight(), nt);
                        // 计算 FOLLOW 时后一次字符是非终结符即需要依赖
                        if (ntIndex != -1 && ntIndex == p.getRight().size() - 1
                                || p.getRight().get(ntIndex + 1) instanceof NonTerminal) {
                            isDependent = true;
                            break;
                        }
                    }

                    if (isDependent) {
                        if (followCycle(nt) != null) {
                            // 有循环，延后处理
                            continue;
                        }

                        // FOLLOW 依赖于别的非终结符，但是没有循环依赖，每次检查后都添加进去
                        for (Production p : relatedProduction) {
                            List<ValidSign> right = p.getRight();
                            int ntIndex = containNT(right, nt);

                            assert ntIndex != -1;
                            if (ntIndex == right.size() - 1) {
                                // ß = ε
                                List<Terminal> leftFollow = followMap.get(p.getLeft().getRepresentation());

                                if (leftFollow != null) {
                                    result.removeAll(leftFollow);
                                    result.addAll(leftFollow);
                                }
                            } else {
                                // ß != ε
                                boolean deriveToNull = false;
                                List<Terminal> nextFirst = new LinkedList<>();
                                nextFirst.addAll(firstMap.get(right.get(ntIndex + 1).getRepresentation()));
                                for (Terminal t : nextFirst) {
                                    if (t.getRepresentation().equals("ε")) deriveToNull = true;
                                }

                                if (deriveToNull) {
                                    // ß can derive ε

                                    // 移除 ε，得到 FIRST(A)-{ε}
                                    for (int j = 0; j < nextFirst.size(); j++) {
                                        if (nextFirst.get(j).getRepresentation().equals("ε")) {
                                            nextFirst.remove(j);
                                            break;
                                        }
                                    }
                                    result.removeAll(nextFirst);
                                    result.addAll(nextFirst);

                                    // FOLLOW(B)
                                    List<Terminal> leftFollow = followMap.get(p.getLeft().getRepresentation());
                                    if (leftFollow != null) {
                                        result.removeAll(leftFollow);
                                        result.addAll(leftFollow);
                                    }
                                } else {
                                    // ß cannot derive ε
                                    result.removeAll(nextFirst);
                                    result.addAll(nextFirst);
                                }

                            }
                        }

                        //  否则此次没有酸出，延后计算
                        if (result.size() > 0) {
                            hasNewHandledTerminalWithoutCycle = checkPreAndNowHasNew(result, nt) | hasNewHandledTerminalWithoutCycle;
                        }
                    } else {
                        // 直接计算
                        for (Production p : getRelatedProductionWithRight(nt)) {
                            int ntIndex = containNT(p.getRight(), nt);
                            Terminal follow = (Terminal) p.getRight().get(ntIndex + 1);
                            if (!result.contains(follow)) result.add(follow);
                        }

                        hasNewHandledTerminalWithoutCycle = checkPreAndNowHasNew(result, nt) | hasNewHandledTerminalWithoutCycle;
                    }
                }
                if (!hasNewHandledTerminalWithoutCycle) break;
            }

            // 处理循环的非终结符，找到循环的两点，中间所有经过的非终结符 FOLLOW 都相同
            for (int i = 0; i < needToHandle.size(); i++) {
                NonTerminal nt = needToHandle.get(i);

                FirstOrFollowCycle cycle = followCycle(nt);
                if (cycle != null) {
                    // 循环的需要进行处理
                    List<Terminal> result = cycle.getCycleValue();
                    for (NonTerminal temp : cycle.getCycleBody()) {
                        List<Terminal> curFollow = followMap.get(temp.getRepresentation());
                        if (curFollow != null) {
                            result.removeAll(curFollow);
                            result.addAll(curFollow);
                        }
                    }

                    hasNewHandledTerminalWithCycle = checkPreAndNowHasNew(result, nt) | hasNewHandledTerminalWithCycle;
                }
            }

            if (!hasNewHandledTerminalWithCycle) break;
        }
    }

    /**
     * 检查经过此次比对后，是否新增了元素
     */
    private boolean checkPreAndNowHasNew(List<Terminal> result, NonTerminal nt) {
        List<Terminal> preFollow = followMap.get(nt.getRepresentation());
        if (preFollow != null) {
            result.removeAll(preFollow);

            if (result.size() != 0) {
                // 新增了元素
                result.addAll(preFollow);
                followMap.put(nt.getRepresentation(), result);
                return true;
            } else {
                return false;
            }
        } else {
            followMap.put(nt.getRepresentation(), result);
            return true;
        }
    }

    /**
     * 检查非终结符 nt 在文法中是否存在 FIRST 循环依赖
     */
    private FirstOrFollowCycle firstCycle(NonTerminal nt) {
        List<NonTerminal> encounteredNT = new LinkedList<>();
        encounteredNT.add(nt);

        List<Terminal> cycleValue = new LinkedList<>();

        List<Production> toCheck = new LinkedList<>();
        toCheck.addAll(productions);

        // 控制 do while 循环跳出
        boolean hasNewNT;
        // 标识是否存在循环
        boolean hasCycle = false;

        do {
            hasNewNT = false;
            for (int i = 0; i < toCheck.size(); ) {
                Production p = toCheck.get(i);
                if (encounteredNT.contains(p.getLeft())) {
                    ValidSign rightFirst = p.getRight().get(0);
                    if (rightFirst instanceof NonTerminal) {
                        if (encounteredNT.contains(rightFirst)) {
                            // 已包含，即存在循环
                            hasCycle = true;
                        } else {
                            // 不包含，即为可以产生的，即加入 encountered
                            encounteredNT.add((NonTerminal) rightFirst);
                            hasNewNT = true;
                            toCheck.remove(p);
                            continue;
                        }
                    } else if (rightFirst instanceof Terminal) {
                        if (!cycleValue.contains(rightFirst)) cycleValue.add((Terminal) rightFirst);
                    }
                }
                i++;
            }
        } while (hasNewNT);

        if (hasCycle) return new FirstOrFollowCycle(encounteredNT, cycleValue);
        else return null;
    }

    /**
     * 检查非终结符 nt 在文法中是否存在 FELLOW 循环依赖，存在则返回依赖循环，否则返回空值
     */
    private FirstOrFollowCycle followCycle(NonTerminal nt) {
        List<NonTerminal> encounteredNT = new LinkedList<>();
        encounteredNT.add(nt);

        List<Terminal> cycleValue = new LinkedList<>();

        List<Production> toCheck = new LinkedList<>();
        toCheck.addAll(productions);

        // 控制 do while 循环跳出
        boolean hasNewNT;
        // 标识是否存在循环
        boolean hasCycle = false;

        do {
            hasNewNT = false;
            for (int i = 0; i < toCheck.size(); ) {
                Production p = toCheck.get(i);
                List<ValidSign> right = p.getRight();

                int ntIndex = right.indexOf(nt);
                if (ntIndex != -1) {
                    // ß = ε 或 ε belongs to FIRST(ß) 时，需要计算 FOLLOW(A)

                    boolean deriveToNull = false;
                    if (ntIndex < right.size() - 1) {
                        ValidSign next = right.get(ntIndex + 1);
                        if (next instanceof NonTerminal) {
                            List<Terminal> nextFirst = firstMap.get(right.get(ntIndex + 1).getRepresentation());
                            for (Terminal t : nextFirst) {
                                if (t.getRepresentation().equals("ε")) deriveToNull = true;
                            }
                        } else if (next instanceof Terminal) {
                            if (!cycleValue.contains(next)) cycleValue.add((Terminal) next);
                        }
                    }

                    if ((ntIndex == right.size() - 1) || deriveToNull) {
                        if (encounteredNT.contains(p.getLeft())) {
                            // 包含，即存在循环
                            hasCycle = true;
                        } else {
                            // 不包含，加入检测
                            encounteredNT.add(p.getLeft());
                            hasNewNT = true;
                            toCheck.remove(p);
                            continue;
                        }
                    }
                }
                i++;
            }
        } while (hasNewNT);

        if (hasCycle) return new FirstOrFollowCycle(encounteredNT, cycleValue);
        else return null;
    }

    /**
     * 找寻以该非终结符 nonTerminal 开头的所有产生式
     */
    private List<Production> getRelatedProductionWithLeft(NonTerminal nonTerminal) {
        List<Production> result = new LinkedList<>();
        for (Production p : productions) {
            if (p.getLeft().equals(nonTerminal)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * 找寻右部有该非终结符 nonTerminal 的所有产生式
     */
    private List<Production> getRelatedProductionWithRight(NonTerminal nonTerminal) {
        List<Production> result = new LinkedList<>();
        for (Production p : productions) {
            if (p.getRight().contains(nonTerminal)) {
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
                    List<Terminal> follow = followMap.get(p.getLeft().getRepresentation());

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