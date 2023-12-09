package com.example.imagesgallery.Utility;

import static java.security.AccessController.getContext;

import android.content.ContentResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    public static boolean isImageFile(File file) {
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
    public static ArrayList<File> getAllImageInADirectory(String sourceFolderPath ) {
        File sourceFolder = new File(sourceFolderPath);


        // Check if the source folder exists and is a directory
        if (sourceFolder.exists() && sourceFolder.isDirectory()) {

            // Get a list of files in the source folder
            File[] files = sourceFolder.listFiles();

            if (files != null) {
                ArrayList<File> resultFiles= new ArrayList<File>();
                for (File file : files) {
                    if (file.isFile()) {
                        // Check if the file is an image (you can modify this condition as per your requirements)
                        if (isImageFile(file)) {
                            resultFiles.add(file);

                        }
                    }
                }
                return resultFiles;
            }
        }
        return null;
    }

    public static void copyFilesFromFolder(String sourceFolderPath, String destinationFolderPath) {


        // Check if the source folder exists

        File sourceFolder = new File(sourceFolderPath);
        File destinationFolder = new File(destinationFolderPath);
        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            return;
        }

        // Create the destination folder if it doesn't exist
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        // Get all files in the source folder
        File[] files = sourceFolder.listFiles();
        if (files == null) {
            return;
        }

        // Get the content resolver to access MediaStore


        // Copy each file to the destination folder
        for (File file : files) {
            File destinationFile = new File(destinationFolder, file.getName());

            try {
                // Copy the file
                Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);


            } catch (IOException e) {
                e.printStackTrace();
                // Handle any exceptions
            }
        }
    }

    public static void moveImageToFolder(File sourceImage, String folderPath) {
        // Create the hidden folder if it doesn't exist
        File hiddenFolder = new File(folderPath);
        if (!hiddenFolder.exists()) {
            hiddenFolder.mkdirs();
        }

        // Get the source image file name
        String sourceFileName = sourceImage.getName();

        // Generate the destination file path in the hidden folder
        String destinationFilePath = folderPath + File.separator + sourceFileName;

        // Create the destination file
        File destinationFile = new File(destinationFilePath);

        // Move the image file to the hidden folder
        if (sourceImage.renameTo(destinationFile)) {
            // File moved successfully

        } else {
            // Failed to move the file
        }
    }




}

