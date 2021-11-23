package cz.mzk.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class FileWriterUtils {

    private final String fileName;
    private final String logsDir;

    public FileWriterUtils(final String fileName) {
        this.fileName = fileName;
        this.logsDir = getWorkDirLogs();
    }

    private String getWorkDirLogs() {
        final String projectDir = Paths.get("").toAbsolutePath().toString();
        return projectDir + "/logs/";
    }

    private String getTextFileNameWithDate() {
        final SimpleDateFormat simpleDate = new SimpleDateFormat("_dd-MM-yyyy-HH:mm:ss");
        return this.fileName + simpleDate.format(new Date()) + ".txt";
    }

    public void writeLine(final String rootUuid) {
        try {
            final String fileName = this.logsDir + getTextFileNameWithDate();
            final FileWriter fileWriter = new FileWriter(fileName, true);
            final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(rootUuid);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            log.warn("Can't write data to file '" + fileName + "'! " + e.getMessage());
            e.printStackTrace();
        }
    }

}
