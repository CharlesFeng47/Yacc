package lexicalAnalyzer.finiteAutomata;

import lexicalAnalyzer.exceptions.UnexpectedRegularExprRuleException;
import lexicalAnalyzer.finiteAutomata.entity.DFA;
import lexicalAnalyzer.finiteAutomata.entity.NFA;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cuihua on 2017/10/27.
 * <p>
 * 控制将输入的所有 RE 转换为拥有最少数目状态的 DFA
 */
public class FA_Controller {

    private static final Logger logger = Logger.getLogger(FA_Controller.class);

    public List<DFA> lexicalAnalysis(List<String> res, List<String> patternType) {
        RegularExpressionHandler rgHandler = new RegularExpressionHandler();
        NFA_Handler nfaHandler = new NFA_Handler();
        DFA_Handler dfaHandler = new DFA_Handler();

        // 对每个正则定义依次生成最小 DFA
        List<DFA> result = new LinkedList<>();
        for (int i = 0; i < res.size(); i++) {
            // 处理当前 RE
            String re = res.get(i);
            try {
                re = rgHandler.convertInfixToPostfix(rgHandler.standardizeRE(re));
                logger.debug("正在处理正则定义 " + re);
            } catch (UnexpectedRegularExprRuleException e) {
                e.printStackTrace();
            }

            // RE => NFA
            NFA nfa = nfaHandler.getFromRE(re, patternType.get(i));
            logger.debug("将正则定义 " + re + " 成功转化为 NFA");

            // 转化为最小DFA
            DFA dfa = dfaHandler.optimize(dfaHandler.getFromNFA(nfa));
            logger.debug("正则定义 " + re + " 的状态数量: " + dfa.getStates().size());

            result.add(dfa);
        }

        return result;
    }
}
