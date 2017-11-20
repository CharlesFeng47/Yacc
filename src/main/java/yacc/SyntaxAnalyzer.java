package yacc;

import entities.Production;
import entities.Terminal;
import entities.ValidSign;
import exceptions.ParsingTableConflictException;
import exceptions.SyntaxException;
import exceptions.YaccFileInputException;
import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.lex.entity.Token;
import org.apache.log4j.Logger;
import yacc.entities.Action;
import yacc.entities.ParsingTable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by cuihua on 2017/11/16.
 * <p>
 * 文法分析器
 * 输入：根据用户输入得到的 Token 序列
 * 输出：归约序列
 */
public class SyntaxAnalyzer {

    private static final Logger logger = Logger.getLogger(SyntaxAnalyzer.class);

    /**
     * .l 文件生成的词法分析器
     */
    private LexicalAnalyzer lexicalAnalyzer;

    /**
     * 输入的产生式
     */
    private List<Production> productions;

    public List<Production> getProductions() {
        return productions;
    }

    public SyntaxAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    /**
     * 文法分析的控制器
     */
    public List<Action> analyze(List<Token> inputTokens) throws YaccFileInputException, ParsingTableConflictException, SyntaxException {
        // 从 .y 文件中获取 PT
        YaccFileHandler handler = new YaccFileHandler(lexicalAnalyzer);
        this.productions = handler.getProductions();
        ParsingTable pt = handler.convertToPT();
        logger.info("文法分析表 PT 已生成结束");

        Map<String, ValidSign> vsMap = handler.getValidSignMap();
        // 用户输入转换得到的词法单元 Token 序列 => 文法分析所需要的终结符 Terminal 序列
        List<Terminal> toReduce = new LinkedList<>();
        for (Token token : inputTokens) {
            // Token 词法单元的模式 Pattern 即为合理的终结符
            String curTokenPattern = token.getPatternType();
            if (vsMap.containsKey(curTokenPattern)) {
                toReduce.add((Terminal) vsMap.get(curTokenPattern));
            } else {
                Terminal t = new Terminal(curTokenPattern);
                toReduce.add(t);
                vsMap.put(curTokenPattern, t);
            }
        }
        // 增加 $ 标记 input 的起止方便读取
        toReduce.add(0, (Terminal) vsMap.get("$"));
        toReduce.add((Terminal) vsMap.get("$"));
        logger.info("输入的词法单元 Token 序列与终结符对应结束");

        // 根据转换得来的 Token 序列和文法的分析表，进行模拟器分析
        Monitor monitor = new Monitor(productions, pt);
        return monitor.syntaxAnalyze(toReduce);
    }

}
