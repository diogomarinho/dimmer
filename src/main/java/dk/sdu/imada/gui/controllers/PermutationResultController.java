package dk.sdu.imada.gui.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import dk.sdu.imada.gui.controllers.util.DMRParametersUtil;
import dk.sdu.imada.gui.plots.VolcanoPlot;
import dk.sdu.imada.gui.plots.XYLogData;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class PermutationResultController {

	@FXML CheckBox useLog10;
	@FXML CheckBox useLog10glob;
	@FXML CheckBox useLog10sdm;
	@FXML CheckBox useLog10fdr;
	@FXML CheckBox useLogOrig;


	@FXML public ImageView empiricalHist;
	@FXML public ImageView empiricalScatter;
	@FXML public ImageView empiricalVulcano;

	@FXML public ImageView fwerHist;
	@FXML public ImageView fwerScatter;
	@FXML public ImageView fwerVulcano;

	@FXML public ImageView fdrHist;
	@FXML public ImageView fdrScatter;
	@FXML public ImageView fdrVulcano;

	@FXML public ImageView sdmHist;
	@FXML public ImageView sdmScatter;
	@FXML public ImageView sdmVulcano;

	@FXML public ImageView maxFdrHist;
	@FXML public ImageView maxFdrScatter;
	@FXML public ImageView maxFdrVulcano;

	@FXML public ImageView origHist;
	@FXML public ImageView origVulcano;

	@FXML public VBox scrollVboxOrig;
	@FXML public VBox scrollVboxFwer;
	@FXML public VBox scrollVboxFdr;
	@FXML public VBox scrollVboxEmp;
	@FXML public VBox scrollVboxMax;
	@FXML public VBox scrollVboxSdc;

	@FXML public Pane paneOrig;
	@FXML public Pane paneFwer;
	@FXML public Pane paneFdr;
	@FXML public Pane paneEmp;
	@FXML public Pane peaneMax;
	@FXML public Pane peaneSdc;

	@FXML public TextField vulcanoOrigField;
	@FXML public TextField vulcanoEmpField;
	@FXML public TextField vulcanoFdrField;
	@FXML public TextField vulcanoFwerField;
	@FXML public TextField vulcanoMinPField;


	@FXML public ScrollPane scrollPane;

	MainController mainController;

	@FXML public void pushContinue(ActionEvent actionEvent) {
		DMRParametersUtil dmrParametersUtil = new DMRParametersUtil(mainController);
		dmrParametersUtil.setScreen();
		mainController.loadScreen("dmrParameters");
	}

	@FXML public void pushBack(ActionEvent actionEvent) {
		mainController.loadScreen("summary");
	}

	// empirical pvalues ........................................................
	@FXML public void showEmpiricalScatterPlot(ActionEvent actionEvent) {

		try {
			JFreeChart chart;
			if (useLog10.isSelected()) {
				chart = mainController.getEmpiricalPvaluesScatterPlotLogChart();
			}else {
				chart = mainController.getEmpiricalPvaluesScatterPlotChart();
			}

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {

					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});

		}catch (NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showEmpiricalHistogram(ActionEvent actionEvent) {
		try {
			JFreeChart chart = mainController.getEmpiricalPvaluesDistributionChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showEmpiricalVulcanoPlot(ActionEvent actionEvent) {
		double treshold = Double.parseDouble(vulcanoEmpField.getText());
		mainController.setEmpiricalPvaluesVulcanoPlotChart(vulcanoPlot(mainController.getEmpiricalPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Emp. p-values", "Methylation difference", "Emp. p-values (-log10)", treshold));
		try {
			JFreeChart chart = mainController.getEmpiricalPvaluesVulcanoPlotChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void saveEmpiricalHistogram(ActionEvent e ) {
		JFreeChart chart1 = mainController.getEmpiricalPvaluesDistributionChart();
		BufferedImage img = chart1.createBufferedImage(1200, 800);
		exportChart(img);
	}

	@FXML public void saveEmpiricalScatterPlot(ActionEvent e) {
		JFreeChart chart;
		if (useLog10.isSelected()) {
			chart = mainController.getEmpiricalPvaluesScatterPlotLogChart();
		}else {
			chart = mainController.getEmpiricalPvaluesScatterPlotChart();
		}
		BufferedImage img = chart.createBufferedImage(800, 800);
		exportChart(img);
	}

	@FXML public void saveEmpiricalVulcanoPlot(ActionEvent e) {
		double treshold = Double.parseDouble(vulcanoEmpField.getText());
		mainController.setEmpiricalPvaluesVulcanoPlotChart(vulcanoPlot(mainController.getEmpiricalPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Emp. p-values", "Methylation difference", "Emp. p-values (-log10)", treshold));
		
		
		JFreeChart chart = mainController.getEmpiricalPvaluesVulcanoPlotChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}


	@FXML public void saveAll(ActionEvent e) {

		exportChart(mainController.getEmpiricalPvaluesDistributionChart().createBufferedImage(1800, 1200), "emp_histogram.png");
		exportChart(mainController.getEmpiricalPvaluesScatterPlotLogChart().createBufferedImage(1800, 1200), "emp_scatter_plot_log.png");
		exportChart(mainController.getEmpiricalPvaluesScatterPlotChart().createBufferedImage(1800, 1200), "emp_scatter_plot.png");

		if (!mainController.isRegression()) {
			exportChart(mainController.getEmpiricalPvaluesVulcanoPlotChart().createBufferedImage(1800, 1200), "emp_vulcano_plot.png");
			exportChart(mainController.getFdrVulcanoPlotChart().createBufferedImage(1800, 1200), "fdr_vulcano_plot.png");
			exportChart(mainController.getFwerVulcanoPlotChart().createBufferedImage(1800, 1200), "fwer_vulcano_plot.png");
			exportChart(mainController.getStepDownVulcanoPlotChart().createBufferedImage(1800, 1200), "sdc_vulcano_plot.png");
		}

		exportChart(mainController.getFdrDistributionChart().createBufferedImage(1800, 1200), "max_fdr_hist.png");
		exportChart(mainController.getFdrLogScatterPlotChart().createBufferedImage(1800, 1200), "max_fdr_scatter_plot_log.png");
		exportChart(mainController.getFdrScatterPlotChart().createBufferedImage(1800, 1200), "max_fdr_scatter_plot.png");


		exportChart(mainController.getFwerDistributionChart().createBufferedImage(1800, 1200), "fwer_hist.png");
		exportChart(mainController.getFwerScatterPlotChart().createBufferedImage(1800, 1200), "fwer_scaltter_plot.png");
		exportChart(mainController.getFwerScatterPlotLogChart().createBufferedImage(1800, 1200), "fwer_scaltter_plot_log.png");

		exportChart(mainController.getSteDownDistributionChart().createBufferedImage(1800, 1200), "sdc_hist.png");
		exportChart(mainController.getStepDownScatterPlotChart().createBufferedImage(1800, 1200), "sdc_scaltter_plot.png");
		exportChart(mainController.getStepDownLogScatterPlotChart().createBufferedImage(1800, 1200), "sdc_scaltter_plot_log.png");

		FXPopOutMsg.showMsg("All plots were saved at " + mainController.getOutputDirectory());
	}

	// fwer pvalues ........................................................
	@FXML public void showFwerScatterPlot(ActionEvent actionEvent) {

		try {
			JFreeChart chart;
			if (useLog10glob.isSelected()) {
				chart = mainController.getFwerScatterPlotLogChart();
			}else {
				chart = mainController.getFwerScatterPlotChart();
			}

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {

					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});

		}catch (NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showFwerHistogramPlot(ActionEvent actionEvent) {
		try {
			JFreeChart chart = mainController.getFwerDistributionChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showFwerVulcanoPlot(ActionEvent actionEvent) {
		double treshold = Double.parseDouble(vulcanoFwerField.getText());
		mainController.setFwerVulcanoPlotChart(vulcanoPlot(mainController.getFwerPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. FWER p-values", "Methylation difference", "FWER p-values (-log10)", treshold));
		try {
			JFreeChart chart = mainController.getFwerVulcanoPlotChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void saveFwerHistogram(ActionEvent e ) {
		JFreeChart chart1 = mainController.getFwerDistributionChart();
		BufferedImage img = chart1.createBufferedImage(1200, 800);
		exportChart(img);
	}

	@FXML public void saveFwerScatterPlot(ActionEvent e) {
		JFreeChart chart;
		if (useLog10glob.isSelected()) {
			chart = mainController.getFwerScatterPlotLogChart();
		}else {
			chart = mainController.getFwerScatterPlotChart();
		}
		BufferedImage img = chart.createBufferedImage(800, 800);
		exportChart(img);
	}

	@FXML public void saveFwerVulcanoPlot(ActionEvent e) {

		double treshold = Double.parseDouble(vulcanoFwerField.getText());
		mainController.setFwerVulcanoPlotChart(vulcanoPlot(mainController.getFwerPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. FWER p-values", "Methylation difference", "FWER", treshold));

		JFreeChart chart = mainController.getFwerVulcanoPlotChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}

	// FDR pvalues ........................................................
	@FXML public void showSdcScatterPlot(ActionEvent actionEvent) {

		try {
			JFreeChart chart;
			if (useLog10sdm.isSelected()) {
				chart = mainController.getStepDownLogScatterPlotChart();
			}else {
				chart = mainController.getStepDownScatterPlotChart();
			}

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {

					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});

		}catch (NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showSdcHistogramPlot(ActionEvent actionEvent) {
		try {
			JFreeChart chart = mainController.getSteDownDistributionChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showSdcVulcanoPlot(ActionEvent actionEvent) {
		double treshold = Double.parseDouble(vulcanoMinPField.getText());
		mainController.setStepDownVulcanoPlotChart(vulcanoPlot(mainController.getStepDownMinPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Step-down minP. p-values", "Methylation difference", "Step-down minP (-log10)", treshold));
		try {
			JFreeChart chart = mainController.getStepDownVulcanoPlotChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void saveSdcHistogram(ActionEvent e ) {
		JFreeChart chart1 = mainController.getSteDownDistributionChart();
		BufferedImage img = chart1.createBufferedImage(1200, 800);
		exportChart(img);
	}

	@FXML public void saveSdcScatterPlot(ActionEvent e) {
		JFreeChart chart;
		if (useLog10sdm.isSelected()) {
			chart = mainController.getStepDownLogScatterPlotChart();
		}else {
			chart = mainController.getStepDownScatterPlotChart();
		}
		BufferedImage img = chart.createBufferedImage(800, 800);
		exportChart(img);
	}

	@FXML public void saveSdcVulcanoPlot(ActionEvent e) {
		double treshold = Double.parseDouble(vulcanoMinPField.getText());
		mainController.setStepDownVulcanoPlotChart(vulcanoPlot(mainController.getStepDownMinPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Step-down minP. p-values", "Methylation difference", "Step-down minP (-log10)", treshold));
		JFreeChart chart = mainController.getStepDownVulcanoPlotChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}

	//FDR pvalues ........................................................
	@FXML public void showFdrScatterPlot(ActionEvent actionEvent) {

		try {
			JFreeChart chart;
			if (useLog10fdr.isSelected()) {
				chart = mainController.getFdrLogScatterPlotChart();
			}else {
				chart = mainController.getFdrScatterPlotChart();
			}

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {

					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});

		}catch (NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showFdrHistogramPlot(ActionEvent actionEvent) {
		try {
			JFreeChart chart = mainController.getFdrDistributionChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showFdrVulcanoPlot(ActionEvent actionEvent) {

		double treshold = Double.parseDouble(vulcanoFdrField.getText());
		mainController.setFdrVulcanoPlotChart(vulcanoPlot(mainController.getFdrPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. FDR p-values", "Methylation difference", "FDR p-values (-log10)", treshold));

		try {
			JFreeChart chart = mainController.getFdrVulcanoPlotChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void saveFdrHistogram(ActionEvent e ) {

		JFreeChart chart1 = mainController.getFdrDistributionChart();
		BufferedImage img = chart1.createBufferedImage(1200, 800);
		exportChart(img);
	}

	@FXML public void saveFdrScatterPlot(ActionEvent e) {
		JFreeChart chart;
		if (useLog10fdr.isSelected()) {
			chart = mainController.getFdrLogScatterPlotChart();
		}else {
			chart = mainController.getFdrScatterPlotChart();
		}
		BufferedImage img = chart.createBufferedImage(800, 800);
		exportChart(img);
	}

	@FXML public void saveFdrVulcanoPlot(ActionEvent e) {
		double treshold = Double.parseDouble(vulcanoFdrField.getText());
		mainController.setFdrVulcanoPlotChart(vulcanoPlot(mainController.getFdrPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. FDR p-values", "Methylation difference", "FDR p-values (-log10)", treshold));
		JFreeChart chart = mainController.getFdrVulcanoPlotChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}	

	// Orig FDR pvalues ........................................................
	@FXML public void showOrigHistogramPlot(ActionEvent actionEvent) {
		try {
			JFreeChart chart = mainController.getOrigDistributionChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}


	@FXML public void showOrigVulcanoPlot(ActionEvent actionEvent) {

		double treshold = Double.parseDouble(vulcanoOrigField.getText());

		mainController.setOrigVulcanoPlotChart(vulcanoPlot(mainController.getOriginalPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Orig. p-values", "Methylation difference", "Orig. p-values (-log10)", treshold));

		try {
			JFreeChart chart = mainController.getOrigVulcanoPlotChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void saveOrigHistogram(ActionEvent e ) {
		JFreeChart chart1 = mainController.getOrigDistributionChart();
		BufferedImage img = chart1.createBufferedImage(1200, 800);
		exportChart(img);
	}

	@FXML public void saveOrigVulcanoPlot(ActionEvent e) {
		double treshold = Double.parseDouble(vulcanoOrigField.getText());
		mainController.setOrigVulcanoPlotChart(vulcanoPlot(mainController.getOriginalPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Orig. p-values", "Methylation difference", "Orig. p-values (-log10)", treshold));
		JFreeChart chart = mainController.getOrigVulcanoPlotChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}

	public void setCanvasController(MainController canvasController) {
		this.mainController = canvasController;
	}

	private void exportChart(BufferedImage bufferedImage) {
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File((mainController.inputController.getOutputPath())));

			File outPutFile = fileChooser.showSaveDialog(null);

			if (outPutFile!=null) {
				ImageIO.write(bufferedImage, "png", outPutFile);
			}

		}catch(IOException e) {
			FXPopOutMsg.showWarning("Can't save file");
		}
	}

	private void exportChart(BufferedImage bufferedImage, String name) {
		try {
			File outPutFile = new File(mainController.inputController.getOutputPath() + name);
			if (outPutFile!=null) {
				ImageIO.write(bufferedImage, "png", outPutFile);
			}
		}catch(IOException e) {
			FXPopOutMsg.showWarning("Can't save file");
		}
	}

	private JFreeChart vulcanoPlot(float correctedPvalues[], float meanDiff[], String title, String xlab, String ylab, double treshold) {

		XYLogData xyLogData = new XYLogData(MatrixUtil.toDouble(meanDiff), 
				MatrixUtil.toDouble(correctedPvalues), treshold, 
				-Math.log10(1.0/(mainController.getNumPermutations() * 10.0)));

		VolcanoPlot vp = new VolcanoPlot(title, xyLogData, xlab, ylab);
		JFreeChart chart = vp.getChart();
		return chart;
	}
}
