package lexicalAnalyzer.finiteAutomata.entity;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by cuihua on 2017/10/24.
 * <p>
 * 标记子集构造法中产生的映射关系
 */
public class DTran {

    private static Logger logger = Logger.getLogger(DTran.class);

    /**
     * 构造中产生的等价转换的出发状态
     */
    private List<FA_State> from;

    /**
     * 构造中产生的等价转换的到达状态
     */
    private List<FA_State> to;

    /**
     * 标记的转换条件
     */
    private char label;

    public DTran(List<FA_State> from, List<FA_State> to, char label) {
        this.from = from;
        this.to = to;
        this.label = label;
    }

    public List<FA_State> getFrom() {
        return from;
    }

    public void setFrom(List<FA_State> from) {
        this.from = from;
    }

    public List<FA_State> getTo() {
        return to;
    }

    public void setTo(List<FA_State> to) {
        this.to = to;
    }

    public char getLabel() {
        return label;
    }

    public void setLabel(char label) {
        this.label = label;
    }

    // 控制台呈现该DTran
    public void show() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (FA_State state : from) {
            sb.append(state.getStateID() + " ");
        }
        sb.append("\n" + label + "\n");
        for (FA_State state : to) {
            sb.append(state.getStateID() + " ");
        }
        logger.debug(sb.toString());
    }
}
