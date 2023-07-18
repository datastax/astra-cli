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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.dtsx.astra.cli.core.out.LoggerShell;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Utility operations on Files.
 */
public class FileUtils {
    
    /**
     * Hide Default Constructor
     */
    private FileUtils() {}

    public static void extractZipArchiveInAstraCliHome(String zipFilePath) {
        byte[] buffer = new byte[1024];

        File folder = new File(AstraCliUtils.ASTRA_HOME);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(AstraCliUtils.ASTRA_HOME + File.separator + fileName);
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
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
            throw new RuntimeException(e);
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
                      File outputFile = Paths.get(AstraCliUtils.ASTRA_HOME, escapeTarEntry(tarEntry.getName())).toFile();
                          if (tarEntry.isDirectory()) {
                              if (!outputFile.exists() && outputFile.mkdirs())
                                  LoggerShell.debug("Repository %s has been created"
                                          .formatted(outputFile.getAbsolutePath()));
                          } else {
                              if (outputFile.getParentFile().mkdirs())
                                  LoggerShell.debug("Repository %s has been created"
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
     * Escape value for the entry.
     *
     * @param tarEntry
     *      entry
     * @return
     *      escaped
     */
    private static String escapeTarEntry(String tarEntry) {
        return tarEntry
                .replaceAll(">", "")
                .replace("<", "")
                .replace("\\*", "")
                .replace("\\|", "");
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
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        if (directoryToBeDeleted == null || !directoryToBeDeleted.exists()) {
            return false;
        }
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
