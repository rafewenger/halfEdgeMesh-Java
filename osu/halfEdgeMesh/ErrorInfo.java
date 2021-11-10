package osu.halfEdgeMesh;


/** Class containing error information. */
public class ErrorInfo {
	
	/** If true, an error was detected. */
	public boolean flag_error;
	
	/** Index of object (vertex, half edge or cell) that triggered the error. */
	public int index;
	
	/** Error message. */
	public String message;
	
	/** Constructor. */
	public ErrorInfo()
	{
		flag_error = false;
		index = 0;
	}
	
	// Set function
	
	/** Set error flag to true, and this.index to index. */
	public void SetError(int index)
	{ 
		flag_error = true;
		this.index = index; 
	}
	
	/** Set error message to s. */
	public void SetMessage(String s)
	{ message = s; }
	
	
	// Get functions.
	
	/** Return index of object creating problem. 
	 * <ul> <li> Type of object (vertex, half edge, cell) must be determined
	 *  		from context.
	 *  </ul>*/
	public int Index()
	{ return index; }
	
	/** Return an error message. */
	public String Message()
	{ return message; }
	
	/** Return error flag. If true, an error was detected. */
	public boolean FlagError()
	{ return flag_error; }
	
};
