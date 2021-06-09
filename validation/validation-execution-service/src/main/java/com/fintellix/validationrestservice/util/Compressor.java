package com.fintellix.validationrestservice.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Compressor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Compressor.class);
    private Compressor instance = new Compressor();

    private Compressor() {
    }

/*    public static void main(String[] args) {
        String path = "D:\\Validation_Results";
        File folder = new File(path);
        listFilesForFolder(folder);
    }*/

    public static void compressFiles(String path) {
        try {
            File folder = new File(path);
            listFilesForFolder(folder);
        } catch (Exception e) {
            // do-nothing
        }
    }

    private static void listFilesForFolder(File folder) {
        for (final File fileEntry : folder.listFiles()) {
            try {
                if (fileEntry.isDirectory()) {
                    listFilesForFolder(fileEntry);
                } else {
                    if (!fileEntry.getName().endsWith(".gz")) {
                        gzipFile(fileEntry.getAbsolutePath(), fileEntry.getAbsolutePath() + ".gz");
                        fileEntry.delete();
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to zip file: " + fileEntry.getName());
                e.printStackTrace();
            }
        }
    }

    private static void gzipFile(String source_filepath, String destinaton_zip_filepath) {

        byte[] buffer = new byte[1024];

        try {

            FileOutputStream fileOutputStream = new FileOutputStream(destinaton_zip_filepath);

            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);

            FileInputStream fileInput = new FileInputStream(source_filepath);

            int bytes_read;

            while ((bytes_read = fileInput.read(buffer)) > 0) {
                gzipOutputStream.write(buffer, 0, bytes_read);
            }

            fileInput.close();

            gzipOutputStream.finish();
            gzipOutputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void zipFolder(String sourceDirPath, OutputStream outputStream) throws Exception {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        for (File file : new File(sourceDirPath).listFiles()) {
            createZipEntry(zipOutputStream, file.getName(), FileUtils.readFileToByteArray(file));
        }

        zipOutputStream.flush();
        zipOutputStream.close();
        outputStream.flush();
        outputStream.close();
    }

    private static void createZipEntry(ZipOutputStream zipOutputStream, String filename, byte[] content) throws Exception {
        ZipEntry entry = new ZipEntry(filename);
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(content);
        zipOutputStream.closeEntry();
    }
}