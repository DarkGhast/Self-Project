package per.darkghast.briefing.util;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import per.darkghast.briefing.exception.NewNotUpdateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 检查新闻是否更新的工具类
 *
 * @author Dark_Ghast
 */
public class UpdateCheckUtil {
    private static final String HASH_FILE = "./output/record/hash.text";

    /**
     * 检查是否已经更新
     *
     * @param version 新闻版本 简报(X月X日)
     */
    public static void checkIsUpdate(String version) throws IOException {
        if (!FileUtil.isFile(HASH_FILE)) {
            FileUtil.touch(HASH_FILE);
        }
        File recordFile = FileUtil.file(HASH_FILE);

        FileReader fileReader = new FileReader(recordFile);
        String oldVersion= fileReader.readString();
        if (version.equals(oldVersion)) {
            throw new NewNotUpdateException("新闻未更新");
        } else {
            try (FileWriter writer = new FileWriter(recordFile)) {
                writer.write(version);
            }
        }
    }
}
