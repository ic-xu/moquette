package pushmanager.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtils {


    public static void replace(File file) throws IOException {
        RandomAccessFile src = new RandomAccessFile(file, "r");
        String path = file.getParent() + File.separator + "csv" + File.separator;
        File desc = new File(path + file.getName().substring(0, file.getName().lastIndexOf(".")) + ".csv");
        if (!desc.exists()) {
            desc.getParentFile().mkdirs();
            desc.createNewFile();
        }
        FileWriter writer = new FileWriter(desc);
        String line = src.readLine();
        String end = "\n";
        while (null != line) {
            String s = line.trim().replaceFirst(" ", ",");
            writer.write(s);
            writer.write(end);
            line = src.readLine();
        }
        writer.flush();
        writer.close();
        src.close();

    }


    public static void main(String[] args) throws IOException {
        File[] files = new File("/local/analysis").listFiles();
//        replace(files[0]);
        for (File src : files) {
            if (src.isDirectory()) {
                System.out.println(src.getAbsolutePath() + "   是一个目录，跳过执行");
                continue;
            } else {
                System.out.println(src.getAbsolutePath());
                replace(src);
            }

        }
    }
}
