package lexicalAnalyzer.finiteAutomata;

import lexicalAnalyzer.finiteAutomata.entity.*;
import org.apache.log4j.Logger;
import lexicalAnalyzer.utilities.*;

import java.util.*;

/**
 * Created by cuihua on 2017/10/27.
 * <p>
 * 对 NFA 进行处理
 * NFA => DFA
 * optimize DFA
 */
public class DFA_Handler {

    private static Logger logger = Logger.getLogger(DFA_Handler.class);

    private static FA_StateComparator comparator = new FA_StateComparator();

    public DFA_Handler() {
    }

    /**
     * @param nfa 需要转变的NFA
     * @return 与输入NFA一致的DFA
     */
    public DFA getFromNFA(final NFA nfa) {
        List<DTran> dTrans = new LinkedList<>();

        // dStates为<闭包, 已标记>，LinkedHashMap保证为顺序而不是 hash 过的
        Map<List<FA_State>, Boolean> dStates = new LinkedHashMap<>();
        dStates.put(closure(nfa.getStart()), false);

        // 清理当前节点计算 closure 时的递归现场
        ClosureRecursionHandler.reset();

        while (true) {
            // dStates中是否还有未标记的状态，并对未标记的状态进行处理
            boolean hasStopped = true;
            List<FA_State> unhandled = null;
            for (Map.Entry<List<FA_State>, Boolean> entry : dStates.entrySet()) {
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
            for (char c : nfa.getAlphabet()) {
                List<FA_State> curFollowing = move(unhandled, c);
                int curFollowingSize = curFollowing.size();

                if (curFollowingSize != 0) {
                    // 否则此等价状态在此字符上无后继状态，标记为空

                    // 保存当前要计算闭包的核
                    List<FA_State> curFollowingClosure = new FA_StatesList();
                    curFollowingClosure.addAll(curFollowing);


                    // 遍历后继的核，得到核的闭包
                    for (FA_State tempState : curFollowing) {
                        List<FA_State> tempClosure = closure(tempState);

                        // 清理当前节点计算 closure 时的递归现场
                        ClosureRecursionHandler.reset();

                        // 在 curFollowingClosure 中加入所有 tempClosure 没有的元素
                        curFollowingClosure.removeAll(tempClosure);
                        curFollowingClosure.addAll(tempClosure);
                    }

                    // 排序后对比，判断此集合是都在dStates中
                    curFollowingClosure.sort(comparator);
                    if (!isInDSates(dStates, curFollowingClosure)) {
                        dStates.put(curFollowingClosure, false);
                    }

                    // 标记dTrans转换表
                    dTrans.add(new DTran(unhandled, curFollowingClosure, c));
                }
            }
        }

        // 打印 DFA 的状态对应表
        logger.info("NFA => DFA 子集构造法结束");
        for (DTran dTran : dTrans) {
            dTran.show();
        }

        return getEquivalentDFA(nfa, dStates, dTrans);
    }

    /**
     * 计算当前节点的ε闭包 ε-closure
     */
    private List<FA_State> closure(FA_State nowState) {
        List<FA_State> result = new FA_StatesList();
        result.add(nowState);
        ClosureRecursionHandler.addState(nowState);

        // 遍历当前节点的每一个后续节点
        for (FA_Edge tempEdge : nowState.getFollows()) {
            if (tempEdge.getLabel() == 'ε') {
                // 若递归 closure 结果集中不包含此节点，则将此节点加入结果集
                FA_State nextState = tempEdge.getPointTo();
                if (!ClosureRecursionHandler.contain(nextState)) {
                    List<FA_State> temp = closure(nextState);
                    result.addAll(temp);
                }
            }
        }

        result.sort(comparator);
        return result;
    }

    /**
     * 将此状态以label后移
     */
    private List<FA_State> move(List<FA_State> cur, char label) {
        List<FA_State> result = new FA_StatesList();

        for (FA_State tempState : cur) {
            for (FA_Edge tempEdge : tempState.getFollows()) {
                if (tempEdge.getLabel() == label) {
                    result.add(tempEdge.getPointTo());
                }
            }
        }

        result.sort(comparator);
        return result;
    }

    /**
     * 判断states是否已经在DSates中了
     */
    private boolean isInDSates(Map<List<FA_State>, Boolean> DStates, List<FA_State> states) {
        for (Map.Entry<List<FA_State>, Boolean> entry : DStates.entrySet()) {
            List<FA_State> keyStates = entry.getKey();
            if (keyStates.size() == states.size()) {
                boolean allEqual = true;
                for (int i = 0; i < states.size(); i++) {
                    if (states.get(i).getStateID() != keyStates.get(i).getStateID()) allEqual = false;
                }

                // 找到已经存在的状态
                if (allEqual) return true;
            }
        }
        return false;
    }

    /**
     * 判断toTest是否与pre有交集
     * 有交集，现等价状态即为现DFA的终止态
     */
    private boolean isTerminatedState(final List<FA_State> pre, final List<FA_State> toTest) {
        // 取交集无并集
        // 深度拷贝复制 toTest，保证 retainAll 之后 toTest 不会被修改
        List<FA_State> newList = new FA_StatesList();
        newList.addAll(toTest);
        newList.retainAll(pre);

        return newList.size() != 0;
    }

    /**
     * @param nfa 原 NFA
     * @return 通过子集构造法构建的等价的简单 DFA
     */
    private DFA getEquivalentDFA(NFA nfa, Map<List<FA_State>, Boolean> dStates, List<DTran> dTrans) {
        // 子集构造法结束，根据dStates、dTrans构造相对应的DFA（dStates从后往前即为现等价状态的产生顺序）
        // pre代表原NFA，cur代表对应的DFA
        List<FA_State> preTerminatedStates = nfa.getTerminatedStates();

        List<FA_State> curStates = new FA_StatesList();
        List<FA_State> curTerminatedStates = new FA_StatesList();

        // 标记子集构造法中形成的等价节点和现在简化的节点之间的映射
        Map<List<FA_State>, FA_State> faStatesConvertTable = new LinkedHashMap<>();

        // dStates 顺序压入，重新更换为简单 FA_State 也是顺序
        int curIndex = 0;
        for (List<FA_State> nowConvertedNFAStates : dStates.keySet()) {
            FA_State equivalentState = new FA_State(curIndex);
            curStates.add(equivalentState);
            curIndex++;

            faStatesConvertTable.put(nowConvertedNFAStates, equivalentState);

            // 含有原 NFA 终止态的即为现终止态
            if (isTerminatedState(preTerminatedStates, nowConvertedNFAStates)) {
                curTerminatedStates.add(equivalentState);
            }
        }

        // 把 dTrans 上的连接加入现在 DFA，并存入 DFA 成员变量 move
        Map<FA_State, Map<Character, FA_State>> move = new LinkedHashMap<>();
        for (DTran dTran : dTrans) {
            FA_State curStart = faStatesConvertTable.get(dTran.getFrom());
            FA_State curTo = faStatesConvertTable.get(dTran.getTo());
            char label = dTran.getLabel();

            FA_Edge curEdge = new FA_Edge(label, curTo);
            curStart.getFollows().add(curEdge);

            Map<Character, FA_State> curMove = move.get(curStart);
            if (curMove != null) {
                curMove.put(label, curTo);
            } else {
                curMove = new HashMap<>();
                curMove.put(label, curTo);
                move.put(curStart, curMove);
            }
        }

        // 打印真正 DFA 的状态对应表
        logger.debug("NFA 经过子集构造法完成后真正的状态转换表");
        showDFATrans(move);

        curStates.sort(comparator);
        curTerminatedStates.sort(comparator);

        DFA dfa = new DFA();
        dfa.setStart(curStates.get(0));
        dfa.setAlphabet(nfa.getAlphabet());
        dfa.setStates(curStates);
        dfa.setTerminatedStates(curTerminatedStates);
        dfa.setMove(move);

        // 将原 NFA 对应的模式 pattern 加入现在的 DFA 映射
        DFA_StatePatternMappingController.add(dfa, NFA_StatePatternMappingController.getMap().get(nfa));
        return dfa;
    }


    /**
     * @param dfa 需要被优化的DFA
     * @return 具有最少状态的DFA
     */
    public DFA optimize(DFA dfa) {
        List<FA_State> nonTerminatedStates = new FA_StatesList();
        List<FA_State> terminatedStates = dfa.getTerminatedStates();
        List<Character> alphabet = dfa.getAlphabet();

        // 构造初始两个集合 终结状态／非终结状态
        nonTerminatedStates.addAll(dfa.getStates());
        nonTerminatedStates.removeAll(terminatedStates);

        nonTerminatedStates.sort(comparator);
        terminatedStates.sort(comparator);

        // 第一次分的这两个集合手动排序，让程序先处理非终结状态
        List<FA_Node> nodes = new FA_NodesList();

        if (nonTerminatedStates.size() > 0) {
            // 只有终结态
            FA_Node node1 = new FA_Node(nonTerminatedStates);
            nodes.add(node1);
        }
        FA_Node node2 = new FA_Node(terminatedStates);
        nodes.add(node2);

        // while 循环保证算法的 traceBacking 回头看
        while (true) {
            // 所有叶节点内部的 FA_State 都是等价的
            boolean isWeakEqual = true;
            for (int i = 0; i < nodes.size(); i++) {

                // 节点中只有一个状态，已是最少，无需再进行分化此节点
                if (nodes.get(i).getStates().size() == 1) {
                    continue;
                }

                // 子集分化
                for (int j = 0; j < alphabet.size(); ) {

                    char c = alphabet.get(j);
                    List<FA_Node> tempResult = optimizeOneNodeOneChar(dfa, nodes, nodes.get(i), c);

                    nodes.remove(i);
                    nodes.addAll(i, tempResult);

                    if (tempResult.size() > 1) {
                        // 发生了子集替换，重新遍历每个 label
                        isWeakEqual = false;
                        j = 0;
                    } else {
                        j++;
                    }
                }

            }

            // 全都弱等价，结束算法
            if (isWeakEqual) break;
        }

        // 重构 DFA
        return reconstruction(dfa, nodes);
    }

    /**
     * 在特定字母下子集分化一个 FA_Node 节点
     *
     * @param dfa         当前 DFA
     * @param curDivision 目前的分化
     * @param node        要优化的叶节点
     * @param c           分化基于的条件
     */
    private List<FA_Node> optimizeOneNodeOneChar(final DFA dfa, List<FA_Node> curDivision, FA_Node node, char c) {
        List<FA_Node> result = new FA_NodesList();

        if (node.getStates().size() == 1) {
            // 节点中只有一个状态，已是最少，无需再进行分化此节点
            result.add(node);
            return result;
        }

        // 在此 label 下分别无后继分化、有后继分化
        List<FA_State> parentToNull = new FA_StatesList();
        Map<FA_State, FA_State> parentToSon = new HashMap<>();

        for (FA_State parentState : node.getStates()) {
            // 该节点在该映射条件下的后继
            Map<Character, FA_State> curEdges = dfa.getMove().get(parentState);
            if (curEdges != null) {
                FA_State sonState = curEdges.get(c);
                if (sonState != null) {
                    // 有后继边且后继边中有 label 为 c 的边
                    parentToSon.put(parentState, sonState);
                } else {
                    parentToNull.add(parentState);
                }
            } else {
                parentToNull.add(parentState);
            }
        }

        if (parentToNull.size() != 0) {
            parentToNull.sort(comparator);
            result.add(new FA_Node(parentToNull));
        }

        if (parentToSon.size() != 0) {
            // 判断 following 是不是在同一叶节点中（FA_Node 为此次判断中原来的Node，List<FA_State> 为此 Node 下的父节点）
            Map<FA_Node, List<FA_State>> judge = new HashMap<>();
            for (Map.Entry<FA_State, FA_State> entry : parentToSon.entrySet()) {
                FA_State sonState = entry.getValue();
                FA_Node belongingNode = getBelongingNode(curDivision, sonState);
                if (judge.get(belongingNode) == null) {
                    List<FA_State> temp = new FA_StatesList();
                    temp.add(entry.getKey());
                    judge.put(belongingNode, temp);
                } else {
                    judge.get(belongingNode).add(entry.getKey());
                }
            }

            if (judge.size() > 1) {
                // 形成了不同的分化
                for (List<FA_State> states : judge.values()) {
                    states.sort(comparator);
                    result.add(new FA_Node(states));
                }
            } else {
                // parentToSon 不形成新分化
                List<FA_State> states = new FA_StatesList(parentToSon.keySet());
                states.sort(comparator);
                result.add(new FA_Node(states));
            }
        }

        return result;
    }

    /**
     * 找到当前状态所在的节点
     */
    private FA_Node getBelongingNode(List<FA_Node> curDivision, FA_State state) {
        for (FA_Node node : curDivision) {
            if (node.getStates().contains(state)) return node;
        }
        return null;
    }

    /**
     * 根据子集分化的算法结果，对 DFA 重的等价状态进行合并
     */
    private DFA reconstruction(DFA dfa, List<FA_Node> nodes) {
        // <被删除的状态节点, 用于替换的状态节点>
        Map<FA_State, FA_State> deleteTran = new HashMap<>();
        for (FA_Node node : nodes) {
            // 只需要第一个状态作为代表，从后面向前记录要删除的节点
            List<FA_State> division = node.getStates();
            for (int i = 1; i < division.size(); i++) {
                deleteTran.put(division.get(i), division.get(0));
            }
        }

        // 移除这些状态
        List<FA_State> needDeleteStates = new FA_StatesList(deleteTran.keySet());
        needDeleteStates.sort(comparator);

        // 转移链接关系
        // 需移除节点 指向 其他节点
        for (FA_State state : needDeleteStates) {
            dfa.getMove().remove(state);
        }

        // 其他节点 指向 需移除节点
        Map<FA_State, Map<Character, FA_State>> newMove = new LinkedHashMap<>();
        for (Map.Entry<FA_State, Map<Character, FA_State>> curMove : dfa.getMove().entrySet()) {
            FA_State curStart = curMove.getKey();

            if (newMove.get(curStart) == null) {
                Map<Character, FA_State> edges = new LinkedHashMap<>();
                newMove.put(curStart, edges);
            }

            // 转换表
            for (Map.Entry<Character, FA_State> curEdge : curMove.getValue().entrySet()) {
                char label = curEdge.getKey();
                FA_State deleteState = curEdge.getValue();
                if (needDeleteStates.contains(curEdge.getValue())) {
                    newMove.get(curStart).put(label, deleteTran.get(deleteState));
                } else {
                    newMove.get(curStart).put(label, deleteState);
                }
            }

            // 状态链接
            for (FA_Edge curEdge : curStart.getFollows()) {
                if (needDeleteStates.contains(curEdge.getPointTo())) {
                    curEdge.setPointTo(deleteTran.get(curEdge.getPointTo()));
                }
            }
        }

        dfa.getStates().removeAll(needDeleteStates);
        dfa.getTerminatedStates().removeAll(needDeleteStates);
        dfa.setMove(newMove);

        // 如果删除了初始节点
        if (needDeleteStates.contains(dfa.getStart())) {
            dfa.setStart(deleteTran.get(dfa.getStart()));
        }

        logger.info("DFA 已经优化为最少数目");
        showDFATrans(dfa.getMove());
        return dfa;
    }

    /**
     * 输出 NFA 的转换信息到控制台
     */
    private void showDFATrans(Map<FA_State, Map<Character, FA_State>> move) {
        for (Map.Entry<FA_State, Map<Character, FA_State>> entryState : move.entrySet()) {
            FA_State start = entryState.getKey();
            for (Map.Entry<Character, FA_State> entryEdge : entryState.getValue().entrySet()) {
                logger.debug(start.getStateID() + " through " + entryEdge.getKey() + " to " + entryEdge.getValue().getStateID());
            }
        }
    }
}
