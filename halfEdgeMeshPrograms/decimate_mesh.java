package halfEdgeMeshPrograms;

import static java.lang.System.out;
import static java.lang.System.err;
import static java.lang.Math.*;
import java.io.*;
import java.util.*;

import halfEdgeMesh.*;
import halfEdgeMeshEdit.*;
import halfEdgeMeshMeasure.*;



/** Some simple mesh decimation routines.
 *  - Uses data structure HMeshEditBase().
 *
 */
public class decimate_mesh {

	static String input_filename;
	static String output_filename;
	
	// Global variables controlling output/checks.
	static boolean flag_silent = false;
	static boolean flag_terse = false;
	static boolean flag_no_warn = false;
	static boolean flag_time = false;
	static boolean flag_reduce_checks = false;
	
	// Global variables controlling decimation.
	static boolean flag_collapse_short_edges = false;
	static boolean flag_split_cells = false;
	static boolean flag_join_cells = false;
	static boolean flag_split_long_edges = false;
	static boolean flag_triangulate_cells = false;
	static boolean flag_allow_non_manifold = false;
	static boolean flag_fail_on_non_manifold = false;	
	
	static MeasureMeshC measure_mesh = new MeasureMeshC();
	
	
	public static void main(String[] argv) {
		
		// Number of cells in "large" data sets.
		int LARGE_DATA_NUM_CELLS = 10000;
		
		long begin_time = System.nanoTime();
		
		HMeshEditC mesh = new HMeshEditC();
		OffFileReaderC file_reader = new OffFileReaderC();
		OffFileWriterC file_writer = new OffFileWriterC();
		
		parse_command_line(argv);
		
		file_reader.OpenAndReadFile(input_filename, mesh);
		
		long time2 = System.nanoTime();
		
		try {
			
			if (!flag_reduce_checks) {
				flag_reduce_checks = 
					reduce_checks_on_large_datasets
						(mesh, flag_no_warn, LARGE_DATA_NUM_CELLS);
			}

			if (flag_collapse_short_edges) {
				collapse_shortest_edge_in_each_cell
					(mesh, flag_terse, flag_no_warn);
			}
			
			if (flag_split_long_edges) {
				split_longest_edge_in_each_cell
					(mesh, flag_terse, flag_no_warn);
			}
			
			if (flag_split_cells) {
				split_each_cell(mesh, flag_terse, flag_no_warn);
			}
			
			if (flag_join_cells) {
				join_each_cell(mesh, flag_terse, flag_no_warn);
			}
			
			if (flag_triangulate_cells) {
				triangulate_each_cell(mesh, flag_terse, flag_no_warn);
			}
			
			boolean passed_check = check_mesh(mesh, flag_silent && flag_no_warn);
			
			if (!flag_silent) {
				if (passed_check)
				{ out.println("Mesh data structure passed check."); }
			}
			
			long time3 = System.nanoTime();

			if (!flag_silent) {
				out.println();
				print_mesh_info(mesh);
			}
			
			if (output_filename == null || output_filename.equals("")) 
			{ output_filename = "out.off"; }
			if (output_filename.equals(input_filename))
			{ output_filename = "out2.off"; }
			
			if (!(flag_silent)) {
				out.println();
				out.println("Writing file: " + output_filename); 
			}
			
			file_writer.OpenAndWriteFile(output_filename, mesh);

			long end_time = System.nanoTime();
			
			if (flag_time) {
				print_time("Time to read file:  ", (time2-begin_time));
				print_time("Time to check mesh: ", (time3-time2));
				print_time("Time to write file: ", (end_time-time3));
				print_time("Total time:         ", (end_time-begin_time));
			}
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}	
	}

	
	// *** Collapse edge routines ***
	
