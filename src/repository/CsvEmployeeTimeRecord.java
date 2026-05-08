/**
 *
 * @author Leianna Cruz
 */

package repository;



import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvEmployeeTimeRecord  {

    private final String filePath = "data/MotorPH Employee Record.csv";

    private void ensureFileExists() {
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create MotorP.csv: " + e.getMessage());
        }
    }
    
}

