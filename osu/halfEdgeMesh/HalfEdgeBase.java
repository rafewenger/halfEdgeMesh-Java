package osu.halfEdgeMesh;

/// \file HalfEdgeBase.java
/// Class for HalfEdgeMesh half edges.

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

/** Base class for half edges */
public abstract class HalfEdgeBase {

	/// index: Unique non-negative integer identifying the half-edge.
	protected int index;
	
	/// Reference to next half edge in cell.
	protected HalfEdgeBase next_half_edge_in_cell;
	
	/// Reference to previous half_edge_in_cell.
	protected HalfEdgeBase prev_half_edge_in_cell;
	
	/// Reference to next half edge around edge.
	/// - Equivalent to opposite when at most two cells share an edge
	///   and cells are consistently oriented.
	/// - Equals itself (this) if half edge is a boundary half edge.
	protected HalfEdgeBase next_half_edge_around_edge;
	
	/// Reference to from vertex.
	protected VertexBase from_vertex;
	
	/// Reference to cell containing half edge.
	protected CellBase cell;
	
	protected void Init()
	{
		next_half_edge_in_cell = null;
		prev_half_edge_in_cell = null;
		next_half_edge_around_edge = this;
		from_vertex = null;
		cell = null;
	}
	
	/** Construct and initialize the half edge. */
	public HalfEdgeBase() { Init(); }
	
	// Get functions.
	
	/** Return half edge index. */
	public int Index()
	{ return index; }
	
	/** Return next half edge in cell. */
	public HalfEdgeBase NextHalfEdgeInCell()
	{ return next_half_edge_in_cell; }
	
	/** Return previous half edge in cell. */
	public HalfEdgeBase PrevHalfEdgeInCell()
	{ return prev_half_edge_in_cell; }
	
	/** Return next half edge around edge.
	 * <ul> <li> NextHalfEdgeAroundEdge() is similar to Opposite()
	 *   when edges are incident on at most two cells and 
	 *   cells sharing an edge are consistently oriented. </ul>
	 */
	public HalfEdgeBase NextHalfEdgeAroundEdge()
	{ return next_half_edge_around_edge; }
	
	/** Return from vertex. */
	public VertexBase FromVertex()
	{ return from_vertex ; }
	
	/** Return index of from vertex. */
	public int FromVertexIndex()
	{ return FromVertex().Index(); }
	
	/** Return to vertex. */
	public VertexBase ToVertex()
	{ return NextHalfEdgeInCell().FromVertex(); }
	
	/** Return index of to vertex. */
	public int ToVertexIndex()
	{ return ToVertex().Index(); }
	
	/** Return cell containing half edge. */
	public CellBase Cell()
	{ return cell; }
	
	/** Return index of cell containing half edge. */
	public int CellIndex()
	{ return cell.Index(); }
		
	/** Return previous half edge around from vertex.
	 * <ul>
	 *   <li> Returns PrevHalfEdgeInCell() if PrevHalfEdgeInCell()
	 *   	is a boundary half edge.
	 *   <li> NextHalfEdgeAroundFromVertex() is not defined, since
	 *   	PrevHalfEdgeAroundFromVertex() should always be used
	 *   	in moving around a vertex.
	 *   </ul>
	 */
	public HalfEdgeBase PrevHalfEdgeAroundFromVertex()
	{ return PrevHalfEdgeInCell().NextHalfEdgeAroundEdge(); }
	
	/** Return previous edge around vertex iv.
	 * <ul> 
	 * <li> Returns PrevHalfEdgeInCell() if PrevHalfEdgeInCell()
	 * 		is a boundary half edge.
	 * <li> Note: If iv equals ToVertex(), returns
	 * 		(NextHalfEdgeInCell().NextHalfEdgeAroundEdge()) so that
	 * 		repeated calls to PrevHalfEdgeAroundVertex() move
	 * 		in a consistent direction.
	 * </ul>
	 */
	public HalfEdgeBase PrevHalfEdgeAroundVertex(int iv)
	{
		if (FromVertexIndex() == iv)
		{ return PrevHalfEdgeAroundFromVertex(); }
		else
		{ return NextHalfEdgeInCell().NextHalfEdgeAroundEdge(); } 
	}
	
	/** Return true if half edge is a boundary half edge. */
	public boolean IsBoundary()
	{ return (this == NextHalfEdgeAroundEdge()); }
	
	
	/** Count number of half edges around edge. */
	public int CountNumHalfEdgesAroundEdge()
	{ 
		// Cannot have more than max_num half edges around an edge.
		int max_num = FromVertex().NumHalfEdgesFrom() + ToVertex().NumHalfEdgesFrom();
		
		int num = 1;
		HalfEdgeBase half_edge = NextHalfEdgeAroundEdge();
		while ((this != half_edge) && (num <= max_num)) {
			half_edge = half_edge.NextHalfEdgeAroundEdge();
			num = num + 1;
		}
		
		return num;
	}
	
	
	/** Return true if this half edge and half_edgeB
	 * have the same endpoints.
	 */
	public boolean SameEndpoints(HalfEdgeBase half_edgeB)
	{
		if ((FromVertex() == half_edgeB.ToVertex()) &&
			(ToVertex() == half_edgeB.FromVertex()))
			{ return true; }
		
		if ((FromVertex() == half_edgeB.FromVertex()) &&
			(ToVertex() == half_edgeB.ToVertex()))
			{ return true; }
		
		return false;
	}
	
	
	/** Return a string of the half edge endpoint indices. */
	public String EndpointsStr(String separator)
	{
		String s = String.valueOf(FromVertexIndex()) + separator;
		if (NextHalfEdgeInCell() == null) {
			s += "*";
		}
		else {
			s += String.valueOf(ToVertexIndex());
		}
		
		return s;
	};
	
	/** Return a string of half edge index and endpoint indices. */
	public String IndexAndEndpointsStr(String separator)
	{
		String s = String.valueOf(Index()) + " ("
				+ EndpointsStr(separator) + ")";
		return s;
	};

}
