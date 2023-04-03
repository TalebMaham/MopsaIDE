package com.mopsa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;

import magpiebridge.util.SourceCodePositionFinder;



/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
     
            //executeScript();
     
            MopsaServerAnalysis mps = new MopsaServerAnalysis(); 
           System.out.println(mps.convertToolOutput()); 
        
     
    }



    public static void executeScript() {
        try {
            // Créer une instance de Runtime
            Runtime runtime = Runtime.getRuntime();
    
            // Exécuter la commande pour exécuter le script Bash
            Process process = runtime.exec("./commande.sh");
    
            // Récupérer la sortie de la commande
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
    
            // Vérifier le code de sortie de la commande
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Script exécuté avec succès.");
            } else {
                System.out.println("Erreur lors de l'exécution du script. Code de sortie: " + exitCode);
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = errorReader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static JsonObject parseJsonFile(String filePath) throws IOException {

        // Création d'un objet Gson
        Gson gson = new Gson();

        // Ouverture du fichier JSON
        BufferedReader br = new BufferedReader(new FileReader(filePath));

        // Conversion du contenu du fichier JSON en objet JSON
        JsonObject jsonObject = gson.fromJson(br, JsonObject.class);

        // Fermeture du fichier JSON
        br.close();

        return jsonObject;
    }



public static String getEndValue(JsonObject obj) {
    // Récupère la liste des checks
    JsonArray checks = obj.getAsJsonArray("checks");

    // Récupère le premier check de la liste
    JsonObject firstCheck = checks.get(0).getAsJsonObject();

    // Récupère la valeur de la propriété "end" dans le range du premier check
    JsonObject range = firstCheck.getAsJsonObject("range");
    JsonObject end = range.getAsJsonObject("end");
    String file = end.get("file").getAsString();
    int line = end.get("line").getAsInt();
    int column = end.get("column").getAsInt();

    // Retourne la valeur de la propriété "end" sous forme de String
    return "file: " + file + ", line: " + line + ", column: " + column;
}




    
    
}
