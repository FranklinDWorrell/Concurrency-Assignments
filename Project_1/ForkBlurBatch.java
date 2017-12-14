import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException; 
import java.util.concurrent.CancellationException; 
import java.util.concurrent.ExecutionException; 
import java.util.concurrent.ForkJoinPool; 
import java.util.concurrent.Future; 
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit; 
import javax.imageio.ImageIO;

/**
 * This program iterates through the image files in a subdirectory 
 * named "images" and recursively blurs each image using Java's 
 * fork/join capabilities. Subdirectory should be clean expect for 
 * the desired image files. Images should be jpg files. Blurred 
 * images will be output to the same subdirectory with "blurred-" 
 * prefixed to each image's original file name. 
 * 
 * This code is built upon sample code provided by Oracle to 
 * illustrate Java's fork/join functionality. 
 * @author Franklin D. Worrell
 * @version 13 December 2017 
 */
public class ForkBlurBatch extends RecursiveAction{
    private int[] mSource;
    private int mStart;
    private int mLength;
    private int[] mDestination; 
    private int imageWidth;                 // Only initialized for complete image instances. 
    private int imageHeight;                // Only initialized for complete image instances. 
    private String outputFileName = null;   // If not null, this instance is a complete image. 
    private int mBlurWidth = 15; 			// Processing window size, should be odd.
    protected static int sThreshold = 10000; 
    private static ForkJoinPool pool = new ForkJoinPool(); 


    /**
     * Creates a new <code>RecursiveAction</code> that blurs a 
     * portion of an image file. Used to initialize instances
     * for recursive fork calls. 
     * @param src
     * @param start
     * @param length
     * @param dst
     */
    public ForkBlurBatch(int[] src, int start, int length, int[] dst) {
        mSource = src;
        mStart = start;
        mLength = length;
        mDestination = dst;
    }


    /**
     * Creates a new <code>RecursiveAction</code> used to represent a complete
     * image for blurring. 
     * @param src
     * @param start
     * @param length
     * @param dst 
     * @param w the width of the output image
     * @param h the height of the output image
     * @param outputName the name of the file for the blurred image 
     */
    public ForkBlurBatch(int[] src, int start, int length, int[] dst, 
                         int w, int h, String outputName) {
        mSource = src;
        mStart = start;
        mLength = length;
        mDestination = dst; 
        imageWidth = w; 
        imageHeight = h; 
        outputFileName = outputName; 
    }


    /**
     * Average pixels from source, write results into destination.
     */
    protected void computeDirectly() {
        int sidePixels = (mBlurWidth - 1) / 2; 
        // Calculate the average.
        for (int index = mStart; index < mStart + mLength; index++) { 
            float rt = 0,
                  gt = 0,
                  bt = 0;
            for (int mi = -sidePixels; mi <= sidePixels; mi++) {
                int mindex = Math.min(Math.max(mi + index, 0), mSource.length - 1);
                int pixel = mSource[mindex];
                rt += (float) ((pixel & 0x00ff0000) >> 16) / mBlurWidth;
                gt += (float) ((pixel & 0x0000ff00) >> 8) / mBlurWidth;
                bt += (float) ((pixel & 0x000000ff) >> 0) / mBlurWidth;
            } 
            // Reassemble destination pixel. 
            int dpixel = (0xff000000) | (((int) rt) << 16) | (((int) gt) << 8) | (((int) bt) << 0);
            mDestination[index] = dpixel;
        }
    }


    /**
     * Recursively divides each image if it is larger than the 
     * threshold specified as a class variable. 
     */
    @Override
    protected void compute() {
        if (mLength < sThreshold) {
            computeDirectly();
            return;
        }
        int split = mLength / 2;
        invokeAll(new ForkBlurBatch(mSource, mStart, split, mDestination),
                  new ForkBlurBatch(mSource, mStart + split, mLength - split, mDestination));
        
        // This instance was a complete image and not a recursive fork. 
        if (outputFileName != null) {   
            BufferedImage dstImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB); 
            dstImage.setRGB(0, 0, imageWidth, imageHeight, mDestination, 0, imageWidth); 
            File dstFile = new File("images", outputFileName); 
            System.out.println("Beginning write of: " + outputFileName); 
            try {
                ImageIO.write(dstImage, "jpg", dstFile); 
            } catch (IOException e) {
                System.out.println("Error writing output image."); 
                e.printStackTrace(); 
            }
            System.out.println("Finish write of: " + outputFileName); 
        }
    }


    /**
     * The thread that uniquely handles reading in the images before 
     * they are blurred. This thread also waits for all others to 
     * complete so that all threads can execute to completion in the 
     * <code>ForkJoinPool</code>. 
     */
    private static class IOThread extends RecursiveAction {
        /**
         * Loads the images for blurring, then calls shutdown on the ForkJoinPool. 
         */
        @Override
        public void compute() {
            // Get the array of files for processing from "images" subdirectory. 
            String srcDirName = "images";       
            File srcDir = new File(srcDirName); 
            File[] srcFiles = srcDir.listFiles((d, s) -> {
                return s.toLowerCase().endsWith(".jpg"); 
            }); 
            
            createAndStartTasks(srcFiles); 

            // Once all tasks added to the FJPool, request termination when they finish. 
            pool.shutdown();
        }

        
        /**
         * Performs the input I/O for each image file, creates a 
         * <code>RecursiveAction</code> for that image, then adds that
         * <code>RecursiveAction</code> to the <code>ForkJoinPool</code>. 
         * @param srcFiles an array of image file objects to blur
         */
        private void createAndStartTasks(File[] srcFiles) {
            for (File srcFile : srcFiles) {
                BufferedImage srcImage = null; 
                try {
                    srcImage = ImageIO.read(srcFile); 
                } catch (IOException e) {
                    System.out.println("Error opening source image."); 
                    e.printStackTrace(); 
                }

                // Image loaded properly, so create a new RecursiveAction.
                if (srcImage != null) {
                    int w = srcImage.getWidth(); 
                    int h = srcImage.getHeight(); 
                    String outputName = "blurred-" + srcFile.getName(); 
                    int[] src = srcImage.getRGB(0, 0, w, h, null, 0, w); 
                    int[] dst = new int[src.length]; 
                    ForkBlurBatch fb = new ForkBlurBatch(src, 0, src.length, 
                                                         dst, w, h, outputName); 
                    System.out.println("Invoking FJPool for: " + outputName); 
                    // This call is asynchronous, so blurring begins immediately. 
                    pool.execute(fb); 
                }
            }
        }
    } // end class IOThread 

    
    /**
     * Iterates through subdirectory of current directory and
     * blurs each image file found there. 
     */ 
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis(); 
        pool.invoke(new ForkBlurBatch.IOThread()); 
                           
        // Await termination of all threads in ForkJoinPool before exiting. 
        try {
            pool.awaitTermination(1, TimeUnit.MINUTES); 
        } catch (InterruptedException e) {
            e.printStackTrace(); 
        }

        System.out.println("Blurring all images took " + 
                           (System.currentTimeMillis() - startTime) + " milliseconds."); 
    }
}
