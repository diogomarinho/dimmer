package dk.sdu.imada.gui.plots;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractXYZDataset;
import org.jfree.data.xy.XYDataset;

@SuppressWarnings("serial")
public class XYLogData extends AbstractXYZDataset implements 
XYDataset, DomainInfo, RangeInfo {

	/**
	 * 
	 */
	double [][]xValues;
	double [][]yValues;

	int seriesCount;
	int itemCount;

	double minX = Double.MAX_VALUE;
	double maxX = Double.MIN_VALUE;
	double minY = Double.MAX_VALUE;
	double maxY = Double.MIN_VALUE;

	double maxLogValue;

	public XYLogData(double[] dataX, double[] dataY, double cutoff, double maxLogValue) {

		this.maxLogValue = maxLogValue;

		this.seriesCount = 3;
		this.itemCount = dataX.length;

		xValues = new double[seriesCount][itemCount];
		yValues = new double[seriesCount][itemCount];

		for (int i = 0; i < itemCount; i++) {

			double x = dataX[i];

			if (x < minX) {
				minX = x;
			}
			if (x > maxX) {
				maxX = x;
			}

			double y = dataY[i];
			if (y < minY) {
				minY = y;
			}
			if (y > maxY) {
				maxY = y;
			}

			if (dataY[i] <= cutoff) {
				
				xValues[0][i] = x;

				if (y == 0.0) {
					yValues[2][i] = maxLogValue;
				}else {
					yValues[0][i] = -Math.log10(y);
				}
				
			}else {
				
				xValues[1][i] = x;
				
				if (y == 0.0) {
					yValues[2][i] = maxLogValue;
				}else {
					yValues[1][i] = -Math.log10(y);
				}
			}
		}
	}

	// AbstractXYZDataset implementation
	public Number getZ(int series, int item) {
		return null;
	}

	public int getItemCount(int series) {
		return itemCount;
	}

	public Number getX(int series, int item) {
		return this.xValues[series][item];
	}

	public Number getY(int series, int item) {
		return this.yValues[series][item];
	}

	@Override
	public int getSeriesCount() {
		return seriesCount;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Comparable getSeriesKey(int series) {
		return "Sample " + series;
	}
	// ............

	// . Domain Implementation 
	public double getDomainLowerBound(boolean includeInterval) {
		return minX;
	}

	public double getDomainUpperBound(boolean includeInterval) {
		return maxX;
	}

	public Range getDomainBounds(boolean includeInterval) {
		return new Range(minX, maxX);
	}

	//....... Range Info implementation
	public double getRangeLowerBound(boolean includeInterval) {
		//return minY;
		return 0.0;
	}

	public double getRangeUpperBound(boolean includeInterval) {
		if (Double.isInfinite(-Math.log10(minY))) {
			return -Math.log10(maxLogValue);
		} else {
			return -Math.log10(minY);
		}
	}

	public Range getRangeBounds(boolean includeInterval) {
		return new Range(0.0, -Math.log10(minY));
		//return new Range(minY, maxY);
	}
}
