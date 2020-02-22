package icd_10.generate_multi_codes;

import core.files.reader.ReadTXTFile;

import java.util.ArrayList;
import java.util.List;

import static core.files.writer.WriteTXTFile.writeTxtFile;

/**
 * Created by ismail on 1/24/20
 */
public class GenerateTwoIDataSet {

    private String outputFolder = "src/test/resources/codes/output/all/";
    private int start, end;

    public GenerateTwoIDataSet(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public void run() {
        // Read Diseases codes sheet for I
        List<String> iDiseases = ReadTXTFile.readTxtFile("i_diseases_codes.txt");
        // Remove headers
        iDiseases.remove(0);
        // Build output Files
        String iOutputFile = outputFolder + "i_diseases";
        // Generate a Data-set of diseases sentences
        if (end < 373)
            generateTwoDiseasesSentences(iOutputFile, iDiseases);
    }

    // This method generates diseases data set
    private void generateTwoDiseasesSentences(String outputFile, List<String> diseases) {
        // Read Variations sheets for multi codes
        List<String> variationsTwoCodes = ReadTXTFile.readTxtFile("two_codes_variations.txt");
        // Remove variations headers
        variationsTwoCodes.remove(0);
        // Initialize diseases variations
        ArrayList<String> firstDiseases = new ArrayList<>(diseases);
        ArrayList<String> secondDiseases = new ArrayList<>(diseases);

        // Loop over the disease for two codes
        for (String variationTwoCodes : variationsTwoCodes) {
            for (String firstDisease : firstDiseases) {
                for (int counter = start; counter < end; counter++) {
                    String secondDisease = secondDiseases.get(counter);

                    String firstDiseaseCode = firstDisease.split("\t")[0];
                    String firstDiseaseSentence = firstDisease.split("\t")[1];

                    String secondDiseaseCode = secondDisease.split("\t")[0];
                    String secondDiseaseSentence = secondDisease.split("\t")[1];

                    String output = firstDiseaseCode + "," + secondDiseaseCode + "|" +
                            buildDiseaseSentence(variationTwoCodes.split("\t"), firstDiseaseSentence, secondDiseaseSentence);
                    writeTxtFile(outputFile + "_two.txt", output);
                }
            }
        }
    }

    // This method convert the variation to sentence for one disease
    private String buildDiseaseSentence(String[] variationRow, String... diseasesArray) {
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
