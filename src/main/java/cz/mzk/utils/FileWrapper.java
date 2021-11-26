package cz.mzk.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class FileWrapper {

    private final String fileName;
    private final String pathToFile;
    private BufferedWriter bufferedWriter;

    public FileWrapper(final String fileName) {
        this.fileName = getTextFileNameWithDate(fileName);
        this.pathToFile = getWorkDirLogs() + this.fileName;
    }

    private String getWorkDirLogs() {
        final String projectDir = Paths.get("").toAbsolutePath().toString();
        return projectDir + "/logs/";
    }

    private String getTextFileNameWithDate(final String fileName) {
        final SimpleDateFormat simpleDate = new SimpleDateFormat("_dd-MM-yyyy-HH:mm:ss");
        return fileName + simpleDate.format(new Date()) + ".txt";
    }

    public void writeLine(final String rootUuid) {
        try {
            final FileWriter fileWriter = new FileWriter(this.pathToFile, true);
            this.bufferedWriter = new BufferedWriter(fileWriter);
            this.bufferedWriter.write(rootUuid);
            this.bufferedWriter.newLine();
        } catch (IOException e) {
            log.warn("Can't write data to file '" + this.fileName + "'! " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            this.bufferedWriter.close();
        } catch (IOException e) {
            log.warn("Can't close file '" + this.fileName + "'! " + e.getMessage());
            e.printStackTrace();
        }
    }

}
