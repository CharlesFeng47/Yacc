package lexicalAnalyzer.lex.generator;

import lexicalAnalyzer.finiteAutomata.FA_Controller;
import lexicalAnalyzer.finiteAutomata.entity.DFA;

import java.util.*;

/**
 * Created by cuihua on 2017/11/1.
 * <p>
 * 处理 Lex .l 文件中的数据（只含有正则定义）
 */
public class LexInputHandler {

    /**
     * .l 文件的内容（模式 pattern + 正则定义 re）
     */
    private List<String> content;

    /**
     * 模式 与 正则定义 的一一映射
     */
    private Map<String, String> patternREMap;

    public LexInputHandler(List<String> content) {
        this.content = content;
        initMap();
    }

    /**
     * 根据 .l 文件初始化映射表
     * LinkedHashMao 保证顺序与读入顺序相同
     */
    private void initMap() {
        patternREMap = new LinkedHashMap<>();
        for (String line : content) {
            String[] parts = line.split(" ");
            patternREMap.put(parts[0], parts[1]);
        }
    }

    /**
     * .l 文件内容对应的 DFA
     */
    public List<DFA> convert() {
        // 处理正则定义
        List<String> res = new LinkedList<>(patternREMap.values());
        List<String> patternTypes = new LinkedList<>(patternREMap.keySet());

        FA_Controller controller = new FA_Controller();
        return controller.lexicalAnalysis(res, patternTypes);
    }

}