	/** Collapse edge half_edge. */
	protected static void collapse_edge
		(HMeshEditC mesh, int ihalf_edge,
			boolean flag_terse, boolean flag_no_warn, boolean flag_check)
			throws Exception
	{
		HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
		
		if (mesh.IsIllegalEdgeCollapseH(ihalf_edge)) {
			if (!flag_no_warn)
				{ print_illegal_edge_collapse(half_edge); }
			return;
		}
		
		if (mesh.DoesEdgeCollapseChangeMeshTopologyH(ihalf_edge)) {
			if (!flag_no_warn)
				{ print_edge_collapse_topology_change(mesh, half_edge); }
			if (!flag_allow_non_manifold)  {
				if (!flag_no_warn)
					{ print_skipped_edge_collapse(half_edge); }
				return;
			}
		}
		
		if (!flag_terse) {
			out.println("Collapsing edge (" + 
						half_edge.EndpointsStr(",") + ").");
		}
		
		VertexBase vnew = 
				mesh.CollapseEdge(half_edge.Index());
		if (vnew == null) {
			out.println("Skipped illegal collapse of edge (" +
						half_edge.EndpointsStr(",") + ").");
		}

		if (flag_check)
			{ check_mesh(mesh, flag_no_warn); }
	}
	
	
	/** Split shortest cell edge. */
	protected static void collapse_shortest_cell_edge
		(HMeshEditC mesh, int icell, boolean flag_terse,
				boolean flag_no_warn, boolean flag_check)
			throws Exception
	{
		CellA cell = mesh.Cell(icell);
		MinMaxInfo min_max_info = new MinMaxInfo();
		
		//cell.ComputeMinMaxEdgeLengthSquared(min_max_info);
		measure_mesh.compute_cell_min_max_edge_length_squared(cell, min_max_info);
		final int ihalf_edge_min = min_max_info.imin;

		collapse_edge
			(mesh, ihalf_edge_min, flag_terse, flag_no_warn, flag_check);
	}
	
	/** Collapse shortest edge in each cell. */
	protected static void collapse_shortest_edge_in_each_cell
		(HMeshEditC mesh, boolean flag_terse, boolean flag_no_warn)
			throws Exception
	{
		int n = mesh.NumCells();
		
		boolean flag_check = !flag_reduce_checks;
		
		ArrayList<Integer> cell_list = new ArrayList<Integer>();
		cell_list.addAll(mesh.CellIndices());
		
		// Sort so that cells are processed in sorted order.
		Collections.sort(cell_list);
		
		int kount = 0;
		for (Integer icell:cell_list) {
			
			// Note: Some cells may have been deleted.  Cell icell may not exist.
			if (mesh.Cell(icell) == null) 
				{ continue; }
			
			collapse_shortest_cell_edge
				(mesh, icell, flag_terse, flag_no_warn, flag_check);
			kount++;
		
			if (flag_reduce_checks) {
				// Check mesh halfway through.
				if (kount == n/2) 
					{ check_mesh(mesh, flag_no_warn); }
			}
		}

	}
	
	
	protected static void print_illegal_edge_collapse(HalfEdgeBase half_edge)
	{
		out.println("Collapse of edge (" + half_edge.EndpointsStr(",") +
			") is illegal.");
		out.println("  Some cell contains vertices " +
			half_edge.EndpointsStr(" and ") + " but not edge (" +
			half_edge.EndpointsStr(","));
	}
	
	
	protected static void print_edge_collapse_topology_change
	(HMeshEditC mesh, HalfEdgeBase half_edge)
	{
		VertexI vertexI = new VertexI();
		int ihalf_edge = half_edge.Index();
		int icell = half_edge.CellIndex();
		
		if (mesh.FindTriangleHole(ihalf_edge, vertexI)) {
			out.println("Collapsing edge (" + half_edge.EndpointsStr(",") +
						") will change the mesh topology.");
			out.println("  Vertices (" + half_edge.EndpointsStr(", ") +
						", " + Integer.toString(vertexI.index) +
						") form a triangle hole.");
		}
		
		if (mesh.IsInteriorEdgeWithBoundaryVertices(ihalf_edge)) {
			out.println("Collapsing edge (" + half_edge.EndpointsStr(",") +
						") merges two non-adjacent boundary vertices.");
		}
		
		if (mesh.IsIsolatedTriangle(icell)) {
			out.println("Collapsing edge (" + half_edge.EndpointsStr(",") +
						") will delete isolated triangle " +
						Integer.toString(icell) + ".");
		}
		
		if (mesh.IsInTetrahedron(icell)) {
			out.println("Collapsing edge (" + half_edge.EndpointsStr(",") +
						") will collapse a tetrahedron.");
		}
	}
	
	
	// Print skipped edge collapse.
	protected static void print_skipped_edge_collapse
	(HalfEdgeBase half_edge)
	{
		out.println("Skipped collapse of edge(" +
					half_edge.EndpointsStr(",") + ").");
	}
	
	
	// *** Split cell routines ***
	
