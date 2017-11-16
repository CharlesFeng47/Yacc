package yacc;

import utilities.MyResourceFileReader;
import yacc.entities.ParsingTable;

import java.util.List;

/**
 * Created by cuihua on 2017/11/16.
 *
 * 处理 Yacc .y 文件中的数据（只含有文法产生式）
 */
public class YaccFileHandler {

    /**
     * .y 文件的路径
     */
    private static final String path = "yacc_specification.y";

    /**
     * .y 文件的内容（产生式 String）
     */
    private List<String> content;

    public YaccFileHandler() {
        // 从 .y 文件中读取数据
        this.content = new MyResourceFileReader().readFile(path);
    }

    /**
     * .y 文件对应的预测分析表
     */
    public ParsingTable convertToPT() {
        return null;
    }



}
