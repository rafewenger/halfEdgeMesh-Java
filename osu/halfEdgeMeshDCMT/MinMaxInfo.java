package osu.halfEdgeMeshDCMT;

/** Class for returning min/max information */
public class MinMaxInfo {

	/** Min value */
	public double minVal;
	
	/** Max value */
	public double maxVal;
	
	/** Index of object with minVal */
	public int imin;
	
	/** Index of object with maxVal */
	public int imax;
	
	/** Initialize to minVal=maxVal=0.0 and imin=imax=0. */
	public void InitializeToZero()
	{
		minVal = 0.0;
		maxVal = 0.0;
		imin = 0;
		imax = 0;
	}
}
