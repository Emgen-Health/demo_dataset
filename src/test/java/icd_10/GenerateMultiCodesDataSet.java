package icd_10;

import core.files.reader.ReadExcelFile;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Random;

import static core.files.writer.WriteTXTFile.writeTxtFile;

/**
 * Created by ismail on 11/2/19
 */
public class GenerateMultiCodesDataSet {

    private String inputExcel = "disease_variations.xlsx";
    private String outputFolder = "src/test/resources/codes/output/all/";
    private int start, end;

    public GenerateMultiCodesDataSet(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public void runTwoK() {
        // Read Diseases codes sheet for K
        ArrayList<ArrayList<String>> kDiseases = ReadExcelFile.getSheetData(inputExcel, "K00-K95 Diseases Codes");
        // Remove headers
        kDiseases.remove(0);

        // Build output Files
        String kOutputFile = outputFolder + "k_diseases";
        // Generate a Data-set of diseases sentences
        generateTwoDiseasesSentences(kOutputFile, kDiseases);
    }

    public void runTwoI() {
        // Read Diseases codes sheet for I
        ArrayList<ArrayList<String>> iDiseases = ReadExcelFile.getSheetData(inputExcel, "I00-I99 Diseases Codes");
        // Remove headers
        iDiseases.remove(0);
        // Build output Files
        String iOutputFile = outputFolder + "i_diseases";
        // Generate a Data-set of diseases sentences
        if (end < 373)
            generateTwoDiseasesSentences(iOutputFile, iDiseases);
    }

    public void runThreeK() {
        // Read Diseases codes sheet for K
        ArrayList<ArrayList<String>> kDiseases = ReadExcelFile.getSheetData(inputExcel, "K00-K95 Diseases Codes");
        // Remove headers
        kDiseases.remove(0);

        // Build output Files
        String kOutputFile = outputFolder + "k_diseases";
        // Generate a Data-set of diseases sentences
        generateThreeDiseasesSentences(kOutputFile, kDiseases);
    }

    public void runThreeI() {
        // Read Diseases codes sheet for I
        ArrayList<ArrayList<String>> iDiseases = ReadExcelFile.getSheetData(inputExcel, "I00-I99 Diseases Codes");
        // Remove headers
        iDiseases.remove(0);
        // Build output Files
        String iOutputFile = outputFolder + "i_diseases";
        // Generate a Data-set of diseases sentences
        if (end < 373)
            generateThreeDiseasesSentences(iOutputFile, iDiseases);
    }

    // This method generates diseases data set
    private void generateTwoDiseasesSentences(String outputFile, ArrayList<ArrayList<String>> diseases) {
        // Read Variations sheets for multi codes
        ArrayList<ArrayList<String>> variationsTwoCodes = ReadExcelFile.getSheetData(inputExcel, "Two Codes Variations");
        // Remove variations headers
        variationsTwoCodes.remove(0);
        // Initialize diseases variations
        ArrayList<ArrayList<String>> firstDiseases = new ArrayList<>(diseases);
        ArrayList<ArrayList<String>> secondDiseases = new ArrayList<>(diseases);

        // Loop over the disease for two codes
        for (ArrayList<String> variationTwoCodes : variationsTwoCodes) {
            System.out.println("Started Two");
            for (ArrayList<String> firstDisease : firstDiseases) {
                for (int counter = start; counter < end; counter++) {
                    ArrayList<String> secondDisease = secondDiseases.get(counter);
                    String firstDiseaseCode = firstDisease.get(0);
                    String firstDiseaseSentence = firstDisease.get(1);
                    String secondDiseaseCode = secondDisease.get(0);
                    String secondDiseaseSentence = secondDisease.get(1);

                    String output = firstDiseaseCode + "," + secondDiseaseCode + "|" +
                            buildDiseaseSentence(variationTwoCodes, firstDiseaseSentence, secondDiseaseSentence);
                    writeTxtFile(outputFile + "_two.txt", output);
                }
            }
        }
        System.out.println("Ended Two");

    }

    private void generateThreeDiseasesSentences(String outputFile, ArrayList<ArrayList<String>> diseases) {
        // Read Variations sheets for multi codes
        ArrayList<ArrayList<String>> variationsThreeCodes = ReadExcelFile.getSheetData(inputExcel, "Three Codes Variations");
        // Remove variations headers
        variationsThreeCodes.remove(0);
// Initialize diseases variations
        ArrayList<ArrayList<String>> firstDiseases = new ArrayList<>(diseases);
        ArrayList<ArrayList<String>> secondDiseases = new ArrayList<>(diseases);
        ArrayList<ArrayList<String>> thirdDiseases = new ArrayList<>(diseases);
        // Loop over the disease for three codes
        for (ArrayList<String> variationThreeCodes : variationsThreeCodes) {
            System.out.println("Started Three");
            for (ArrayList<String> firstDisease : firstDiseases) {
                for (ArrayList<String> secondDisease : secondDiseases) {
                    for (int counter = start; counter < end; counter++) {
                        ArrayList<String> thirdDisease = thirdDiseases.get(counter);
                        String firstDiseaseCode = firstDisease.get(0);
                        String firstDiseaseSentence = firstDisease.get(1);
                        String secondDiseaseCode = secondDisease.get(0);
                        String secondDiseaseSentence = secondDisease.get(1);
                        String thirdDiseaseCode = thirdDisease.get(0);
                        String thirdDiseaseSentence = thirdDisease.get(1);

                        String output = firstDiseaseCode + "," + secondDiseaseCode + "," + thirdDiseaseCode + "|" +
                                buildDiseaseSentence(variationThreeCodes, firstDiseaseSentence, secondDiseaseSentence, thirdDiseaseSentence);
                        writeTxtFile(outputFile + "_three.txt", output);
                    }
                }
            }
        }
        System.out.println("Ended Three");
    }

    // This method convert the variation to sentence for one disease
    private String buildDiseaseSentence(ArrayList<String> variationRow, String... diseasesArray) {
        String dataSetString = "";
        ArrayList<String> diseases = new ArrayList<>();

        for (String disease : diseasesArray) {
            if (!disease.isEmpty())
                diseases.add(disease);
        }

        for (String variationCell : variationRow) {
            if (variationCell.contains("disease_name")) {
                dataSetString += " " + variationCell.replace("disease_name", diseases.get(0));
                diseases.remove(0);
            } else if (variationCell.equalsIgnoreCase("blank"))
                continue;
            else
                dataSetString += " " + variationCell;
        }
        return dataSetString.trim();
    }
}
