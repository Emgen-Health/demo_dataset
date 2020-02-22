package icd_10.generate_multi_codes;

import core.files.reader.ReadTXTFile;

import java.util.ArrayList;
import java.util.List;

import static core.files.writer.WriteTXTFile.writeTxtFile;

/**
 * Created by ismail on 1/24/20
 */
public class GenerateThreeIDataSet {

    private String outputFolder = "src/test/resources/codes/output/all/";
    private int start, end;

    public GenerateThreeIDataSet(int start, int end) {
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
            generateThreeDiseasesSentences(iOutputFile, iDiseases);
    }

    // This method generates diseases data set
    private void generateThreeDiseasesSentences(String outputFile, List diseases) {
        // Read Variations sheets for multi codes
        List<String> variationsThreeCodes = ReadTXTFile.readTxtFile("three_codes_variations.txt");
        // Remove variations headers
        variationsThreeCodes.remove(0);
// Initialize diseases variations
        ArrayList<String> firstDiseases = new ArrayList<>(diseases);
        ArrayList<String> secondDiseases = new ArrayList<>(diseases);
        ArrayList<String> thirdDiseases = new ArrayList<>(diseases);
        // Loop over the disease for three codes
        for (String variationThreeCodes : variationsThreeCodes) {
            for (String firstDisease : firstDiseases) {
                for (String secondDisease : secondDiseases) {
                    for (int counter = start; counter < end; counter++) {
                        String thirdDisease = thirdDiseases.get(counter);

                        String firstDiseaseCode = firstDisease.split("\t")[0];
                        String firstDiseaseSentence = firstDisease.split("\t")[1];

                        String secondDiseaseCode = secondDisease.split("\t")[0];
                        String secondDiseaseSentence = secondDisease.split("\t")[1];

                        String thirdDiseaseCode = thirdDisease.split("\t")[0];
                        String thirdDiseaseSentence = thirdDisease.split("\t")[1];

                        String output = firstDiseaseCode + "," + secondDiseaseCode + "," + thirdDiseaseCode + "|" +
                                buildDiseaseSentence(variationThreeCodes.split("\t"), firstDiseaseSentence, secondDiseaseSentence, thirdDiseaseSentence);
                        writeTxtFile(outputFile + "_three.txt", output);
                    }
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
