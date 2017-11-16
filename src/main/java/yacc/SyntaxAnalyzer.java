package yacc;

import lexicalAnalyzer.lex.entity.Token;

import java.util.List;

/**
 * Created by cuihua on 2017/11/16.
 *
 * 文法分析器
 * 输入：根据用户输入得到的 Token 序列
 * 输出：归约序列
 */
public class SyntaxAnalyzer {

    /**
     * 用户输入转换得到的 Token 序列
     */
    private List<Token> inputTokens;


    public SyntaxAnalyzer(List<Token> inputTokens) {
        this.inputTokens = inputTokens;
    }


}
