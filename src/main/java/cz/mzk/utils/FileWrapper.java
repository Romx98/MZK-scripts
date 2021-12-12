package cz.mzk.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class FileWrapper {
    private final String fileName;
    private final BufferedWriter bufferedWriter;

    private static final String outputFolder = Paths.get("").toAbsolutePath() + "/output";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");

    public FileWrapper(final String fileName, final String scriptFolder) throws IOException {
        this.fileName = getFormattedFileName(fileName);
        final String pathToFile = outputFolder + "/" + scriptFolder + "/" + this.fileName;
        final File file = new File(pathToFile);
        final File parentDirectory = file.getParentFile();
        if (parentDirectory != null && !parentDirectory.exists() && !parentDirectory.mkdirs()) {
            throw new IOException("Couldn't create dir: " + parentDirectory);
        }
        this.bufferedWriter = new BufferedWriter(new FileWriter(file, true));
    }

    private String getFormattedFileName(final String fileName) {
        return fileName + "_" + dateFormat.format(new Date()) + ".txt";
    }

    public void writeLine(final String line) {
        try {
            this.bufferedWriter.write(line);
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
