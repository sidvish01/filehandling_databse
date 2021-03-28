import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class DBM {
    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner kb = new Scanner(System.in);
        System.out.print("Please enter number of files we want to work with today: ");
        int length = Integer.parseInt(kb.nextLine());
        String[] fileName = new String[length];
        Scanner[] csv = new Scanner[length];
        PrintWriter[] json = new PrintWriter[length];

        for (int i = 0; i < length; i++){
           System.out.print("Enter the name of file "+ (i+1) +" :");
            fileName[i] = kb.nextLine();
            try{
                csv[i] = new Scanner(new FileInputStream(fileName[i]));
            }catch (Exception e){
                System.out.println("Could not open " +fileName[i]+" for reading");
                System.out.println("Please check if file exists! Program will terminate after closing all opened files");
                for (int j = i-1; j >= 0; j--){
                    csv[j].close();
                }
                System.exit(0);
            }
        }
        for (int i = 0; i < length; i++){
            try{
                json[i] = new PrintWriter(new FileOutputStream((fileName[i].substring(0, fileName[i].length()-3)+"json")));
            }catch (FileNotFoundException e){
               System.out.println("Could not be create" + (fileName[i].substring(0, fileName[i].length()-3)+"json")+" for writing");
                System.out.println("Program will terminate after deleting all the files that were created and closing all that were opened");
                for (int j = i-1; j >= 0; j--){
                    json[j].close();
                    Files.deleteIfExists(Path.of(fileName[j].substring(0, fileName[j].length() - 3) + "json"));
                }
                for (int k = length-1; k >= 0; k--){
                    csv[k].close();
                }
                System.exit(0);
            }
        }
        for (int i = 0; i < length; i++){
            PrintWriter log = new PrintWriter(new FileOutputStream("errorLog.txt",true));
            processFilesForValidation(csv[i], json[i], log, fileName[i]);
            System.out.println("Processing "+fileName[i]);
            TimeUnit.MILLISECONDS.sleep(500);
            System.out.println("............");
            TimeUnit.MILLISECONDS.sleep(500);
            csv[i].close();
            json[i].close();
            log.close();
        }
        displayProcessedFiles();
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
     */
    static void processFilesForValidation(Scanner csv, PrintWriter json, PrintWriter log, String inputFileName) {
        String dataLine = csv.nextLine();
        Records r = new Records(dataLine);
        Records r1 = null;
        int cntField = 0;
        int cntLine = 0;
        int cntData = 0;

        try {
            for (int i = 0; i < r.splitData.length; i++)
                if (r.splitData[i] == null) cntField++;
            if (cntField > 0) {
                throw new CSVFileInvalidException();
            } else {
                json.println("[");
                while (csv.hasNextLine()) {
                    r1 = new Records(csv.nextLine());
                    try {
                        for (int i = 0; i < r1.splitData.length; i++) {
                            if (r1.splitData[i] == null) {
                                cntData++;
                                cntLine = i;
                            }
                        }
                        if (cntData > 0) throw new CSVDataMissingException();
                        else writeToJsonFile(r, r1, json);
                    } catch (CSVDataMissingException e) {
                        System.out.println("In file " + inputFileName + " line " + cntLine + " not converted to JSON: Missing Data");
                        System.out.println();
                        writeToLogFile(r, r1, log, inputFileName, cntLine, 2);
                        cntData = 0;
                    }
                }
                json.println("]");
            }
        } catch (CSVFileInvalidException e) {
            System.out.println("File " + inputFileName + " is invalid: Field is missing");
            System.out.println("File is not converted to JSON");

            writeToLogFile(r, r1, log, inputFileName, cntField, 1);
            System.out.println("File was not processed");
            System.out.println();
        }
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
            log.println("Missing Field: " + (r.splitData.length - cnt) + " detected, " + cnt + " missing");

            for (int i = 0; i < r.splitData.length - 1; i++) {
                if (r.splitData[i]!=null)
                    log.print(r.splitData[i] + ", ");
                else
                    log.print("***, ");
            }

            if (r.splitData[r.splitData.length - 1]!=null)
                log.println(r.splitData[r.splitData.length - 1]);
            else
                log.println("***");

            System.out.println();
        }
        else{
            /*int to store the position of Field corresponding to missing Data*/
            int line = 0;
            log.println("In file "+ inputFileName +" line "+cnt);
            for (int i = 0; i < r1.splitData.length; i++){
                if (r1.splitData[i]==null){
                    log.print("***   ");
                    line = i;
                }
                else
                    log.print(r1.splitData[i] + "   ");
            }
            log.println();
            log.println("Missing: "+r.splitData[line]);
        }
        log.println("..................................");
        log.println();
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
        for (int i = 0; i < r.splitData.length; i++){
            json.println("      \""+r.splitData[i]+"\":  \""+r1.splitData[i]+"\",");
        }
        json.println("  },");
    }

    /**
     * Prints the file that user inputs
     *<p>
     *     Gives user two attempt to input a valid filename that they wanna display
     *     when user inputs a valid filename displays the data inside
     *     and asks if they want to display any more files
     *     if not, method ends.
     *</p>
     */
    public static void displayProcessedFiles() {
        String displayFileName;
        String displayLine ;
        int prompt = 1;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader bp;

        while (prompt == 1) {
            try {
                System.out.print("Enter the name of the file you want to display: ");
                displayFileName = br.readLine();
                System.out.println();
                try {
                    bp = new BufferedReader((new FileReader(displayFileName)));
                } catch (FileNotFoundException e) {
                    System.out.print("You got one more chance to enter a valid file name, butterfingers: ");
                    displayFileName = br.readLine();
                    bp = new BufferedReader((new FileReader(displayFileName)));
                    System.out.println();
                }
                displayLine = bp.readLine();
                while (displayLine!=null) {
                    System.out.println(displayLine);
                    displayLine = bp.readLine();
                }
                System.out.print("That was all the data inside the file, Enter 1 if you want to display another file: ");
                prompt = Integer.parseInt(br.readLine());
            } catch (FileNotFoundException e) {
                System.out.println("Guess we don't want to display anything, Bye!");
                prompt = 0;
            } catch (Exception e){
                System.out.println("Wwwwhy? I mean that's okay, but you could have just entered 0 instead!");
                prompt = 0;
            }
        }
    }

}