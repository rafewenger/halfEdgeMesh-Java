package halfEdgeMeshMeasure;

/**
 *  \file ComputeGeom.java
 *  Functions for geometric computations (midpoints, edge lengths, angles, etc.)
 */


/*
* Copyright (C) 2021-2023 Rephael Wenger
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public License
* (LGPL) as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
* 
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


/// Class for geometric computations (midpoints, edge lengths, angles, etc.)
public class ComputeGeom {
	
	/** Vertex dimension. Number of vertex coordinates. */
	static final int DIMENSION = 3;
	
	/** Return DIMENSION */
	public static int Dimension()
	{ return DIMENSION; }
	
	
	/** Compute the midpoint of coord0[] and coord1[]
	 *    and store in coord2[].
	 */
	public static void compute_midpoint
	(float[] coord0, float[] coord1, float[] coord2)
	{
		for (int d = 0; d < Dimension(); d++)
		{ coord2[d] = (coord0[d] + coord1[d])/2.0f; }
	}
	
	
	/** Compute the squared distance between two coordinates. */
	public static double compute_squared_distance
	(float[] coord0, float coord1[])
	{
		double sum = 0.0;
		for (int d = 0; d < Dimension(); d++) {
			double diff = coord0[d] - coord1[d];
			sum += (diff*diff);
		}
		
		return sum;
	}

	
	/** Compute the square of a vector magnitude */
	public static double compute_magnitude_squared(float[] coord)
	{
		double magnitude_squared = 0.0;
		for (int d = 0; d < DIMENSION; d++) {
			double c = coord[d];
			magnitude_squared += (c*c);
		}
		
		return magnitude_squared;
	}

	
	/** Compute the vector magnitude */
	public static double compute_magnitude(float[] coord)
	{
		double magnitude_squared = 
				compute_magnitude_squared(coord);
		return Math.sqrt(magnitude_squared);
	}

	
	/** Set all coordinates to c. */
	public static void set_all_coord(float c, float[] coord)
	{
		for (int d = 0; d < DIMENSION; d++)
		{ coord[d] = c; }
	}
	
	
	/** Copy coord0[] to coord1[]. */
	public static void copy_coord(float[] coord0, float[] coord1)
	{
		for (int d = 0; d < DIMENSION; d++)
		{ coord1[d] = coord0[d]; }
	}
	
	
	/** Divide coord by a (non-zero) scalar */
	public static void divide_by_scalar(float c, float[] coord)
	{
		for (int d = 0; d < DIMENSION; d++)
		{ coord[d] = coord[d]/c; }
	}
		

	/** Subtract coord1[] from coord0[]. */
	public static void subtract_coord
	(float[] coord0, float[] coord1, float[] coord2)
	{
		for (int d = 0; d < DIMENSION; d++)
		{ coord2[d] = coord0[d]	- coord1[d]; }
	}
		
	
	/** Normalize vector.
	 * - Also returns the magnitude of the original vector.
	 * - If all coordinates are 0, the magnitude will be set to 0,
	 *   and the vector to (1,0,0).
	 */
	public static double normalize_vector(float[] coord)
	{
		double magnitude = compute_magnitude(coord);
		
		if (Math.abs(magnitude) == 0.0) {
			magnitude = 0.0;
			set_all_coord(0, coord);
			coord[0] = 1.0f;
			return magnitude;
		}
		
		// Extra processing to avoid overflow if vector has small magnitude.
		
		// Get the coordinates with maximum absolute value.
		float max_abs_coord = Math.abs(coord[0]);
		int dmax = 0;
		
		for (int d = 1; d < Dimension(); d++) {
			float abs_c = Math.abs(coord[d]);
			if (abs_c > max_abs_coord) {
				max_abs_coord = abs_c;
				dmax = d;
			}
		}
		
		// Divide by max_abs_coord.
		for (int d = 0; d < Dimension(); d++) {
			if (d == dmax) {
				// Ensure that coord[d] is 1 or -1.
				if (coord[d] < 0)
				{ coord[d] = -1; }
				else
				{ coord[d] = 1; }
			}
			else {
				// Note: abs(coord[d]) <= max_abs_coord.
				coord[d] = coord[d]/max_abs_coord;
			}
		}
		
		// Since coord[dmax] is 1 or -1, magnitudeB >= 1.
		float magnitudeB = (float) compute_magnitude(coord);
		
		// Divide by magnitudeB.
		divide_by_scalar(magnitudeB, coord);
		
		return magnitude;
	}
	
	
	/** Normalize vector.
	 * - Also returns the magnitude of the original vector.
	 * - Version that stores normalized vector in coord1[].
	 */
	public static double normalize_vector
	(float[] coord0, float[] coord1)
	{
		copy_coord(coord0, coord1);
		return normalize_vector(coord1);
	}
	
	
	/** Return the inner product (dot product) of two vectors. */
	public static double compute_inner_product
	(float[] coord0, float[] coord1)
	{
		double product = 0.0;
		for (int d = 0; d < Dimension(); d++)
		{ product += (coord0[d] * coord1[d]); }
		
		return product;
	}
	
	/** Compute cosine of angle between two vectors */
	public static double compute_cos_angle
	(float[] coord0, float[] coord1, FlagZero flag_zero)
	{
		float[] temp_coord0 = new float[DIMENSION];
		float[] temp_coord1 = new float[DIMENSION];
		double magnitude0 = normalize_vector(coord0, temp_coord0);
		double magnitude1 = normalize_vector(coord1, temp_coord1);
			if ((magnitude0 == 0.0) || (magnitude1 == 0.0)) {
			flag_zero.flag = true;
			return 0.0;
		}
		
		flag_zero.flag = false;
		
		double cos_angle =
			compute_inner_product(temp_coord0, temp_coord1);
		
		// Clamp to [-1,1] to handle numerical errors.
		if (cos_angle < -1) { cos_angle = -1; }
		if (cos_angle > 1) { cos_angle = 1; }
		
		return cos_angle;
	}
		
		
	/** Compute cosine of triangle angle at coord1[]
	 * in triangle(coord0[], coord1[], coord2[]).
	 * - If coord0[] == coord1[], (or is very, very close,) or
	 *   coord2[] == coord1[] (or is very, very close,)
	 *   sets flag_zero to true and returns 0.
	 */
	public static double compute_cos_triangle_angle
	(float[] coord0, float[] coord1, float[] coord2, 
			FlagZero flag_zero)
	{
		float[] vect0 = new float[DIMENSION];
		float[] vect1 = new float[DIMENSION];
		subtract_coord(coord0, coord1, vect0);
		subtract_coord(coord2, coord1, vect1);
		
		double cos_angle =
			compute_cos_angle(vect0, vect1, flag_zero);
		
		return cos_angle;
	}

}