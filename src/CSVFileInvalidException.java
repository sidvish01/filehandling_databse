/**
 * Exception class for when error occurs due to missing attribute
 */
public class CSVFileInvalidException extends InvalidException{
    public CSVFileInvalidException() {
        super();
    }
    public CSVFileInvalidException(String msg) { super(msg); }
}
