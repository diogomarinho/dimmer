package dk.sdu.imada.jlumina.core.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.Control;
import dk.sdu.imada.jlumina.core.primitives.MSet;
import dk.sdu.imada.jlumina.core.primitives.RGSet;
import dk.sdu.imada.jlumina.core.primitives.USet;


public class IlluminaNormalization implements Normalization{
	HashMap<String, double[]> betaValues;
	RGSet rgSet;
	MSet mSet;
	USet uSet;
	ReadManifest manifest;
	HashMap<String, double[]> rawBetaValues;

	public IlluminaNormalization(RGSet rgset, ReadManifest manifest){
		this.rgSet = rgset;
		this.manifest = manifest;
	}


	public HashMap<String, double[]> performNormalization() {
		RGSet newRGSet = bgcorrectionIllumina(manifest, rgSet);
		float test [] = newRGSet.getRedSet().get(10600313);
		normalizeIlluminaControl(newRGSet, manifest, 1);
		//DRGSet bRGSet = normalizeIlluminaControl(newRGSet,manifest,reference);
		System.out.println("der");
		//double[] test = bRGSet.getRedSet().get(10600313);
		System.out.println("værdi ");
		for(int i=0;i<test.length;i++){
			System.out.print(test[i]+" ");
		}

		MSet mSet = new MSet();
		mSet.setRgSet(newRGSet);
		mSet.setManifest(manifest);
		mSet.loadData();

		USet uSet = new USet();
		uSet.setRgSet(newRGSet);
		uSet.setManifest(manifest);
		uSet.loadData();

		/*DMSet mSet = new DMSet(bRGSet,manifest);
		System.out.println(mSet.getMethylatedMap().size());
		DUSet uSet = new DUSet(bRGSet,manifest);
		System.out.println("done");
		System.out.println(uSet.getUnmethylatedMap().size());*/
		return null;

	}

	public RGSet normalizeIlluminaControl(RGSet rgset, ReadManifest manifest, int reference){
		int[] AT_control = getControlAddress("NORM_A, NORM_T", manifest,0);
		int[] CG_control = getControlAddress("NORM_C, NORM_G", manifest,0);
		int sampleNo = getSampleNo(AT_control,rgset);
		float[] meanGreen = getMeanValuePatient(rgset.getGreenSet(),CG_control, sampleNo);
		float[] meanRed = getMeanValuePatient(rgset.getRedSet(),AT_control, sampleNo);
		float ref = (meanGreen[reference-1]+meanRed[reference-1])/2;
		float[] factorGreen = getFactor(ref, meanGreen);
		float[] factorRed = getFactor(ref, meanRed);
		HashMap<Integer,float[]> green = new HashMap<Integer,float[]>();
		HashMap<Integer,float[]> red = new HashMap<Integer,float[]>();
		green = calculateNormalValues(green,rgset.getGreenSet(),factorGreen);
		red = calculateNormalValues(red,rgset.getRedSet(),factorRed);
		//RGSet normalSet = new RGSet(green, red, rgset.getSampleIDs());
		RGSet normalSet = new RGSet();
		normalSet.setGreenSet(green); 
		normalSet.setRedSet(red);

		return normalSet;
	}

	public RGSet bgcorrectionIllumina(ReadManifest manifest, RGSet rgset){
		int[] negative = getControlAddress("NEGATIVE", manifest,0);
		int sampleNo = getSampleNo(negative,rgset);
		float[] bgGreen = sortAndGet31Element(rgset.getGreenSet(),negative,sampleNo);
		float[] bgRed = sortAndGet31Element(rgset.getRedSet(),negative,sampleNo);
		HashMap<Integer,float[]> greenBG =  calculateBackground(rgset.getGreenSet(),bgGreen);
		HashMap<Integer,float[]> redBG =  calculateBackground(rgset.getRedSet(),bgRed);
		//RGSet newSet = new RGSet(greenBG, redBG, rgset.getSampleIDs());
		RGSet newSet = new RGSet();
		newSet.setGreenSet(greenBG); 
		newSet.setRedSet(redBG);
		return newSet;
	}


