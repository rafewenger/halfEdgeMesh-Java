package halfEdgeMeshMeasure;

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
	
	/** Initialize minVal=maxVal=val and imin=imax=i0. */
	public void Initialize(double val, int i0)
	{
		minVal = val;
		maxVal = val;
		imin = i0;
		imax = i0;
	}
	
	/** Set minVal=val and imin=i */
	public void SetMin(double val, int i)
	{
		minVal = val;
		imin = i;
	}
	
	/** Set maxVal=val and imax=i */
	public void SetMax(double val, int i)
	{
		maxVal = val;
		imax = i;
	}
	
	
	/** Copy MinMaxInfo fields */
	public void Copy(MinMaxInfo min_max_info)
	{
		SetMin(min_max_info.minVal, min_max_info.imin);
		SetMax(min_max_info.maxVal, min_max_info.imax);
	}
}
