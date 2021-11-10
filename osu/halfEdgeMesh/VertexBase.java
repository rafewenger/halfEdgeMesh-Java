package osu.halfEdgeMesh;

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
	
	// Vertex dimension. Number of vertex coordinates.
	static final int DIMENSION = 3;
	
	/// index: Unique non-negative integer identifying the vertex.
	protected int index;
	
	protected List<HalfEdgeBase> half_edge_from = new ArrayList<HalfEdgeBase>();
	
	protected void _MoveBoundaryHalfEdgeToHalfEdgeFrom0()
	{
		if (NumHalfEdgesFrom() < 1) {
			// Nothing to move.
			return;
		}
		
		if (KthHalfEdgeFrom(0).IsBoundary())
		{ return; }
		
		for (int k = 0; k < NumHalfEdgesFrom(); k++) {
			HalfEdgeBase half_edge = KthHalfEdgeFrom(k);
			if (half_edge.IsBoundary()) {
				// Swap half_edge_from[0] and half_edge_from[k].
				HalfEdgeBase temp = KthHalfEdgeFrom(0);
				half_edge_from.set(0, half_edge);
				half_edge_from.set(k, temp);
				return;
			}
		}
	}

	/***  Vertex coordinates */
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

	/** Return incident half edge whose from vertex is this vertex
	 *    and whose ToVertexIndex() is iv.
	 * <ul> <li> Return null if no half edge found. </ul>
	 */
	public HalfEdgeBase FindIncidentHalfEdge(int iv)
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
	
}
