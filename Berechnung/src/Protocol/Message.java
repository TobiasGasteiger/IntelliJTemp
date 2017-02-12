package Protocol;

import java.io.Serializable;


public abstract class Message implements Serializable {

	private static final long serialVersionUID = -5916299812031439353L;
	
	public abstract String getMessage();

	public abstract void setMessage(String message);
	
}
