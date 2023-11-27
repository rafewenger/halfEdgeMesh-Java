package halfEdgeMeshMeasure;

import java.util.*;

/** Class for returning min/max angle information */
public class CosMinMaxAngleInfo {

	/** 
	 * Cosine of min angle.
	 * - Note: Min angle has maximum cosine. 
	 */
	public double cos_min_angle;
	
	/**
	 * Cosine of max angle.
	 * - Note: Max angle has minimum cosine.
	 */
	public double cos_max_angle;
	
	/** Index of half edge whose from vertex forms min angle. */
	public int imin;
	
	/** Index of half edge whose to vertex forms max angle. */
	public int imax;
	
	/** True if computation encountered a zero length edge. */
	boolean flag_zero;
	
	/** List of small angle bounds */
	ArrayList<Float> small_angle_bounds = new ArrayList<Float>();
	
	/** List of large angle bounds */
	ArrayList<Float> large_angle_bounds = new ArrayList<Float>();
	
	/** 
	 * num_cells_with_angle_le[i] = 
	 *   Number of cells with angle less than or equal to small_angle_bounds[i].
	 */
	ArrayList<Integer> num_cells_with_angle_le_small = new ArrayList<Integer>();
	
	/**
	 * num_cells_with_angle_ge[i] =
	 *   Number of cells with angle greater than or equal to large_angle_bounds[i].
	 */
	ArrayList<Integer> num_cells_with_angle_ge_large = new ArrayList<Integer>();
	
	
	// *** Get functions ***
	
	public int SizeSmallAngleBounds()
	{ return small_angle_bounds.size(); }
	
	public int SizeLargeAngleBounds()
	{ return large_angle_bounds.size(); }
	
	public float SmallAngleBounds(int i)
	{ return small_angle_bounds.get(i); }
	
	public float LargeAngleBounds(int i)
	{ return large_angle_bounds.get(i); }
	
	public int NumCellsWithAngleLESmall(int i)
	{ return num_cells_with_angle_le_small.get(i); }
	
	public int NumCellsWithAngleGELarge(int i)
	{ return num_cells_with_angle_ge_large.get(i); }
	
	
	// *** Set functions ***
	
	/** Clear lists */
	public void ClearLists() 
	{
		small_angle_bounds.clear();
		large_angle_bounds.clear();
		num_cells_with_angle_le_small.clear();
		num_cells_with_angle_ge_large.clear();
	}

	/** Initialize */
	public void Initialize()
	{
		cos_min_angle = -1;
		cos_max_angle = 1;
		imin = 0;
		imax = 0;
		flag_zero = false;
		ClearLists();
	}


	/** Set cos_min_angle=cos_angle and imin=i */
	public void SetMinAngle(double cos_angle, int i)
	{
		cos_min_angle = cos_angle;
		imin = i;
	}
	
	/** Set cos_max_angle=cos_angle and imax=i */
	public void SetMaxAngle(double cos_angle, int i)
	{
		cos_max_angle = cos_angle;
		imax = i;
	}
	
	/** Set both cos min and cos max angles to cos_angle */
	public void SetMinMaxAngle(double cos_angle, int i)
	{
		SetMinAngle(cos_angle, i);
		SetMaxAngle(cos_angle, i);
	}
	
	
	/** Copy MinMaxInfo fields */
	public void Copy(CosMinMaxAngleInfo info)
	{
		SetMinAngle(info.cos_min_angle, info.imin);
		SetMaxAngle(info.cos_max_angle, info.imax);
	}
	
	
	/** 
	 * Set small_angle_bounds.
	 * - Also sets num_cells_with_angle_le to size small_angle_bounds.size()
	 *   and initializes every number to 0.
	 */
	public void SetSmallAngleBounds(ArrayList<Float> _small_angle_bounds)
	{
		small_angle_bounds.clear();
		small_angle_bounds.addAll(_small_angle_bounds);
		
		num_cells_with_angle_le_small.clear();
		for (int i = 0; i < small_angle_bounds.size(); i++)
			{ num_cells_with_angle_le_small.add(0); } 
	}
	
	
	/** 
	 * Set large_angle_bounds.
	 * - Also sets num_cells_with_angle_ge to size large_angle_bounds.size()
	 *   and initializes every number to 0.
	 */
	public void SetLargeAngleBounds(ArrayList<Float> _large_angle_bounds)
	{
		large_angle_bounds.clear();
		large_angle_bounds.addAll(_large_angle_bounds);
		
		num_cells_with_angle_ge_large.clear();
		for (int i = 0; i < large_angle_bounds.size(); i++)
			{ num_cells_with_angle_ge_large.add(0); } 
	}
	
	
	/** Increment num_cells_with_angle_le[i]. */
	public void IncrementNumCellsWithAngleLE(int i)
	{
		int n = num_cells_with_angle_le_small.get(i);
		num_cells_with_angle_le_small.set(i, n+1);
	}
	
	
	/** Increment num_cells_with_angle_ge[i]. */
	public void IncrementNumCellsWithAngleGE(int i)
	{
		int n = num_cells_with_angle_ge_large.get(i);
		num_cells_with_angle_ge_large.set(i, n+1);
	}
}
