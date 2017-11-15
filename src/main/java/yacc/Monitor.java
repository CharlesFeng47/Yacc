package yacc;

import entity.NonTerminal;
import entity.Production;
import entity.Terminal;
import entity.ValidSign;
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
public class Monitor {

    /**
     * 文法的产生式
     */
    private List<Production> productions;

    /**
     * 文法的预测分析表
     */
    private ParsingTable pt;

    public Monitor(List<Production> productions, ParsingTable pt) {
        this.productions = productions;
        this.pt = pt;
    }

    /**
     * 对输入 input 进行词法分析
     */
    public List<String> syntaxAnalyze(final List<Terminal> input) throws SyntaxException {
        List<String> resultSimpleProduction = new LinkedList<>();

        Stack<Integer> stateStack = new Stack<>();
        Stack<ValidSign> symbolStack = new Stack<>();

        stateStack.push(0);
        symbolStack.push(new Terminal("$"));

        for (int i = 0; i < input.size(); i++) {
            Terminal t = input.get(i);

            int curState = stateStack.peek();
            Action curAction = pt.getActionMap().get(curState).get(t);
            if (curAction == null) {
                throw new SyntaxException("ERROR 文法分析表中对应表格为空");
            } else {
                // 移入
                if (curAction.getType() == ActionType.SHIFT) {
                    stateStack.push(curAction.getOperand());
                    symbolStack.push(t);
                }

                // 归约
                if (curAction.getType() == ActionType.REDUCTION) {
                    int reduceProductionIndex = curAction.getOperand();
                    Production reduceProduction = productions.get(reduceProductionIndex);
                    for (int j = 0; j < reduceProduction.getRight().size(); j++) {
                        stateStack.pop();
                        symbolStack.pop();
                    }
                    resultSimpleProduction.add(reduceProduction.toSimpleString());

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
            }

        }

        return resultSimpleProduction;
    }
}
