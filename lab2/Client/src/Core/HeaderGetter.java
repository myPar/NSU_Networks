package Core;
import Core.Client.Mode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

class HeaderGetter {
    // if mode equals 'TEST' - this file contains header data
    private final String inputFileName = "header_data.txt";
    // delimiter
    private String delimiter = "\\\\";

    byte[] getHeader(Mode mode, File file) {
        switch (mode) {
            case TEST: {
                Scanner sc = null;
                FileInputStream input;
                try {
                    input = new FileInputStream(inputFileName);
                    sc = new Scanner(input);
                }
                catch (FileNotFoundException e) {
                    assert false;
                }
                String str = sc.nextLine();
                return str.getBytes();
            }
            case NORMAL: {
                return (file.getName() + delimiter + file.length() + delimiter).getBytes();
            }
            default:
                assert false;
        }
        // invalid case
        return null;
    }
}
