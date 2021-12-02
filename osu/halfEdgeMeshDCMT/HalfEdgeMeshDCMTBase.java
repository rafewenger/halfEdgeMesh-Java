package osu.halfEdgeMeshDCMT;

import osu.halfEdgeMesh.*;

/** Half edge mesh supporting decimation operations. */

public abstract class HalfEdgeMeshDCMTBase
<VERTEX_TYPE extends VertexBase, HALF_EDGE_TYPE extends HalfEdgeDCMTBase, 
	CELL_TYPE extends CellDCMTBase> 
extends HalfEdgeMeshBase<VERTEX_TYPE,HALF_EDGE_TYPE,CELL_TYPE> {
	
	// *** Internal methods ***
	
	/** Link half edges around edges merged by merging v0 and v1. */
	protected void _LinkHalfEdgesAroundMergedEdges
	(VertexDCMTBase v0, VertexDCMTBase v1)
	{
		v0._ClearVisitedFlagsInAdjacentVertices();
		v1._SetVisitedFlagsInAdjacentVertices(true);
		
		for (int k = 0; k < v1.NumHalfEdgesFrom(); k++) {
			HalfEdgeDCMTBase half_edgeA = v1.KthHalfEdgeFrom(k);
			VertexDCMTBase vtoA = half_edgeA.ToVertex();
			
			if (vtoA._IsVisited()) {
				// vtoA is a neighbor of v0 and v1.
				
				_FindAndLinkHalfEdgeAroundEdge(v0, vtoA, half_edgeA);
				
				// Set vtoA.visited_flag to false so that vtoA
				//   will not be processed twice.
				vtoA.visited_flag = false;
			}
			
			HalfEdgeDCMTBase half_edgeB =
					half_edgeA.PrevHalfEdgeInCell();
			VertexDCMTBase vfromB = half_edgeB.FromVertex();
			
			// Check vfromB to handle boundary edges and/or cells
			//   with arbitrary orientations.
			if (vfromB._IsVisited()) {
				// vfromB is a neighbor of v0 and v1.
				
				_FindAndLinkHalfEdgeAroundEdge(v0, vfromB, half_edgeB);
				
				// Set vfromB.visited_flag to false so that vfromB
				//   will not be processed twice.
				vfromB.visited_flag = false;
			}
		}
	}

	
	// *** Collapse/Split/Join methods ***
	
	/** Collapse edge mapping two vertices to a single vertex.
	 *  - New vertex is at edge midpoint.
	 *  - Returns new vertex.
	 *  - Returns null if collapse is illegal.
	 *    (See IsIllegalEdgeCollapse().)
	 *  - Note: This routine DOES NOT check/ensure that mesh
	 *    will be a manifold after the edge collapse.
	 */
	public VertexDCMTBase
		CollapseEdge(int ihalf_edge0) throws Exception
	{
		HALF_EDGE_TYPE half_edge0 = HalfEdge(ihalf_edge0);
		
		if (IsIllegalEdgeCollapse(half_edge0))
		{ return null; }
		
		int max_num_half_edges_around_edge =
			half_edge0.FromVertex().NumHalfEdgesFrom() +
			half_edge0.ToVertex().NumHalfEdgesFrom();
			
		VertexDCMTBase v0 = half_edge0.FromVertex();
		VertexDCMTBase v1 = half_edge0.ToVertex();
		
		// Update *.next_half_edge_around_edge links.
		_LinkHalfEdgesAroundMergedEdges(v0, v1);
		
		// Update *.prev_half_edge_in_cell and 
		//   *.next_half_edge_in_cell links.
		// Delete triangle cells containing edge (v0,v1).
		HalfEdgeDCMTBase half_edge = half_edge0;
		int k = 0;
		do {
			CellDCMTBase cell = half_edge.Cell();
			HalfEdgeDCMTBase prev_half_edge =
					half_edge.PrevHalfEdgeInCell();
			HalfEdgeDCMTBase next_half_edge =
					half_edge.NextHalfEdgeInCell();
			
			if (cell.HalfEdge() == half_edge)
			{ cell.SetHalfEdge(half_edge.NextHalfEdgeInCell()); }
			
			if (cell.IsTriangle()) {
				// *** DEBUG ***
				//System.err.printf("Deleting cell %d%n", cell.Index());
				
				// Cell is a triangle.
				// Collapsing half edge removes cell.
				VertexDCMTBase v2 = prev_half_edge.FromVertex();
				_DeleteHalfEdge(prev_half_edge);
				_DeleteHalfEdge(next_half_edge);
				_DeleteCell(cell);
				v2._MoveBoundaryHalfEdgeToHalfEdgeFrom0();
			}
			else {
				_RelinkHalfEdgesInCell(prev_half_edge, next_half_edge);
				
				cell.DecrementNumVertices();
			}
			
			half_edge = half_edge.NextHalfEdgeAroundEdge();
			k++;
		} while(k < max_num_half_edges_around_edge &&
				half_edge != half_edge0);
		
		// Compute an upper bound on the number of half edges
		//   with endpoints (v0,v1).
		int max_numh = v0.NumHalfEdgesFrom() + v1.NumHalfEdgesFrom();
		
		// Move all half edges from v0 to be from v1.
		// - Moves v0.half_edge_from_list to v1.half_edge_list.
		_MoveVertexHalfEdgeFromList(v0, v1);
		
		// Set v1 to midpoint of (v0,v1).
		ComputeCoord.compute_midpoint(v0.coord, v1.coord, v1.coord);
		
		// Delete half edges around edge (v0,v1).
		_DeleteHalfEdgesAroundEdge(half_edge0, max_numh);
		
		_DeleteVertex(v0);
		
		v1._MoveBoundaryHalfEdgeToHalfEdgeFrom0();

		return v1;
	}
	

	// *** Methods to check edge collapses. ***
	
	/** Return true if edge collapse is illegal.
	 *  - Edge collapse (vA,vB) is illegal if some cell
	 *      contains both vA and vB but not edge (vA,vB).
	 */
	public boolean IsIllegalEdgeCollapse
	(VertexDCMTBase vA, VertexDCMTBase vB)
	{
		if (vA.NumHalfEdgesFrom() > vB.NumHalfEdgesFrom()) {
			// Swap vA and vB to reduce number of cells processed.
			return IsIllegalEdgeCollapse(vB, vA);
		}
		else {
			
			for (int k = 0; k < vA.NumHalfEdgesFrom(); k++) {
				HalfEdgeDCMTBase half_edge0 = vA.KthHalfEdgeFrom(k);
				CellDCMTBase cell = half_edge0.Cell();
				if (cell.NumVertices() < 4) {
					// All pairs of cell vertices form an edge.
					continue;
				}
				
				HalfEdgeDCMTBase half_edge =
						half_edge0.NextHalfEdgeInCell().NextHalfEdgeInCell();
				for (int i = 2; i < cell.NumVertices()-2; i++) {
					if (half_edge.FromVertex() == vB)
					{ return true; }
				}
			}
		}
		
		return false;
	}
	
	
	/** Return true if edge collapse is illegal.
	 *  - Version that takes HalfEdgeDCMTBase as argument.
	 */
	public boolean IsIllegalEdgeCollapse
	(HalfEdgeDCMTBase half_edge)
	{
		return IsIllegalEdgeCollapse
				(half_edge.FromVertex(), half_edge.ToVertex());
	}
		
	
	/** Return true if half edge endpoints and v are 
	 * in a mesh triangle.
	 */
	public boolean IsInTriangle
		(HalfEdgeDCMTBase half_edge0, VertexDCMTBase v)
	{
		// Cannot have more than max_numh half edges around this edge.
		int max_numh =
			half_edge0.FromVertex().NumHalfEdgesFrom() +
			half_edge0.ToVertex().NumHalfEdgesFrom();
		
		HalfEdgeDCMTBase half_edge = half_edge0;
		int k = 0;
		do {
			if (half_edge.Cell().IsTriangle()) {
				HalfEdgeDCMTBase prev_half_edge =
					half_edge.PrevHalfEdgeInCell();
				
				if (prev_half_edge.FromVertex() == v)
				{ return true; }
			}
			
			half_edge = half_edge.NextHalfEdgeAroundEdge();
			k++;
		} while ((k < max_numh) && (half_edge != half_edge0));
		
		return false;
	}
	
	
	/** Return true if cell icell is a triangle whose three edges
	 *    are boundary edges.
	 */
	public boolean IsIsolatedTriangle(int icell)
	{
		int THREE = 3;
		
		CellDCMTBase cell = Cell(icell);
		if (cell == null) { return false; }
		
		if (!cell.IsTriangle())
			{ return false; }
		
		HalfEdgeDCMTBase half_edge = cell.HalfEdge();
		for (int i = 0; i < THREE; i++) {
			if (!half_edge.IsBoundary())
			{ return false; }
			
			half_edge = half_edge.NextHalfEdgeInCell();
		}
		
		// Cell has three vertices (and three edges) and 
		//   all cell edges are boundary edges.
		return true;
	}
	
	
	/** Return true if cell icell is in the boundary 
	 *    of a tetrahedron.
	 */
	public boolean IsInTetrahedron(int icell)
	{
		CellDCMTBase cell0 = Cell(icell);
		if (cell0 == null) { return false; }
		
		if (!cell0.IsTriangle()) 
			{ return false; }
			
		HalfEdgeDCMTBase half_edge0 = cell0.HalfEdge();
		VertexDCMTBase iv2 = 
			half_edge0.PrevHalfEdgeInCell().FromVertex();
		
		// Cannot have more than max_numh half edges around this edge.
		int max_numh =
			half_edge0.FromVertex().NumHalfEdgesFrom() +
			half_edge0.ToVertex().NumHalfEdgesFrom();
		
		HalfEdgeDCMTBase half_edge = 
			half_edge0.NextHalfEdgeAroundEdge();
		int k = 0;
		while (k < max_numh && half_edge != half_edge0) {
			CellDCMTBase cell = half_edge.Cell();
			
			if (cell.IsTriangle()) {
				HalfEdgeDCMTBase prev_half_edge =
					half_edge.PrevHalfEdgeInCell();
				HalfEdgeDCMTBase next_half_edge =
					half_edge.NextHalfEdgeInCell();
				
				if (IsInTriangle(prev_half_edge, iv2) &&
					IsInTriangle(next_half_edge, iv2)) {
					// cell0, cell, and two triangles form a tetrahedron.
					return true;
				}
			}
			
			k = k+1;
			half_edge = half_edge.NextHalfEdgeAroundEdge();
		}
		
		return false;
	}
	
	
	/** Return true if half edge (v0,v1) is part of a triangle hole,
	 *    i.e. if there is a vertex v2 that is adjacent to v0 and to v1
	 *    but where (v0,v1,v2) is NOT a mesh triangle.
	 *    
	 *  @param vertex_info Return index iv2 in vertex_info.index0.
	 */
	public boolean FindTriangleHole
		(HalfEdgeDCMTBase half_edgeA, VertexInfo vertex_info)
	{
		// Initialize.
		vertex_info.index0 = 0;
		
		VertexDCMTBase v0 = half_edgeA.FromVertex();
		VertexDCMTBase v1 = half_edgeA.ToVertex();
		v1._ClearVisitedFlagsInAdjacentVertices();
		v0._SetVisitedFlagsInAdjacentVertices(true);
		
		for (int k = 0; k < v1.NumHalfEdgesFrom(); k++) {
			HalfEdgeDCMTBase half_edgeB = v1.KthHalfEdgeFrom(k);
			VertexDCMTBase v2 = half_edgeB.ToVertex();
			
			if (v2._IsVisited()) {
				// v2 is a neighbor of v0 and of v1.
				if (!IsInTriangle(half_edgeA, v2)) {
					vertex_info.index0 = v2.Index();
					
					// *** DEBUG ***
					/*
					System.out.println("Found triangle hole with edge " +
										half_edgeB.EndpointsStr(","));
										*/
					return true;
				}
			}
			
			HalfEdgeDCMTBase half_edgeC = half_edgeB.PrevHalfEdgeInCell();
			v2 = half_edgeC.FromVertex();
			
			// Check half_edgeC.FromVertex() to handle boundary edges 
			//   and/or cells with arbitrary orientations.
			if (v2._IsVisited()) {
				// v2 is a neighbor of v0 and of v1.
				if (!IsInTriangle(half_edgeA, v2)) {
					vertex_info.index0 = v2.Index();
					
					// *** DEBUG ***
					/*
					System.out.println("Found triangle hole (B) with edge " +
										half_edgeC.EndpointsStr(","));
					*/
					return true;
				}
			}
		}
	
		return false;
	}
	
	
	// *** Methods to compute mesh information ***

	/** Compute min and max squared edge lengths in the mesh. */
	public void ComputeMinMaxEdgeLengthsSquared
	(MinMaxInfo min_max_info)
	{
		// Initialize.
		min_max_info.InitializeToZero();
		
		boolean flag_found = false;
		for (Integer ihalf_edge: HalfEdgeIndices()) {
			
			HALF_EDGE_TYPE half_edge = HalfEdge(ihalf_edge);
			
			double length_squared = 
					half_edge.ComputeLengthSquared();
			
			if (!flag_found || 
				(length_squared < min_max_info.minVal)) {
				min_max_info.minVal = length_squared;
				min_max_info.imin = ihalf_edge;
			}
			
			if (!flag_found ||
				(length_squared > min_max_info.maxVal)) {
				min_max_info.maxVal = length_squared;
				min_max_info.imax = ihalf_edge;
			}
			
			flag_found = true;
		}
	}
	
	
	/** Compute min squared ratio of the min to max edge in any cell.
	 *  - Ignores cells with all edge lengths 0.
	 *  - Returns ratio of 1.0 if there are no cells or all edge lengths are 0.
	 */
	public void ComputeMinCellEdgeLengthRatioSquared
	(MinCellRatioInfo min_cell_ratio_info)
	{
		// Initialize.
		min_cell_ratio_info.Initialize();
		
		MinMaxInfo cell_min_max_info = new MinMaxInfo();
		
		for (Integer icell: CellIndices()) {
			
			CellDCMTBase cell = Cell(icell);
			
			cell.ComputeMinMaxEdgeLengthSquared(cell_min_max_info);
			if (cell_min_max_info.maxVal == 0) {
				// Skip.
				continue;
			}
			
			double ratio = 
				cell_min_max_info.minVal/cell_min_max_info.maxVal;
			
			if (ratio < min_cell_ratio_info.ratio) {
				min_cell_ratio_info.ratio = ratio;
				min_cell_ratio_info.icell = icell;
				min_cell_ratio_info.minVal = cell_min_max_info.minVal;
				min_cell_ratio_info.maxVal = cell_min_max_info.maxVal;
				min_cell_ratio_info.imin = cell_min_max_info.imin;
				min_cell_ratio_info.imax = cell_min_max_info.imax;
			}
		}
	}
	
	
	/** Compute cos angle at v1 of triangle (v0,v1,v2).
	 */
	public double ComputeCosTriangleAngle
	(VERTEX_TYPE v0, VERTEX_TYPE v1, VERTEX_TYPE v2,
		FlagZero flag_zero)
	{
		return ComputeCoord.compute_cos_triangle_angle
				(v0.coord, v1.coord, v2.coord, flag_zero);
	}
	
	
	/** Compute cosine of min and max cell angles.
	 */
	public void ComputeCosMinMaxAngle
	(MinMaxInfo min_max_info, FlagZero flag_zero)
	{
		// Initialize.
		min_max_info.InitializeToZero();
		
		MinMaxInfo cell_min_max_info = new MinMaxInfo();
		
		boolean flag_found = false;
		for (Integer icell: CellIndices()) {
			
			CELL_TYPE cell = Cell(icell);
			
			cell.ComputeCosMinMaxAngle
				(cell_min_max_info, flag_zero);
			
			if (!flag_found || 
				(cell_min_max_info.minVal < min_max_info.minVal)) {
				min_max_info.minVal = cell_min_max_info.minVal;
				min_max_info.imin = cell_min_max_info.imin;
			}
			
			if (!flag_found ||
				(cell_min_max_info.maxVal > min_max_info.maxVal)) {
				min_max_info.maxVal = cell_min_max_info.maxVal;
				min_max_info.imax = cell_min_max_info.imax;
			}
			
			flag_found = true;
		}
	}

}
