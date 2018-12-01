package dk.sdu.imada.jlumina.core.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReadIdatTest {
	public static void main(String args[]) {
		ReadIDAT idat = new ReadIDAT();
		idat.readNonEncryptedIDAT("/Users/diogo/Desktop/idat/5815381013_R03C01_Grn.idat");

		try {
			File file = new File("/Users/diogo/Desktop/means.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (int v : idat.getMean()) {
				bw.write(v + "\n");
			}
			bw.close();
		}catch(IOException e ){

		}
		
		System.out.println(Short.MAX_VALUE);
	}
	
}
