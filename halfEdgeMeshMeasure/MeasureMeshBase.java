package halfEdgeMeshMeasure;

/**
 *  @file MeasureMeshBase.java
 *  Functions for computing mesh edge lengths, angles, etc.
 *  @Version 0.1.0
 *  @author Rephael Wenger
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

import java.util.*;

import halfEdgeMesh.CellBase;
import halfEdgeMesh.HalfEdgeBase;
import halfEdgeMesh.HalfEdgeMeshBase;
import halfEdgeMesh.VertexBase;

/**
 * Class to measure mesh (edge lengths, angles, etc.)
 */
public abstract class MeasureMeshBase
	<VERTEX_TYPE extends VertexBase, HALF_EDGE_TYPE extends HalfEdgeBase,
	CELL_TYPE extends CellBase,
	MESH_TYPE extends HalfEdgeMeshBase<VERTEX_TYPE,HALF_EDGE_TYPE,CELL_TYPE> > {
	
	/**
	 * Compute edge length squared of edge half_edge.
	 * @param half_edge Half edge.
	 * @return Edge length squared of edge half_edge.
	 */
	public double compute_edge_length_squared(HalfEdgeBase half_edge)
	{
		VertexBase vfrom = half_edge.FromVertex();
		VertexBase vto = half_edge.ToVertex();
		double length_squared = ComputeGeom.compute_squared_distance(vfrom.coord, vto.coord);
		
		return length_squared;
	}
	
	
	/**
	 * Compute minimum and maximum edge length squared in mesh.
	 * @param mesh Half edge mesh.
	 * @param[out] min_max_info Information on minimum, maximum edge length squared. 
	 */
	public void compute_min_max_edge_lengths_squared
	(MESH_TYPE mesh, MinMaxInfo min_max_info)
	{
		List<HALF_EDGE_TYPE> edge_list = mesh.GetEdgeList();
		if (edge_list.size() == 0) {
			min_max_info.Initialize(0.0, 0);
			return;
		}
		
		boolean flag_set = false;
		for (HalfEdgeBase half_edge:edge_list) {
			final double length_squared = compute_edge_length_squared(half_edge);
			if (!flag_set ||(length_squared < min_max_info.minVal)) 
			{ min_max_info.SetMin(length_squared, half_edge.Index()); }
			
			if (!flag_set || (length_squared > min_max_info.maxVal)) 
			{ min_max_info.SetMax(length_squared, half_edge.Index()); }
			flag_set = true;
		}
	}
	

	/**
	 * Compute minimum and maximum edge length squared in cell.
	 */
	public void compute_cell_min_max_edge_length_squared
	(CellBase cell, MinMaxInfo min_max_info)
	{
		if (cell.NumVertices() == 0) {
			min_max_info.Initialize(0.0, 0);
			return;
		}
		
		HalfEdgeBase half_edge = cell.HalfEdge();
		final double length0_squared = compute_edge_length_squared(half_edge);
		min_max_info.Initialize(length0_squared, half_edge.Index());
		
		for (int i = 1; i < cell.NumVertices(); i++) {
			half_edge = half_edge.NextHalfEdgeInCell();
			final double length_squared = compute_edge_length_squared(half_edge);
			if (length_squared < min_max_info.minVal)
			{ min_max_info.SetMin(length_squared,  half_edge.Index()); }
			
			if (length_squared > min_max_info.maxVal)
			{ min_max_info.SetMax(length_squared,  half_edge.Index()); }
		}
	}
	
	
	/**
	 * Compute cell edge length ratio squared.
	 */
	public void compute_cell_edge_length_ratio_squared
	(CellBase cell, MinCellRatioInfo ratio_info)
	{
		MinMaxInfo min_max_info = new MinMaxInfo();
		
		ratio_info.Initialize();
		compute_cell_min_max_edge_length_squared(cell, min_max_info);
		if (min_max_info.maxVal > 0.0) {
			ratio_info.ratio = min_max_info.minVal / min_max_info.maxVal;
			ratio_info.icell = cell.Index();
			ratio_info.SetMin(min_max_info.minVal, min_max_info.imin);
			ratio_info.SetMax(min_max_info.maxVal, min_max_info.imax);
		}
	}
	
	
	/**
	 * Compute min cell edge length ratio squared.
	 */
	public void compute_min_cell_edge_length_ratio_squared
	(MESH_TYPE mesh, MinCellRatioInfo min_cell_ratio_info)
	{
		MinCellRatioInfo ratio_info = new MinCellRatioInfo();
		
		min_cell_ratio_info.Initialize();
		boolean flag_set = false;
		for (int icell:mesh.CellIndices()) {
			CellBase cell = mesh.Cell(icell);
			compute_cell_edge_length_ratio_squared(cell, ratio_info);
			if (!flag_set || 
				(ratio_info.ratio < min_cell_ratio_info.ratio)) 
			{ 
				min_cell_ratio_info.Copy(ratio_info);
				flag_set = true;
			}
		}
	}
	
	
	/**
	 * Compute cos of angle at half_edge1.FromVertex().
	 */
	public double compute_cos_vertex_angle
	(HalfEdgeBase half_edge1, FlagZero flag_zero)
	{
		HalfEdgeBase half_edge0 = half_edge1.PrevHalfEdgeInCell();
		HalfEdgeBase half_edge2 = half_edge1.NextHalfEdgeInCell();
		VertexBase v0 = half_edge0.FromVertex();
		VertexBase v1 = half_edge1.FromVertex();
		VertexBase v2 = half_edge2.FromVertex();
		
		final double cos_angle = ComputeGeom.compute_cos_triangle_angle
		(v0.coord, v1.coord, v2.coord, flag_zero);
		
		return cos_angle;
	}
	
	
	/**
	 * Compute cos of min and max angles in cell.
	 * - Returns cos_min, cos_max, and index of half edges
	 *   whose from vertices have the min and max angles.
	 * - Returns also flag_zero, true if some edge has zero length.
	 * - Note: min angle has maximum cosine and max angle has minimum cosine.
	 * - Returns angles 0.0, 0.0, if cell has no vertices.
	 * - Ignores vertices incident on 0 length edges.
	 */
	public void compute_cos_min_max_cell_angles
	(CellBase cell, CosMinMaxAngleInfo cos_min_max_angle_info)
	{
		FlagZero flag_zero = new FlagZero();
		
		// Initialize.
		cos_min_max_angle_info.Initialize();

		HalfEdgeBase half_edge = cell.HalfEdge();
		boolean flag_set = false;
		for (int i = 0; i < cell.NumVertices(); i++) {
			final int ihalf_edge = half_edge.Index();
			final double cos_angle = compute_cos_vertex_angle(half_edge, flag_zero);
			if (flag_zero.flag)
				{ cos_min_max_angle_info.flag_zero = true; }
			
			if (!flag_zero.flag) {
				if (!flag_set) {
					cos_min_max_angle_info.SetMinMaxAngle(cos_angle, ihalf_edge);
					flag_set = true;
				}
				else {
					if (cos_angle > cos_min_max_angle_info.cos_min_angle) {
						// Note: if cos angle is large, then angle is small.
						cos_min_max_angle_info.SetMinAngle(cos_angle, ihalf_edge);
					}
				
					if (cos_angle < cos_min_max_angle_info.cos_max_angle) {
						// Note: if cos angle is small, then angle is large.
						cos_min_max_angle_info.SetMaxAngle(cos_angle, ihalf_edge);
					}
				}
			}
			
			half_edge = half_edge.NextHalfEdgeInCell();
		}
		
	}
	
	
	/**
	 * Compute cos of min and max angles in mesh.
	 * - Returns cos_min, cos_max, and index of half edges
	 *   whose from vertices have the min and max angles.
	 * - Returns also flag_zero, true if some edge has zero length.
	 * - Note: min angle has maximum cosine and max angle has minimum cosine.
	 * - Returns angles 0.0, 0.0, if no cells has in mesh.
	 * - Ignores vertices incident on 0 length edges.
	 */
	public void compute_cos_min_max_mesh_angles
	(MESH_TYPE mesh, CosMinMaxAngleInfo cos_min_max_angle_info)
	{
		CosMinMaxAngleInfo cell_angle_info = new CosMinMaxAngleInfo();

		// Initialize.
		cos_min_max_angle_info.Initialize();
		
		boolean flag_set = false;
		for (int icell:mesh.CellIndices()) {
			final CellBase cell = mesh.Cell(icell);
			compute_cos_min_max_cell_angles(cell, cell_angle_info);
			
			final double cos_min_angle = cell_angle_info.cos_min_angle;
			final double cos_max_angle = cell_angle_info.cos_max_angle;
			if (!flag_set) {
				cos_min_max_angle_info.SetMinAngle
					(cos_min_angle, cell_angle_info.imin);
				cos_min_max_angle_info.SetMaxAngle
					(cos_max_angle, cell_angle_info.imax);
				flag_set = true;
			}
	
			if (cos_min_angle > cos_min_max_angle_info.cos_min_angle) {
				// Note: If angle is small, then cos_angle is large.
				cos_min_max_angle_info.SetMinAngle
					(cos_min_angle, cell_angle_info.imin);
			}
			
	
			if (cos_max_angle < cos_min_max_angle_info.cos_max_angle) {
				// Note: If angle is small, then cos_angle is large.
				cos_min_max_angle_info.SetMaxAngle
					(cos_max_angle, cell_angle_info.imax);
			}
		}
	}
	
	
	/**
	 * Compute mesh angle information.
	 * - Return min and max angles and number of angles less than or equal to and
	 *   greater than or equal to lists of angle bounds.
	 * - Returns cos_min, cos_max, and index of half edges
	 *   whose from vertices have the min and max angles.
	 * - Returns also flag_zero, true if some edge has zero length.
	 * - Note: min angle has maximum cosine and max angle has minimum cosine.
	 * - Ignores vertices incident on 0 length edges.
	 * @param small_angle_bounds In cos_min_max_angle_info, return number
	 *   of cells less than or equal to small_angle_bounds[i] for each i.
	 * @param large_angle_bounds In cos_min_max_angle_info, return number
	 *   of cells greater than or equal to large_angle_bounds[i] for each i.
	 */
	public void compute_angle_info
	(MESH_TYPE mesh, 
		ArrayList<Float> small_angle_bounds, ArrayList<Float> large_angle_bounds,
		CosMinMaxAngleInfo cos_min_max_angle_info)
	{
		CosMinMaxAngleInfo cell_angle_info = new CosMinMaxAngleInfo();
		ArrayList<Double> cos_small_angle_bounds = new ArrayList<Double>();
		ArrayList<Double> cos_large_angle_bounds = new ArrayList<Double>();

		// Initialize.
		cos_min_max_angle_info.Initialize();
		cos_min_max_angle_info.SetSmallAngleBounds(small_angle_bounds);
		cos_min_max_angle_info.SetLargeAngleBounds(large_angle_bounds);
		
		for (int i = 0; i < small_angle_bounds.size(); i++) {
			final float A = small_angle_bounds.get(i);
			final double cosA = Math.cos(Math.toRadians(A));
			cos_small_angle_bounds.add(cosA);
		}
		
		for (int i = 0; i < large_angle_bounds.size(); i++) {
			final float A = large_angle_bounds.get(i);
			final double cosA = Math.cos(Math.toRadians(A));
			cos_large_angle_bounds.add(cosA);
		}
		
		boolean flag_set = false;
		for (int icell:mesh.CellIndices()) {
			final CellBase cell = mesh.Cell(icell);
			compute_cos_min_max_cell_angles(cell, cell_angle_info);
			
			final double cos_min_angle = cell_angle_info.cos_min_angle;
			final double cos_max_angle = cell_angle_info.cos_max_angle;
			if (!flag_set) {
				cos_min_max_angle_info.SetMinAngle
					(cos_min_angle, cell_angle_info.imin);
				cos_min_max_angle_info.SetMaxAngle
					(cos_max_angle, cell_angle_info.imax);
				flag_set = true;
			}
	
			if (cos_min_angle > cos_min_max_angle_info.cos_min_angle) {
				// Note: If angle is small, then cos_angle is large.
				cos_min_max_angle_info.SetMinAngle
					(cos_min_angle, cell_angle_info.imin);
			}
			
	
			if (cos_max_angle < cos_min_max_angle_info.cos_max_angle) {
				// Note: If angle is small, then cos_angle is large.
				cos_min_max_angle_info.SetMaxAngle
					(cos_max_angle, cell_angle_info.imax);
			}
			
			for (int i = 0; i < small_angle_bounds.size(); i++) {
				if (cos_min_angle >= cos_small_angle_bounds.get(i)) {
					// Note: If cos_min_angle >= cos_min_angle_bounds[i],
					//  then min_angle <= min_angle_bounds[i].
					cos_min_max_angle_info.IncrementNumCellsWithAngleLE(i);
				}
			}
			
			for (int i = 0; i < large_angle_bounds.size(); i++) {
				if (cos_max_angle <= cos_large_angle_bounds.get(i)) {
					// Note: if cos_max_angle <= cos_max_angle_bounds[i],
					//   then max_angle >= max_angle_bounds[i].
					cos_min_max_angle_info.IncrementNumCellsWithAngleGE(i);
				}
			}
		}
	}
	
}