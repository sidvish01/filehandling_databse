import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class DBM {
    public static void main(String[] args) throws FileNotFoundException {

        Scanner csv = new Scanner(new FileInputStream("Car Maintenance Record.csv"));
        PrintWriter json = new PrintWriter(new FileOutputStream("Car Maintenance Record.json"));
        PrintWriter log = new PrintWriter(new FileOutputStream("errorLog.txt"));

        int p = processFilesForValidation(csv, json, log, "Car Maintenance Record.csv");
        System.out.println(p);
        json.close();
        log.close();
        json.close();
    }


    /**
     * Method used to process files to check if they contain valid information
     * <p>
     * First checks if there is a missing field in the first line of the the csv file
     * if there is a missing field it throws exception that will print error msg to user and call writeToLog function
     * nothing is written to the output file in this case and the method returns -0
     * if not, it starts reading data in following lines
     * if any line has data missing it will throw exception that will print error msg and call writeToLog function
     * but it will continue reading consecutive line
     * if a line has all the data it will call writeToJsonFile function with field and data line as arguments
     * </p>
     *
     * @param csv           scanner object that has file from which data is read
     * @param json          printwriter object that has file to which json objects are written
     * @param log           printwriter object that has file that logs any error in input file
     * @param inputFileName string that contains the name of the file from which data is being read
     * @return -0 if file has a missing field else 1
     */
    static int processFilesForValidation(Scanner csv, PrintWriter json, PrintWriter log, String inputFileName) {
        String dataLine = csv.nextLine();
        Records r = new Records(dataLine);
        Records r1 = null;
        int cntField = 0;
        int cntLine = 0;
        int cntData = 0;

        try {
            for (int i = 0; i < r.finalData.length; i++)
                if (r.finalData[i] == null) cntField++;
            if (cntField > 0) {
                throw new CSVFileInvalidException();
            } else {
                while (csv.hasNextLine()) {
                    r1 = new Records(csv.nextLine());
                    try {
                        for (int i = 0; i < r1.finalData.length; i++) {
                            if (r1.finalData[i] == null) {
                                cntData++;
                                cntLine = i;
                            }
                        }

                        if (cntData > 0) throw new CSVDataMissingException();
                        else writeToJsonFile(r, r1, json);
                    } catch (CSVDataMissingException e) {
                        System.out.println("In file " + inputFileName + " line " + cntLine + " not converted to JSON: Missing Data");

                        writeToLogFile(r, r1, log, inputFileName, cntLine, 2);
                        cntData = 0;
                    }
                }
            }
        } catch (CSVFileInvalidException e) {
            System.out.println("File " + inputFileName + " is invalid: Field is missing");
            System.out.println("File is not converted to JSON");

            writeToLogFile(r, r1, log, inputFileName, cntField, 1);
            return -0;
        }
        return 1;
    }

    /**
     * Writes data information in error log
     * <p>
     *     checks errorCode to see if the error was caused due to missing Fields or missing Data
     *     then writes error information in corresponding format
     * </p>
     * @param r records object containing Fields line
     * @param r1 records object containing Data line
     * @param log printwriter object that has file that logs any error in input file
     * @param inputFileName string that contains the name of the file from which data is being read
     * @param cnt int that contains either number of fields missing or line no which has data missing depending on errorCode
     * @param errorCode int is equal to 1 if error thrown due to missing Field or contains 2 if it was due to missing data in a record line
     */
    private static void writeToLogFile(Records r, Records r1, PrintWriter log, String inputFileName, int cnt, int errorCode) {
        if (errorCode==1){
            log.println("File "+inputFileName+" is invalid");
            log.println("Missing Field: " + (r.finalData.length - cnt) + " detected, " + cnt + " missing");

            for (int i = 0; i < r.finalData.length - 1; i++) {
                if (r.finalData[i]!=null)
                    log.print(r.finalData[i] + ", ");
                else
                    log.print("***, ");
            }

            if (r.finalData[r.finalData.length - 1]!=null)
                log.println(r.finalData[r.finalData.length - 1]);
            else
                log.println("***");

            System.out.println();
        }
        else{
            /*int to store the position of Field corresponding to missing Data*/
            int line = 0;
            log.println("In file "+ inputFileName +" line "+cnt);
            for (int i = 0; i < r1.finalData.length; i++){
                if (r1.finalData[i]==null){
                    log.print("***   ");
                    line = i;
                }
                else
                    log.print(r1.finalData[i] + "   ");
            }
            log.println();
            log.println("Missing: "+r.finalData[line]);
        }
    }

    /**
     *Writes valid data to file in JSON object format
     *
     * @param r records object containing Fields line
     * @param r1 r1 records object containing Data line
     * @param json printwriter object that has file to which json objects are written
     */
    private static void writeToJsonFile(Records r, Records r1, PrintWriter json) {
        json.println("  {");
        for (int i = 0; i < r.finalData.length; i++){
            json.println("       \""+r.finalData[i]+"\":  \""+r1.finalData[i]+"\",");
        }
        json.println("  },");
    }

}