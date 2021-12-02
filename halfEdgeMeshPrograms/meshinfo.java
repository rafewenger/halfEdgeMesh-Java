package halfEdgeMeshPrograms;

import osu.halfEdgeMesh.ManifoldInfo;
import osu.halfEdgeMesh.OrientationInfo;
import osu.halfEdgeMeshDCMT.*;

import static java.lang.System.out;
import static java.lang.Math.*;
import java.io.PrintStream;


/** Print mesh information */
public class meshinfo {

	static String input_filename;
	static boolean flag_more_info = false;
	
	
	public static void main(String[] argv) {
		
		HalfEdgeMeshDCMTA mesh = new HalfEdgeMeshDCMTA();
		OffFileReaderDCMTA file_reader = new OffFileReaderDCMTA();
		
		parse_command_line(argv);
		
		file_reader.OpenAndReadFile(input_filename, mesh);

		try {
			print_mesh_size(mesh, flag_more_info);
			print_min_max_edge_lengths(mesh, flag_more_info);
			print_min_cell_edge_length_ratio(mesh, flag_more_info);
			print_min_max_cell_angles(mesh, flag_more_info);
			print_manifold_info(mesh, flag_more_info);
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}


	
	// *** PRINT MESH INFORMATION ***
	

	public static void print_mesh_size
	(HalfEdgeMeshDCMTA mesh, boolean flag_more_info)
	{
		int num_vertices = mesh.NumVertices();
		int num_isolated_vertices = mesh.CountNumIsolatedVertices();
		int num_edges = mesh.CountNumEdges();
		int num_boundary_edges = mesh.CountNumBoundaryEdges();
		int num_cells = mesh.NumCells();
		
		out.printf("Number of mesh vertices: %d%n", num_vertices);
		if (num_isolated_vertices > 0) {
			out.printf("Total number of vertices in the input file: %d%n", 
						num_isolated_vertices);
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
	
	
	public static void print_min_max_edge_lengths
	(HalfEdgeMeshDCMTA mesh, boolean flag_more_info)
	{
		MinMaxInfo min_max_info = new MinMaxInfo();
		
		mesh.ComputeMinMaxEdgeLengthsSquared(min_max_info);
		out.printf("Min edge length: %.4f%n", 
				sqrt(min_max_info.minVal));
		
		if (flag_more_info) {
			HalfEdgeDCMTA half_edge = 
					mesh.HalfEdge(min_max_info.imin);
			out.printf("  Length of edge: (%s).  In cell: %d.%n",
						half_edge.EndpointsStr(","),
						half_edge.CellIndex());
		}
		
		out.printf("Max edge length: %.4f%n",
				sqrt(min_max_info.maxVal));
		
		if (flag_more_info) {
			HalfEdgeDCMTA half_edge = 
					mesh.HalfEdge(min_max_info.imax);
			out.printf("  Length of edge: (%s).  In cell: %d.%n",
						half_edge.EndpointsStr(","),
						half_edge.CellIndex());
		}
	}
	
	
	public static void print_min_cell_edge_length_ratio
	(HalfEdgeMeshDCMTA mesh, boolean flag_more_info)
	{
		MinCellRatioInfo min_cell_ratio_info = new MinCellRatioInfo();
		
		mesh.ComputeMinCellEdgeLengthRatioSquared(min_cell_ratio_info);
		
		out.printf("Min cell edge length ratio: %.2f%n", 
					sqrt(min_cell_ratio_info.ratio));
		
		if (flag_more_info) {
			out.printf("  In cell: %d%n", min_cell_ratio_info.icell);
			HalfEdgeDCMTA half_edge_min =
					mesh.HalfEdge(min_cell_ratio_info.imin);
			HalfEdgeDCMTA half_edge_max =
					mesh.HalfEdge(min_cell_ratio_info.imax);
			out.printf("  Min cell edge length: %f.",
						sqrt(min_cell_ratio_info.minVal));
			out.println("  Edge: (" + half_edge_min.EndpointsStr(",")
						+ ").");
			out.printf("  Max cell edge length: %f.",
					sqrt(min_cell_ratio_info.maxVal));
			out.println("  Edge: (" + half_edge_max.EndpointsStr(",")
						+ ").");
		}
	}
	
	
	public static void print_min_max_cell_angles
	(HalfEdgeMeshDCMTA mesh, boolean flag_more_info)
	{
		MinMaxInfo min_max_info = new MinMaxInfo();
		FlagZero flag_zero = new FlagZero();
		
		mesh.ComputeCosMinMaxAngle(min_max_info, flag_zero);
		// Note: Largest angle has smallest cosine.
		//   Smallest angle has largest cosine.
		out.printf("Minimum cell angle: %.2f%n", 
				toDegrees(acos(min_max_info.maxVal)));
		
		if (flag_more_info) {
			HalfEdgeDCMTBase half_edge =
					mesh.HalfEdge(min_max_info.imax);
			out.printf("  At vertex %d in cell %d.%n",
						half_edge.FromVertexIndex(),
						half_edge.CellIndex());
		}
		
		out.printf("Maximum cell angle: %.2f%n",
				toDegrees(acos(min_max_info.minVal)));
		
		if (flag_more_info) {
			HalfEdgeDCMTBase half_edge =
					mesh.HalfEdge(min_max_info.imin);
			out.printf("  At vertex %d in cell %d.%n",
						half_edge.FromVertexIndex(),
						half_edge.CellIndex());
		}
	}
	
	
	public static void print_manifold_info
	(HalfEdgeMeshDCMTA mesh, boolean flag_more_info)
	{
		ManifoldInfo manifold_info = mesh.CheckManifold();
		OrientationInfo orientation_info = mesh.CheckOrientation();
		
		if (manifold_info.FlagManifold() && 
			orientation_info.IsOriented())
		{ out.println("Mesh is an oriented manifold.");	}
		else if (!manifold_info.FlagManifoldEdges()) {
			int ihalf_edge = manifold_info.HalfEdgeIndex();
			HalfEdgeDCMTA half_edge = mesh.HalfEdge(ihalf_edge);
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
			HalfEdgeDCMTA half_edge = mesh.HalfEdge(ihalf_edge);
			HalfEdgeDCMTBase half_edgeX = 
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
			String s = argv[iarg];
			if (s.equals("-more"))
			{ flag_more_info = true; }
			else if (s.equals("-h"))
			{ help(); }
			else {
				System.err.println
					("Usage error.  Option " + s + " is undefined.");
				usage_error();
			}
			
			iarg++;
		}
		
		if (iarg+1 != argv.length )
		{ usage_error(); }
		
		input_filename = argv[iarg];
	}
	
	static void usage_msg(PrintStream out)
	{
		out.println("Usage: meshinfo <input filename>");
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
		System.out.println("Options:");
		System.out.println("-more:  Print additional information.");
		System.out.println("-h:     Print this help message and exit.");
		System.exit(0);
	}
	
}