	protected static HalfEdgeBase split_cell
	(HMeshEditC mesh, int ihalf_edgeA, int ihalf_edgeB,
		boolean flag_terse, boolean flag_no_warn, boolean flag_check)
				throws Exception
	{
		final HalfEdgeBase half_edgeA = mesh.HalfEdge(ihalf_edgeA);
		final HalfEdgeBase half_edgeB = mesh.HalfEdge(ihalf_edgeB);
		VertexBase vA = half_edgeA.FromVertex();
		VertexBase vB = half_edgeB.FromVertex();
		int ivA = vA.Index();
		int ivB = vB.Index();
		int icell = half_edgeA.CellIndex();
		
		boolean flag = 
			check_split_cell(mesh, ihalf_edgeA, ihalf_edgeB, flag_no_warn);
		
		if (mesh.IsIllegalSplitCell(ihalf_edgeA, ihalf_edgeB)) 
			{ return null; }
		
		if (flag || flag_allow_non_manifold) {
			if (!flag_terse) {
				out.println("Splitting cell " + String.valueOf(icell) + 
						" at diagonal (" + String.valueOf(ivA) + "," +
						String.valueOf(ivB) + ").");
			}
		
			HalfEdgeBase split_edge = 
				mesh.SplitCell(ihalf_edgeA, ihalf_edgeB);
			if (split_edge == null) {
				out.println("Skipped illegal split of cell " +
						String.valueOf(icell) + " at diagonal (" +
						String.valueOf(ivA) + "," 
						+ String.valueOf(ivB) + ").");
			}
			
			if (flag_check)
				{ check_mesh(mesh, flag_no_warn); }
			
			return split_edge;
		}
		else {
			if (!flag_terse) {
				out.println("Skipped split of cell " +
						String.valueOf(icell) + " at diagonal (" +
						String.valueOf(ivA) + "," 
						+ String.valueOf(ivB) + ").");
			}
			
			return null;
		}
	}
	
	
	/** Split cell at largest angle.
	 *  - Split cell at vertex forming the largest angle.
	 */
	protected static void
		split_cell_at_largest_angle
			(HMeshEditC mesh, CellBase cell,
			boolean flag_terse, boolean flag_no_warn, boolean flag_check)
				throws Exception
	{
		if (cell.IsTriangle()) {
			// Nothing to split.
			return;
		}
		
		CosMinMaxAngleInfo cos_min_max_angle_info = 
			new CosMinMaxAngleInfo();
		
		measure_mesh.compute_cos_min_max_cell_angles
			(cell, cos_min_max_angle_info);
		
		// cos_min_max_angle_info.imax is the index of the half edge
		//   whose from_vertex has min cosine and MAX angle.
		int ihalf_edgeA = cos_min_max_angle_info.imax;
		HalfEdgeBase half_edgeA = mesh.HalfEdge(ihalf_edgeA);
	
		int numv = cell.NumVertices();
		int half_numv = numv/2;
		HalfEdgeBase half_edgeB = half_edgeA;
		for (int i = 0; i < half_numv; i++) {
			half_edgeB = half_edgeB.NextHalfEdgeInCell();
		}
		
		int ihalf_edgeB = half_edgeB.Index();
		final HalfEdgeBase split_edge = 
			split_cell(mesh, ihalf_edgeA, ihalf_edgeB, 
						flag_terse, flag_no_warn, flag_check);
			
		if (split_edge == null) {
			// Cannot split cell at largest angle.
			return;
		}
			
		if (flag_check) 
			{ check_mesh(mesh, flag_no_warn); }
	}
	
	
	/** Split each cell. */
	protected static void split_each_cell
		(HMeshEditC mesh, boolean flag_terse, boolean flag_no_warn)
			throws Exception
	{
		int n = mesh.MaxCellIndex();
		boolean flag_check = !flag_reduce_checks;
		
		// Create a list of the cell indices.
		ArrayList<Integer> cell_list = new ArrayList<Integer>();
		cell_list.addAll(mesh.CellIndices());
		
		// Sort so that cells are processed in sorted order.
		Collections.sort(cell_list);
		
		int kount = 0;
		for (Integer icell:cell_list) {
			
			CellBase cell = mesh.Cell(icell);
			// Note: Some cells may have been deleted.  Cell icell may not exist.
			if (cell == null) { continue; }
			
			split_cell_at_largest_angle
				(mesh, cell, flag_terse, flag_no_warn, flag_check);
			kount++;
		
			if (flag_reduce_checks) {
				// Check mesh halfway through.
				if (kount == n/2) 
					{ check_mesh(mesh, flag_no_warn); }
			}
		}
	}
	
	
	// *** Join cell routines. ***
	
