package com.example.imagesgallery.Utility;

import java.io.File;
import java.util.ArrayList;

public class FileUtility {
    public static ArrayList<File> moveAllImagesInAFolderToAnotherFolder(String sourceFolderPath, String destinationFolderPath) {
        File sourceFolder = new File(sourceFolderPath);
        File destinationFolder = new File(destinationFolderPath);

        // Check if the source folder exists and is a directory
        if (sourceFolder.exists() && sourceFolder.isDirectory()) {
            // Check if the destination folder exists, create it if it doesn't
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            // Get a list of files in the source folder
            File[] files = sourceFolder.listFiles();

            if (files != null) {
                ArrayList<File> resultFiles= new ArrayList<File>();
                for (File file : files) {
                    if (file.isFile()) {
                        // Check if the file is an image (you can modify this condition as per your requirements)
                        if (isImageFile(file)) {
                            // Move the file to the destination folder
                            File newFile = new File(destinationFolder, file.getName());
                            file.renameTo(newFile);
                            resultFiles.add(file);
                            //System.out.println("Moved image: " + file.getAbsolutePath() + " to " + newFile.getAbsolutePath());
                        }
                    }
                }
                return resultFiles;
            }
        }
        return null;
    }
    private static boolean isImageFile(File file) {
        String extension = getFileExtension(file);
        return extension != null && (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png")
                || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("gif"));
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return null;
    }



}

