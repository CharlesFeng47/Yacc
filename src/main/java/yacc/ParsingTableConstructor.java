package yacc;

import entity.NonTerminal;
import entity.Production;
import entity.ValidSign;
import layeredFA.FA_Constructor;
import layeredFA.entities.LayeredFA;
import yacc.entities.ParsingTable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * 构造此文法的文法分析表
 */
public class ParsingTableConstructor {

    /**
     * 此文法的所有起始产生式（indicator 为 0）
     */
    final private List<Production> productions;

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
    public ParsingTable getParsingTable() {
        ParsingTable pt = new ParsingTable();

        FA_Constructor faConstructor = new FA_Constructor(productions);
        LayeredFA fa = faConstructor.parse();

        // 根据状态间填写非终结符的 ACTION SHIFT 和终结符的 GOTO


        // 根据状态内的可归约项归约产生式，填写非终结符的 ACTION REDUCTION

        return pt;
    }

}