package utilities;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by cuihua on 2017/11/16.
 *
 * 读取资源文件（Lex 的规格 .l 文件 ／ Yacc .y 文件中的数据（只含有文法产生式））
 */
public class MyResourceFileReader {

    public List<String> readFile(String path) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        Scanner sc = new Scanner(is);

        List<String> result = new LinkedList<>();
        while (sc.hasNext()) {
            result.add(sc.nextLine());
        }
        return result;
    }
}