	/** Join two cells by deleting half edge. */
	protected static void join_two_cells
		(HMeshEditC mesh, int ihalf_edge,
		boolean flag_terse, boolean flag_no_warn, boolean flag_check)
			throws Exception
	{
		final HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
		int icell = half_edge.CellIndex();
		int icellX = half_edge.NextCellAroundEdge().Index();
		
		boolean flag = check_join_cell(mesh, ihalf_edge, flag_no_warn);
		
		if (mesh.IsIllegalJoinTwoCells(ihalf_edge)) { return; }
		
		if (flag) {
			if (!flag_terse) {
				out.printf
					("Joining cell %d to cell %d by deleting edge (%s).\n",
						icell, icellX, half_edge.EndpointsStr(","));
			}
			
			final CellBase new_cell =  mesh.JoinTwoCells(ihalf_edge);
			
			if (new_cell == null) {
				out.printf("Join of cell %d to cell %d failed.\n", 
							icell, icellX);
			}
			else {
				if (flag_check)
					{ check_mesh(mesh, flag_no_warn); }
			}
			
			return;
		}
		else {
			if (!flag_no_warn) {
				out.printf("Skipping join of cell %d to cell %d.%n%n",
							icell, icellX);
			}
		}
	}
	
	
	// *** Triangulation routines. ***
	
	/** Triangulate cell from vertex half_edge0.FromVertex(). */
	protected static void triangulate_cell_from_vertex
	(HMeshEditC mesh, int ihalf_edge,
		boolean flag_terse, boolean flag_no_warn, boolean flag_check)
				throws Exception
	{
		final HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
		final int iv = half_edge.FromVertexIndex();
		final int icell = half_edge.CellIndex();
		final CellBase cell = half_edge.Cell();
		
		if (cell.IsTriangle()) {
			// Cell is already a triangle.
			return;
		}
		
		if (!flag_terse) {
			out.println("Triangulating cell " + String.valueOf(icell) + 
					" from vertex " + String.valueOf(iv) + ".");
		}	
		
		mesh.TriangulateCellFromVertex(ihalf_edge);
			
		if (flag_check)
			{ check_mesh(mesh, flag_no_warn); }
	}
	

	/** Triangulate cell from vertex with largest angle. */
	protected static void
		triangulate_cell_from_vertex_with_largest_angle
			(HMeshEditC mesh, CellBase cell,
			boolean flag_terse, boolean flag_no_warn, boolean flag_check)
				throws Exception
	{
		CosMinMaxAngleInfo cos_min_max_angle_info = 
			new CosMinMaxAngleInfo();
		
		measure_mesh.compute_cos_min_max_cell_angles
			(cell, cos_min_max_angle_info);
		
		// cos_min_max_angle_info.imax is the index of the half edge
		//   whose from_vertex has min cosine and MAX angle.
		int ihalf_edge_max = cos_min_max_angle_info.imax;
		triangulate_cell_from_vertex
			(mesh, ihalf_edge_max, flag_terse, flag_no_warn, flag_check);
	}
	
	
	/**
	 * Triangulate each cell.
	 */
	protected static void triangulate_each_cell
		(HMeshEditC mesh, boolean flag_terse, boolean flag_no_warn)
			throws Exception
	{
		int n = mesh.MaxCellIndex();
		boolean flag_check = !flag_reduce_checks;
		
		// Create a list of the cell indices.
		ArrayList<Integer> cell_list = new ArrayList<Integer>();
		cell_list.addAll(mesh.CellIndices());
		
		// Sort so that cells are processed in sorted order.
		Collections.sort(cell_list);
		
		int kount = 0;
		for (Integer icell:cell_list) {
			CellBase cell = mesh.Cell(icell);
			// Note: Some cells may have been deleted.  Cell icell may not exist.
			if (cell == null) { continue; }
			
			triangulate_cell_from_vertex_with_largest_angle
				(mesh, cell, flag_terse, flag_no_warn, flag_check);
			kount++;
		
			if (flag_reduce_checks) {
				// Check mesh halfway through.
				if (kount == n/2) 
					{ check_mesh(mesh, flag_no_warn); }
			}
		}
	}
	
	
	/** Attempt to join each cell by deleting longest edge. */
	protected static void join_each_cell
		(HMeshEditC mesh, boolean flag_terse, boolean flag_no_warn)
			throws Exception
	{
		// Don't join cells with MAX_NUMV or more vertices.
		int MAX_NUMV = 6;
		int n = mesh.NumCells();
		boolean flag_check = !flag_reduce_checks;
		MinMaxInfo min_max_info = new MinMaxInfo();
		
		// Create a list of the cell indices.
		ArrayList<Integer> cell_list = new ArrayList<Integer>();
		cell_list.addAll(mesh.CellIndices());
		
		// Sort so that cells are processed in sorted order.
		Collections.sort(cell_list);
		
		int kount = 0;
		for (Integer icell:cell_list) {
			
			CellBase cell = mesh.Cell(icell);
			// Note: Some cells may have been deleted.  Cell icell may not exist.
			if (cell == null) { continue; }
		
			if (cell.NumVertices() >= MAX_NUMV) {
				// Don't let the cell get too large.
				continue;
			}
			
			measure_mesh.compute_cell_min_max_edge_length_squared(cell, min_max_info);
			final int ihalf_edge_max = min_max_info.imax;
			final HalfEdgeBase half_edge_max = mesh.HalfEdge(ihalf_edge_max);
			if (half_edge_max.NextCellAroundEdge().NumVertices() >= MAX_NUMV) {
				// Don't let cell get too large.
				continue;
			}
			
			join_two_cells
				(mesh, ihalf_edge_max, flag_terse, flag_no_warn, flag_check);
			kount++;
		
			if (flag_reduce_checks) {
				// Check mesh halfway through.
				if (kount == n/2) 
					{ check_mesh(mesh, flag_no_warn); }
			}
		}
	}
	
	
	// *** Split edges routines. ***
	
