package model_testing;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static core.files.reader.ReadExcelFile.*;
import static core.files.writer.WriteTXTFile.writeTxtFile;

/**
 * Created by ismail on 10/24/19
 */
public class DiseaseClassificationModel {

    // Define Class global variables
    String inputExcel = "full_data_set_of_diseases_with_variations.xlsx";
    String outputFolder = "src/test/resources/model_testing/output/disease_classification/";

    @Test
    public void testingClassificationModel() {
        processExcelFile("classification_", "http://localhost:5000/classify");
    }

    private void processExcelFile(String modelName, String modelUrl) {
        // Read number of excel sheets then iterate through them
        int sheetsNo = getNumberOfSheets(inputExcel);
        for (int counter = 1; counter <= sheetsNo; counter++) {
            // Get Sheet name
            String sheetName = getSheetName(inputExcel, counter);
            // Print which sheet is working on right now
            System.out.println("Working on Sheet " + sheetName + " model " + modelUrl);
            // Request sheet utterances
            processSheet(modelName, modelUrl, sheetName);
        }
    }

    private void processSheet(String modelName, String modelUrl, String sheetName) {
        // Retrieve all rows in the sheet
        ArrayList<ArrayList<String>> allRows = getSheetData(inputExcel, sheetName);
        // Define output excel file
        String outputFile = outputFolder + modelName + sheetName.toLowerCase().replace(" ", "_") + ".txt";
        // Save header to the sheet
        writeHeaders(outputFile);
        // Remove header from allRows list
        allRows.remove(0);
        // Processing all utterances in the sheet and save the results to output excel file
        for (ArrayList<String> row : allRows) {
            String result = processModel(modelUrl, row);
            writeResultToFile(outputFile, result);
        }
    }

    private void writeHeaders(String outputFile) {
        String headers = "Expected Diseases Codes|Expected Disease Sentence|Actual Disease Codes|Response Status Code|Compare Exp vs Actual|Response Body";
        writeTxtFile(outputFile, headers);
    }

    private String processModel(String modelUrl, ArrayList<String> row) {
        String expectedCode = row.get(0);
        String diseaseSentence = row.get(1);

        Response response = requestModelAPI(modelUrl, diseaseSentence);
        String result = compareModelResult(response, expectedCode);

        return expectedCode + "|" + diseaseSentence + "|" + result;
    }

    private Response requestModelAPI(String modelUrl, String diseaseSentence) {
        try {
            return RestAssured.given().that().param("description", diseaseSentence).when().get(modelUrl).thenReturn();

        } catch (Throwable throwable) {
            return null;
        }
    }

    private void writeResultToFile(String outputFile, String result) {
        writeTxtFile(outputFile, result);
    }

    private String compareModelResult(Response response, String expectedCode) {
        return getResponseResult(response, expectedCode);
    }

    private String getResponseResult(Response response, String expectedCode) {
        if (response != null) {
            try {
                // Get Status code and full response body
                String statusCode = String.valueOf(response.getStatusCode());
                String responseBody = response.getBody().jsonPath().get().toString();
                // Get Actual Diseases Codes
                HashMap hashMap = response.getBody().jsonPath().get();
                String actualCode = "";
                if (hashMap.containsKey("code"))
                    actualCode = hashMap.get("code").toString();
                else
                    actualCode = "Actual Code isn't exist, please check response body.";
                // Compare expected and actual codes
                String comparisonResult = String.valueOf(actualCode.equalsIgnoreCase(expectedCode));
                // Return results
                return actualCode + "|" + statusCode + "|" + comparisonResult + "|" + responseBody;
            } catch (Throwable th) {
                return "Something went wrong while processing response body" + th.getMessage();
            }
        } else
            return "Response of Model API is null";
    }
}
