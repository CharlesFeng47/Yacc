package lexicalAnalyzer.finiteAutomata.entity;

/**
 * Created by cuihua on 2017/10/24.
 * <p>
 * Nondeterministic FA，不确定的有穷自动机
 */
public class NFA extends FA {

    /**
     * TODO 因为没有使用 NFA 来校验词素，所以等需要的时候再来实现
     */
    @Override
    public boolean isValid(String lexeme) {
        return false;
    }
}
