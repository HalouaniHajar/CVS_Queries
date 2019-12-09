/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csv_test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class csv {
    
    private List<String> files;     // liste des fichiers importés
    private String nomDuFicher;     // nom du fichier ou executer la requete sql actuelle
    private String[] columns;       // les colonnes de la table
    Scanner in;                     // pour la lecture du fichier
    
    List<Integer> index;

    public csv() {
        this.files = new ArrayList<>();
    }

    // pour avoir la possibilité d'importer différents fichiers
    public void setNomDuFicher(String nomDuFicher) {
        this.nomDuFicher = nomDuFicher;
        this.setColumns();
    }

    private void setColumns() {
        File fichier = new File(this.nomDuFicher);
        try
            {
                in = new Scanner(fichier);
                this.columns = in.next().split(",");
            }
        catch(FileNotFoundException e)
            {
                System.out.println("fichier non trouver");
            }
    }
    
    private String getColumnName( int index )
    {
        if( index <= this.columns.length )
            return this.columns[index];
        return null;
    }
    
    
    private void setIndexOfSELECT(String query)
    {
        this.index = new ArrayList<>();
        String[] Champs = query.split(" ");
        
        // pour enlever les virgule
        for( int i=0; i<Champs.length; i++)
            Champs[i] = Champs[i].replaceAll(",", "");
        
        // i=1 pour le pas prendre en consideration le 'SELECT'
        for( int i=1; i<Champs.length && !Champs[i].toLowerCase().equals("from") ; i++ )
            for( int j=0; j<this.columns.length; j++ )
                if( this.columns[j].toLowerCase().equals( Champs[i].toLowerCase() ) )
                    {
                        this.index.add(j);
                        break;
                    }
    }
    
    private String getFrom( String query )
    {
        String[] champs = query.split(" ");
        int index = 0;
        for( index=0; index<champs.length; index++)
            {
                if( champs[index].toLowerCase().equals("from") )
                    break;
            }
        return champs[index+1];
    }
    
    
    private int getIndexColumn( String nomColumn )
    {
        for( int i=0; i<this.columns.length; i++ )
            if( this.columns[i].equals(nomColumn) )
                return i;
        return -1;
    }
    
    private boolean conditionSimple( String condition, String[] ligne )
    {
        /**
         * au niveau des conditions, on a 4 cas:
         * cas1 :  de <
         * cas2 : de >
         * cas3 : de !=
         * cas4 : de =
         */
        
        // pour eviter le traitement des caractaire speciaux
        condition = condition.replaceAll(",", "");
        //condition = condition.replaceAll("(", "");
        //condition = condition.replaceAll(")", "");
        condition = condition.replaceAll(" ", "");
        
        boolean result = false;
        
        // cas de <
        if( condition.contains("<") )
        {
            String[] tmp = condition.split("<");
            int index = this.getIndexColumn( tmp[0] );
            
            // le cas ou on a column < entier
            if( index != -1 )
                result = Integer.parseInt( ligne[index] ) < Integer.parseInt( tmp[1] );
            // le cas de entier < column
            else
                {
                    index = this.getIndexColumn( tmp[1] );
                    result = Integer.parseInt( tmp[0] )< Integer.parseInt( ligne[index] );
                }
        }
        // cas de >
        else if( condition.contains(">") )
        {
            String[] tmp = condition.split(">");
            int index = this.getIndexColumn( tmp[0] );
            
            // le cas ou on a column > entier
            if( index != -1 )
                result = Integer.parseInt( ligne[index] ) > Integer.parseInt( tmp[1] );
            // le cas de entier > column
            else
                {
                    index = this.getIndexColumn( tmp[1] );
                    result = Integer.parseInt( tmp[0] )> Integer.parseInt( ligne[index] );
                }
        }
        // cas de !=
        else if( condition.contains("!=") )
        {
            String[] tmp = condition.split("!=");
            int index = this.getIndexColumn( tmp[0] );
            
            // le cas ou on a column = entier
            if( index != -1 )
                result = !ligne[index].equals(tmp[1]);
            // le cas de entier = column
            else
                {
                    index = this.getIndexColumn( tmp[1] );
                    result = !tmp[0].equals(ligne[index]);
                }
        }
        // cas de =
        else if( condition.contains("=") )
        {
            String[] tmp = condition.split("=");
            int index = this.getIndexColumn( tmp[0] );
            
            // le cas ou on a column = entier
            if( index != -1 )
                result = ligne[index].equals(tmp[1]);
            // le cas de entier = column
            else
                {
                    index = this.getIndexColumn( tmp[1] );
                    result = tmp[0].equals(ligne[index]);
                }
        }
        
        return result;
    }
    
    
    private boolean  conditionAndOr( String condition, String[] ligne )
    {
        // si la condition ne contient pas AND ou OR
        if( !condition.contains("AND") || !condition.contains("OR") )
            return this.conditionSimple(condition, ligne);
        
        // on suppose que cette fonction ne peut être utilisée que si
        // la condition ne contient pas des ( ou )
        // dans le cas d'une succession de AND et OR
        // on donne la priorité pour le AND
        boolean resultat = false;
        boolean resAnd = true;
        boolean resOr = false;
        
        // pour supprimer tous les espaces
        condition = condition.replaceAll(" ", "");
        String[] conditionsAnd = condition.split("AND");
        for( String cdsAnd : conditionsAnd ) // cds abréviation de conditions
            {
                String[] conditionsOr = cdsAnd.split("OR");
                for( String cdsOr : conditionsOr )
                    {
                        resOr |= this.conditionSimple(cdsOr, ligne);
                    }
                resAnd &= resOr;
            }
        resultat = resAnd;
        return resultat;
    }
    
    
    private boolean conditionparenthese( String condition, String[] ligne )
    {
        // si la condition ne contient pas des parenthèses ou des parenthèses manquantes
        // dans le cas des parenthéses manquantes, tous les parenthéses seront supprimées
        if( !condition.contains("(") || !condition.contains(")")  )
            {
                condition = condition.replaceAll("\\(", "").replaceAll("\\)", "");
                return this.conditionAndOr(condition, ligne);
            }
        
        
        boolean resultat = false;
        boolean resAnd = true;
        boolean resOr = false;
        
        String[] conditionAnd = condition.split("AND");
        for( String cdsAnd: conditionAnd )
            {
                resOr = false;
                String[] conditionOr = cdsAnd.split("OR");
                for( String cdsOr: conditionOr )
                    {
                        // enlever les parenthéses ( qui restes
                        cdsOr = cdsOr.replace("(", "");
                        resOr |= conditionparenthese(cdsOr, ligne);
                    }
                resAnd &= resOr;
            }
        resultat  =resAnd;
        return resultat;
    }
    
    
    public ArrayList<ArrayList<String> > executer( String query )
    {
        query = query.replace(";", "");
        int indexRes = 0;
        ArrayList<ArrayList<String> >resultat = new ArrayList< ArrayList<String> >();
        
        // SET FROM
        String from = this.getFrom(query);
        String pathOfFile = "";
        for( String s : this.files )
        {
            String[] test = s.split("/");
            if( test[test.length-1].equals(from+".csv") )
            {
                pathOfFile = s;
                break;
            }
        }
        
        if( pathOfFile.equals("") )
        {
            System.out.println("la table preciser n'est pas importer");
            return null;
        }
        this.setNomDuFicher(pathOfFile);
        
        
        // SET SELECT
        this.setIndexOfSELECT(query);
        
        // SET condition
        String[] conditon = query.split("WHERE");
        
        
        
        while( in.hasNext() )
        {
            String [] ligne = in.nextLine().split(",");
            boolean res = this.conditionparenthese(conditon[1], ligne );
            
            // si la ligne actuelle verifie la condition
            if( res )
                {
                    resultat.add(new ArrayList<String>());
                    
                    for( int i : this.index )
                        resultat.get(indexRes).add( ligne[i] );
                    indexRes++;
                }
        }
        return resultat;
    }
    
    
    public void importerFile( String query )
    {
        String[] tab = query.split("IMPORT ");
        String filePAth = tab[1].replaceAll(" ", "").replaceAll(",", "").replaceAll(";", "");
        /*
        String filaPath = "C:\\Users\\pc\\Documents\\NetBeansProjects\\test_csv\\src\\main\\java\\com\\mycompany/test_csv/"
                            + fileName
                            + ".csv";
        */
        this.files.add(filePAth);
        System.out.println(filePAth);
    }
}
