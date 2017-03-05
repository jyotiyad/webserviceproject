package com.jyoti.ws.project.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<File> getAllFilesInDirectory(File dir) {
        List<File> files = new ArrayList<>();
        File[] listFiles = dir.listFiles();
        if (listFiles != null) {
            for (final File file : listFiles) {
                if (file.isDirectory()) {
                    files.addAll(getAllFilesInDirectory(file));
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }
}
