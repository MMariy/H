import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws IOException {
		
		long startTime = System.currentTimeMillis();
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter Input File Path: ");
		String file1 = scan.nextLine();
		System.out.println("Enter Empty Output File Path: ");
		String file2 = scan.nextLine();
		  File in  = new File(file1);
		  File out  = new File(file2);
		  ExternalSort s = new ExternalSort();
		s.sort(in,out);
		System.out.println("Sorted");
		
		 long endTime = System.currentTimeMillis();
         long wholeTime = (endTime - startTime) / 1000;
         System.out.println("Whole time: " + wholeTime / 60 + " min " +  wholeTime % 60 + " sec");
		
	}

}
