package osu.halfEdgeMesh;

/** \file HalfEdgeMeshBase.java
 * Class for half edge mesh.
 */

/*
* Copyright (C) 2021 Rephael Wenger
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


/** Base class for storing half edge mesh.
 *  @see osu.halfEdgeMesh Package osu.halfEdgeMesh documentation.
 */
public abstract class HalfEdgeMeshBase<VERTEX_TYPE extends VertexBase, HALF_EDGE_TYPE extends HalfEdgeBase, 
		CELL_TYPE extends CellBase> {
	
	/** Class factory for creating new vertices, half_edges, and cells.
	 *  - Creates objects of type VERTEX_TYPE, HALF_EDGE_TYPE or CELL_TYPE.
	 *  - The concrete class derived from HalfEdgeMeshBase needs to create
	 *    this object.
	 */
	protected HalfEdgeMeshFactoryBase<VERTEX_TYPE,HALF_EDGE_TYPE,CELL_TYPE> factory;

	/** Hash table of all vertices. */
	protected HashMap<Integer,VERTEX_TYPE> vertex_hashtable = new HashMap<Integer,VERTEX_TYPE>();
	
	/** Hash table of all half edges. */
	protected HashMap<Integer,HALF_EDGE_TYPE> half_edge_hashtable = new HashMap<Integer,HALF_EDGE_TYPE>();
	
	/** Hash table of all cells. */
	protected HashMap<Integer,CELL_TYPE> cell_hashtable = new HashMap<Integer,CELL_TYPE>();
	
	/// Upper bound on the vertex index.  
	/// - Could be greater than the maximum if some vertices are deleted.
	protected int _max_vertex_index = -1;
	
	/// Upper bound on the half edge index.  
	/// - Could be greater than the maximum if some half edges are deleted.
	protected int _max_half_edge_index = -1;
	
	/// Upper bound on the cell index.  
	/// - Could be greater than the maximum if some half edges are deleted.
	protected int _max_cell_index = -1;
	
	
	// Get functions.
	
	/** Return vertex with index iv. */
	public VERTEX_TYPE Vertex(int iv)
	{ return vertex_hashtable.get(iv); }
	
	/** Return set of vertex indices (vertex_hashtable keys). */
	public Set<Integer> VertexIndices()
	{ return vertex_hashtable.keySet(); }
		
	/** Return half edge with index ihalf_edge. */
	public HALF_EDGE_TYPE HalfEdge(int ihalf_edge)
	{ return half_edge_hashtable.get(ihalf_edge); }
	
	/** Return set of half edge indices (half_edge_hashtable keys). */
	public Set<Integer> HalfEdgeIndices()
	{ return half_edge_hashtable.keySet(); }
	
	/** Return cell with index icell. */
	public CELL_TYPE Cell(int icell)
	{ return cell_hashtable.get(icell); }
	
	/** Return set of cell indices (cell_hasthable keys). */
	public Set<Integer> CellIndices()
	{ return cell_hashtable.keySet(); }
	
	/** Return number of vertices. (Number of vertices in vertex_hashtable.) */
	public int NumVertices()
	{ return vertex_hashtable.size(); }
	
	/** Return number of half edges. (Number of half edges in half_edge_hashtable.) */
	public int NumHalfEdges()
	{ return half_edge_hashtable.size(); }
	
	/** Return number of cells. (Number of cells in cell_hashtable.) */
	public int NumCells()
	{ return cell_hashtable.size(); }
	
	
	/** Return max index in a hashtable.
	 * - Return -1 if hashtable is empty.
	 * - Note: This is a time consuming operation that inspects all the hashtable keys.
	 * @param <VALUE_TYPE>
	 * @param hashtable
	 * @return
	 */
	protected <VALUE_TYPE> int _MaxIndex(HashMap<Integer,VALUE_TYPE> hashtable)
	{
		if (hashtable.size() == 0) { return(-1); }
		
		// Otherwise:
		return Collections.max(hashtable.keySet());
	}
	
	/** Return upper bound on vertex indices in the half edge mesh.
	 * <ul> 
	 * 		<li> Return -1 if there are no vertices.
	 *      <li> MaxVertexIndex() could be greater than the maximum vertex index
	 *      	if some vertices have been deleted.
	 * </ul> 
	 */
	public int MaxVertexIndex()
	{ return _max_vertex_index; }
	
	/** Return upper bound on half edge indices in the half edge mesh.
	 * <ul> 
	 * 		<li> Return -1 if there are no half edges.
	 * 		<li> MaxHalfEdgeIndex() could be greater than the maximum half edge index
	 *      	 if some half edges have been deleted.
	 * </ul>
	 */
	public int MaxHalfEdgeIndex()
	{ return _max_half_edge_index; }
	
	/** Return upper bound on cell indices in the half edge mesh.
	 * <ul> 
	 * 		<li> Return -1 if there are no cells. 
	 * 		<li> MaxCellIndex() could be greater than the maximum cell index
	 *      	 if some cells have been deleted.
	 * </ul>
	 */
	public int MaxCellIndex()
	{ return _max_cell_index; }
	
	
	// Count functions
	
	/** Count the number of isolated vertices.
	 * - Added: 11-28-2021 - RW
	 * @return
	 */
	public int CountNumIsolatedVertices()
	{
		int num_isolated_vertices = 0;
		for (Integer iv: VertexIndices()) {
			VERTEX_TYPE v = Vertex(iv);
			if (v.NumHalfEdgesFrom() == 0)
			{ num_isolated_vertices++; }
		}
		
		return num_isolated_vertices;
	}
	
	/** Count the number of boundary edges.
	 * - Added: 11-28-2021 - RW
	 * @return
	 */
	public int CountNumBoundaryEdges()
	{
		int num_boundary_edges = 0;
		for (Integer ihalf_edge: HalfEdgeIndices()) {
			HALF_EDGE_TYPE half_edge = HalfEdge(ihalf_edge);
			if (half_edge.IsBoundary())
			{ num_boundary_edges++; }
		}
		
		return num_boundary_edges;
	}
	
	/** Count number of cells with a given number of vertices.
	 * - Added: 11-28-2021 - RW
	 * @return
	 */
	public int CountNumCellsOfSize(int numv)
	{
		int num_cells = 0;
		for (Integer icell: CellIndices()) {
			CELL_TYPE cell = Cell(icell);
			if (cell.NumVertices() == numv)
			{ num_cells++; }
		}
		
		return num_cells;
	}
	
	/** Count number of cells with number of vertices
	 *  greater than or equal to.
	 * @return
	 */
	public int CountNumCellsOfSizeGE(int numv)
	{
		int num_cells = 0;
		for (Integer icell: CellIndices()) {
			CELL_TYPE cell = Cell(icell);
			if (cell.NumVertices() >= numv)
			{ num_cells++; }
		}
		
		return num_cells;
	}
	
	
	// *** Internal (protected) functions ***
	
	/// Create vertex with index iv, if it does not yet exist.
	/// - Returns reference to vertex.
	/// - Returns reference to vertex, if vertex already exists.
	protected VERTEX_TYPE _CreateVertex(int iv) throws Exception
	{
		if (iv < 0) {
			throw new Exception("Illegal argument to HalfEdgeMeshBase::_CreateVertex.  Vertex index must be non-negative.");
		}
		
		VERTEX_TYPE v = vertex_hashtable.get(iv);
		if (v == null)
		{
			/// Cast to VERTEX_TYPE. Needed even though factory.NewVertex() should create vertex of type VERTEX_TYPE.
			v = (VERTEX_TYPE) factory.NewVertex();
			v.index = iv;
			vertex_hashtable.put(iv,v);
		}
		
		_max_vertex_index = Math.max(_max_vertex_index, iv);
		
		return v;
	}
	
	
	/// Add half_edgeB after half_edgeA to cyclic list of half edges around edge.
	protected void _LinkHalfEdgesAroundEdge
		(HalfEdgeBase half_edgeA, HalfEdgeBase half_edgeB)
	{
		HalfEdgeBase temp = half_edgeA.NextHalfEdgeAroundEdge();
		half_edgeA.next_half_edge_around_edge = half_edgeB;
		half_edgeB.next_half_edge_around_edge = temp;
	}
	
	/// Add half edge with index ihalf_edge to half_edge_hashtable.
	/// - Returns new half edge.
	/// @pre No existing half edge has index ihalf_edge.
	protected HALF_EDGE_TYPE _AddHalfEdge(int ihalf_edge)
	{
		/// Cast to HALF_EDGE_TYPE. Needed even though factory.NewHalfEdge() 
		///   should create half edge of type HALF_EDGE_TYPE.
		HALF_EDGE_TYPE half_edge = (HALF_EDGE_TYPE) factory.NewHalfEdge();
		half_edge.index = ihalf_edge;
		half_edge_hashtable.put(ihalf_edge, half_edge);
		_max_half_edge_index = Math.max(_max_half_edge_index, ihalf_edge);
		return half_edge;
	}
	
	
	/// Link half edges in cell.
	protected void _LinkHalfEdgesInCell
	(HALF_EDGE_TYPE hprev, HALF_EDGE_TYPE hnext) throws Exception
	{
		if (hprev.Cell() != hnext.Cell())
		{
			throw new Exception("Error in _LinkHalfEdgesInCell. Half edges are in different cells.");
		}
	
		hprev.next_half_edge_in_cell = hnext;
		hnext.prev_half_edge_in_cell = hprev;
	}
		
	/// Add half edge.
	/// - Returns new half edge.
	/// @pre No existing half edge has index ihalf_edge.
	protected HALF_EDGE_TYPE _AddAndLinkHalfEdge
	(int ihalf_edge, CellBase cell, VertexBase vfrom, VertexBase vto, HalfEdgeBase hprev) throws Exception
	{
		if (cell == null || vfrom == null || vto == null)
		{
			throw new Exception("Illegal argument ot HalfEdgeMesh::_AddHalfEdge.  Arguments cell, vfrom, vto cannot be null.");
		}
		
		HALF_EDGE_TYPE half_edge = _AddHalfEdge(ihalf_edge);
		
		half_edge.cell = cell;
		cell.num_vertices++;
		half_edge.from_vertex = vfrom;
		half_edge.prev_half_edge_in_cell = hprev;
		if (hprev != null)
		{ hprev.next_half_edge_in_cell = half_edge; }
		
		// Link half edge with other half edges around edge.
		HalfEdgeBase half_edgeB = vto.FindIncidentHalfEdge(vfrom.Index());
		if (half_edgeB == null) {
			// Check whether there is a half edge around edge with the same
			// orientation as half_edge.
			HalfEdgeBase half_edgeC = vfrom.FindIncidentHalfEdge(vto.Index());
			if (half_edgeC == null)
			{ half_edge.next_half_edge_around_edge = half_edge; }
			else
			{
				// Link half_edge with half_edgeC, even though they
				//   have the same orientation.
				_LinkHalfEdgesAroundEdge(half_edgeC, half_edge);
			}
		}
		else {
			_LinkHalfEdgesAroundEdge(half_edgeB, half_edge);
		}
		
		vfrom.half_edge_from.add(half_edge);
		
		// Deleted: 11-28-2021 - RW
		// Revised version of _MoveBoundaryHalfEdgeToHalfEdgeFrom(),
		//   requires prev_half_edge_in_cell to be set before call.
		// Calls have been moved to _AddCell().
		// Making these calls here will throw an exception.
		//vto._MoveBoundaryHalfEdgeToHalfEdgeFrom0();
		//vfrom._MoveBoundaryHalfEdgeToHalfEdgeFrom0();
		
		return half_edge;
	}
	
	/** Move boundary half edge to half_edge_from[0] for each vertex
	 *  in cell_vertex[].
	 * @param cell_vertex
	 * @throws Exception
	 * - Added: 11-24-2021 - RW
	 */
	protected void _MoveBoundaryHalfEdgeToHalfEdgeFrom0
	(ArrayList<Integer> cell_vertex) throws Exception
	{
		for (int i = 0; i < cell_vertex.size(); i++) {
			int iv = cell_vertex.get(i);
			VERTEX_TYPE v = Vertex(iv);
			if (v == null) {
				throw new Exception("Programming error. Attempt to access non-existant vertex in _MoveBoundaryHalfEdgeToHalfEdgeFrom0()");
			}
			
			v._MoveBoundaryHalfEdgeToHalfEdgeFrom0();
		}
	}
	
	
	/** Add cell with index icell.
	 	@pre No existing cell has index icell. */
	protected CELL_TYPE _AddCell(int icell)
	{
		/// Cast to CELL_TYPE. Needed even though factory.NewCell() should create cell of type CELL_TYPE.
		CELL_TYPE cell = (CELL_TYPE) factory.NewCell();
		cell.index = icell;
		cell_hashtable.put(icell, cell);
		_max_cell_index = Math.max(_max_cell_index, icell);
		return cell;
	}
	
	
	// *** Public AddVertices(), AddCell() functions ***

	/** Add vertices [0..(numv-1)] to the mesh. */
	public void AddVertices(int numv) throws Exception
	{
		for (int iv = 0; iv < numv; iv++)
		{ _CreateVertex(iv); }
	}
	
	
	/** Add cell with index icell.
	    @param icell Cell index. Precondition: No existing cell has index icell. */
	public CELL_TYPE AddCell(int icell, ArrayList<Integer> cell_vertex) throws Exception
	{
		if (cell_vertex.size() < 3) {
			throw new Exception("Illegal argument to AddCell.  Vector cell_vertex must have 3 or more vertices.");
		}
		
		CELL_TYPE cell = _AddCell(icell);
		
		// Create first half edge.
		int iv0 = cell_vertex.get(0);
		int iv1 = cell_vertex.get(1);
		VERTEX_TYPE v0 = _CreateVertex(iv0);
		VERTEX_TYPE v1 = _CreateVertex(iv1);
		int ihalf_edge0 = MaxHalfEdgeIndex() + 1;
		HALF_EDGE_TYPE half_edge0 = _AddAndLinkHalfEdge(ihalf_edge0, cell, v0, v1, null);
		cell.half_edge = half_edge0;
		
		HALF_EDGE_TYPE hprev = half_edge0;
		int ihalf_edge = ihalf_edge0;
		
		for (int i0 = 1; i0 < cell_vertex.size(); i0++) {
			int i1 = (i0+1)%cell_vertex.size();
			iv0 = cell_vertex.get(i0);
			iv1 = cell_vertex.get(i1);
			v0 = Vertex(iv0);
			v1 = _CreateVertex(iv1);
			ihalf_edge++;
			HALF_EDGE_TYPE half_edge = 
					_AddAndLinkHalfEdge(ihalf_edge, cell, v0, v1, hprev);
			hprev = half_edge;
		}
		
		// Link last half edge (hprev) and first half edge (half_edge0).
		_LinkHalfEdgesInCell(hprev, half_edge0);

		// - Added: 11-28-2021 - RW
		// - This call must be AFTER _LinkHalfEdgesInCell(hprev, half_edge0).
		_MoveBoundaryHalfEdgeToHalfEdgeFrom0(cell_vertex);
		
		if (cell_vertex.size() != cell.NumVertices()) {
			throw new Exception("Error in AddCell().  Incorrect number of vertices in cell.");
		}

		return cell;
	}
	
	/** Set coordinate ic of vertex iv. */
	public void SetCoord(int iv, int ic, float c) throws Exception
	{
		VERTEX_TYPE v = _CreateVertex(iv);
		
		if (ic < 0 || ic >= VERTEX_TYPE.Dimension())
		{ throw new Exception("Illegal argument to HalfEdgeMeshBase::SetCoord. Coordinate ic is out of bounds."); }
		
		v.coord[ic] = c;
	}
	
	
	/** Set coordinates of vertex iv. */
	public void SetCoord(int iv, float coord[]) throws Exception
	{
		VERTEX_TYPE v = _CreateVertex(iv);

		if (coord.length < VERTEX_TYPE.Dimension())
		{ throw new Exception("Error in SetCoord(). Argument coord[] has too few coordinates."); }
		
		for (int ic = 0; ic < VERTEX_TYPE.Dimension(); ic++)
		{ v.coord[ic] = coord[ic]; }
	}
	
	
	

	
	
	// Check routines.
	
	/** Check data structure vertices.
	 * @return Returns error flag, index of problem vertex,
	 *   and error message.
	 */
	public ErrorInfo CheckVertices()
	{
		ErrorInfo error_info = new ErrorInfo();
		
		int ivmax = _MaxIndex(vertex_hashtable);
		if (_max_vertex_index < ivmax) {
			error_info.SetError(ivmax);
			error_info.SetMessage
			("Incorrect value (" + String.valueOf(_max_vertex_index) +
					") of _max_vertex_index.  Max vertex is " +
					String.valueOf(ivmax) + ".");
			return error_info;
		}

		for (Integer iv: VertexIndices()) {

			VERTEX_TYPE v = Vertex(iv);
			
			if (v.Index() != iv) {
				error_info.SetError(iv);
				error_info.SetMessage 
					("Incorrect vertex index for vertex " + String.valueOf(iv) + ".");
				return error_info;
			}
			
			boolean flag_boundary = false;
			HalfEdgeBase boundary_half_edge = null;
			for (int k = 0; k < v.NumHalfEdgesFrom(); k++) {
				HalfEdgeBase half_edge = v.KthHalfEdgeFrom(k);
				if (half_edge == null) {
					error_info.SetError(iv);
					String msg = "Vertex " + String.valueOf(iv)
						+ " half_edge_from[" + String.valueOf(k)
						+ "] = null.";
					error_info.SetMessage(msg);
					return error_info;
				}
				
				if (half_edge.IsBoundary()) {
					flag_boundary = true;
					boundary_half_edge = half_edge;
				}
			}
			
			if (flag_boundary) {
				HalfEdgeBase half_edge = v.KthHalfEdgeFrom(0);
				
				if (!(half_edge.IsBoundary())) {
					error_info.SetError(iv);
					String msg = "Vertex " + String.valueOf(iv)
					+ " is on a boundary half edge "
					+ boundary_half_edge.IndexAndEndpointsStr(",")
					+ " but first incident half edge is not a boundary half edge.";
					error_info.SetMessage(msg);
					return error_info;
				}
			}	
		}
		
		// No errors.
		error_info.flag_error = false;
		return error_info;
	};
	
	
	/** Check data structure half edges.
	 *  @return Returns error flag, index of problem half edge,
	 *    and error message.
	 */
	public ErrorInfo CheckHalfEdges()
	{
		ErrorInfo error_info = new ErrorInfo();
	
		int ihmax = _MaxIndex(half_edge_hashtable);
		if (_max_half_edge_index < ihmax) {
			error_info.SetError(ihmax);
			error_info.SetMessage
			("Incorrect value (" + String.valueOf(_max_half_edge_index) +
					") of _max_half_edge_index.  Max half edge is " +
					String.valueOf(ihmax) + ".");
			return error_info;
		}
		
		// First check for bad indices or null pointers.
		for (Integer ihalf_edge: HalfEdgeIndices()) {
		
			HALF_EDGE_TYPE half_edge = HalfEdge(ihalf_edge);
			
			if (half_edge.Index() != ihalf_edge) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage 
					("Incorrect half edge index for half edge " + String.valueOf(ihalf_edge) + ".");
				return error_info;
			}
			
			VertexBase vfrom = half_edge.FromVertex();
			if (vfrom == null) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage 
					("Missing (null) from vertex in half edge " + 
							String.valueOf(ihalf_edge) + ".");
				return error_info;
			}
			
			int num_match = Collections.frequency(vfrom.half_edge_from, half_edge);
			if (num_match < 1) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage
					("Half edge " + half_edge.IndexAndEndpointsStr(",") +
							" does not appear in half_edge_from[] list for vertex " +
							String.valueOf(vfrom.Index()) + ".");
				return error_info;
			}
			else if (num_match > 1) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage
					("Half edge " + half_edge.IndexAndEndpointsStr(",") +
							" appears more than once in half_edge_from[] list for vertex " +
							String.valueOf(vfrom.Index()) + ".");
				return error_info;
			}
			
			if (half_edge.Cell() == null) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage
				("Half edge " + half_edge.IndexAndEndpointsStr(",") +
						" is missing cell containing half edge.");
				return error_info;
			}
			
			if (half_edge.PrevHalfEdgeInCell() == null) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage
				("Half edge " + half_edge.IndexAndEndpointsStr(",") +
						" is missing previous half edge in cell.");
				return error_info;
			}
			
			if (half_edge.NextHalfEdgeInCell() == null) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage
				("Half edge " + half_edge.IndexAndEndpointsStr(",") +
						" is missing next half edge in cell.");
				return error_info;
			}
			
			if (half_edge.NextHalfEdgeAroundEdge() == null) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage
				("Half edge " + half_edge.IndexAndEndpointsStr(",") +
						" is missing next half edge around edge.");
				return error_info;
			}
		}
		
		
		// Check for mismatches between half edges.
		for (Integer ihalf_edge: HalfEdgeIndices()) {
			
			HALF_EDGE_TYPE half_edge = HalfEdge(ihalf_edge);
			
			VertexBase vfrom = half_edge.FromVertex();
			
			HalfEdgeBase half_edge0 = vfrom.KthHalfEdgeFrom(0);
			if (half_edge.IsBoundary() && !(half_edge0.IsBoundary())) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage
					("Half edge " + half_edge.IndexAndEndpointsStr(",") +
							" is a boundary half edge but vertex " +
							String.valueOf(vfrom.Index()) + 
							" KthHalfEdgeFrom(0) is not a boundary half edge.");
				return error_info;
			}
			
			CellBase cell = half_edge.Cell();
			HalfEdgeBase prev_half_edge = half_edge.PrevHalfEdgeInCell();
			HalfEdgeBase next_half_edge = half_edge.NextHalfEdgeInCell();
			
			if (prev_half_edge.Cell() != cell) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage
				("Half edge " + half_edge.IndexAndEndpointsStr(",") +
						" and previous half edge " +
						prev_half_edge.IndexAndEndpointsStr(",") +
						" are in different cells.");
				return error_info;
			}
			
			if (next_half_edge.Cell() != cell) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage
				("Half edge " + half_edge.IndexAndEndpointsStr(",") +
						" and next half edge " +
						next_half_edge.IndexAndEndpointsStr(",") +
						" are in different cells.");
				return error_info;
			}
			
			HalfEdgeBase half_edgeX = half_edge.NextHalfEdgeAroundEdge();
	
			
			if (half_edgeX != half_edge) {
				
				if (!half_edge.SameEndpoints(half_edgeX)) {
					error_info.SetError(ihalf_edge);
					error_info.SetMessage
					("Half edge " + half_edge.IndexAndEndpointsStr(",") +
							" and next half edge around edge " +
							half_edgeX.IndexAndEndpointsStr(",") +
							" have different endpoints.");
					return error_info;
				}
				
				if (half_edge.Cell() == half_edgeX.Cell()) {
					error_info.SetError(ihalf_edge);
					error_info.SetMessage
					("Half edge " + half_edge.IndexAndEndpointsStr(",") +
							" and next half edge around edge " +
							half_edgeX.IndexAndEndpointsStr(",") +
							" are in the same cells.");
					return error_info;
				}
			}
		}
		
		// Hash map tracking visited half edges.
		// Avoid revisiting (reprocessing) visited half edges.
		HashSet<Integer> is_visited = new HashSet<Integer>();
		for (Integer ihalf_edge: HalfEdgeIndices()) {
			
			if (is_visited.contains(ihalf_edge))
			{ continue; }
			
			HALF_EDGE_TYPE half_edge = HalfEdge(ihalf_edge);
			int numh = half_edge.CountNumHalfEdgesAroundEdge();
			VertexBase vfrom = half_edge.FromVertex();
			VertexBase vto = half_edge.ToVertex();
			int ivfrom = vfrom.Index();
			int ivto = vto.Index();
			int numh2 = vto.CountNumIncidentHalfEdges(ivfrom) +
					vfrom.CountNumIncidentHalfEdges(ivto);
			
			if (numh != numh2) {
				error_info.SetError(ihalf_edge);
				error_info.SetMessage
				("Inconsistency between half edges around edge and" +
				 " vertex incident lists for edge (" +
				 half_edge.EndpointsStr(",") + ").");
				return error_info;
			}
			
			// Mark all visited half edges so that they are not processed again.
			// Reduces time spent checking half edge around edge.
			HalfEdgeBase half_edgeX = half_edge;
			for (int k = 0; k < numh; k++) {
				int ihalf_edgeX = half_edgeX.Index();
				is_visited.add(ihalf_edgeX);
			}
		}
		
		// No errors.
		error_info.flag_error = false;
		return error_info;
	}

	
	/** Check data structure cells.
	 * @return Returns error flag, index of problem cell, 
	 *   and error message.
	 */
	public ErrorInfo CheckCells()
	{
		ErrorInfo error_info = new ErrorInfo();

		int icmax = _MaxIndex(cell_hashtable);
		if (_max_cell_index < icmax) {
			error_info.SetError(icmax);
			error_info.SetMessage
			("Incorrect value (" + String.valueOf(_max_cell_index) +
					") of _max_cell_index.  Max cell is " +
					String.valueOf(icmax) + ".");
			return error_info;
		}
		
		for (Integer icell: CellIndices()) {
		
			CELL_TYPE cell = Cell(icell);
			
			if (cell.Index() != icell) {
				error_info.SetError(icell);
				error_info.SetMessage 
					("Incorrect cell index for cell " + String.valueOf(icell) + ".");
				return error_info;
			}
			
			HalfEdgeBase half_edge0 = cell.HalfEdge();
			if (half_edge0 == null) {
				error_info.SetError(icell);
				error_info.SetMessage
				("Cell " + String.valueOf(icell) +
						" is missing half edge.");
				return error_info;
			}
			
			if (half_edge0.Cell() != cell) {
				error_info.SetError(icell);
				error_info.SetMessage
				("Incorrect half edge stored in cell " + 
				 String.valueOf(icell) + ".");
				return error_info;
			}
			
			int cell_numv = cell.NumVertices();
			HalfEdgeBase half_edge = half_edge0;
			
			for (int k = 1; k < cell_numv; k++) {
				half_edge = half_edge.NextHalfEdgeInCell();
				
				if (half_edge == half_edge0) {
					error_info.SetError(icell);
					error_info.SetMessage
					("Incorrect number of vertices (" +
					String.valueOf(cell_numv) + ") stored in cell " +
					String.valueOf(icell) + ". Counted " +
					String.valueOf(k) + " vertices.");
					return error_info;
				}
			}
			
			if (half_edge.NextHalfEdgeInCell() != half_edge0) {
				error_info.SetError(icell);
				error_info.SetMessage
				("Incorrect number of vertices (" +
				String.valueOf(cell_numv) + ") stored in cell " +
				String.valueOf(icell) + ". Cell has more than " +
				String.valueOf(cell_numv) + " vertices.");
				return error_info;
			}
		}
		
		// No errors.
		error_info.flag_error = false;
		return error_info;
	}
	
	
	/** Check vertices, half edges, and cells.
	 *  @return Returns error flag and error message.
	 */
	public ErrorInfo CheckAll()
	{
		ErrorInfo error_info;
		
		error_info = CheckVertices();
		if (error_info.FlagError()) 
		{ return error_info; }
		
		error_info = CheckHalfEdges();
		if (error_info.FlagError())
		{ return error_info; }
		
		error_info = CheckCells();
		if (error_info.FlagError())
		{ return error_info; }
		
		// No errors found.
		return (new ErrorInfo());
	}
	
	
	/** Check if mesh cells are consistently oriented.
	 * @return Returns flag and index of half edge mesh,
	 *   where the corresponding edge is incident 
	 *   on three or more cells.
	 */
	public OrientationInfo CheckOrientation()
	{
		OrientationInfo orientation_info = new OrientationInfo();
		
		for (Integer ihalf_edge: HalfEdgeIndices()) {
			
			HalfEdgeBase half_edge = HalfEdge(ihalf_edge);
			HalfEdgeBase half_edgeB = half_edge.NextHalfEdgeAroundEdge();
			
			if (half_edge == half_edgeB) {
				// Skip boundary half edge.
				continue;
			}
			
			if (half_edge.FromVertexIndex() == half_edgeB.FromVertexIndex()) {
				// Cells half_edge.Cell() and half_edgeB.Cell() have
				//   inconsistent orientations.
				orientation_info.SetNotOriented(ihalf_edge);
				return orientation_info;
			}
		}
		
		// All mesh cells are consistently oriented.
		orientation_info.is_oriented = true;
		return orientation_info;
	}
	
	
	/** Check manifold edge property.
	 * <ul> <li> Checks if every edge is incident on at most two cells. </ul>
	 * @return Returns flag and index of a non-manifold edge, if one exists.
	 */
	public ManifoldInfo CheckManifoldEdges()
	{
		ManifoldInfo manifold_info = new ManifoldInfo();
		
		for (Integer ihalf_edge: HalfEdgeIndices()) {
		
			HalfEdgeBase half_edge = HalfEdge(ihalf_edge);
			int nume = half_edge.CountNumHalfEdgesAroundEdge();
			
			if (nume >= 3) {
				manifold_info.SetNonManifoldEdges(ihalf_edge);
				return manifold_info;
			}
		}
		
		manifold_info.flag_manifold_edges = true;
		return manifold_info;	
	}
	
	
	/** Check manifold vertex property.
	 * <ul> <li> Checks if cells incident on each vertex form a fan. </ul>
	 * @return Returns flag and index of a non-manifold vertex, if one exists.
	 */
	public ManifoldInfo CheckManifoldVertices()
	{
		ManifoldInfo manifold_info = new ManifoldInfo();
		
		for (Integer iv: VertexIndices()) {
			
			VertexBase v = Vertex(iv);
			
			int numh = v.NumHalfEdgesFrom();
		
			if (numh == 0) {
				// No half edges/cells are incident on vertex iv.
				continue;
			}
			
			HalfEdgeBase half_edge0 = v.KthHalfEdgeFrom(0);
			HalfEdgeBase half_edge = 
					half_edge0.PrevHalfEdgeAroundVertex(iv);
			
			int num_cells = 1;
			while ((half_edge != half_edge0) &&
					!(half_edge.IsBoundary()) &&
					(num_cells <= numh)) {
				num_cells++;
				half_edge = half_edge.PrevHalfEdgeAroundVertex(iv);
			}
			
			if (num_cells != numh) {
				manifold_info.SetNonManifoldVertices(iv);
				return manifold_info;
			}
		}
		
		manifold_info.flag_manifold_vertices = true;
		return manifold_info;	
	}
	
	
	/** Check manifold vertex and edge properties.
	 * @return Returns non-manifold vertex or edge flags and indices
	 *   of a non-manifold vertex or edge, if one exists.
	 */
	public ManifoldInfo CheckManifold()
	{
		ManifoldInfo manifoldE_info = CheckManifoldEdges();
		ManifoldInfo manifoldV_info = CheckManifoldVertices();
		
		// Combine manifoldV_info with manifoldE_info
		manifoldE_info.flag_manifold_vertices =
				manifoldV_info.FlagManifoldVertices();
		manifoldE_info.vertex_index =
				manifoldV_info.VertexIndex();
		
		return manifoldE_info;
	}
	

	/** Check that iv is the index of some vertex. */
	public ErrorInfo CheckVertexIndex(int iv)
	{
		ErrorInfo error_info = new ErrorInfo();
		
		if (NumVertices() == 0) {
			error_info.SetError(iv);
			error_info.SetMessage("Mesh has no vertices.");
			return error_info;
		}
		
		if (iv < 0) {
			error_info.SetError(iv);
			error_info.SetMessage
			("Illegal negative vertex index: " + String.valueOf(iv) +
				"\n  Vertex indices cannot be negative.");
			return error_info;
		}
		
		if (iv > MaxVertexIndex()) {
			error_info.SetError(iv);
			error_info.SetMessage
			("Illegal vertex index: " + String.valueOf(iv) +
				"\n  Maximum vertex index: " + String.valueOf(MaxVertexIndex()));
			return error_info;
		}
		
		// No error.  iv is the index of some vertex.
		return error_info;
	}
}
