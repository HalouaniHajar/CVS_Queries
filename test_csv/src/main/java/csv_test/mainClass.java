package csv_test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * pour le AND et le OR ils doivent etre
 * en Majiscule pour que le programme
 * ne traite pas les mots composés de and et or comme par exemple apporter ou apport
 * 
 * le Path du fichier au niveau de "IMPORT"
 * doit être avec des / et non pas des backslash
 * 
 * on suppose que l'utilisateur utilise toujours le chemin absolut
 * 
 */
public class mainClass {
    
    
    public static void main(String[] args) throws FileNotFoundException
    {
        System.out.println("\n\n******************************************");
        
        //" IMPORT C:\\Users\\pc\\Documents\\NetBeansProjects\\test_csv\\src\\main\\java\\com\\mycompany/test_csv/file.csv;";
        //" SELECT year, industry_code_ANZSIC, industry_name_ANZSIC FROM file_1 WHERE year = 2010;"
        csv obj = new csv();
        Scanner in = new Scanner(System.in);
        
        System.out.print("veillez saisire une requete :");
        String query = in.nextLine();
        
        while( !query.equals("EXIT") )
        {
            
            if( query.contains("IMPORT") )
            {
                obj.importerFile(query);
            }
            
            else if( query.contains("SELECT") && query.contains("FROM") )
            {
                ArrayList<ArrayList<String> >resultat = obj.executer(query);
                if( resultat != null )
                    for( int i=0; i<resultat.size(); i++ )
                        {
                            for( int j=0; j<resultat.get(i).size(); j++ )
                                System.out.print( resultat.get(i).get(j) + "  " );
                            System.out.println();
                        }
            }
            
            
            System.out.print("\n\nveillez saisire une requete :");
            query = in.nextLine();
        }
        
        
        
        
        System.out.println("******************************************");
    }
    
    
}
