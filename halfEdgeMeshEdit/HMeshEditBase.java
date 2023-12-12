package halfEdgeMeshEdit;

/** 
 * @file HMeshEditBase.java
 * Extension of half edge mesh supporting mesh edit operations.
 * - Supports SplitCell, JoinTwoCells, and CollapseEdge.
 * @Version 0.1.0
 * @author Rephael Wenger
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
import halfEdgeMesh.VertexBase;
import halfEdgeMeshMeasure.ComputeGeom;
import halfEdgeMesh.HalfEdgeMeshBase;


/** 
 *  Extension of halfEdgeMesh with routines to collapse, split, join, triangulate
 *    mesh edges and cells.
 */
public abstract class HMeshEditBase
<VERTEX_TYPE extends VertexBase, HALF_EDGE_TYPE extends HalfEdgeBase, CELL_TYPE extends CellBase>
extends HalfEdgeMeshBase<VERTEX_TYPE,HALF_EDGE_TYPE,CELL_TYPE> {
	
	/** Hash table indicating visited vertices */
	protected HashMap<Integer,Boolean> is_vertex_visited = 
		new HashMap<Integer,Boolean>();

	
	// *** Collapse/split/join routines.
	
	/**
	 * Collapse edge ihalf_edge0.
	 * - Returns vertex formed by merging endpoints of ihalf_edge0.
	 * - Does not delete any vertices.
	 */
	public VertexBase CollapseEdge(int ihalf_edge0) throws Exception
	{
		final int NUM_VERTICES_PER_TRIANGLE = 3;
		final HalfEdgeBase half_edge0 = HalfEdge(ihalf_edge0);
		if (half_edge0 == null) {
			// Can't collapse an edge that doesn't exist.
			return null;
		}
		
		// Don't collapse half_edge0 if its two endpoints (vA, vB) are
		//   in some cell, but edge (vA,vB) is not in the cell.
		if (IsIllegalEdgeCollapseH(ihalf_edge0)) {
			return null;
		}
		
		final VertexBase vA = half_edge0.FromVertex();
		final VertexBase vB = half_edge0.ToVertex();
		
		float midpoint_coord[] = new float[VertexBase.Dimension()];
		ComputeGeom.compute_midpoint(vA.coord, vB.coord, midpoint_coord);
		
		final int numh = half_edge0.CountNumHalfEdgesAroundEdge();
		ArrayList<HalfEdgeBase> half_edges_incident_on_triangles = 
			new ArrayList<HalfEdgeBase>();
		ArrayList<HalfEdgeBase> half_edges_not_incident_on_triangles =
			new ArrayList<HalfEdgeBase>();
		
		HalfEdgeBase half_edge = half_edge0;
		for (int i = 0; i < numh; i++) {
			final CellBase cell = half_edge.Cell();
			if (cell.NumVertices() <= NUM_VERTICES_PER_TRIANGLE) 
				{ half_edges_incident_on_triangles.add(half_edge); }
			else
				{ half_edges_not_incident_on_triangles.add(half_edge); }
			half_edge = half_edge.NextHalfEdgeAroundEdge();
		}
		
		// Delete triangles incident on edge.
		for (HalfEdgeBase temp_half_edge:half_edges_incident_on_triangles) {
			int icell = temp_half_edge.CellIndex();
			DeleteCell(icell);
		}
		
		// Merge linked lists of half edges incident on (vA, vC) and (vB, vC)
		//   for some vertex vC.
		_MergeHalfEdgesIncidentOnVertices(vA, vB);
		
		for (HalfEdgeBase temp_half_edge:half_edges_not_incident_on_triangles) {
			_RemoveHalfEdgeFromVertexList(temp_half_edge);
			_RemoveHalfEdgeFromCell(temp_half_edge);
			int itemp_half_edge = temp_half_edge.Index();
			half_edge_hashtable.remove(itemp_half_edge);
		}
		
		// Move all half edges from vA to be from vB.
		// - Moves v.half_edge_from to vB.half_edge_from
		_MoveVertexHalfEdgeFromList(vA, vB);
		
		// Ensure that vB.half_edge_from[0] is boundary edge.
		vB.MoveBoundaryHalfEdgeToHalfEdgeFrom0();
		
		// Set vB to midpoint of (vA, vB).
		SetCoord(vB.Index(), midpoint_coord);
		
		return vB;
	}
	
	
	/**
	 * Split cell into two cells.
	 * - Split cell at from vertices of ihalf_edgeA and ihalf_edgeB.
	 * - Return half edge representing split edge.
	 * @pre ihalf_edgeA and ihalf_edgeB are in the same cell.
	 */
	public HalfEdgeBase SplitCell(int ihalf_edgeA, int ihalf_edgeB) 
		throws Exception
	{
		final HalfEdgeBase half_edgeA = HalfEdge(ihalf_edgeA);
		final HalfEdgeBase half_edgeB = HalfEdge(ihalf_edgeB);
		
		if ((half_edgeA == null) || (half_edgeB == null)) {
			// Can't split if some half edge is null.
			return null;
		}
		
		if (IsIllegalSplitCell(ihalf_edgeA, ihalf_edgeB)) {
			// Don't split if edges share vertices.
			return null;
		}
		
		ArrayList<Integer> listA =
			GetListOfConsecutiveCellVertices(half_edgeA, half_edgeB);
		ArrayList<Integer> listB =
				GetListOfConsecutiveCellVertices(half_edgeB, half_edgeA);
		final VertexBase vA = half_edgeA.FromVertex();
		final VertexBase vB = half_edgeB.FromVertex();
		
		// Delete cell.
		final int icell = half_edgeA.CellIndex();
		DeleteCell(icell);
		
		AddNewCell(listA);
		AddNewCell(listB);
		
		final HalfEdgeBase split_half_edge = FindEdge(vA, vB);
		
		if (split_half_edge == null) {
			throw new Exception
				("Programming error detected in SplitCell.\n" +
					"  Unable to find hal edge representing split edge.");
		}
		
		return split_half_edge;
	}
	
	
	/**
	 * Join two cells sharing an edge.
	 * - Returns new joined cell.
	 */
	public CellBase JoinTwoCells(int ihalf_edgeA) throws Exception
	{
		final HalfEdgeBase half_edgeA = HalfEdge(ihalf_edgeA);
		if (half_edgeA == null) {
			throw new Exception
				("Programming error. Argument to JoinTwoCells is not half edge index.");
		}
		
		if (IsIllegalJoinTwoCells(ihalf_edgeA))
			{ return null; }
		
		final HalfEdgeBase half_edgeB = half_edgeA.NextHalfEdgeAroundEdge();
		if (half_edgeB.NextHalfEdgeAroundEdge() != half_edgeA) {
			throw new Exception
				("Programming error. Half edge passed to JoinTwoCells is an edge shared by three or more cells.");
		}
		
		ArrayList<Integer> listA = 
			GetListOfAllCellVertices(half_edgeA.NextHalfEdgeInCell());
		ArrayList<Integer> listB =
			GetListOfAllCellVertices(half_edgeB.NextHalfEdgeInCell());
		
		// Remove last element of each list, since those elements
		//   are in the other list.
		listA.remove(listA.size()-1);
		listB.remove(listB.size()-1);
		
		// Merge two lists, storing in listA.
		listA.addAll(listB);
		
		final int icellA = half_edgeA.CellIndex();
		final int icellB = half_edgeB.CellIndex();
		DeleteCell(icellA);
		DeleteCell(icellB);
		
		CellBase cellC = AddNewCell(listA);
		return cellC;
	}
	
	
	// ***I Triangulate routines ***
	
	/**
	 * Triangulate cell from half_edge0.FromVertexIndex().
	 * - Split cells into triangles, each triangle incident
	 *   on half_edge0.FromVertexIndex().
	 */
	public void TriangulateCellFromVertex(int ihalf_edge0) throws Exception
	{
		final HalfEdgeBase half_edge0 = HalfEdge(ihalf_edge0);
		if (half_edge0 == null) {
			throw new Exception
				("Programming error. Argument to TriangulateFromVertex() is not a half edge index.");
		}
		
		final CellBase cell0 = half_edge0.Cell();
		if (cell0.IsTriangle()) {
			// cell0 is already a triangle.
			return;
		}
		
		ArrayList<Integer> cell0_vlist =
			GetListOfAllCellVertices(half_edge0);
		
		DeleteCell(cell0.Index());
		
		final int numv = cell0_vlist.size();
		ArrayList<Integer> triangle_vlist = new ArrayList<Integer>();
		triangle_vlist.add(cell0_vlist.get(0));
		triangle_vlist.add(cell0_vlist.get(1));
		triangle_vlist.add(cell0_vlist.get(2));
		
		for (int i = 1; i < numv-1; i++) {
			triangle_vlist.set(1, cell0_vlist.get(i));
			triangle_vlist.set(2, cell0_vlist.get(i+1));
			AddNewCell(triangle_vlist);
		}
	}
	
	
	// *** Check for illegal collapse/split/join ***
	
	/**
	 * Return true if edge collapse is illegal.
	 * - Edge collapse (vA, vB) is illegal if some cell contains
	 *   both vA and vB but not edge (vA, vB).
	 * - Version that takes two vertex indices.
	 * - Note: Function suffix is 'V' (for vertex index arguments).
	 * - Takes time proportional to the sum of the number of vertices
	 *   in all cells incident on vA and vB.
	 */
	public boolean IsIllegalEdgeCollapseV(int ivA, int ivB)
	{
		final int NUM_VERTICES_PER_TRIANGLE = 3;
		VertexBase vA = Vertex(ivA);
		VertexBase vB = Vertex(ivB);
		
		if (vA.NumHalfEdgesFrom() > vB.NumHalfEdgesFrom()) {
			// Swap vA and vB to reduce number of cells processed.
			return IsIllegalEdgeCollapseV(ivB, ivA);
		}
		else {
			for (int k = 0; k < vA.NumHalfEdgesFrom(); k++) {
				final HalfEdgeBase half_edge0 = vA.KthHalfEdgeFrom(k);
				final CellBase cell = half_edge0.Cell();
				if (cell.NumVertices() <= NUM_VERTICES_PER_TRIANGLE) {
					// All pairs of cell vertices form an edge.
					continue;
				}
				
				HalfEdgeBase half_edge =
					(half_edge0.NextHalfEdgeInCell()).NextHalfEdgeInCell();
				
				for (int i = 2; i+1 < cell.NumVertices(); i++) {
					if (half_edge.FromVertexIndex() == ivB) 
					{ return true; }
					half_edge = half_edge.NextHalfEdgeInCell();
				}
			}
		}
		
		return false;
	}

	
	/**
	 * Return true if edge collapse is illegal.
	 * - Note: Function suffix is 'H' (for half edge index argument).
	 * - Takes time proportional to the sum of the number of vertices
	 *   in all cells incident on the endpoints.
	 */
	public boolean IsIllegalEdgeCollapseH(int ihalf_edge)
	{
		HalfEdgeBase half_edge = HalfEdge(ihalf_edge);
		return IsIllegalEdgeCollapseV
			(half_edge.FromVertexIndex(), half_edge.ToVertexIndex());
	}
	
	
	/**
	 * Return true if edge collapse changes mesh topology.
	 * - Note: Function suffix is 'H' (for half_edge index argument).
	 */
	public boolean DoesEdgeCollapseChangeMeshTopologyH(int ihalf_edge)
	{
		VertexI vertexI = new VertexI();
		
		if (FindTriangleHole(ihalf_edge, vertexI)) {
			// Edge collapse closes a hole in the mesh.
			return true;
		}
		
		if (IsInteriorEdgeWithBoundaryVertices(ihalf_edge)) 
			{ return true; }
		
		final HalfEdgeBase half_edge = HalfEdge(ihalf_edge);
		final int icell = half_edge.CellIndex();
		if (IsIsolatedTriangle(icell))
			{ return true; }
		
		if (IsInTetrahedron(icell))
			{ return true; }
		
		return false;
	}
	
	
	/**
	 * Return True if split cell is illegal.
	 * - Split cell is illegal
	 *     if half_edgeA and half_edgeB are in different cells or
	 *     if half_edgeA.FromVertex() and half_edgeB.FromVertex()
	 *     are adjacent vertices.
	 */
	public boolean IsIllegalSplitCell(int ihalf_edgeA, int ihalf_edgeB)
	{
		final HalfEdgeBase half_edgeA = HalfEdge(ihalf_edgeA);
		final HalfEdgeBase half_edgeB = HalfEdge(ihalf_edgeB);
		
		if (half_edgeA.Cell() != half_edgeB.Cell()) 
			{ return true; }
		
		if (half_edgeA == half_edgeB)
			{ return true; }
		
		if (half_edgeA.FromVertex() == half_edgeB.ToVertex()) 
			{ return true; };
			
		if (half_edgeA.ToVertex() == half_edgeB.FromVertex())
			{ return true; };
		
		final VertexBase vA = half_edgeA.FromVertex();
		final VertexBase vB = half_edgeB.FromVertex();
		final HalfEdgeBase half_edge = FindEdge(vA, vB);
		if (half_edge != null) {
			// Some cell in mesh intersects half_edgeA.Cell() in (vA,vB)
			//   but (vA,vB) is not an edge of half_edgeA.Cell().
			// Split would create edge (vA,vB) in 3 or more cells.
			return true;
		}
		
		return false;
	}
	

	/**
	 * Return true if triangulate cell from vertex changes mesh topology.
	 * - Triangulate cell from vertex changes mesh topology
	 *     if some triangulation diagonal is already a mesh edge.
	 */
	public boolean DoesTriangulateCellFromVertexChangeTopology(int ihalf_edgeA)
	{
		final int NUM_VERT_PER_TRIANGLE = 3;
		final HalfEdgeBase half_edgeA = HalfEdge(ihalf_edgeA);
		final CellBase cellA = half_edgeA.Cell();
		final VertexBase vA = half_edgeA.FromVertex();
		
		if (cellA.NumVertices() <= NUM_VERT_PER_TRIANGLE) {
			// Triangulate cell from vertex does nothing.
			return false;
		}
		
		HalfEdgeBase half_edgeB = half_edgeA.NextHalfEdgeInCell();
		for (int i = 2; i < cellA.NumVertices()-1; i++) {
			half_edgeB = half_edgeB.NextHalfEdgeInCell();
			final VertexBase vB = half_edgeB.FromVertex();
			final HalfEdgeBase half_edge = FindEdge(vA, vB);
			if (half_edge != null) {
				// Some cell in mesh has edge (vA,vB).
				// Triangulation would change mesh topology
				//   possibly creating edge (vA,vB) 
				//   in 3 or more cells.
				return true;
			}
		}
		
		
		return false;
	}
	
	
	/**
	 * Return true if join cells is illegal.
	 * - Join cells is illegal if ihalf_edge is a boundary half_edge
	 *   or more than two cells are incident on the edge
	 *   or some endpoint of half edge has degree 2.
	 */
	public boolean IsIllegalJoinTwoCells(int ihalf_edge)
	{
		final int TWO = 2;
		final HalfEdgeBase half_edge = HalfEdge(ihalf_edge);
		if (half_edge.IsBoundary())
			return true;
		
		final int ivfrom = half_edge.FromVertexIndex();
		final int ivto = half_edge.ToVertexIndex();
		if (!IsVertexIncidentOnMoreThanTwoEdges(ivfrom))
			{ return true; }
		
		if (!IsVertexIncidentOnMoreThanTwoEdges(ivto))
		{ return true; }

		final HalfEdgeBase half_edgeX = 
			half_edge.NextHalfEdgeAroundEdge();
		if (half_edge != half_edgeX.NextHalfEdgeAroundEdge()) {
			// More than two cells are incident on edge
			//   (half_edge.FromVertex(), half_edge.ToVertex()).
			return true;
		}
		
		final int num_shared_vertices =
			CountNumVerticesSharedByTwoCells
				(half_edge.CellIndex(), half_edgeX.CellIndex());
		if (num_shared_vertices > TWO) {
			// Cells share more than two vertices.
			return true;
		}
		
		// Joine is LEGAL.
		return false;
	}
	
	
	/**
	 * Return true if half edge endpoints and v are in a mesh triangle/
	 */
	public boolean IsInTriangle(int ihalf_edge0, int iv)
	{
		final HalfEdgeBase half_edge0 = HalfEdge(ihalf_edge0);
		final VertexBase v = Vertex(iv);
		final int numh = half_edge0.CountNumHalfEdgesAroundEdge();
		
		HalfEdgeBase half_edge = half_edge0;
		for (int k = 0; k < numh; k++) {
			if (half_edge.Cell().IsTriangle()) {
				final HalfEdgeBase prev_half_edge = 
					half_edge.PrevHalfEdgeInCell();
				if (prev_half_edge.FromVertex() == v)
				{ return true; }
			}
			half_edge = half_edge.NextHalfEdgeAroundEdge();
		}
		
		return false;
	}
	
	
	/**
	 * Return true if both endpoints (vfrom,vto) of hal_edge0
	 *   are neighbors of some vertex vC, but (vfrom, vto, vC)
	 *   is not a mesh triangle.
	 * - Returns ivC, the index of vertex vC, in vertexI.
	 */
	public boolean FindTriangleHole(int ihalf_edge0, VertexI vertexI)
	{
		HalfEdgeBase half_edge0 = HalfEdge(ihalf_edge0);
		VertexBase vA = half_edge0.FromVertex();
		VertexBase vB = half_edge0.ToVertex();
		
		// Initialize.
		vertexI.index = 0;
		
		// Get list of vertices that are neighbors of both vA and vB.
		ArrayList<VertexBase> common_vneighbors_list =
			GetListOfCommonVertexNeighbors(vA, vB);
		
		final int numh = half_edge0.CountNumHalfEdgesAroundEdge();
		for (VertexBase vC: common_vneighbors_list) {
			final int ivC = vC.Index();
			HalfEdgeBase half_edge = half_edge0;
			for (int k = 0; k < numh; k++) {
				if (!IsInTriangle(half_edge.Index(), ivC)) {
					vertexI.index = ivC;
					return true;
				}
				half_edge = half_edge.NextHalfEdgeAroundEdge();
			}
		}
		
		return false;
	}
	
	
	/**
	 * Return true if half edge is in interior but both vertices are on boundary.
	 */
	public boolean IsInteriorEdgeWithBoundaryVertices(int ihalf_edge)
	{
		HalfEdgeBase half_edge = HalfEdge(ihalf_edge);
		if (half_edge.IsBoundary()) {
			// Half edge is on boundary.
			return false;
		}
		
		if (half_edge.FromVertex().IsBoundary() &&
			half_edge.ToVertex().IsBoundary()) {
			// Half edge is in mesh interior, but connects boundary vertices.
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Return true if cell icell is a triangle whose 3 edges
	 *   are boundary edges.
	 */
	public boolean IsIsolatedTriangle(int icell)
	{
		final int NUM_VERTICES_PER_TRIANGLE = 3;
		
		final CellBase cell = Cell(icell);
		if (cell == null) {
			// Cell icell does not exist.
			return false;
		}
		
		if (!(cell.IsTriangle())) 
			{ return false; }

		HalfEdgeBase half_edge = cell.HalfEdge();
		for (int i = 0; i < NUM_VERTICES_PER_TRIANGLE; i++) {
			if (!(half_edge.IsBoundary()))
				{ return false; }
			half_edge = half_edge.NextHalfEdgeInCell();
		}
		
		// Cell has exactly three edges and all edges are boundary edges.
		return true;
	}
	
	
	/**
	 * Return true if cell icell is in the boundary of a tetrahedron.
	 */
	public boolean IsInTetrahedron(int icell)
	{
		final CellBase cell0 = Cell(icell);
		if (cell0 == null) {
			// Cell icell does not exist.
			return false;
		}
		
		if (!cell0.IsTriangle()) 
			{ return false; }
		
		final HalfEdgeBase half_edge0 = cell0.HalfEdge();
		final int iv2 = half_edge0.PrevHalfEdgeInCell().FromVertexIndex();
		
		final int numh = half_edge0.CountNumHalfEdgesAroundEdge();
		HalfEdgeBase half_edge = half_edge0;
		for (int k = 0; k < numh; k++) {
			half_edge = half_edge.NextHalfEdgeAroundEdge();
			final CellBase cell = half_edge.Cell();
			
			// Check if cell and vertex iv2 form a tetrahedron.
			if (cell.IsTriangle()) {
				final HalfEdgeBase prev_half_edge = half_edge.PrevHalfEdgeInCell();
				final HalfEdgeBase next_half_edge = half_edge.NextHalfEdgeInCell();
				final int iprev = prev_half_edge.Index();
				final int inext = next_half_edge.Index();
				
				if (IsInTriangle(iprev, iv2) && IsInTriangle(inext, iv2)) {
					// cell0, cell and two triangles form a tetrahedron.
					return true;
				}
			}
		}

		return false;
	}
	
	
	/**
	 * Return true if vertex is incident on more than two edges.
	 */
	public boolean IsVertexIncidentOnMoreThanTwoEdges(int iv)
	{	
		final int TWO = 2;
		final VertexBase v = Vertex(iv);
		if (v.NumHalfEdgesFrom() > TWO)
			{ return true; }
		
		if (!v.IsBoundary())
			{ return false; }
		
		if (v.NumHalfEdgesFrom() == TWO) {
			// Boundary vertex in two cells must have
			//   at least three incident edges.
			return true;
		}
		else {
			// Boundary vertex is in just one cell, and
			//   has exactly two incident edges.
			return false;
		}
	}
	
	
	/**
	 * Count number of vertices shared by two cells.
	 */
	public int CountNumVerticesSharedByTwoCells(int icellA, int icellB)
	{
		int num_shared_vertices = 0;
		ClearVisitedFlagsInAllCellVertices(icellB);
		SetVisitedFlagsInAllCellVertices(icellA, true);
		
		final CellBase cellB = Cell(icellB);
		HalfEdgeBase half_edgeB = cellB.HalfEdge();
		for (int k = 0; k < cellB.NumVertices(); k++) {
			int iv = half_edgeB.FromVertexIndex();
			if (IsVertexVisited(iv)) 
				{ num_shared_vertices++; }
			half_edgeB = half_edgeB.NextHalfEdgeInCell();
		}
		
		return num_shared_vertices;
	}
	
	
	// *** Vertex visit routines ***
	
	/**
	 * Return true if is_vertex_visited[iv] is true.
	 * - Return false, if iv is not in hash table is_vertex_visited.
	 */
	public boolean IsVertexVisited(int iv)
	{
		final Boolean flag = is_vertex_visited.get(iv);
		if (flag == null) { return false; }
		else { return flag; }
	}
	
	
	/**
	 * Set is_vertex_visited[iv] to flag.
	 */
	public void SetVertexVisitedFlag(int iv, boolean flag)
	{
		is_vertex_visited.put(iv,  flag);
	}
	
	
	/**
	 * Set is_vertex_visited[iv] to false.
	 */
	public void ClearVertexVisitedFlag(int iv)
	{
		SetVertexVisitedFlag(iv, false);
	}
	
	
	/**
	 * Set is_vertex_visited to flag in all neighbors of vertex iv.
	 */
	public void SetVisitedFlagsInAdjacentVertices(int iv, boolean flag)
	{
		VertexBase v = Vertex(iv);
		for (int k = 0; k < v.NumHalfEdgesFrom(); k++) {
			final HalfEdgeBase half_edgeA = v.KthHalfEdgeFrom(k);
			final int ivA = half_edgeA.ToVertexIndex();
			SetVertexVisitedFlag(ivA, flag);
			
			final HalfEdgeBase half_edgeB = 
					half_edgeA.PrevHalfEdgeInCell();
			final int ivB = half_edgeB.FromVertexIndex();
			if (ivB != iv) {
				// Set is_vertex_visited for ivB in case
				//   of boundary edges or cells with arbitrary orientations.
				SetVertexVisitedFlag(ivB, flag);
			}
		}
	}
	
	
	/**
	 * Set is_vertex_visited to false in all neighbors of vertex iv.
	 */
	public void ClearVisitedFlagsInAdjacentVertices(int iv)
	{
		SetVisitedFlagsInAdjacentVertices(iv, false);
	}
	
	
	/** 
	 * Set visited flag to flag in all vertices of cell icell.
	 */
	public void SetVisitedFlagsInAllCellVertices(int icell, boolean flag)
	{
		final CellBase cell = Cell(icell);
		HalfEdgeBase half_edge = cell.HalfEdge();
		for (int i = 0; i < cell.NumVertices(); i++) {
			SetVertexVisitedFlag(half_edge.FromVertexIndex(), flag);
			half_edge = half_edge.NextHalfEdgeInCell();
		}
	}
	
	
	/**
	 * Set visited flag to false in all vertices of cell icell.
	 */
	public void ClearVisitedFlagsInAllCellVertices(int icell)
	{
		SetVisitedFlagsInAllCellVertices(icell, false);
	}
	
	
	// *** Get vertex list routines ***
	
	/**
	 * Get list of vertices that are common neighbors of vA and vB.
	 * - Runs in time proportional to number of edges incident on vA
	 *   plus the number of edges incident on vB.
	 */
	public ArrayList<VertexBase> GetListOfCommonVertexNeighbors
	(VertexBase vA, VertexBase vB)
	{
		int ivA = vA.Index();
		int ivB = vB.Index();
		ArrayList<VertexBase> common_vneighbors_list = new ArrayList<VertexBase>();
		ClearVisitedFlagsInAdjacentVertices(ivB);
		SetVisitedFlagsInAdjacentVertices(ivA, true);
		
		for (int k = 0; k < vB.NumHalfEdgesFrom(); k++) {
			final HalfEdgeBase half_edge = vB.KthHalfEdgeFrom(k);
			final VertexBase vC = half_edge.ToVertex();
			final int ivC = vC.Index();
			if (IsVertexVisited(ivC)) {
				// Vertex vC is a neighbor of vA and vB.
				common_vneighbors_list.add(vC);
				
				// Clear flag so vC is only appended once.
				ClearVertexVisitedFlag(ivC);
			}
			
			final HalfEdgeBase prev_half_edge_in_cell = 
				half_edge.PrevHalfEdgeInCell();
			
			final VertexBase vD = prev_half_edge_in_cell.FromVertex();
			final int ivD = vD.Index();
			if (IsVertexVisited(ivD)) {
				// Vertex vD is a neighbor of vA and vB.
				common_vneighbors_list.add(vD);
				
				// Clear flag so vC is only appended once.
				ClearVertexVisitedFlag(ivD);
			}
		}
		
		// *** DEBUG ***
//		err.printf("Common vertex neighbors of %d and %d: ", vA.Index(), vB.Index() );
//		for (VertexBase tempv:common_vneighbors_list) {
//			err.printf(" %d", tempv.Index());
//		}
//		err.printf("%n");
		
		return common_vneighbors_list;
	}
	
	
	/**
	 * Get list of consecutive cell vertices, starting at half_edgeA.FromVertex()
	 *   until half_edgeB.FromVertex().
	 * - Vertices are represented by their indices.
	 * @pre half_edgeA and half_edgeB are in the same cell.
	 */
	public ArrayList<Integer> GetListOfConsecutiveCellVertices
	(HalfEdgeBase half_edgeA, HalfEdgeBase half_edgeB) throws Exception
	{
		if (half_edgeA.Cell() != half_edgeB.Cell()) {
			throw new Exception
				("Programming error detected in GetListOfConsecutiveCellVertices.\n" +
					"  Half edges are in different cells.");
		}
		
		final CellBase cell = half_edgeA.Cell();
		ArrayList<Integer> listA = new ArrayList<Integer>();
		HalfEdgeBase half_edge = half_edgeA;
		// Check that kount is less than cell.NumVertices(), just in case
		//   data structure is corrupted.
		int kount = 0;
		while ((half_edge != half_edgeB) && (kount <= cell.NumVertices())) {
			listA.add(half_edge.FromVertexIndex());
			half_edge = half_edge.NextHalfEdgeInCell();
			kount = kount+1;
		}
		
		if (kount > cell.NumVertices()) {
			// Something went wrong.
			final int icell = cell.Index();
			throw new Exception
				("Programming error detected in GetListOfConsecutiveCellVertices.\n" +
					"  Error getting vertices in cell " + Integer.toString(icell) + ".");
		}
		
		listA.add(half_edgeB.FromVertexIndex());
		
		return listA;
	}
	
	
	/**
	 * Get list of all cell vertices, starting at half_edge.FromVertex().
	 * - Vertices are represented by their indices.
	 * - Vertices are in order around cell, starting at half_edge.FromVertex().
	 */
	public ArrayList<Integer> GetListOfAllCellVertices(HalfEdgeBase half_edge)
	throws Exception
	{
		ArrayList<Integer> cell_vlist =
			GetListOfConsecutiveCellVertices
			(half_edge, half_edge.PrevHalfEdgeInCell());
		return cell_vlist;
	}
	
	
	// *** Protected routines to edit mesh ***
		
	
	/**
	 *  Merge linked lists of half edges incident on (vA,vC) and (vB,vC)
	 *    for some vertex vC.
	 */
	protected void _MergeHalfEdgesIncidentOnVertices
		(VertexBase vA, VertexBase vB) throws Exception
	{
		// Get list of vertices thar are neighbors of both vA and vB.
		ArrayList<VertexBase> common_vneighbors_list =
			GetListOfCommonVertexNeighbors(vA, vB);
		
		for (VertexBase vC:common_vneighbors_list) {
			final HalfEdgeBase half_edgeA = FindEdge(vA, vC);
			final HalfEdgeBase half_edgeB = FindEdge(vB, vC);
			if ((half_edgeA == null) || (half_edgeB == null)) {
				throw new Exception
					("Programming error. Unalbe to find half edge with given endpoints.");
			}
			
			_MergeHalfEdgesAroundEdge(half_edgeA, half_edgeB);
		}
	}
	
}
