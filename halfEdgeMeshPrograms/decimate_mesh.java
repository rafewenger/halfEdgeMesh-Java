package halfEdgeMeshPrograms;

import static java.lang.System.out;
import static java.lang.System.err;
import static java.lang.Math.*;
import java.io.*;
import java.util.*;

import osu.halfEdgeMesh.*;
import osu.halfEdgeMeshDCMT.*;


/** Some simple mesh decimation routines.
 *  - Uses data structure HalfEdgeMeshDCMTABase (DCMT = decimate).
 * @author rafew
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
	static boolean flag_collapse_edges = false;
	static boolean flag_collapse_short_edges = false;
	static boolean flag_split_cells = false;
	static boolean flag_split_all_cells = false;
	static boolean flag_join_cells = false;
	static boolean flag_join_each_cell = false;
	static boolean flag_split_edges = false;
	static boolean flag_split_long_edges = false;
	static boolean flag_allow_non_manifold = false;
	static boolean flag_fail_on_non_manifold = false;	
	
	
	public static void main(String[] argv) {
		
		long begin_time = System.nanoTime();
		
		HalfEdgeMeshDCMTA mesh = new HalfEdgeMeshDCMTA();
		OffFileReaderDCMTA file_reader = new OffFileReaderDCMTA();
		OffFileWriterDCMTA file_writer = new OffFileWriterDCMTA();
		
		parse_command_line(argv);
		
		file_reader.OpenAndReadFile(input_filename, mesh);
		
		long time2 = System.nanoTime();
		
		try {
			
			long time3 = System.nanoTime();
			
			if (flag_collapse_edges) {
				prompt_and_collapse_edges
					(mesh, flag_terse, flag_no_warn); 
			}
			
			if (flag_collapse_short_edges) {
				collapse_shortest_edge_in_each_cell
					(mesh, flag_terse, flag_no_warn);
			}
			
			boolean passed_check = check_mesh(mesh, flag_silent && flag_no_warn);
			
			if (!flag_silent) {
				if (passed_check)
				{ out.println("Mesh data structure passed check."); }
			}
			
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
		(HalfEdgeMeshDCMTA mesh, HalfEdgeDCMTBase half_edge,
		boolean flag_terse, boolean flag_no_warn, boolean flag_check)
			throws Exception
	{
		boolean flag = check_edge_collapse(mesh, half_edge, flag_no_warn);
	
		if (mesh.IsIllegalEdgeCollapse(half_edge))
		{ return; }
		
		if (flag || flag_allow_non_manifold) {
			if (!flag_terse) {
				out.println("Collapsing edge (" + 
							half_edge.EndpointsStr(",") + ")."); 
			}
		
			VertexDCMTBase vnew = 
				mesh.CollapseEdge(half_edge.Index());
			if (vnew == null) {
				out.println("Skipped illegal collapse of edge (" +
							half_edge.EndpointsStr(",") + ").");
			}
		
			if (flag_check)
				{ check_mesh(mesh, flag_no_warn); }
		}
		else {
			if (!flag_terse) {
				out.println("Skipped collapse of edge (" +
							half_edge.EndpointsStr(",") + ").");
			}
		}
		
	}
	
	
	/** Prompt and collapse edges. */
	protected static void prompt_and_collapse_edges
	(HalfEdgeMeshDCMTA mesh, boolean flag_terse, boolean flag_no_warn)
		throws Exception
	{
		Scanner scanner = new Scanner(System.in);
		
		while (true) {
			HalfEdgeDCMTBase half_edge0 = 
				prompt_for_mesh_edge(scanner, mesh, false);
			
			if (half_edge0 == null) {
				// End.
				out.println();			
				return;
			}
			
			collapse_edge
				(mesh, half_edge0, flag_terse, flag_no_warn, true);
		}
	}
	
	
	
	/** Split shortest cell edge. */
	protected static void collapse_shortest_cell_edge
		(HalfEdgeMeshDCMTA mesh, int icell, boolean flag_terse,
				boolean flag_no_warn, boolean flag_check)
			throws Exception
	{
		CellDCMTA cell = mesh.Cell(icell);
		MinMaxInfo min_max_info = new MinMaxInfo();
		
		cell.ComputeMinMaxEdgeLengthSquared(min_max_info);
		HalfEdgeDCMTA half_edge_min =
				mesh.HalfEdge(min_max_info.imin);
		
		collapse_edge
			(mesh, half_edge_min, flag_terse, flag_no_warn, flag_check);
	}
	
	/** Collapse shortest edge in each cell. */
	protected static void collapse_shortest_edge_in_each_cell
		(HalfEdgeMeshDCMTA mesh, boolean flag_terse, boolean flag_no_warn)
			throws Exception
	{
		int n = mesh.NumCells();
		
		// SHOULD CALL reduce_checks_on_large_datasets;
		boolean flag_check = !flag_reduce_checks;
		
		ArrayList<Integer> cell_list = new ArrayList<Integer>();
		cell_list.addAll(mesh.CellIndices());
		
		// Sort so that cells are processed in sorted order.
		Collections.sort(cell_list);
		
		int kount = 0;
		for (Integer icell:cell_list) {
			
			// *** DEBUG ***
			/*
			System.out.printf("Collapsing shortest edge in cell %d%n", icell);
			*/
			
			// Note: Some cells may have been deleted.  Cell icell may not exist.
			if (mesh.Cell(icell) == null) {
				
				// *** DEBUG ***
				/*
				System.out.printf("Cell %d does not exist.%n", icell);
				*/
				
				continue;
			}
			
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
	
	
	// *** Check routines ***
	
	/** Return true if mesh passed mesh check, manifold check,
	 *    and orientation check.
	 */
	protected static boolean check_mesh
		(HalfEdgeMeshDCMTA mesh, boolean flag_no_warn)
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
		(HalfEdgeMeshDCMTA mesh, boolean flag_no_warn)
	{
		ManifoldInfo manifold_info = mesh.CheckManifold();
		OrientationInfo orientation_info = mesh.CheckOrientation();
		
		if (!manifold_info.FlagManifoldEdges()) {
			if (!flag_no_warn) {
				int ihalf_edge = manifold_info.HalfEdgeIndex();
				HalfEdgeDCMTA half_edge = mesh.HalfEdge(ihalf_edge);
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
					HalfEdgeDCMTA half_edge = 
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
	
	
	/* Return true if collapse is not illegal and does not change
	 *   mesh topology.
	 * - Print a warning message if collapsing half edges illegal
	 *   or will change mesh topology. 
	 */
	public static boolean check_edge_collapse
	(HalfEdgeMeshDCMTA mesh, HalfEdgeDCMTBase half_edge, boolean flag_no_warn)
	{
		VertexInfo vertex_info = new VertexInfo();
		int icell = half_edge.CellIndex();
		boolean return_flag = true;
		
		if (mesh.IsIllegalEdgeCollapse(half_edge)) {
			if (!flag_no_warn) {
				out.println("Collapse of edge (" +
							half_edge.EndpointsStr(",") +
							") is illegal.");
				out.println("  Some cell contains vertices " +
							half_edge.EndpointsStr(" and ") +
							" but not edge (" +
							half_edge.EndpointsStr(",") + ").");
			}
			
			return_flag = false;
		}
		
		
		if (mesh.FindTriangleHole(half_edge, vertex_info)) {
			if (!flag_no_warn) {
				out.println("Collapsing edge (" +
							half_edge.EndpointsStr(",") +
							") will change the mesh topology.");
				out.printf("  Vertices(%d,%d,%d) form a triangle hole.%n",
							half_edge.FromVertexIndex(),
							half_edge.ToVertexIndex(),
							vertex_info.index0);
			}
			
			return_flag = false;
		}
		
		if (!half_edge.IsBoundary()) {
			if (half_edge.FromVertex().IsBoundary() &&
				half_edge.ToVertex().IsBoundary()) {
				if (!flag_no_warn) {
					out.println("Collapsing edge(" +
								half_edge.EndpointsStr(",") +
								") merges two non-ajacent boundary vertices.");
				}
				
				return_flag = false;
			}
		}
		
		if (mesh.IsIsolatedTriangle(icell)) {
			if (!flag_no_warn) {
				out.printf("Collapsing edge (%s) will delete isolated cell %d.%n",
							half_edge.EndpointsStr(","), icell);
			}
			
			return_flag = false;
		}
		
		if (mesh.IsInTetrahedron(icell)) {
			if (!flag_no_warn) {
				out.println("Collapsing edge (" +
							half_edge.EndpointsStr(",") +
							") will collapse a tetrahedron.");
			}

			
			return_flag = false;
		}
		
		return return_flag;
	}
	
	
	// *** SUBROUTINES ****
	
	protected static void parse_command_line(String[] argv)
	{
		int iarg = 0;
		
		while (iarg < argv.length &&
				argv[iarg].charAt(0) == '-') {
			String s= argv[iarg];
			if (s.equals("-collapse_edges"))
			{ flag_collapse_edges = true; }
			else if (s.equals("-collapse_short_edges"))
			{ flag_collapse_short_edges = true; }
			else if (s.equals("-allow_non_manifold"))
			{ flag_allow_non_manifold = true; }
			else if (s.equals("-s"))
			{ flag_silent = true; }
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
	}


	/** Prompt for mesh edge.
	 *  - Return null if user enters a negative number.
	 */
	static HalfEdgeDCMTBase prompt_for_mesh_edge
		(Scanner scanner, HalfEdgeMeshDCMTA mesh, boolean flag_only_internal)
	{
		String error_msg;
		
		while (true) {
			out.print("Enter vertex (-1 to end): ");
			int iv0 = scanner.nextInt();
			if (iv0 < 0 ) { return null; }
			
			ErrorInfo error_info = mesh.CheckVertexIndex(iv0);
			if (error_info.FlagError()) {
				out.println(error_info.Message());
				continue;
			}
			
			VertexDCMTA v0 = mesh.Vertex(iv0);
			
			if (v0.NumHalfEdgesFrom() == 0) {
				out.printf("Vertex %d is not incident on any cell.%n", iv0);
				continue;
			}
			
			if (flag_only_internal) {
				int num_internal_half_edges_from = 0;
				out.printf("Internal half edges from %d:", iv0);
				for (int k = 0; k < v0.NumHalfEdgesFrom(); k++) {
					HalfEdgeDCMTBase half_edge = v0.KthHalfEdgeFrom(k);
					if (!half_edge.IsBoundary()) {
						out.print("  (" + half_edge.EndpointsStr(",") + ")");
						num_internal_half_edges_from++;
					}
				}
				out.println();
				
				if (num_internal_half_edges_from == 0) {
					out.printf("No internal half edges from %d.%n", iv0);
					out.println("Start again.");
					out.println();
					continue;
				}
			}
			else {
				out.printf("Half edges from %d:", iv0);
				for (int k = 0; k < v0.NumHalfEdgesFrom(); k++) {
					HalfEdgeBase half_edge = v0.KthHalfEdgeFrom(k);
					out.printf("  (" + half_edge.EndpointsStr(",") + ")");
				}
				out.println();
			}
			
			out.printf
				("Enter vertex adjacent to vertex %d (-1 to end): ", iv0);
			int iv1 = scanner.nextInt();
			if (iv1 < 0) { return null; }
			
			error_info = mesh.CheckVertexIndex(iv1);
			if (error_info.FlagError()) {
				out.println(error_info.Message());
				continue;
			}
			
			HalfEdgeDCMTBase half_edge0 = 
				v0.FindIncidentHalfEdge(iv1);
			
			if (flag_only_internal && half_edge0.IsBoundary()) {
				out.println("Half edge (" + half_edge0.EndpointsStr(",") +
							") is a boundary half edge.");
				out.println("Start  again.");
				continue;
			}
			
			return half_edge0;
		}
	}
	
	
	/** Print mesh information (number of vertices, edges, etc. and
	 *    minimum and maximum edge length, cell angle, etc.)
	 */
	static void print_mesh_info(HalfEdgeMeshDCMTA mesh)
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
		MinMaxInfo angle_info = new MinMaxInfo();
		FlagZero  flag_zero = new FlagZero();
		
		mesh.ComputeMinMaxEdgeLengthsSquared(length_info);
		mesh.ComputeMinCellEdgeLengthRatioSquared(ratio_info);
		mesh.ComputeCosMinMaxAngle(angle_info, flag_zero);
		
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
					toDegrees(acos(angle_info.maxVal)));
		
		// Note: maximum angle has minimum cosine.
		out.printf("Maximum cell angle: %.2f%n",
					toDegrees(acos(angle_info.minVal)));
		
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
		out.println("Usage: decimate_mesh [-s] [-no_warn] [-time] [-h] <input filename> [<output_filename>]");
	}
	
	
	static void usage_error()
	{
		usage_msg(System.err);
		System.exit(-1);
	}
	
	static void help()
	{
		usage_msg(System.out);
		System.out.println();
		System.exit(0);
	}
}
