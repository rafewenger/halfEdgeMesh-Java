package halfEdgeMeshMeasure;

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
		
		super.Initialize(0.0, 0);
	}
	
	
	/** Copy min_cell_ratio fields */
	public void Copy(MinCellRatioInfo min_cell_ratio_info)
	{
		super.Copy(min_cell_ratio_info);
		ratio = min_cell_ratio_info.ratio;
		icell = min_cell_ratio_info.icell;
	}
}
