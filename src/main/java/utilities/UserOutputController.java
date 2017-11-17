package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Created by cuihua on 2017/11/17.
 *
 * 统一处理实验结果
 */
public class UserOutputController {

    /**
     * 控制台输出
     */
    public static void showInConsole(String s) {
        System.out.println(s);
    }

    /**
     * 文件输出
     */
    public static void showInFile(String s) throws IOException {
        File file = new File(System.getProperty("user.dir") + " "+ LocalDateTime.now() + ".txt");
        if (file.createNewFile()) {
            FileWriter writer = new FileWriter(file);
            writer.write(s);
            writer.flush();
            writer.close();
        }

    }
}
