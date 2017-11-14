package layeredFA;

import entity.NonTerminal;
import entity.Production;
import entity.ValidSign;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * 构造此文法的文法分析表
 */
public class ParsingTableConstructor {

    /**
     * 此文法的所有产生式
     */
    private List<Production> productions;

    public ParsingTableConstructor(List<Production> productions) {
        if (isSingleStartLeft(productions)) {
            // 新增开始符 & ，统领全局
            NonTerminal newStratProLeft = new NonTerminal('&');

            List<ValidSign> newStartProRight = new LinkedList<>();
            newStartProRight.add(productions.get(0).getLeft());
            Production newStartPro = new Production(newStratProLeft, newStartProRight);
            productions.add(0, newStartPro);
        }

        this.productions = productions;
    }

    /**
     * 检查 productions 中是不是开始符只出现了一次
     */
    private boolean isSingleStartLeft(List<Production> productions) {
        NonTerminal startNT = productions.get(0).getLeft();
        char startSign = startNT.getRepresentation();

        int count = 0;
        for (Production p : productions) {
            if (p.getLeft().getRepresentation() == startSign) count++;
        }

        return count == 1;
    }
}
