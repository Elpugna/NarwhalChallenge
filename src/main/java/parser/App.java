package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class App {
    //Modify this value to match the file when executing (Path from project's root)
    private static final String filePath = System.getProperty("user.dir") +  "/src/test/resources/text0.mp4";

    public static void main( String[] args ) {
        File inputFile = new File(filePath);
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            parseFile(fis, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The parseFile Function parses the file and reads the size and type ob boxes on it.
     * @param stream - the stream of type FileInputStream
     * @param depth - the depth of the boxes
     * @throws IOException - if the stream has been closed. See FileInputStream.available() reference.
     */
    private static void parseFile(FileInputStream stream, int depth) throws IOException{
        while (stream.available() > 0) {
            int boxSize = readInt(stream);
            String boxType = readBoxType(stream);
            String indentation = "    ".repeat(depth);
            System.out.println(indentation + "Box ID: " + boxType + " of size " + boxSize);
            if ("mdat".equals(boxType)) {
                parseMdatBox(stream);
            } else if ("moof".equals(boxType) || "traf".equals(boxType)) {
                parseFile(stream, depth + 1);
            } else {
                stream.skip(boxSize - 8);
            }
        }
    }


    /**
     * Reads 4 bytes of the stream that determines the box size and returns it
     *
     * @param stream - the stream of bytes of the mpeg file
     * @return - int representing the box size
     * @throws IOException
     */
    private static int readInt(FileInputStream stream) throws IOException {
        byte[] bytes = new byte[4];
        stream.read(bytes);
        // 1000 0000(24) 1000 0000(16) 1000 0000(8) 1000 0000
        return bytes[0] << 24 | (bytes[1] & 0xff) << 16 | (bytes[2] & 0xff) << 8 | (bytes[3] & 0xff);
    }

    /**
     * Readt 4 bytes of the stream that determines the box type and returns it
     * @param stream - the stream of bytes of the mpeg file
     * @return - String representing the box type
     * @throws IOException
     */
    private static String readBoxType(FileInputStream stream) throws IOException {
        byte[] bytes = new byte[4];
        stream.read(bytes);
        return new String(bytes);
    }

    /**
     * This function prints to console the content of the mdat box.
     * @param stream - stream of bytes of the file.
     * @throws IOException
     */
    private static void parseMdatBox(FileInputStream stream) throws IOException {
        byte[] mdatContent = new byte[stream.available()];
        stream.read(mdatContent);
        String mdatContentString = new String(mdatContent, "UTF-8");
        System.out.println("Mdat content:");
        System.out.println(mdatContentString);
    }
}