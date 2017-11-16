package lexicalAnalyzer.lex.generator;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by cuihua on 2017/10/24.
 * <p>
 * 用于读取 Lex 的规格 .l 文件
 */
public class LexInputReader {

    /**
     * .l 文件的路径
     */
    private static final String path = "regular_expression.l";

    public LexInputReader() {
    }

    /**
     * 从 .l 文件中读取数据
     */
    public List<String> readREs() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        Scanner sc = new Scanner(is);

        List<String> reContent = new LinkedList<>();
        while (sc.hasNext()) {
            reContent.add(sc.nextLine());
        }
        return reContent;
    }
}
