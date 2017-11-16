package lexicalAnalyzer.exceptions;

/**
 * Created by cuihua on 2017/10/25.
 * <p>
 * 处理RE的时候，不期望输入的格式
 * 如：(*, (|, |), |*, ||, ·), ·*, ·|, (·, |·, ··
 */
public class UnexpectedRegularExprRuleException extends Exception {

    /**
     * 不合理的正则定义
     */
    private String re;

    public UnexpectedRegularExprRuleException(String re) {
        this.re = re;
    }

    @Override
    public String getMessage() {
        return "输入中 " + re + " 不符合规格";
    }
}
