import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class PalindromeSample {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(new File("words.dat")));
			String line = "";
			while ((line = br.readLine()) != null) {
				System.out.format("%s: [%b]\n", line, isPalindrome(line));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static boolean isPalindrome(String word) {
		word = word.replace(" ", "");
		word = word.replaceAll("[^\\s\\w]*", "");
		//System.out.println(word);
		StringBuffer sbWord = new StringBuffer();
		sbWord.append(word);
		sbWord.reverse();
		
		return word.equalsIgnoreCase(sbWord.toString());
	}
}
