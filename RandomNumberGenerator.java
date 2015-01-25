
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

/** Generate 10 random integers in the range 0..99. */
public final class RandomNumberGenerator {

	public static final void main(String... aArgs) throws IOException {
		System.out.println("Enter Full .txt file path where random numbe are generated for eg. [/Desktop/testfile.txt] ");
		Scanner sc = new Scanner(System.in);
		String outputFile = sc.next();
		File file = new File(outputFile);

		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		PrintWriter writer = new PrintWriter(file, "UTF-8");
		Random randomGenerator = new Random();
		for (int idx = 1; idx <= 1000000; ++idx) {
			int randomInt = randomGenerator.nextInt(1000000);
			writer.println(randomInt);

		}
		writer.close();
		System.out.println("Done");
	}

}
