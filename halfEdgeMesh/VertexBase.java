package halfEdgeMesh;

/// \file VertexBase.java
/// Class for HalfEdgeMesh vertices.

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
import java.io.PrintStream;

/** Base class for vertices. 
 *  <p>
 *  Contains:
 *  <ul>
 *  	<li> Vertex index.
 *  	<li> Vertex coordinates.
 *  	<li> ArrayList of half edges whose from vertex is this vertex.
 *  </ul>
 */
public abstract class VertexBase {
	
	/** Vertex dimension. Number of vertex coordinates. */
	static final int DIMENSION = 3;
	
	/** index: Unique non-negative integer identifying the vertex. */
	protected int index;
	
	/** Array of half edges whose from vertex is this. */
	protected List<HalfEdgeBase> half_edge_from = new ArrayList<HalfEdgeBase>();
	
	/** Swap half edges in half_edge_from. */
	protected void _SwapHalfEdgesInHalfEdgeFromList(int k0, int k1) {
		HalfEdgeBase temp = KthHalfEdgeFrom(k0);
		half_edge_from.set(k0, KthHalfEdgeFrom(k1));
		half_edge_from.set(k1, temp);
	}
	
	
	/** Move boundary half edge to half_edge_from[0].
	 *  - If there are no boundary half edges in half_edge_from[],
	 *    but half_edge_from[k]->PreviousHalfEdgeInCell() is a boundary half edge,
	 *    move half_edge_from[k] to half_edge_from[0].
	 */
	public void MoveBoundaryHalfEdgeToHalfEdgeFrom0()
	{
		if (NumHalfEdgesFrom() < 1) {
			// Nothing to move.
			return;
		}
		
		if (KthHalfEdgeFrom(0).IsBoundary())
		{ return; }
		
		for (int k = 1; k < NumHalfEdgesFrom(); k++) {
			HalfEdgeBase half_edge = KthHalfEdgeFrom(k);
			if (half_edge.IsBoundary()) {
				// Swap half_edge_from[0] and half_edge_from[k].
				_SwapHalfEdgesInHalfEdgeFromList(0,k);
				return;
			}
		}
		
		// No boundary half edges found.
		
		// Extra processing in case cells are inconsistently oriented.
		// Check if half_edge_from[k]->PreviousHalfEdgeInCell()
		//   is a boundary half edge for some k.
		HalfEdgeBase prev_half_edge0 = 
				KthHalfEdgeFrom(0).PrevHalfEdgeInCell();
		if (prev_half_edge0.IsBoundary())
		{ return; }
		
		for (int k = 1; k < NumHalfEdgesFrom(); k++) {
			HalfEdgeBase half_edge = KthHalfEdgeFrom(k);
			if (half_edge.PrevHalfEdgeInCell().IsBoundary()) {
				// Swap half_edge_from[0] and half_edge_from[k].
				_SwapHalfEdgesInHalfEdgeFromList(0,k);
				return;
			}
		}
	}

	/** Vertex coordinates */
	public float[] coord = new float[DIMENSION];
	
	protected void Init()
	{
		for (int ic = 0; ic < Dimension(); ic++)
		{ coord[ic] = 0.0f; }
	};
	
	
	// Get functions.
	
	/** Return vertex dimension, i.e., number of vertex coordinates. */
	static public int Dimension()
	{ return DIMENSION; }
	
	/** Return coord[ic].
	 * @param ic Coordinate index.  Precondition: ic is in the range [0..(Dimension()-1)]
	 */
	public float Coord(int ic)
	{ return(coord[ic]); }
	
	/** Return vertex index. */
	public int Index()
	{ return(index); }
	
	/** Return number of half edges whose from vertex is this vertex. */
	public int NumHalfEdgesFrom()
	{ return(half_edge_from.size()); };

	/** Return the kth half edge in the ArrayList of half edges
	 *    whose from vertex is k. 
	 */
	public HalfEdgeBase KthHalfEdgeFrom(int k)
	{ return(half_edge_from.get(k)); };

	
	/** Return true if vertex is on the boundary. */
	public boolean IsBoundary()
	{
		if (NumHalfEdgesFrom() == 0) {
			// Vertex is not incident on any cells.
			return true;
		}
		
		// Vertex is on the boundary iff 
		//   half_edge_from[0].IsBoundary() or
		//   half_edge_from[0].PrevHalfEdgeInCell().IsBoundary().
		HalfEdgeBase prev_half_edge = KthHalfEdgeFrom(0);
		return (KthHalfEdgeFrom(0).IsBoundary() ||
				prev_half_edge.IsBoundary());
	}
	
	
	/** Return half edge from curren vertex to vertex iv.
	 * <ul> <li> Return null if no half edge found. </ul>
	 */
	public HalfEdgeBase FindHalfEdgeTo(int iv)
	{
		for (int k = 0; k < NumHalfEdgesFrom(); k++) {
			HalfEdgeBase half_edge = KthHalfEdgeFrom(k);
			if (half_edge.ToVertexIndex() == iv)
			{ return half_edge; }
		}
		
		// No half edge found.
		return null;
	}
	
	
	/** Count number of half edges with from vertex this vertex
	 * and ToVertexIndex() equal to iv. */
	public int CountNumIncidentHalfEdges(int iv)
	{
		int num = 0;
		for (int k = 0; k < NumHalfEdgesFrom(); k++) {
			HalfEdgeBase half_edge = KthHalfEdgeFrom(k);
			if (half_edge.ToVertexIndex() == iv)
			{ num++; }
		}
		
		return num;
	}
	
	
	/** Return string of vertex coordinates. */
	public String CoordStr()
	{
		if (Dimension() < 1) { return(""); }
		
		String s = String.valueOf(Coord(0));
		for (int ic = 1; ic < Dimension(); ic++)
		{ s += " " + String.valueOf(Coord(ic)); }
		
		return s;
	}
	
	
	// *** Print routines ***
	
	
	/** Print edges incident on vertex.
	 *  @param out Output stream.
	 *  @param prefix Print prefix at beginning of each line.
	 */
	public void PrintIncidentEdges(PrintStream out, String prefix)
	{
		out.print(prefix);
		out.print("Vertex " + String.valueOf(Index()));
		out.print(" incident edges:");
		int numh = NumHalfEdgesFrom();
		for (int k = 0; k < numh; k++) {
			HalfEdgeBase half_edge = KthHalfEdgeFrom(k);
			out.print("  (" + half_edge.EndpointsStr(",") + ")");
		}
		out.println();
	}
}