	/** Split edge. */
	protected static void split_edge
		(HMeshEditC mesh, int ihalf_edge, 
		boolean flag_terse, boolean flag_no_warn, boolean flag_check)
			throws Exception
	{
		final HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
		if (!flag_terse) {
			out.println("Splitting edge (" + half_edge.EndpointsStr(",") + ").");
		}
		
		final VertexBase v = mesh.SplitEdge(ihalf_edge);
		if (v == null) {
			out.println("Splitting of edge (" + 
				half_edge.EndpointsStr(",") + ") failed.");
		}
		
		if (flag_check)
			{ check_mesh(mesh, flag_no_warn); }
	}
	
	
	/** Split longest cell edge. */
	protected static void split_longest_cell_edge
		(HMeshEditC mesh, int icell,
			boolean flag_terse, boolean flag_no_warn, boolean flag_check)
				throws Exception
	{	
		MinMaxInfo min_max_info = new MinMaxInfo();
		CellBase cell = mesh.Cell(icell);
		
		measure_mesh.compute_cell_min_max_edge_length_squared(cell, min_max_info);
		
		final int ihalf_edge_max = min_max_info.imax;		
		split_edge(mesh, ihalf_edge_max, flag_terse, flag_no_warn, flag_check);		
	}

	
	/** Split longest edge in each cell. */
	protected static void split_longest_edge_in_each_cell
		(HMeshEditC mesh, boolean flag_terse, boolean flag_no_warn)
			throws Exception
	{
		int n = mesh.NumCells();
		boolean flag_check = !flag_reduce_checks;
		
		// Create a list of the cell indices.
		ArrayList<Integer> cell_list = new ArrayList<Integer>();
		cell_list.addAll(mesh.CellIndices());
		
		// Sort so that cells are processed in sorted order.
		Collections.sort(cell_list);
		
		int kount = 0;
		for (Integer icell:cell_list) {
			
			CellBase cell = mesh.Cell(icell);
			// Note: Some cells may have been deleted.  Cell icell may not exist.
			if (cell == null) { continue; }
			
			split_longest_cell_edge
				(mesh, icell, flag_terse, flag_no_warn, flag_check);
			kount++;
		
			if (flag_reduce_checks) {
				// Check mesh halfway through.
				if (kount == n/2) 
					{ check_mesh(mesh, flag_no_warn); }
			}
		}	 
	}
	
	
	// *** Check routines ***
	
