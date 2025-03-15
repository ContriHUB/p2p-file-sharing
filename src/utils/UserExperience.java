package utils;

public class UserExperience {
	public static void printProgressBar(long currentProgress, long totalSize) {
	    int barLength = 50; // Length of the progress bar
	    int progress = (int) ((double) currentProgress / totalSize * barLength);
	
	    // Build the progress bar string
	    StringBuilder bar = new StringBuilder("[");
	    for (int i = 0; i < barLength; i++) {
	        if (i < progress) {
	            bar.append("="); // Filled part
	        } else {
	            bar.append(" "); // Empty part
	        }
	    }
	    bar.append("] ");
	    bar.append((currentProgress * 100)/ totalSize).append("%");
	    
	    bar.append("   " + currentProgress + " / " + totalSize);
	   
	
	    // Print the progress bar
	    System.out.print("\r" + bar.toString()); // \r to overwrite the same line
	}
}
