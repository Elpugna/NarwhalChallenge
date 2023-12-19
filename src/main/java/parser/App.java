package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
            if ("mdat".equals(boxType)) {
                System.out.println("Box ID: " + boxType + " of size " + boxSize);
                parseMdatBox(stream, boxSize);
            } else if ("moof".equals(boxType) || "traf".equals(boxType)) {
                System.out.println(indentation + "Box ID: " + boxType + " of size " + boxSize);
                parseFile(stream, depth + 1);
            } else {
                System.out.println(indentation + "Box ID: " + boxType + " of size " + boxSize);
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
    private static void parseMdatBox(InputStream stream, int boxSize) throws IOException {
        int mdatInfoSize = boxSize - 8;
        byte[] buffer = new byte[1024];

        int totalBytesRead = 0;

        System.out.println("Mdat content:");

        while (totalBytesRead < mdatInfoSize) {
            int bytesToRead = Math.min(buffer.length, mdatInfoSize - totalBytesRead);
            int bytesRead = stream.read(buffer, 0, bytesToRead);

            if (bytesRead == -1) {
                break;
            }
            String data = new String(buffer);
            if (bytesToRead < buffer.length) {
                data = data.substring(0, bytesRead);
            }
            System.out.print(data);

            totalBytesRead += bytesRead;
        }
    }
}