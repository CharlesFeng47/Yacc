package yacc;

import entities.Production;
import utilities.ActionType;
import utilities.UserOutputController;
import yacc.entities.Action;

import java.io.IOException;
import java.util.List;

/**
 * Created by cuihua on 2017/11/16.
 * <p>
 * 与使用文法分析器的用户进行交互
 */
public class UserInteractionController {

    /**
     * 输入的产生式
     */
    private List<Production> productions;

    /**
     * 输出所有的归约序列
     */
    public void showAllReductions(List<Action> actions, List<Production> productions) {
        this.productions = productions;
        String output = getReductionOutput(actions);

        UserOutputController.showInConsole(output);
        try {
            UserOutputController.showInFile(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getReductionOutput(List<Action> actions) {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------\n");

        for (Action action : actions) {
            if (action.getType() == ActionType.SHIFT) {
                sb.append(action.toString());
            } else if (action.getType() == ActionType.REDUCTION) {
                sb.append(action.getType()).append(" ").append(productions.get(action.getOperand()).toSimpleString());
            } else if (action.getType() == ActionType.ACCEPT) {
                sb.append(action.getType());
            }
            sb.append("\n");
        }

        sb.append("-------------------");
        return sb.toString();
    }


}
