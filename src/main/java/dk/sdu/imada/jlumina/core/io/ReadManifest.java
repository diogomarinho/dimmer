package dk.sdu.imada.jlumina.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.util.CSVUtil;


/**
 * @author diogo
 * Read the manifest file (CpG information)
 *
 */
public class ReadManifest {

	String inputFile;
	InputStream inputStream;
	CpG [] cpgList;
	boolean done;
	int progress;
	
	public ReadManifest(String inputFile) {
		this.inputFile = inputFile;
	}
	
	public int getProgress() {
		return progress;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public synchronized void loadManifest() {
		
		progress = 0;
		done = false;
		
		try {
			//int nrows = CSVUtil.countRows(inputFile, 1);
			int nrows = CSVUtil.countRows(getClass().getClassLoader().getResourceAsStream(inputFile), 1);
			
			cpgList = new CpG[nrows];
			//CSVReader reader = new CSVReader(new FileReader(inputFile));
			CSVReader reader = new CSVReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(inputFile)));
			reader.readNext();

			for (int i = 0; i < nrows; i++) {
				String [] line = reader.readNext();

				String field0 = line[0];
				Integer field1, field2, field5, field7;

				try {
					field1 = Integer.parseInt(line[1]);
				}catch(NumberFormatException e) {
					field1 = -1;
				}

				try {
					field2 = Integer.parseInt(line[2]);
				}catch(NumberFormatException e) {
					field2 = -1;
				}

				String field3 = line[3];

				String field4 = line[4];

				try {
					field5 = Integer.parseInt(line[5]);
				}catch(NumberFormatException e) {
					field5 = -1;
				}

				String field6 = line[6];

				try {
					field7 = Integer.parseInt(line[7]);
				}catch(NumberFormatException e) {
					field7 = -1;
				}

				String field8 = line[8];

				String field9 = line[9];

				CpG cpg = new CpG( field0, field1, field2, field3, field4, field5, field6, field7, field8, field9);

				cpgList[i] = cpg;
			}
			reader.close();
		}catch(IOException e) {
			System.err.println("Can't read " + inputFile);
		}
		done = true;
		progress++;
		notify();
	}
	
	public synchronized void checkProgress() {
		while (!done) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public CpG getByAddressA(int addressA) {

		for (CpG cpg : cpgList) {
			if (addressA == cpg.getAddressA()) {
				return cpg;
			}
		}
		return null;
	}

	public CpG getByAddressB(int addressB) {
		for (CpG cpg : cpgList) {
			if (addressB == cpg.getAddressB()) {
				return cpg;
			}
		}
		return null;
	}

	public CpG getCpGByName(String cpgName) {
		for (CpG cpg : cpgList) {
			if (cpgName == cpg.getCpgName()) {
				return cpg;
			}
		}
		return null;		
	}

	public CpG[] getCpgList() {
		return cpgList;
	}

	public String[] getCpGsIDs() {
		String cpgIDs[] = new String[getCpgList().length];

		int index = 0;
		for (CpG cpg: getCpgList()) {
			cpgIDs[index] = cpg.getCpgName();
			index++;
		}
		return cpgIDs;
	}

	public String[] getCpGsIDsByChromosome(String chr) {
		ArrayList<String> cpgByChrArray = new ArrayList<String>();

		for (CpG cpg: getCpgList()) {
			if (cpg.getChromosome().equals(chr)) {
				cpgByChrArray.add(cpg.getCpgName());
			}
		}

		String cpgIDs[] = new String[cpgByChrArray.size()];
		int i = 0;
		for (String s : cpgByChrArray) {
			cpgIDs[i++] = s;
		}
		return cpgIDs;
	}

	public CpG[] getCpGsByChromosome(String chr) {
		ArrayList<CpG> cpgByChrArray = new ArrayList<CpG>();

		for (CpG cpg: getCpgList()) {
			if (cpg.getChromosome().equals(chr)) {
				cpgByChrArray.add(cpg);
			}
		}

		CpG cpgIDs[] = new CpG[cpgByChrArray.size()];
		int i = 0;
		for (CpG s : cpgByChrArray) {
			cpgIDs[i++] = s;
		}
		return cpgIDs;
	}
}
