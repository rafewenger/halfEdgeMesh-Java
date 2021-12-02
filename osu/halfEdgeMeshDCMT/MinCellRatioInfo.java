package osu.halfEdgeMeshDCMT;

public class MinCellRatioInfo extends MinMaxInfo {
	
	/** Minimum ratio of min to max edge length in a cell */
	public double ratio;
	
	/** Index of cell that has minimum ratio */
	public int icell;

	public void Initialize()
	{
		// Default ratio is 1.0.
		ratio = 1.0;
		icell = 0;
		
		super.InitializeToZero();
	}
}