	public int[] getControlAddress(String controlType, ReadManifest manifest, int asList){
		ArrayList<Integer> list = new ArrayList<Integer>();
		String[] types = controlType.split(", ");
		//Control[] control = manifest.getControlList();

		/*
		 * TODO
		 * 
		 * */

		Control[] control = new Control[20];
		if(asList==1){
			for(int i=0;i<types.length;i++){
				for(int j = 0;j<control.length;j++){
					String type = control[j].getType();
					if(types[i].equals(type)){
						list.add(control[j].getAddress());
					}
				}
			}
		}else{
			for(int i=0;i<types.length;i++){
				for(int j = 0;j<control.length;j++){
					String type = control[j].getType();
					if(types[i].equals(type)){
						list.add(control[j].getAddress());
					}
				}
			}
		}
		int[] AList = new int[list.size()];
		for(int k=0;k<list.size();k++){
			AList[k]=list.get(k);
		}
		return AList;
	}
	//Use same as in R or okay with double?
	public float[] getMeanValuePatient(HashMap<Integer, float[]> color, int[] addresses,  int sampleNo){
		float[] mean = new float[sampleNo];
		float[] results = new float[sampleNo];
		int noAddresses = addresses.length;
		for(int i = 0;i<noAddresses;i++){
			float[] address = color.get(addresses[i]);
			for(int j=0;j<mean.length;j++){
				mean[j]=mean[j]+address[j];
			}
		}
		for(int k = 0;k<mean.length;k++){
			results[k]= mean[k]/noAddresses;

		}
		return results; 

	}

	public float[] sortAndGet31Element(HashMap<Integer,float[]> color, int[] addresses,int sampleNo){
		int noAddresses = addresses.length;
		float[] results = new float[sampleNo];
		for(int i = 0;i<sampleNo;i++){
			float[] sort = new float[noAddresses];
			for(int j=0;j<noAddresses;j++){
				float[] address = color.get(addresses[j]);
				sort[j]=address[i];
			}
			Arrays.sort(sort);
			results[i]=sort[30];
		}

		return results;
	}


	public float[] getFactor(float ref, float[] vector){
		float[] factorI = new float[vector.length];
		for(int i =0;i<vector.length;i++){
			factorI[i]=ref/vector[i];
		}
		return factorI;
	}

	public HashMap<Integer,float[]> calculateNormalValues(HashMap<Integer,float[]> newColor,HashMap<Integer, float[]> color, float[] vector){
		for (Entry<Integer, float[]> values: color.entrySet()) {
			float[] vals = values.getValue();
			int key = values.getKey();
			float[] temp = new float[vector.length];
			for(int i=0;i<temp.length;i++){
				temp[i] = vals[i]*vector[i];		      
			}
			newColor.put(key, temp);
		}
		return newColor;
	}

	public HashMap<Integer,float[]> calculateBackground(HashMap<Integer, float[]> color, float[] vector){
		for (Entry<Integer, float[]> values: color.entrySet()) {
			float[] vals = values.getValue();
			int key = values.getKey();
			float[] temp = new float[vector.length];
			for(int i=0;i<temp.length;i++){
				temp[i] = vals[i]-vector[i];
				if(temp[i]<0){
					temp[i]=0;
				}
			}
			color.put(key, temp);
		}

		return color;
	}

	public void getBetaValues(){

	}
	public int getSampleNo(int[] addresses, RGSet rgset){
		HashMap<Integer,float[]> green = rgset.getGreenSet();
		float[] data = green.get(addresses[0]);
		int number = data.length;
		return number;
	}

	/*public void detectP(RGSet rgset, ReadManifest manifest){
		int[] controlIdx = getControlAddress("NEGATIVE",manifest,0);
		int sampleNo = getSampleNo(controlIdx,rgset);
		HashMap<Integer,float[]> rBg = getBackground(rgset.getRedSet(),controlIdx);
		HashMap<Integer,float[]> gBg = getBackground(rgset.getGreenSet(),controlIdx);
		int[] medianRed = findMedian(rBg,controlIdx, sampleNo);
		int[] medianGreen = findMedian(gBg,controlIdx, sampleNo);
		System.out.println("Værdi: ");
		for(int i=0;i<medianGreen.length;i++){
			System.out.print(medianGreen[i]+ " ");
		}
		double[] redMAD = MedianAbsoluteDerivation(rBg,controlIdx,medianRed);
		System.out.println("Værdi: ");
		for(int i=0;i<redMAD.length;i++){
			System.out.print(redMAD[i]+ " ");
		}
		double[] greenMAD = MedianAbsoluteDerivation(gBg,controlIdx,medianGreen);

	}*/

