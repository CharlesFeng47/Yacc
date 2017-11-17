import exceptions.ParsingTableConflictException;
import exceptions.SyntaxException;
import exceptions.YaccFileInputException;
import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.exceptions.NotMatchingException;
import lexicalAnalyzer.lex.entity.Token;
import org.apache.log4j.Logger;
import yacc.SyntaxAnalyzer;
import yacc.UserInteractionController;
import yacc.entities.Action;

import java.util.List;

/**
 * Created by cuihua on 2017/11/16.
 * <p>
 * 主程序
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws NotMatchingException, SyntaxException, YaccFileInputException, ParsingTableConflictException {

        // 词法分析
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        List<Token> inputTokens = lexicalAnalyzer.lexicalAnalyze();
        logger.info("词法分析结束");

        // 文法分析
        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(lexicalAnalyzer);
        List<Action> reductions = syntaxAnalyzer.analyze(inputTokens);
        logger.info("文法分析结束");

        // 输出结果
        UserInteractionController userInteractionController = new UserInteractionController();
        userInteractionController.showAllReductions(reductions, syntaxAnalyzer.getProductions());
    }
}
