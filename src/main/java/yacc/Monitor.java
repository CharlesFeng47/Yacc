package yacc;

import entities.NonTerminal;
import entities.Production;
import entities.Terminal;
import entities.ValidSign;
import exceptions.SyntaxException;
import utilities.ActionType;
import yacc.entities.Action;
import yacc.entities.ParsingTable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by cuihua on 2017/11/16.
 * <p>
 * 用模拟器表示 SLR 的文法分析
 */
class Monitor {

    /**
     * 文法的产生式，在模拟器中用于查询被归约的产生式
     */
    private List<Production> productions;

    /**
     * 文法分析表
     */
    private ParsingTable pt;

    Monitor(List<Production> productions, ParsingTable pt) {
        this.productions = productions;
        this.pt = pt;
    }

    /**
     * 对输入 input 进行词法分析
     */
    List<Action> syntaxAnalyze(final List<Terminal> input) throws SyntaxException {
        List<Action> result = new LinkedList<>();

        Stack<Integer> stateStack = new Stack<>();
        Stack<ValidSign> symbolStack = new Stack<>();

        stateStack.push(0);
        symbolStack.push(input.get(0));

        // 已压入了 $
        int readerIndex = 1;
        while (true) {
            assert readerIndex < input.size();
            Terminal t = input.get(readerIndex);
            int curState = stateStack.peek();
            Action curAction = pt.getActionMap().get(curState).get(t);

            if (curAction == null) {
                throw new SyntaxException("ERROR 文法分析表中对应表格为空，文法分析错误。");
            } else {
                result.add(curAction);

                // 移入
                if (curAction.getType() == ActionType.SHIFT) {
                    stateStack.push(curAction.getOperand());
                    symbolStack.push(t);
                    readerIndex++;
                }

                // 归约
                else if (curAction.getType() == ActionType.REDUCTION) {
                    int reduceProductionIndex = curAction.getOperand();
                    Production reduceProduction = productions.get(reduceProductionIndex);

                    int reduceProductionSize = reduceProduction.getRight().size();
                    for (int j = 0; j < reduceProductionSize; j++) {
                        ValidSign productionVS = reduceProduction.getRight().get(reduceProductionSize - 1 - j);
                        ValidSign symbolStackTop = symbolStack.peek();
                        if (symbolStackTop.equals(productionVS)) {
                            stateStack.pop();
                            symbolStack.pop();
                        }
                    }

                    NonTerminal reduceTo = reduceProduction.getLeft();
                    symbolStack.push(reduceTo);

                    int fromStateID = stateStack.peek();
                    for (Map.Entry<NonTerminal, Integer> entry : pt.getGotoMap().get(fromStateID).entrySet()) {
                        if (entry.getKey().getRepresentation().equals(reduceTo.getRepresentation())) {
                            stateStack.push(entry.getValue());
                            break;
                        }
                    }

                }

                // 接受
                else if (curAction.getType() == ActionType.ACCEPT) {
                    break;
                }
            }

        }

        return result;
    }
}