	/** Return true if mesh passed mesh check, manifold check,
	 *    and orientation check.
	 */
	protected static boolean check_mesh
		(HMeshEditC mesh, boolean flag_no_warn)
	{
		ErrorInfo error_info = mesh.CheckAll();
		if (error_info.FlagError()) {
			err.println("Error detected in mesh data structure.");
			if (error_info.Message() != null && !(error_info.Message().equals(""))) {
				err.println(error_info.Message()); 
			}
			System.exit(-1);			
		}
		
		boolean flag_oriented_manifold =
				check_oriented_manifold(mesh, flag_no_warn);
		
		if (!flag_no_warn || flag_fail_on_non_manifold) {	
			if (flag_fail_on_non_manifold && !flag_oriented_manifold) {
				if (!flag_no_warn) {
					err.println("Detected non-manifold or inconsistent orientatins.");
					err.println("Exiting.");
				}
				
				System.exit(-1);
			}
			
			return flag_oriented_manifold;
		}
		else {
			return true;
		}
	}
	
	
	/** Return true if mesh is an oriented manifold. */
	protected static boolean check_oriented_manifold
		(HMeshEditC mesh, boolean flag_no_warn)
	{
		ManifoldInfo manifold_info = mesh.CheckManifold();
		OrientationInfo orientation_info = mesh.CheckOrientation();
		
		if (!manifold_info.FlagManifoldEdges()) {
			if (!flag_no_warn) {
				int ihalf_edge = manifold_info.HalfEdgeIndex();
				HalfEdgeA half_edge = mesh.HalfEdge(ihalf_edge);
				err.println("Warning: Non-manifold edge (" +
							half_edge.EndpointsStr(",") + ").");
				err.flush();
			}
			
			// Non-manifold edge automatically implies inconsistent orientation.
			return false;
		}
		
		if (orientation_info.IsOriented()) {
			if (!manifold_info.FlagManifoldVertices()) {
				if (!flag_no_warn) {
					err.printf("Warning: Non-manifold vertex %d.XXX%n",
								manifold_info.VertexIndex());
					err.flush();
				}
				return false;
			}
		}
		else {
			if (!manifold_info.FlagManifoldVertices()) {
				if (!flag_no_warn) {
					err.printf("Warning: Non-manifold vertex or inconsistent orientation in cells incident on vertex %d.%n",
								manifold_info.VertexIndex());
					err.flush();
				}
			}
			else {
				if (!flag_no_warn) {
					int ihalf_edge = manifold_info.HalfEdgeIndex();
					HalfEdgeA half_edge = 
							mesh.HalfEdge(ihalf_edge);
					err.println("Warning: Inconsistent orientation of cells incident on edge(" +
									half_edge.EndpointsStr(",") + ").");
					err.flush();
				}
			}
			
			return false;
		}
		
		return true;
	}
	
	
	/** Print a warning message if splitting cell at diagonal
	 *    (half_edgeA.FromVertex(), half_edgeB.FromVertex())
	 *    will change the mesh topology.
	 *  - Return true if split does not change the mesh topology.
	 */
	protected static boolean check_split_cell
		(HMeshEditC mesh, int ihalf_edgeA, int ihalf_edgeB, 
			boolean flag_no_warn)
	{
		final HalfEdgeBase half_edgeA = mesh.HalfEdge(ihalf_edgeA);
		final HalfEdgeBase half_edgeB = mesh.HalfEdge(ihalf_edgeB);
		VertexBase vA = half_edgeA.FromVertex();
		VertexBase vB = half_edgeB.FromVertex();
		int ivA = vA.Index();
		int ivB = vB.Index();
		int icell = half_edgeA.CellIndex();
		HalfEdgeBase half_edgeC = mesh.FindEdge(vA, vB);
		
		boolean flag_cell_edge = false;
		boolean return_flag = true;
		
		if (mesh.IsIllegalSplitCell(ihalf_edgeA, ihalf_edgeB)) {
			
			if ((vA == half_edgeB.ToVertex()) ||
				(vB == half_edgeA.FromVertex()))
				{ flag_cell_edge = true; }
				
			if (!flag_no_warn) {
				if (flag_cell_edge) {
					out.printf("(%d,%d) is a cell edge, not a cell diagonal.\n",
								ivA, ivB);
				}
				else {
					out.printf("Illegal split of cell %d with diagonal (%d,%d).\n",
								icell, ivA, ivB);
				}
			}
			
			return_flag = false;
		}
		
		if ((half_edgeC != null) && !flag_cell_edge) {
		
			if (!flag_no_warn) {
				out.printf("Splitting cell %d with diagonal (%d,%d)",
							icell, ivA, ivB);
				out.printf("  creates an edge incident on three or more cells.\n");
			}
			
			return_flag = false;
		}
		
		return return_flag;
	}
	
	
	/** Print a warning if joining cells separated by half_edge is illegal.
	 * <ul> <li> Return true if join is legal. </ul>
	 */
	protected static boolean check_join_cell
		(HMeshEditC mesh, int ihalf_edge, boolean flag_no_warn)
	{
		int TWO = 2;
		HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
		boolean return_flag = true;
		
		if (mesh.IsIllegalJoinTwoCells(ihalf_edge)) {
			HalfEdgeBase half_edgeX = 
				half_edge.NextHalfEdgeAroundEdge();

			if (!flag_no_warn) {
				final int ivfrom = half_edge.FromVertexIndex();
				final int ivto = half_edge.ToVertexIndex();
				if (half_edge.IsBoundary()) {
					out.printf("Only one cell contains edge(%s).\n",
								half_edge.EndpointsStr(","));
				}
				else if (!mesh.IsVertexIncidentOnMoreThanTwoEdges(ivfrom)) {
					out.printf("Half edge endpoint %d is incident on only two edges.\n",
								ivfrom);
				}
				else if (!mesh.IsVertexIncidentOnMoreThanTwoEdges(ivto)) {
					out.printf("Half edge endpoint %d is incident on only two edges.\n",
								ivto);
				}
				else if (half_edge != half_edgeX.NextHalfEdgeAroundEdge()) {
					out.printf("More than two cells are incident on edge (%s).\n",
								half_edge.EndpointsStr(","));
				}
				else {
					final int icell = half_edge.CellIndex();
					final int icellX = half_edgeX.CellIndex();
					final int num_shared_vertices = 
						mesh.CountNumVerticesSharedByTwoCells(icell, icellX);
					if (num_shared_vertices > TWO) {
						out.printf("Cells %d and %d share %d vertices.\n",
									icell, icellX, num_shared_vertices);
					}
					else {
						out.printf
							("Join of two cells incident on edge (%s) is illegal.\n",
									half_edge.EndpointsStr(","));
					}
				}
			}
			
			return_flag = false;
		}
		
		return return_flag;
	}
	
