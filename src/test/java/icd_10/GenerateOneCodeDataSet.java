package icd_10;

import core.files.reader.ReadExcelFile;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static core.files.writer.WriteTXTFile.writeTxtFile;

/**
 * Created by ismail on 10/4/19
 */
public class GenerateOneCodeDataSet {

    private String inputExcel = "disease_variations.xlsx";
    private String outputFolder = "src/test/resources/codes/output/diseases_variations/5_variations/";

    @Test
    public void run() {
        // Read Variations sheets for one code
        ArrayList<ArrayList<String>> variationsOneCode = ReadExcelFile.getSheetData(inputExcel, "One Code Variations");

        // Read Diseases codes sheet for K and I
        ArrayList<ArrayList<String>> kDiseases = ReadExcelFile.getSheetData(inputExcel, "K00-K95 Diseases Codes");
        ArrayList<ArrayList<String>> iDiseases = ReadExcelFile.getSheetData(inputExcel, "I00-I99 Diseases Codes");

        // Write Header or each file
        String kDiseasesFile = outputFolder + "k_one_diseases_data_set.txt";
        String iDiseasesFile = outputFolder + "i_one_diseases_data_set.txt";
        String outputHeaders = "Disease Code|Disease Description|Disease Sentence";
        writeTxtFile(kDiseasesFile, outputHeaders);
        writeTxtFile(iDiseasesFile, outputHeaders);
        // Remove headers from all Lists
        variationsOneCode.remove(0);
        kDiseases.remove(0);
        iDiseases.remove(0);

        // Loop over all K diseases and generate their data set for one code
        generateDiseasesDataSet(kDiseasesFile, kDiseases, variationsOneCode);
        generateDiseasesDataSet(iDiseasesFile, iDiseases, variationsOneCode);
    }

    // This method loops over all diseases and generate data set for them
    private void generateDiseasesDataSet(String outputFile, ArrayList<ArrayList<String>> diseases, ArrayList variations) {
        for (ArrayList<String> disease : diseases) {
            buildDiseasesDataSet(outputFile, disease, variations);
        }
    }

    // This method treats one disease code and save disease data set to output file
    private void buildDiseasesDataSet(String outputFile, ArrayList<String> diseaseLine, ArrayList<ArrayList<String>> variations) {
        for (ArrayList<String> variation : variations) {
            String diseaseCode = diseaseLine.get(0);
            String diseaseDescription = diseaseLine.get(1);
            String diseaseSentence = buildOneDiseaseSentence(variation, diseaseDescription);
            String diseaseOutputLine = diseaseCode + "|" + diseaseDescription + "|" + diseaseSentence;
            writeTxtFile(outputFile, diseaseOutputLine);
        }
    }

    // This method convert the variation to sentence for one disease
    private String buildOneDiseaseSentence(ArrayList<String> variation, String disease) {
        String dataSetString = "";
        for (String variationCell : variation) {
            if (variationCell.contains("disease_name"))
                dataSetString += " " + variationCell.replace("disease_name", disease);
            else if (variationCell.equalsIgnoreCase("blank"))
                continue;
            else
                dataSetString += " " + variationCell;
        }
        return dataSetString.trim();
    }
}
