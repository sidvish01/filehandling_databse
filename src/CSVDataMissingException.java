/**
 * Exception class for when error occurs due to missing data
 */
public class CSVDataMissingException extends InvalidException{
    public CSVDataMissingException() {
        super();
    }

    public CSVDataMissingException(String msg) { super(msg); }
}