	protected static boolean reduce_checks_on_large_datasets
		(HMeshEditC mesh, boolean flag_no_warn, int large_data_num_cells)
	{
		int num_cells = mesh.NumCells();
		if (num_cells >= large_data_num_cells) {
			if (!flag_no_warn) {
				out.printf("Warning: Large data set with %d cells.\n", num_cells);
				out.println("  Reducing checks (using -flag_reduce_checks).");
			}
			
			return true;
		}
		else {
			return false;
		}
	}
	
	
	// *** Parse/print/prompt functions ****
	
	/** Parse command line. */
	protected static void parse_command_line(String[] argv)
	{
		int iarg = 0;
		
		while (iarg < argv.length &&
				argv[iarg].charAt(0) == '-') {
			String s= argv[iarg];
			if (s.equals("-collapse_short_edges"))
			{ flag_collapse_short_edges = true; }
			else if (s.equals("-split_cells"))
			{ flag_split_cells = true; }
			else if (s.equals("-join_cells"))
			{ flag_join_cells = true; }
			else if (s.equals("-split_long_edges"))
			{ flag_split_long_edges = true; }
			else if (s.equals("-split_long_edges_cells")) {
				flag_split_long_edges = true;
				flag_split_cells = true;
			}
			else if (s.equals("-triangulate_cells") ||
					s.equals("-triangulate"))
			{ flag_triangulate_cells = true; }
			else if (s.equals("-allow_non_manifold"))
			{ flag_allow_non_manifold = true; }
			else if (s.equals("-fail_on_non_manifold"))
			{ flag_fail_on_non_manifold = true; }
			else if (s.equals("-s")) {
				flag_silent = true;
				flag_terse = true;
			}
			else if (s.equals("-terse"))
			{ flag_terse = true; }
			else if (s.equals("-no_warn")) 
			{ flag_no_warn = true; }
			else if (s.equals("-time"))
			{ flag_time = true; }
			else if (s.equals("-h"))
			{ help(); }
			else {
				System.err.println("Usage error. Option " + s + " is undefined.");
				usage_error();
			}
			
			iarg++;
		}
		
		if (iarg >= argv.length || iarg+2 < argv.length)
		{ usage_error(); }
		
		input_filename = argv[iarg];
		
		if (iarg+1 < argv.length)
		{ output_filename = argv[iarg+1]; }
		
		if (!(flag_collapse_short_edges || flag_split_long_edges ||
				flag_join_cells || flag_split_cells || 
				flag_triangulate_cells)) {
			System.err.println("No edit operations specified.");
			System.err.println
				("Specify -collapse_short_edges, -split_long_edges, -split_cells");
			System.err.println
				("  -join_cells or triangluate_cells.");
			usage_error();
		}
	}

	
	/** Print mesh information (number of vertices, edges, etc. and
	 *    minimum and maximum edge length, cell angle, etc.)
	 */
	static void print_mesh_info(HMeshEditC mesh)
	{
		int FIVE = 5;
		int num_vertices = mesh.NumVertices();
		int num_edges = mesh.CountNumEdges();
		int num_boundary_edges = mesh.CountNumBoundaryEdges();
		int num_cells = mesh.NumCells();
		int num_triangles = mesh.CountNumTriangles();
		int num_quads = mesh.CountNumQuads();
		int num_large_cells = mesh.CountNumCellsOfSizeGE(FIVE);
		MinMaxInfo length_info = new MinMaxInfo();
		MinCellRatioInfo ratio_info = new MinCellRatioInfo();
		CosMinMaxAngleInfo angle_info = new CosMinMaxAngleInfo();
		ArrayList<Float> small_angle_bounds = new ArrayList<Float>();
		ArrayList<Float> large_angle_bounds = new ArrayList<Float>();
		
		small_angle_bounds.add(1f);
		small_angle_bounds.add(5f);
		small_angle_bounds.add(10f);
		large_angle_bounds.add(175f);
		large_angle_bounds.add(170f);
		
		
		measure_mesh.compute_min_max_edge_lengths_squared(mesh, length_info);
		measure_mesh.compute_min_cell_edge_length_ratio_squared(mesh, ratio_info);
		measure_mesh.compute_angle_info
			(mesh, small_angle_bounds, large_angle_bounds, angle_info);
		
		ManifoldInfo manifold_info = mesh.CheckManifold();
		OrientationInfo orientation_info = mesh.CheckOrientation();
		
		out.printf("Number of vertices: %d%n", num_vertices);
		out.printf("Number of mesh edges: %d%n", num_edges);
		out.printf("Number of boundary mesh edges: %d%n", num_boundary_edges);
		out.printf("Number of mesh cells: %d%n", num_cells);
		out.printf("  Number of mesh triangles: %d%n", num_triangles);
		out.printf("  Number of mesh quadrilaterals: %d%n", num_quads);
		out.printf("  Number of cells with > 4 vertices: %d%n", num_large_cells);
		out.printf("Min edge length: %.4f%n", sqrt(length_info.minVal));
		out.printf("Max edge length: %.4f%n", sqrt(length_info.maxVal));
		out.printf("Min cell edge length ratio: %.4f%n", 
					sqrt(ratio_info.ratio));
		
		// Note: minimum angle has maximum cosine.
		out.printf("Minimum cell angle: %.2f%n",
					toDegrees(acos(angle_info.cos_min_angle)));
		
		for (int i = 0; i < angle_info.SizeSmallAngleBounds(); 
				i++) {
			float A = angle_info.SmallAngleBounds(i);
			int num_cells_angle_le = angle_info.NumCellsWithAngleLESmall(i);
			out.printf("  Num cells with angle <= %.0f: %d%n", A, num_cells_angle_le);
		}
		
		// Note: maximum angle has minimum cosine.
		out.printf("Maximum cell angle: %.2f%n",
					toDegrees(acos(angle_info.cos_max_angle)));
		
		for (int i = 0; i < angle_info.SizeLargeAngleBounds(); 
				i++) {
			float A = angle_info.LargeAngleBounds(i);
			int num_cells_angle_ge = angle_info.NumCellsWithAngleGELarge(i);
			out.printf("  Num cells with angle >= %.0f: %d%n", A, num_cells_angle_ge);
		}
		
		if (manifold_info.FlagManifold() && 
				orientation_info.IsOriented()) {
			out.println("Mesh is an oriented_manifold.");
		}
		else {
			out.println("Mesh is non-manifold or has inconsistent cell orientations.");
		}
	}
	
	
	static void print_time(String label, long time) {
		double nanoseconds_per_second = 1E9;
		String s = String.format("%.4f", (time/nanoseconds_per_second));
		System.out.println(label + s + " seconds.");
	}
	
	
	static void usage_msg(PrintStream out)
	{
		out.println("Usage: decimate_mesh [OPTIONS] <input filename> [<output_filename>]");
		out.println("OPTIONS:");
		out.println("  [-collapse_short_edges] [-split_long_edges");
		out.println("  [-split_cells] [-join_cells]");
		out.println("  [-triangulate_cells | -triangulate]");
		out.println("  [-allow_non_manifold] [-fail_on_non_manifold]");
		out.println("  [-s | -terse] [-no_warn] [-time] [-h]");
	}
	
	
	static void usage_error()
	{
		usage_msg(System.err);
		System.exit(-1);
	}
	
