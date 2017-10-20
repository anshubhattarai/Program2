package exception;

public class ElementUnplacableException extends Exception {
    public ElementUnplacableException(){
        super("Element cannot be placed here");
    }

    public ElementUnplacableException(String message){
        super(message);
    }
}