package discovery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileData implements Serializable{

    // Fields representing file metadata
    private String fileName;
    private long fileSize; // Size in bytes
    private String fileType; // MIME type or file extension
    private String fileHash; // Checksum or hash for integrity verification

    // Constructors
    public FileData() {
        // Default constructor
    }

    /**
     * Constructor that initializes all fields based on a file path.
     *
     * @param filePath The path to the file.
     * @throws IOException If the file cannot be read.
     */
    public FileData(String filePath) throws IOException {
        File file = new File(filePath);

        // Ensure the file exists
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        // Extract file metadata
        this.fileName = file.getName();
        this.fileSize = file.length();
        this.fileType = getFileExtension(this.fileName);

        // Calculate file hash
        this.fileHash = calculateFileHash(file);
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    // Helper method to extract the file extension
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return ""; // No extension
        }
        return fileName.substring(lastDotIndex + 1);
    }

    // Helper method to calculate the MD5 hash of the file
    private String calculateFileHash(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = md.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found.", e);
        }
    }

    // toString method for easy debugging
    @Override
    public String toString() {
        return "File{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", fileHash='" + fileHash + '\'' +
                '}';
    }
}