	static void help()
	{
		usage_msg(System.out);
		out.println();
		out.println("decimate_mesh - Decimate mesh.");
		out.println("  Collapse/split/join mesh edges or cells.");
		out.println();
		out.println("Options:");
		out.println("-collapse_short_edges: Attempt to collapse shortest edge in each cell.");
		out.println("-split_long_edges: Split longest edge in each cell.");
		out.println("-split_cells:      Split cells at vertices with largest angles.");
		out.println("-join_cells:       Join cells sharing long edges.");
		out.println("-triangulate_cells | -triangulate:");
		out.println("     Triangulate each cell from vertex with largest angle.");
		out.println("-allow_non_manifold:   Allow edge collapses or cell splits");
		out.println("     that create non-manifold conditions.");
		out.println("-fail_on_non_manifold: Exit with non-zero return code (fail)");
		out.println("     if non-manifold or inconsistent orientations detected.");
		out.println("-terse:   Terse output. Suppress messages output after each");
		out.println("     collapse/join/split iteration.");
		out.println("   Does not suppress warning messages at each iteration.");
		out.println("   Does not suppress final mesh information.");
		out.println("-s:       Silent. Output only warnings and error messages.");
		out.println("-no_warn: Do not output non-manifold or inconsistent orientation warnings.");
		out.println("-time:    Report run time.");
		out.println("-h:       Output this help message and exit.");
		System.exit(0);
	}
}
