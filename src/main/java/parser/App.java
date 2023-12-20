package parser;

import java.io.*;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    //Modify this value to match the file when executing (Path from project's root)
    private static final String filePath = System.getProperty("user.dir") +  "/src/test/resources/text0.mp4";

    public static void main( String[] args ) {
        File inputFile = new File(filePath);
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            parseFile(fis, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The parseFile method parses the file and reads the size and type of boxes in it.
     * @param stream - the FileInputStream stream
     * @param depth - the depth of the boxes
     * @throws IOException - if the stream has been closed. See FileInputStream.available() reference.
     */
    private static void parseFile(FileInputStream stream, int depth) throws IOException, InterruptedException{
        while (stream.available() > 0) {
            int boxSize = readInt(stream);
            String boxType = readBoxType(stream);
            String indentation = "    ".repeat(depth);
            if ("mdat".equals(boxType)) {
                System.out.println("Box ID: " + boxType + " of size " + boxSize);
                parseMdatBox(stream);
            } else if ("moof".equals(boxType) || "traf".equals(boxType)) {
                System.out.println(indentation + "Box ID: " + boxType + " of size " + boxSize);
                parseFile(stream, depth + 1);
            } else {
                System.out.println(indentation + "Box ID: " + boxType + " of size " + boxSize);
                stream.skip(boxSize - 8L);
            }
        }
    }


    /**
     * Reads 4 bytes from the stream to determine the box size and returns it.
     *
     * @param stream - the stream of bytes of the MPEG file
     * @return - an integer representing the box size
     * @throws IOException
     */
    private static int readInt(FileInputStream stream) throws IOException {
        byte[] bytes = new byte[4];
        stream.read(bytes);
        // 1000 0000(24) 1000 0000(16) 1000 0000(8) 1000 0000
        return bytes[0] << 24 | (bytes[1] & 0xff) << 16 | (bytes[2] & 0xff) << 8 | (bytes[3] & 0xff);
    }

    /**
     * Reads 4 bytes from the stream to determine the box type and returns it.
     * @param stream - the stream of bytes of the MPEG file
     * @return - String representing the box type
     * @throws IOException
     */
    private static String readBoxType(FileInputStream stream) throws IOException {
        byte[] bytes = new byte[4];
        stream.read(bytes);
        return new String(bytes);
    }

    /**
     * This method prints the content of the MDAT box to the console.
     * It also decodes the Base64 images on it and generates the files for them.
     * @param stream - the byte stream of the file
     * @throws IOException
     */
    private static void parseMdatBox(InputStream stream) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        StringBuilder currentImage = new StringBuilder();
        System.out.println("Mdat content:");
        Pattern imageNamePattern = Pattern.compile("id=\"[^\"]*\"");
        Pattern imageTypePattern = Pattern.compile("imagetype=\"[^\"]*\"");
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            if (line.contains("<smpte:image")) {
                Matcher nameMatcher = imageNamePattern.matcher(line);
                Matcher typeMatcher = imageTypePattern.matcher(line);
                String imageName = "", imageType = "";
                while (nameMatcher.find() && typeMatcher.find()) {
                    imageName = nameMatcher.group().substring(4, nameMatcher.group().length()-1);
                     imageType = typeMatcher.group().substring(11, typeMatcher.group().length()-1).toLowerCase();
                }
                line = reader.readLine();
                System.out.println(line);
                byte[] imageBytes = Base64.getDecoder().decode(line);
                try (OutputStream os = new FileOutputStream(imageName + "."+ imageType)) {
                    os.write(imageBytes);
                }
                currentImage.setLength(0);
            }
        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}