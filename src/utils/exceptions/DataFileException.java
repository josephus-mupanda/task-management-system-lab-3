package utils.exceptions;

// thrown when the data file cannot be read or written (missing file, malformed data...)
public class DataFileException extends RuntimeException {

    /** Creates the exception with the reason shown to the user, ex: "File not found". */
    public DataFileException(String message) {
        super(message);
    }
}
