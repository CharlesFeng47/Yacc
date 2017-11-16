package lexicalAnalyzer;

import lexicalAnalyzer.exceptions.NotMatchingException;
import lexicalAnalyzer.finiteAutomata.entity.DFA;
import lexicalAnalyzer.lex.UserInteractionController;
import lexicalAnalyzer.lex.entity.Token;
import lexicalAnalyzer.lex.LexFileHandler;
import lexicalAnalyzer.utilities.DFA_StatePatternMappingController;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cuihua on 2017/11/2.
 * <p>
 * 词法分析器
 * 输入：用户输入要分析的词素
 * 输出：根据已有的 .l 文件返回 Token 序列
 */
public class LexicalAnalyzer {

    /**
     * 由当前 .l 文件生成的最小 DFA
     */
    private List<DFA> allDFAs;

    public LexicalAnalyzer() {
    }

    /**
     * 词法分析的主程序
     */
    public List<Token> lexicalAnalyze() throws NotMatchingException {
        UserInteractionController userInteractionController = new UserInteractionController();
        List<String> lexemes = userInteractionController.readUserContent();

        // 解析 .l 文件，生成词法分析器
        LexFileHandler lexFileHandler = new LexFileHandler();
        this.allDFAs = lexFileHandler.convertToDFA();

        List<Token> resultTokens = new LinkedList<>();
        for (String lexeme : lexemes) {
            resultTokens.add(analyze(lexeme));
        }

        userInteractionController.showAllTokens(resultTokens);
        return resultTokens;
    }


    /**
     * 对每一个词素都进行分析
     *
     * @param lexeme 要分析的词素
     * @return 分析结束之后的的结果词法单元
     */
    private Token analyze(String lexeme) throws NotMatchingException {
        for (DFA curDFA : allDFAs) {
            // 按优先级顺序依次对比，满足了就返回
            if (curDFA.isValid(lexeme))
                return new Token(DFA_StatePatternMappingController.getMap().get(curDFA), lexeme);
        }
        throw new NotMatchingException(lexeme);

    }
}
