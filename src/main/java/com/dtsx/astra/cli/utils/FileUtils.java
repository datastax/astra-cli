package com.dtsx.astra.cli.utils;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.dtsx.astra.cli.core.out.LoggerShell;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility operations on Files.
 */
public class FileUtils {

    private static final String CREATE_FOLDER_MSG = "Directory %s has been created";
    
    /**
     * Hide Default Constructor
     */
    private FileUtils() {}

    /**
     * Extract a Zip archive.
     *
     * @param zipFilePath
     *      zip archive path on local disk
     */
    public static void extractZipArchiveInAstraCliHome(String zipFilePath) {
        byte[] buffer = new byte[1024];
        createFileIfNotExists(new File(AstraCliUtils.ASTRA_HOME));
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                // Escaping fileName to remove malicious entry
                Path zipEntryPath = Paths.get(zipEntry.getName()).normalize();
                File newFile = new File(AstraCliUtils.ASTRA_HOME + File.separator + zipEntryPath);
                if (zipEntry.isDirectory()) {
                    createFileIfNotExists(newFile);
                } else {
                    File parentFolder = new File(newFile.getParent());
                    createFileIfNotExists(parentFolder);
                    FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                    int length;
                    while ((length = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.close();
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot extract zip archive", e);
        }
    }

    private static void createFileIfNotExists(File directory) {
        if (!directory.exists() && directory.mkdirs()) {
            LoggerShell.debug(CREATE_FOLDER_MSG.formatted(directory.getAbsolutePath()));
        }
    }

    /**
     * Un Tar file.
     *
     * @param tarFile
     *      source file
     * @throws IOException
     *      error during opening archive
     */
    public static void extractTarArchiveInAstraCliHome(File tarFile)
    throws IOException{
        try (FileInputStream fis = new FileInputStream(tarFile)) {
            try (GzipCompressorInputStream gzIn = new GzipCompressorInputStream(fis)) {
                try ( TarArchiveInputStream tis = new TarArchiveInputStream(gzIn)) {
                  TarArchiveEntry tarEntry;
                  while ((tarEntry = tis.getNextTarEntry()) != null) {
                      // Escaping to remove invalid entry
                      File outputFile = Paths.get(AstraCliUtils.ASTRA_HOME + File.separator +
                              Paths.get(tarEntry.getName()).normalize()).toFile();
                      if (tarEntry.isDirectory()) {
                        if (!outputFile.exists() && outputFile.mkdirs())
                          LoggerShell.debug(CREATE_FOLDER_MSG
                                  .formatted(outputFile.getAbsolutePath()));
                          } else {
                              if (outputFile.getParentFile().mkdirs())
                                  LoggerShell.debug(CREATE_FOLDER_MSG
                                          .formatted(outputFile.getParentFile().getAbsolutePath()));
                              try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                  IOUtils.copy(tis, fos);
                              }
                          }
                  }
              }
          }
       }
    }
    
    /**
     * downloadFile
     * 
     * @param urlStr String
     * @param file String
     */
    public static void downloadFile(String urlStr, String file) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(urlStr).openConnection();
            int count;
            byte[] buffer = new byte[1024];
            try (
                 BufferedInputStream bis = new BufferedInputStream(urlConnection.getInputStream());
                 FileOutputStream fis = new FileOutputStream(file)) {

                while ((count = bis.read(buffer, 0, 1024)) != -1) {
                    fis.write(buffer, 0, count);
                }
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot read URL, invalid syntax", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot download file:%s".formatted(e.getMessage()), e);
        }
    }

    /**
     * Delete Directory.
     *
     * @param directoryToBeDeleted
     *      directory to be deleted
     */
    public static void deleteDirectory(File directoryToBeDeleted) {
        if (directoryToBeDeleted != null && directoryToBeDeleted.exists()) {
            File[] allContents = directoryToBeDeleted.listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    deleteDirectory(file);
                }
            }
            try {
                Files.delete(directoryToBeDeleted.toPath());
            } catch(IOException e)  {
                throw new IllegalArgumentException("Cannot delete directory", e);
            }
        }
    }
}
