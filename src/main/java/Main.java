import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.exceptions.NotMatchingException;
import lexicalAnalyzer.lex.entity.Token;

import java.util.List;

/**
 * Created by cuihua on 2017/11/16.
 * <p>
 * 主程序
 */
public class Main {

    public static void main(String[] args) throws NotMatchingException {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        List<Token> inputToken = lexicalAnalyzer.lexicalAnalyze();



    }
}