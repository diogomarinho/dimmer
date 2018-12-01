package dk.sdu.imada.gui.controllers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.gui.monitors.InputFilesMonitor;
import dk.sdu.imada.jlumina.core.io.Read450KSheet;
import dk.sdu.imada.jlumina.core.io.ReadIDAT;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.MSet;
import dk.sdu.imada.jlumina.core.primitives.RGSet;
import dk.sdu.imada.jlumina.core.primitives.USet;
import dk.sdu.imada.jlumina.core.statistics.CellCompositionCorrection;
import dk.sdu.imada.jlumina.core.statistics.Normalization;
import dk.sdu.imada.jlumina.core.statistics.QuantileNormalization;
import dk.sdu.imada.jlumina.core.util.DataExecutor;
import dk.sdu.imada.jlumina.core.util.RawDataLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class InputController {

	@FXML TextField labels;
	@FXML TextField output;
	@FXML TextField numThreads;

	@FXML Button b3;
	@FXML Button b4;
	@FXML Button toRight;
	@FXML Button toLeft;

	@FXML Label label1;
	@FXML Label label2;
	@FXML Label label3;

	@FXML Label labelHeader;

	public Label getLabelHeader() {
		return labelHeader;
	}

	public void setLabelHeader(String text) {
		this.labelHeader.setText(text);
	}

	FileChooser fileChooser = new FileChooser();
	DirectoryChooser directoryChooser = new DirectoryChooser();
	String [] extention = {"csv"};

	@FXML ListView<String> source;
	@FXML ListView<String> target;
	ArrayList<String> labelsList;
	ArrayList<String> slectedLabels;
	String coefficient;

	@FXML CheckBox cellComposition;
	@FXML Pane paneCC;

	private CSVReader reader;
	MainController mainController;

	boolean conditionOutput;

	@FXML CheckBox cd8t;
	@FXML CheckBox cd4t;
	@FXML CheckBox nk;
	@FXML CheckBox ncell;
	@FXML CheckBox mono;
	@FXML CheckBox gran;

	HashMap<String, String[]> columnMap;
	HashMap<Integer, String[]> rowMap;

	boolean hasGroupID = false;
	boolean hasPairID = false;
	boolean hasGenderID = false;


	public boolean hasGroupID() {
		return hasGroupID;
	}

	public boolean hasPairID() {
		return hasPairID;
	}

	public boolean hasGenderID() {
		return hasGenderID;
	}

	public HashMap<String, String[]> getColumnMap() {
		return columnMap;
	}

	public HashMap<Integer, String[]> getRowMap() {
		return rowMap;
	}

	public CheckBox getCd8t() {
		return cd8t;
	}

	public CheckBox getNcell() {
		return ncell;
	}

	public CheckBox getMono() {
		return mono;
	}

	public CheckBox getGran() {
		return gran;
	}

	public CheckBox getCd4t() {
		return cd4t;
	}

	public CheckBox getNk() {
		return nk;
	}

	public int getNumThreads() {

		int v = 1;

		try {

			v = Integer.parseInt(numThreads.getText());

			if (v<=0) {
				System.out.println("No speed up: using one core");
				return 1;
			}else {
				System.out.println("Speed up: using " + v  + " cores");
				return v;
			}

		}catch (NumberFormatException e) {
			System.out.println("No speed up: using one core");
			return 1;
		}
	}

	@FXML public void estimateCellCompositionSelect(ActionEvent actionEvent) {

		if (cellComposition.isSelected()) {
			paneCC.setVisible(true);
		}else {
			paneCC.setVisible(false);
		}
	}

	@FXML public void addLabel(ActionEvent actionEvent) {

		String sourceSelection = source.getSelectionModel().getSelectedItem();

		if (sourceSelection!=null) {

			if(checkNumeric(columnMap.get(sourceSelection))) {

				target.getItems().add(sourceSelection);

				source.getItems().remove(sourceSelection);
			}else {
				FXPopOutMsg.showWarning("The values you want to use for your regression have to be numerical");
			}
		}
	}

	private boolean checkMandatoryColumns(String path) throws IOException {

		int count = 0;

		reader = new CSVReader(new FileReader(path));

		for (String cols : reader.readNext()) {
			
			if (cols.equals("Sentrix_ID") || cols.equals("Sentrix_Position")) count++;

			if (cols.equals("Group_ID")){
				hasGroupID = true;
			}else {
				hasGroupID = false;
			}

			if (cols.equals("Pair_ID")) {
				hasPairID = true;
			}else {
				hasPairID = false;
			}

			if (cols.equals("Gender_ID")) {
				hasGenderID = true;
			}else {
				hasGenderID = false;
			}
		}
		return count==2;
	}

	public String getCoefficient() {
		return coefficient;
	}

	public ArrayList<String> getLabelsList() {
		return labelsList;
	}

	public String getLabelsPath() {
		return labels.getText();
	}

	public String getOutputPath() {

		String text = output.getText();
		if(text.isEmpty()) {
			return ("./");
		}else { 
			return text + "/";
		}
	}

	public ArrayList<String> getSelectedLabels() {
		return slectedLabels;
	}

	@FXML public void help(ActionEvent actionEvent) {
		String msg = "testing ";
		FXPopOutMsg.showHelp(msg);
	}

	@FXML public void openLabels(ActionEvent actionEvent) {

		fileChooser.setTitle("Open your sample annotation file");
		File f = fileChooser.showOpenDialog(null);

		if (f!=null) {
			labels.setText(f.getAbsolutePath());
			try {
				if (checkMandatoryColumns(f.getAbsolutePath())) {
					setMaps(f.getAbsolutePath());
					setLabelsList(f.getAbsolutePath());
				}
			} catch (IOException e) {
				FXPopOutMsg.showWarning("Problems were found in loading " + f.getAbsolutePath());
			}
		}
	}

	private void setMaps(String path) {

		columnMap = new HashMap<String, String[]>();
		rowMap = new HashMap<Integer, String[]>();

		int i = 0;
		try {
			reader = new CSVReader(new FileReader(path));
			String nextLine[] = null;

			while ((nextLine = reader.readNext()) != null ) {
				rowMap.put(i++, nextLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		String keys[] = rowMap.get(0);

		for (int j = 0; j < keys.length; j++) {
			String []column = new String[rowMap.size() - 1];
			for (i = 1; i < rowMap.size(); i++) {
				column[i-1] = rowMap.get(i)[j];
			}
			columnMap.put(keys[j], column);
		}
		System.out.println();
	}

	@FXML public void pushBack(ActionEvent actionEvent) {
		this.mainController.loadScreen("model");
	}

	@FXML public void removeLabel(ActionEvent actionEvent) {

		String targetSelection = target.getSelectionModel().getSelectedItem();

		if (targetSelection!=null) {
			source.getItems().add(targetSelection);
			target.getItems().remove(targetSelection);
		}
	}

	public void resetTextFields() {
		labels.setText(null);
		output.setText(null);
		cellComposition.setSelected(false);
		cd8t.setSelected(false);
		cd4t.setSelected(false);
		nk.setSelected(false);
		ncell.setSelected(false);
		mono.setSelected(false);
		gran.setSelected(false);
	}

	@FXML public void selectOutputFolder(ActionEvent actionEvent) {

		directoryChooser.setTitle("Select destination folder");

		File f = directoryChooser.showDialog(null);

		if (f!=null) {

			if (Files.isWritable(FileSystems.getDefault().getPath(f.getAbsolutePath()))){
				output.setText(f.getAbsolutePath());

			} else {
				FXPopOutMsg.showWarning("Directory not writable");
				output.setText("");
			}

		}else {
			output.setText("");
		}
		conditionOutput = true;
	}

	public void setCanvasController(MainController canvasController) {
		this.mainController = canvasController;
	}

	public boolean setLabelsList(String path) throws IOException {

		labelsList = new ArrayList<String>();

		this.target.getItems().removeAll(target.getItems());
		CSVReader reader = new CSVReader(new FileReader(path));

		String [] str = reader.readNext();

		// Group ID split the file....
		// ParID mapp the pairs ...
		for (String s : str) {
			if (!s.equals("Sentrix_ID") && !s.equals("Sentrix_Position") 
					&& !s.equals("Group_ID")  && !s.equals("Pair_ID") ) {
				labelsList.add(s);
			}
		}

		reader.close();

		if (mainController.modelController.isRegression()) {

			label1.setText("Available labels");
			label2.setText("Available variables");
			label3.setText("Select which labels should be considered "
					+ "for the regression model by using the arrows to "
					+ "insert the corresponding variables to the right box. "
					+ "Then, choose the one variable which you want to be explained "
					+ "by the differential methylation from the right box by clicking "
					+ "on it (all other variables of the right box will be treated as co-factors). ");
			ObservableList<String> observableList = FXCollections.observableArrayList(labelsList);
			setRegressionLabelsElementsVisible(true);
			this.source.setItems(observableList);

		}else {
			label1.setText("Available variables");
			label3.setText("");
			ObservableList<String> observableList = FXCollections.observableArrayList(labelsList);
			this.source.setItems(observableList);
			setTTestLabelsElementsVisible(true);
		}

		return true;
	}

	public void setRegressionLabelsElementsVisible(boolean v) {
		label1.setVisible(v);
		label2.setVisible(v);
		label3.setVisible(v);
		source.setVisible(v);
		target.setVisible(v);
		toRight.setVisible(v);
		toLeft.setVisible(v);
	}

	public void setSelectedCoefficients() {
		if (mainController.modelController.isRegression()) {
			coefficient = target.getSelectionModel().getSelectedItem();
		}else {
			coefficient = source.getSelectionModel().getSelectedItem();
		}
	}

	public void setSelectedLabels() {

		slectedLabels = new ArrayList<String>();

		if (mainController.modelController.isRegression()) {
			for (String s : target.getItems()) {
				slectedLabels.add(s);
			}
		}else {
			for (String s : source.getItems()) {
				slectedLabels.add(s);
			}
		}
	}

	public void setTTestLabelsElementsVisible(boolean v) {
		label1.setVisible(v);
		label2.setVisible(false);
		source.setVisible(v);
		label3.setVisible(v);
		target.setVisible(false);
		toRight.setVisible(false);
		toLeft.setVisible(false);
	}

	public CheckBox getCellComposition() {
		return cellComposition;
	}

	private boolean checkNumeric(String vec[]) {
		try {
			for (String s : vec) {
				Double.parseDouble(s);
			}
		}catch(NumberFormatException e) {
			return false;
		}
		return true;
	}

	private boolean checkNumeric() {

		boolean cond = true;

		for (String s : target.getItems()) {
			cond = checkNumeric(columnMap.get(s));
		}

		return cond;
	}

	@FXML public void pushContinue(ActionEvent actionEvent) {

		if (labels.getText().isEmpty()) {

			FXPopOutMsg.showWarning("No file to load... ");

		}else {

			setSelectedLabels();

			if (mainController.modelController.isRegression()) {

				if (getSelectedLabels().size() > 0) {

					setSelectedCoefficients();

					if (getCoefficient()!= null) {

						if (checkNumeric()) {

							startPreprocessing();

						}else {
							FXPopOutMsg.showWarning("Your selected coefficients must have numerical values only");
						}
						//mainController.loadScreen("summary");
					}else {
						FXPopOutMsg.showWarning("Select one coefficient of interest");
					}
				}else {
					FXPopOutMsg.showWarning("Please which labels you want to estimate in the linear regression");
				}
			}else {

				setSelectedCoefficients();

				if (getCoefficient()!= null) {

					if (checkNumeric(columnMap.get(getCoefficient()))) {
						startPreprocessing();
					}else {
						FXPopOutMsg.showWarning("Your selected variable of interest must have numerical values only");
					}
					//mainController.loadScreen("summary");
				}else {
					FXPopOutMsg.showWarning("Select your variable of interest");
				}
			}
		}
	}

	public char[] getGenderList() {
		if (hasGenderID) {
			//return columnMap.get("Gender_ID")
			char ids[] = new char [columnMap.get("Gender_ID").length];
			int i = 0;
			for (String s : columnMap.get("Gender_ID")) {
				if (s.equals("1")) {
					ids[i++] = 'F';
				}else {
					ids[i++] = 'M';
				}
			}
			return ids;
		}else {
			return null;
		}
	}

	// .............................................................................................................
	ReadManifest manifest;
	RGSet rgSet;
	MSet mSet, mRefSet;
	USet uSet, uRefSet;
	Normalization normalizations;
	CellCompositionCorrection cellCompositionCorrection;
	RawDataLoader rawDataLoader;
	InputFilesMonitor inputFilesMonitor;
	int maxCoreSteps;
	int stepsDone;

	private void startPreprocessing() {

		stepsDone = 0;

		testDataType();
		initializeJLuminaCore();

		ProgressForm pf = new ProgressForm();

		rawDataLoader = new RawDataLoader(rgSet, manifest, uSet, mSet,
				cellCompositionCorrection, uRefSet, mRefSet, normalizations, getNumThreads(), getGenderList());
		
		rawDataLoader.setMaxSteps(maxCoreSteps);

		DataExecutor dataExecutor = new DataExecutor(rawDataLoader);
		inputFilesMonitor = new InputFilesMonitor(rawDataLoader, mainController, pf);

		Thread loaderThread = new Thread(dataExecutor);
		Thread progressThread = new Thread(inputFilesMonitor);
		ArrayList<Thread> arrayList = new ArrayList<>();

		arrayList.add(loaderThread);
		arrayList.add(progressThread);
		pf.setThreads(arrayList);

		Platform.runLater(pf);
		progressThread.start();
		loaderThread.start();
	}

	private void testDataType() {
		String idatFilePath = new Read450KSheet(getLabelsPath()).getBaseName()[0] + "_Grn.idat";

		ReadIDAT gIdat = new ReadIDAT();
		gIdat.readNonEncryptedIDAT(idatFilePath);
		int v = gIdat.getnSNPsRead();

		if (v == 622399) {
			mainController.setInfinium(true);
			mainController.setEpic(false);
		}else {
			mainController.setInfinium(false);
			mainController.setEpic(true);
		}
	}

	private void initializeJLuminaCore() {

		maxCoreSteps = 4;

		this.rgSet = new RGSet(getLabelsPath());

		String mf = null;
		if (mainController.isInfinium()) { 

			System.out.println("Using infinium data type");
			mf = "resources/manifest_summary.csv";

			if (getClass().getClassLoader().getResourceAsStream(mf)==null) {
				mf = "manifest_summary.csv";
			}

		}else {
			System.out.println("Using epic data type");
			mf = "resources/epic_manifest.csv";

			if (getClass().getClassLoader().getResourceAsStream(mf)==null) {
				mf = "epic_manifest.csv";
			}
		}

		this.manifest = new ReadManifest(mf);
		this.uSet = new USet();
		this.uRefSet = new USet();
		this.mSet = new MSet();
		this.mRefSet = new MSet();

		normalizations = new QuantileNormalization(); 

		if (mainController.isInfinium()) {
			if (mainController.useCellComposition()) {
				cellCompositionCorrection = new CellCompositionCorrection();
				maxCoreSteps = 9;
			}else {
				cellCompositionCorrection = null;
			}
		}else {
			System.out.println("Cell composition estimation is not avaliable for epic data");
			cellCompositionCorrection = null;	
		}
	}
}
