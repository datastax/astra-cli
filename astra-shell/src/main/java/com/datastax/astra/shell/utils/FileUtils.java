package com.datastax.astra.shell.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Utility operations on Files.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class FileUtils {
    
    /**
     * Hide Default Constructor
     */
    private FileUtils() {}

    
    /**
     * Un Tar file.
     *
     * @param tarFile
     *      source file
     * @param destFile
     *      destination folder
     * @throws IOException
     *      error during opening archive
     */
    public static void extactTargz(File tarFile, File destFile) 
    throws IOException{
      FileInputStream       fis      = new FileInputStream(tarFile);
      // It is a tarGZ, opening the ZIP first, and then the tar
      GzipCompressorInputStream gzIn = new GzipCompressorInputStream(fis);
      TarArchiveInputStream tis      = new TarArchiveInputStream(gzIn);
      TarArchiveEntry       tarEntry = null;
      while ((tarEntry = tis.getNextTarEntry()) != null) {
        File outputFile = new File(destFile + File.separator + tarEntry.getName());
        if (tarEntry.isDirectory()) {
            if (!outputFile.exists()) {
                outputFile.mkdirs();
            }
        } else {
            outputFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(outputFile);
            IOUtils.copy(tis, fos);
            fos.close();
        }
      }
      tis.close();
    }
    
    /**
     * downloadFile
     * 
     * @param urlStr String
     * @param file String
     */
    public static void downloadFile(String urlStr, String file) {
        URL url;
        FileOutputStream    fis = null;
        BufferedInputStream bis = null;
        try {
            url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "bytes");
            bis = new BufferedInputStream(urlConnection.getInputStream());
            fis = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int count=0;
            while((count = bis.read(buffer,0,1024)) != -1) {
                fis.write(buffer, 0, count);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot read URL, invalid syntax",e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot download file",e);
        } finally {
            try {
                if (null != fis) fis.close();
                if (null!= bis)  bis.close();
            } catch (IOException e) {}
        }
    }
    
}
