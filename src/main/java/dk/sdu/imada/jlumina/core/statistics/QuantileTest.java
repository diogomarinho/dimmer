package dk.sdu.imada.jlumina.core.statistics;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;

public class QuantileTest {

	public static void main(String args[]) {

		int[][] data = new int[622399][12];

		try {
			CSVReader reader = new CSVReader(new FileReader(
					"/Users/diogo/Desktop/gree.data.csv"));

			try {
				reader.readNext();

				for (int i = 0; i < 622399; i++) {
					String[] line = reader.readNext();
					int index = 0;
					for (String s : line) {
						data[i][index++] = Integer.parseInt(s);
					}
				}

				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		
		//double[] rowMeans = QuantileUtil.getRowMeans(data);
		//double[][] normData  = QuantileUtil.getMeanReplaceMAtrix(data, rowMeans);
		
		//Util.writeDoubleMatrix(normData, "/Users/diogo/Desktop/green.normalized.csv");
		
		/*int[][] rank = QuantileUtil.getRankingMatrix(data);
		double[][] rankedValues = QuantileUtil.getRankedValues(data);
		double means[] = QuantileUtil.getMean(rankedValues);
		double[][] normalized = QuantileUtil.normalizeMatrix(rank, means);

		double[][] rank2 = QuantileUtil.getRankingMatrix2(data);*/

		System.out.println();
	}
}
