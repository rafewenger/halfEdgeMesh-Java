package halfEdgeMeshPrograms;

import java.io.*;
import java.util.*;

import halfEdgeMesh.*;

/** Test program for deleting cells from HalfEdgeMesh.
 * <ul>
 * 		<li> Reads a .off file into a HalfEdgeMesh.
 * 		<li> Checks mesh for data structure consistency.
 * 		<li> Deletes cells in order, checking mesh data structure consistency
 *           after each delete.
 * </ul>
 * @author Rephael Wenger
 */
public class TestHMeshDeleteCell {
	
	static String input_filename;
	static boolean flag_silent = false;
	static boolean flag_no_warn = false;
	static boolean flag_time = false;
	static boolean flag_verbose = false;

	public static void main(String[] argv)
	{
		long begin_time = System.nanoTime();
		
		HalfEdgeMeshA mesh = new HalfEdgeMeshA();
		OffFileReaderA file_reader = new OffFileReaderA();
		
		parse_command_line(argv);
		
		file_reader.OpenAndReadFile(input_filename, mesh);
		
		long time2 = System.nanoTime();
		
		try {
			
			ErrorInfo error_info = mesh.CheckAll();
			if (error_info.FlagError()) {
				mesh.PrintErrorMessage(System.err, error_info);
				System.err.println("  Exiting...");
				System.exit(-1);
			}
			
	
			if (!flag_silent) {
				System.out.println("Mesh data structure passed initial check.");
			}

			ArrayList<Integer> cell_list = new ArrayList<Integer>();
			cell_list.addAll(mesh.CellIndices());
			
			for (int icell:cell_list) {
				
				if (flag_verbose) 
				{ System.out.println("Deleting cell " + String.valueOf(icell) + "."); }
				
				mesh.DeleteCell(icell);
				error_info = mesh.CheckAll();
				if (error_info.FlagError()) {
					System.err.println
						("Error detected in mesh data structure after deleting cell " +
						String.valueOf(icell) + ".");
					System.err.println(error_info.Message());
					System.err.println("  Exiting...");
					System.exit(-1);
				}
			}
			
			if (mesh.NumCells() != 0) {
				System.err.println
					("Error. All cells deleted but mesh.NumCells() = " +
					String.valueOf(mesh.NumCells()) + ".");
			}
			else {
				if (!flag_silent) {
					System.out.println
						("Mesh data structure passed all checks after all deletions.");
				}
			}
			
			long end_time = System.nanoTime();
			
			if (flag_time) {
				print_time("Time to read file:  ", (time2-begin_time));
				print_time("Time to check delete cells: ", (end_time-time2));
				print_time("Total time:         ", (end_time-begin_time));
			}
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	// *** SUBROUTINES ****
	
	protected static void parse_command_line(String[] argv)
	{
		int iarg = 0;
		
		while (iarg < argv.length &&
				argv[iarg].charAt(0) == '-') {
			String s= argv[iarg];
			if (s.equals("-s"))
			{ flag_silent = true; }
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
		
		if (iarg+1 != argv.length)
		{ usage_error(); }
		
		input_filename = argv[iarg];
	}
	
	static void print_time(String label, long time) {
		double nanoseconds_per_second = 1E9;
		String s = String.format("%.4f", (time/nanoseconds_per_second));
		System.out.println(label + s + " seconds.");
	}
	
	
	static void usage_msg(PrintStream out)
	{
		out.println("Usage: TestHMeshDeleteCell [-s] [-time] [-h] <input filename>");
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
		System.out.println("TestHMeshDeleteCell - Test the HalfEdgeMesh delete cell operations\n" +
				"  by reading a .off file to the mesh, repeatedly deleting cells,\n" +
				"  and running check mesh after each deletion.\n" +
				"  (Does not check manifold or orientation properties.)");
		System.out.println();
		System.out.println("Options:");
		System.out.println("-s:        Silent. Output only warnings and error messages.");
		System.out.println("-time:     Report run time.");
		System.out.println("-h:        Output this help message and exit.");
		System.exit(0);
	}
	
}

