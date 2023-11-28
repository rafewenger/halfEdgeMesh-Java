package halfEdgeMeshPrograms;

/** 
 *  @file meshinfo - Print mesh information.
 *  @version 0.1.0
 *  @author Rephael Wenger
 */


import static java.lang.System.out;
import java.io.*;
import java.util.*;

import halfEdgeMesh.*;
import halfEdgeMeshMeasure.*;

/**
 * Print mesh information.
 * <ul>
 * <li> Print number of vertices, edges, cells, and boundary edges.
 * <li> Print minimum and maximum edge lengths, minimum ratio
 *   of shortest to longest edge length in any cell,
 *   and minimum and maximum cell angles.
 * <li> Prints whether or not mesh is an oriented manifold.
 * </ul>
 */
public class meshinfo {

	static String input_filename;
	static boolean flag_more_info = false;
	static MeasureMeshA measure_mesh = new MeasureMeshA();
	
	public static void main(String[] argv)
	{	
		HalfEdgeMeshA mesh = new HalfEdgeMeshA();
		OffFileReaderA file_reader = new OffFileReaderA();

		
		parse_command_line(argv);
		
		file_reader.OpenAndReadFile(input_filename, mesh);
		
		try {
			
			ErrorInfo error_info = mesh.CheckAll();
			if (error_info.FlagError()) {
				mesh.PrintErrorMessage(System.err, error_info);
				System.err.println("  Exiting...");
				System.exit(-1);
			}
			
			print_mesh_size(mesh, flag_more_info);
			print_min_max_edge_lengths(mesh, flag_more_info);
			print_min_cell_edge_length_ratio(mesh, flag_more_info);
			print_min_max_angles(mesh, flag_more_info);
			print_manifold_info(mesh, flag_more_info);
			out.println();
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	
	// *** PRINT MESH INFORMATION ***

	/**
	 *  Print number of vertices, edges, boundary edges, cells, etc. in mesh.
	 */
	public static void print_mesh_size
	(HalfEdgeMeshA mesh, boolean flag_more_info)
	{
		int num_vertices = mesh.NumVertices();
		int num_isolated_vertices = mesh.CountNumIsolatedVertices();
		int num_edges = mesh.CountNumEdges();
		int num_boundary_edges = mesh.CountNumBoundaryEdges();
		int num_cells = mesh.NumCells();
		
		out.printf("Number of mesh vertices: %d%n", num_vertices-num_isolated_vertices);
		if (num_isolated_vertices > 0) {
			out.printf("Total number of vertices in the input file: %d%n", 
						num_vertices);
		}
		out.printf("Number of mesh edges: %d%n", num_edges);
		out.printf("Number of boundary mesh edges: %d%n", 
					num_boundary_edges);
		out.printf("Number of mesh cells: %d%n", num_cells);
		
		if (flag_more_info) {
			int num_triangles = mesh.CountNumTriangles();
			out.printf("  Number of mesh triangles: %d%n", num_triangles);
			int num_quads = mesh.CountNumQuads();
			out.printf("  Number of mesh quadrilaterals: %d%n", num_quads);
			int num_pentagons = mesh.CountNumPentagons();
			int num_large_cells = mesh.CountNumCellsOfSizeGE(6);
			if (num_pentagons > 0) {
				out.printf("  Number of mesh pentagons: %d%n", num_pentagons);
				out.printf("  Number of cells with > 5 vertices: %d%n",
							num_large_cells);
			}
			else {
				out.printf("  Number of cells with > 4 vertices: %d%n", 
							num_large_cells);
			}
		}
	}

	
	/**
	 *  Print minimum and maximum mesh edge lengths.
	 */
	public static void print_min_max_edge_lengths
	(HalfEdgeMeshA mesh, boolean flag_more_info)
	{
		MinMaxInfo min_max_info = new MinMaxInfo();
		measure_mesh.compute_min_max_edge_lengths_squared(mesh, min_max_info);
		out.printf("Min edge length: %.4f%n", Math.sqrt(min_max_info.minVal));
		if (flag_more_info) {
			HalfEdgeBase half_edge_min = mesh.HalfEdge(min_max_info.imin);
			int icell = half_edge_min.CellIndex();
			out.printf("  Min length = length of edge %s in cell %d.%n",
						half_edge_min.IndexAndEndpointsStr(","), icell);
		}
		out.printf("Max edge length: %.4f%n", Math.sqrt(min_max_info.maxVal));
		if (flag_more_info) {
			HalfEdgeBase half_edge_max = mesh.HalfEdge(min_max_info.imax);
			int icell = half_edge_max.CellIndex();
			out.printf("  Max length = length of edge %s in cell %d.%n",
						half_edge_max.IndexAndEndpointsStr(","), icell);
		}
	} 
	
	
	/**
	 * Print minimum ratio of minimum to maximum edge length in any cell.
	 */
	public static void print_min_cell_edge_length_ratio
	(HalfEdgeMeshA mesh, boolean flag_more_info)
	{
		MinCellRatioInfo min_cell_ratio_info = new MinCellRatioInfo();
		
		measure_mesh.compute_min_cell_edge_length_ratio_squared
		(mesh, min_cell_ratio_info);
		final double min_ratio = min_cell_ratio_info.ratio;
		out.printf("Min cell edge length ratio: %.4f.%n", Math.sqrt(min_ratio));
		if (flag_more_info) {
			final int icell = min_cell_ratio_info.icell;
			final HalfEdgeBase half_edge_min = 
				mesh.HalfEdge(min_cell_ratio_info.imin);
			final HalfEdgeBase half_edge_max =
				mesh.HalfEdge(min_cell_ratio_info.imax);
			out.printf("  In cell: %d%n", icell);
			out.printf("  Min cell edge length: %.4f. Edge: (%s).%n",
						Math.sqrt(min_cell_ratio_info.minVal),
						half_edge_min.EndpointsStr(","));
			out.printf("  Max cell edge length: %.4f. Edge: (%s).%n",
						Math.sqrt(min_cell_ratio_info.maxVal),
						half_edge_max.EndpointsStr(","));
		}
	}
	
	
	/**
	 * Print minimum and maximum angles in mesh.
	 */
	public static void print_min_max_angles
	(HalfEdgeMeshA mesh, boolean flag_more_info)
	{
		CosMinMaxAngleInfo cos_min_max_angle_info =
				new CosMinMaxAngleInfo();
		ArrayList<Float> small_angle_bounds = new ArrayList<Float>();
		ArrayList<Float> large_angle_bounds = new ArrayList<Float>();
		
		small_angle_bounds.add(1f);
		small_angle_bounds.add(5f);
		small_angle_bounds.add(10f);
		large_angle_bounds.add(175f);
		large_angle_bounds.add(170f);
		
		measure_mesh.compute_angle_info
			(mesh, small_angle_bounds, large_angle_bounds, cos_min_max_angle_info);
		final double cos_min_angle = cos_min_max_angle_info.cos_min_angle;
		final double min_angle = Math.toDegrees(Math.acos(cos_min_angle));
		out.printf("Min angle: %.4f.%n", min_angle);
		if (flag_more_info) {
			final HalfEdgeBase half_edge_min = 
				mesh.HalfEdge(cos_min_max_angle_info.imin);
			final int icell_min = half_edge_min.CellIndex(); 
			out.printf("  At vertex %d in cell %d.%n",
						half_edge_min.FromVertexIndex(), icell_min);
		}
		
		for (int i = 0; i < cos_min_max_angle_info.SizeSmallAngleBounds(); 
				i++) {
			float A = cos_min_max_angle_info.SmallAngleBounds(i);
			int num_cells = cos_min_max_angle_info.NumCellsWithAngleLESmall(i);
			out.printf("Num cells with angle <= %.0f: %d%n", A, num_cells);
		}
		
		final double cos_max_angle = cos_min_max_angle_info.cos_max_angle;
		final double max_angle = Math.toDegrees(Math.acos(cos_max_angle));
		out.printf("Max angle: %.4f.%n", max_angle);
		if (flag_more_info) {
			final HalfEdgeBase half_edge_max = 
				mesh.HalfEdge(cos_min_max_angle_info.imax);
			final int icell_max = half_edge_max.CellIndex(); 
			out.printf("  At vertex %d in cell %d.%n",
						half_edge_max.FromVertexIndex(), icell_max);
		}
		
		for (int i = 0; i < cos_min_max_angle_info.SizeLargeAngleBounds(); 
				i++) {
			float A = cos_min_max_angle_info.LargeAngleBounds(i);
			int num_cells = cos_min_max_angle_info.NumCellsWithAngleGELarge(i);
			out.printf("Num cells with angle >= %.0f: %d%n", A, num_cells);
		}
		
	}
	
	
	/**
	 * Print information about whether mesh is a manifold and
	 *   whether there are adjacent cells with inconsistent orientations.
	 */
	public static void print_manifold_info
	(HalfEdgeMeshA mesh, boolean flag_more_info)
	{
		ManifoldInfo manifold_info = mesh.CheckManifold();
		OrientationInfo orientation_info = mesh.CheckOrientation();
		
		if (manifold_info.FlagManifold() && 
			orientation_info.IsOriented())
		{ out.println("Mesh is an oriented manifold.");	}
		else if (!manifold_info.FlagManifoldEdges()) {
			int ihalf_edge = manifold_info.HalfEdgeIndex();
			HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
		    out.println("Mesh has a non-manifold edge (" +
		    			half_edge.EndpointsStr(",") + ").");
		}
		else if (!manifold_info.FlagManifoldVertices() &&
				 orientation_info.IsOriented()) {
			int iv = manifold_info.VertexIndex();
			out.printf("Mesh has a non-manifold vertex %d.%n", iv);
		}
		else if (!manifold_info.FlagManifoldVertices()) {
			int iv = manifold_info.VertexIndex();
			out.printf
				("Non-manifold or inconsistent orientations at vertex %d.%n", iv);		
		}
		else if (!orientation_info.IsOriented()) {
			int ihalf_edge = orientation_info.HalfEdgeIndex();
			HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
			HalfEdgeBase half_edgeX = 
				half_edge.NextHalfEdgeAroundEdge();
			out.println("Mesh is a manifold.");
			out.printf
				("Inconsistent orientations of cells %d and %d.%n",
					half_edge.CellIndex(), half_edgeX.CellIndex());		
		}
	}


	
	// *** SUBROUTINES ****
	
	protected static void parse_command_line(String[] argv)
	{
		int iarg = 0;
		
		while (iarg < argv.length &&
				argv[iarg].charAt(0) == '-') {
			String s= argv[iarg];
			if (s.equals("-h"))
			{ help(); }
			else if (s.equals("-more"))
			{ flag_more_info = true; }
			else {
				System.err.println("Usage error. Option " + s + " is undefined.");
				usage_error();
			}
			
			iarg++;
		}
		
		if (iarg+1 != argv.length)
		{ usage_error(); }
		
		input_filename = argv[iarg];
	}
	
	
	static void usage_msg(PrintStream out)
	{
		out.println("Usage: meshinfo [-more] [-h] <input filename>");
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
		out.println("meshinfo - Print mesh information.");
		out.println();
		out.println("Options:");
		out.println("-h:        Output this help message and exit.");
		out.println("-more:     Print additional information.");
		
		System.exit(0);
	}
	
	/// Print a warning message if the mesh is not a manifold or not oriented.
	/// - Returns true if mesh is not a manifold or not oriented
	static boolean warn_non_manifold_or_not_oriented(HalfEdgeMeshA mesh)
	{
		ManifoldInfo manifold_info = mesh.CheckManifold();
		
		if (!manifold_info.FlagManifold()) {
			if (!manifold_info.FlagManifoldVertices()) {
				int iv = manifold_info.VertexIndex();
				System.err.println
				("Warning: Non manifold vertex " + 
					String.valueOf(iv) + ".");
			}
			
			if (!manifold_info.FlagManifoldEdges()) {
				int ihalf_edge = manifold_info.HalfEdgeIndex();
				HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
				System.err.println
				("Warning: Non manifold edge (" +
					half_edge.EndpointsStr(",") + ").");
			}
			
			return true;
		}
		else {
			OrientationInfo orientation_info = mesh.CheckOrientation();
			
			if (!orientation_info.IsOriented()) {
				int ihalf_edge = orientation_info.HalfEdgeIndex();
				HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
				System.err.println
				("Warning inconsistent orientation of cells incident on edge (" +
						half_edge.EndpointsStr(",") + ").");
				
				return true;
			}
		}
		
		return false;
	}
	
}
