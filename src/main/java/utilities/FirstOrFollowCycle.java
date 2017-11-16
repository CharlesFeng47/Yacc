package utilities;

import entities.NonTerminal;
import entities.Terminal;

import java.util.List;

/**
 * Created by cuihua on 2017/11/16.
 * <p>
 * 计算 FIRST 或者 FELLOW 时，检查是否循环依赖所使用的数据结构
 */
public class FirstOrFollowCycle {

    /**
     * 循环的非终结符
     */
    private List<NonTerminal> cycleBody;

    /**
     * 循环中等于的值
     */
    private List<Terminal> cycleValue;

    public FirstOrFollowCycle(List<NonTerminal> cycleBody, List<Terminal> cycleValue) {
        this.cycleBody = cycleBody;
        this.cycleValue = cycleValue;
    }

    public List<NonTerminal> getCycleBody() {
        return cycleBody;
    }

    public List<Terminal> getCycleValue() {
        return cycleValue;
    }
}
