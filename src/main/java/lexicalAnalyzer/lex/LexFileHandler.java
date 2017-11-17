package lexicalAnalyzer.lex;

import lexicalAnalyzer.finiteAutomata.FA_Controller;
import lexicalAnalyzer.finiteAutomata.entity.DFA;
import utilities.MyResourceFileReader;

import java.util.*;

/**
 * Created by cuihua on 2017/11/1.
 * <p>
 * 处理 Lex .l 文件中的数据（只含有正则定义）
 */
public class LexFileHandler {

    /**
     * .l 文件的路径
     */
    private static final String path = "regular_expression.l";

    /**
     * 模式 与 正则定义 的一一映射
     */
    private Map<String, String> patternREMap;

    public List<String> getAllPattern() {
        return new LinkedList<>(patternREMap.keySet());
    }

    public LexFileHandler() {
        // 从 .l 文件中读取数据
        List<String> content = new MyResourceFileReader().readFile(path);

        // LinkedHashMao 保证顺序与读入顺序相同
        patternREMap = new LinkedHashMap<>();
        for (String line : content) {
            String[] parts = line.split(" ");
            patternREMap.put(parts[0], parts[1]);
        }
    }

    /**
     * .l 文件内容对应的 DFA
     */
    public List<DFA> convertToDFA() {
        // 处理正则定义
        List<String> res = new LinkedList<>(patternREMap.values());
        List<String> patternTypes = new LinkedList<>(patternREMap.keySet());

        FA_Controller controller = new FA_Controller();
        return controller.lexicalAnalysis(res, patternTypes);
    }

}