	public HashMap<Integer,float[]> getBackground(HashMap<Integer,float[]> color, int[] addresses){
		HashMap<Integer,float[]> newSet = new HashMap<Integer,float[]>();	
		for(int i = 0;i<addresses.length;i++){
			int key = addresses[i];
			float[] address = color.get(addresses[i]);
			newSet.put(key, address);
		}
		return newSet;
	}

	public int[] findMedian(HashMap<Integer,int[]> color,int[] addresses, int sampleNo){
		int noAddresses = addresses.length;
		int mid = addresses.length/2;
		int[] results = new int[sampleNo];
		for(int i = 0;i<sampleNo;i++){
			int[] sort = new int[noAddresses];
			for(int j=0;j<noAddresses;j++){
				int[] address = color.get(addresses[j]);
				sort[j]=address[i];
			}
			Arrays.sort(sort);
			results[i] = sort[mid-1];
		}

		return results;
	}


	public double[] MedianAbsoluteDerivation(HashMap<Integer,int[]> color,int[] addresses, int[] median){
		double k = 1.4628;
		int noAddresses = addresses.length;
		int mid = addresses.length/2;
		double[] results = new double[median.length];
		for(int i = 0;i<median.length;i++){
			int[] sort = new int[noAddresses];
			for(int j=0;j<noAddresses;j++){
				int[] address = color.get(addresses[j]);
				sort[j]=address[i]-median[i];
				if(sort[j]<0){
					int a = Math.abs(sort[j]);
					sort[j]=a;
				}
			}
			Arrays.sort(sort);
			results[i] = sort[mid-1]*k;
		}
		return results;
	}

	public void calculateP(RGSet rgset, ReadManifest manifest,int[] rMu, int[] gMu,float[] rSd, float[] gSd ,int sampleNo){
		/*HashMap<Integer, float[]> detP = new HashMap<Integer,float[]>();
		CpG[] list = manifest.getCpgList();
		for(int i=0;i<list.length;i++){
			String name = list[i].getCpgName();
			int addressA = 0;
			int addressB = 0;
			if(list[i].getInifniumType().equals("II")){
				addressA = list[i].getAddressA();
				float[] adAr = rgset.getRedSet().get(addressA);
				float[] adAg = rgset.getGreenSet().get(addressA);
				for(int j=0;j<sampleNo;j++){
					float intensity = adAr[j]+adAg[j];
					//Calculate pnorm and add to hashmap

				}
			}else if(list[i].getInifniumType().equals("I")&&list[i].getColorChannel().equals("Red")){
				addressA = list[i].getAddressA();
				addressB = list[i].getAddressB();
				float[] adAr = rgset.getRedSet().get(addressA);
				float[] adBr = rgset.getRedSet().get(addressB);
				for(int j=0;j<sampleNo;j++){
					float intensity = adAr[j]+adBr[j];
					//Calculate pnorm and add to hashmap

				}
			}else if(list[i].getInifniumType().equals("I")&&list[i].getColorChannel().equals("Green")){
				addressA = list[i].getAddressA();
				addressB = list[i].getAddressB();
				float[] adAg = rgset.getRedSet().get(addressA);
				float[] adBg = rgset.getRedSet().get(addressB);
				for(int j=0;j<sampleNo;j++){
					double intensity = adAg[j]+adBg[j];

					//Calculate pnorm and add to hashmap 
				}
			}
		}*/

	}


	@Override
	public void performNormalization(RGSet methylationData, ReadManifest manifest, char[] gender, int nt)
			throws OutOfMemoryError {

		/*int reference = 1;
		RGSet newRGSet = bgcorrectionIllumina(manifest, rgSet);
		float test [] = newRGSet.getRedSet().get(10600313);
		normalizeIlluminaControl(newRGSet, manifest, 1);
		//DRGSet bRGSet = normalizeIlluminaControl(newRGSet,manifest,reference);
		System.out.println("der");
		//double[] test = bRGSet.getRedSet().get(10600313);
		System.out.println("værdi ");
		for(int i=0;i<test.length;i++){
			System.out.print(test[i]+" ");
		}

		MSet mSet = new MSet();
		mSet.setRgSet(newRGSet);
		mSet.setManifest(manifest);
		mSet.loadData();

		USet uSet = new USet();
		uSet.setRgSet(newRGSet);
		uSet.setManifest(manifest);
		uSet.loadData();
		 */

	}


	@Override
	public void checkProgress() {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void setDone(boolean done) {
		// TODO Auto-generated method stub

	}


	@Override
	public int getProgress() {
		// TODO Auto-generated method stub
		return 0;
	}
}
