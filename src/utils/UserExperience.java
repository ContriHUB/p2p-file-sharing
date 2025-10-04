package utils;

public class UserExperience {
    
    // Transfer stages
    public enum TransferStage {
        CONNECTING("Connecting to peer"),
        REQUESTING("Requesting file"),
        NEGOTIATING("Negotiating transfer"),
        TRANSFERRING("Transferring"),
        ENCRYPTING("Encrypting"),
        DECRYPTING("Decrypting"),
        FINALIZING("Finalizing"),
        COMPLETE("Complete");
        
        private final String description;
        
        TransferStage(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Current stage tracker
    private static TransferStage currentStage = null;
    
    /**
     * Print a stage header when entering a new transfer phase
     */
    public static void printStage(TransferStage stage) {
        currentStage = stage;
        System.out.println("\n==> " + stage.getDescription() + "...");
    }
    
    /**
     * Print a simple status message
     */
    public static void printStatus(String message) {
        System.out.println("[INFO] " + message);
    }
    
    /**
     * Print a success message
     */
    public static void printSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }
    
    /**
     * Print a warning message
     */
    public static void printWarning(String message) {
        System.out.println("[WARNING] " + message);
    }
    
    /**
     * Enhanced progress bar with stage information
     */
    public static void printProgressBar(long currentProgress, long totalSize) {
        int barLength = 40; // Shorter bar for cleaner look
        int progress = (int) ((double) currentProgress / totalSize * barLength);
        int percentage = (int) ((currentProgress * 100) / totalSize);
        
        // Build the progress bar string
        StringBuilder bar = new StringBuilder();
        
        // Add stage prefix if available
        if (currentStage != null) {
            bar.append("[").append(currentStage.getDescription()).append("] ");
        }
        
        bar.append("[");
        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                bar.append("="); // Filled part
            } else if (i == progress) {
                bar.append(">"); // Progress indicator
            } else {
                bar.append(" "); // Empty part
            }
        }
        bar.append("] ");
        
        // Add percentage
        bar.append(percentage).append("%");
        
        // Add size information
        bar.append(" (").append(formatBytes(currentProgress))
           .append(" / ").append(formatBytes(totalSize)).append(")");
        
        // Print the progress bar (overwrite same line)
        System.out.print("\r" + bar.toString());
        
        // New line when complete
        if (currentProgress >= totalSize) {
            System.out.println();
        }
    }
    
    /**
     * Format bytes into human-readable format (public for external use)
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Clear the current line (useful for cleaning up progress bars)
     */
    public static void clearLine() {
        System.out.print("\r" + " ".repeat(100) + "\r");
    }
    
    /**
     * Print a separator line
     */
    public static void printSeparator() {
        System.out.println("-".repeat(60));
    }
}
