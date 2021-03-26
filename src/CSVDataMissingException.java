public class CSVDataMissingException extends Exception{
    public CSVDataMissingException() {
        super("Error: Input row cannot be parsed due to missing information");
    }

    public CSVDataMissingException(String msg) {
        super(msg);
    }
}
