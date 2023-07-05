package per.darkghast.briefing.util;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.crypto.digest.DigestUtil;
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
     * @param file 新闻图片文件
     */
    public static void checkIsUpdate(File file) throws IOException {
        if (!FileUtil.isFile(HASH_FILE)) {
            FileUtil.touch(HASH_FILE);
        }
        File recordFile = FileUtil.file(HASH_FILE);

        String newHash = DigestUtil.md5Hex(file);

        FileReader fileReader = new FileReader(recordFile);
        String oldHash = fileReader.readString();
        if (newHash.equals(oldHash)) {
            throw new NewNotUpdateException("新闻未更新");
        } else {
            try (FileWriter writer = new FileWriter(recordFile)) {
                writer.write(newHash);
            }
        }
    }
}
