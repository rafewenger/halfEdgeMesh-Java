/** Half edge mesh:
 *  This is an implementation of a half edge mesh in java.
 *  <p>
 *  The mesh is stored in HalfEdgeMeshBase, including hash tables
 *  of all the vertices, half edges and cells in the mesh.
 *  <ul>
 *  <li> Each vertex, half edge and cell is in its own class.
 *  <li> All allocations of vertices, half edges and cells should be done 
 *    in HalfEdgeMeshBase or a subclass of HalfEdgeMeshBase.
 *  <li> Each vertex, half edge and cell has an integer index (identifier) 
 *    of the vertex, half edge or cell.
 *  <li> This is NOT a very efficient/compact implementation of half edges. 
 *  <li> This implementation is meant to be simple and (hopefully) robust
 *    for use in OSU CSE homeworks and prototypes.
 *  <li> Note: Many of the simpler get functions do not check their arguments,
 *    e.g. null pointers or indices in range.
 *     Such checks would be too time consuming for large meshes. 
 *     The calling function is responsible to ensure that pointers are
 *     not null and indices are in a specified range.
 * </ul>
 * @author Rephael Wenger
 * @version 0.0.1
 * <p>
 * Notes for java implementation:
 * <ul>
 * <li> VertexBase, HalfEdgeBase, CellBase, HalfEdgeMeshBase are abstract classes.
 *   A concrete class needs to be derived from each of these abstract classes.
 *   Using abstract classes allows the addition of other fields and methods 
 *   in the derived, concrete classes.
 * <li> Because java does not allow creation of generic types with new(), a "factory"
 *   class needs to be provided to create new vertices, half edges or cells
 *   of the appropriate type. HalfEdgeMeshFactoryBase is an abstract class
 *   for creating those objects.
 * <li> Examples of concrete classes derived from the abstract classes are classes
 * 	 VertexA, HalfEdgeA, CellA, HalfEdgeMeshA, HalfEdgeMeshFactoryA.
 * <li> When the concrete class derived from HalfEdgeMeshBase is constructed,
 *   the derived class needs to create a new object derived from
 *   HalfEdgeMeshFactoryBase&lt;VERTEX_TYPE,HALF_EDGE_TYPE,CELL_TYPE&gt;.  
 *   This object needs to be stored in field factory so that HalfEdgeMeshBase 
 *   can use it in creating new vertices, half edges and cells.
 * </ul>
 */
package osu.halfEdgeMesh;

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
