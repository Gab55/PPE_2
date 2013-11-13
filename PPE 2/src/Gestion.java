import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Scanner;

/*
* Back up 10/11 : http://pastebin.com/7EZxjnWw
* Back up 12/11 : http://pastebin.com/c1XbDgWP
*/



public class Gestion {
    public Gestion(){

    }

    public Connection getConnection(String db){
        System.out.println("-------- MySQL JDBC Connection Testing ------------");

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
        }

        System.out.println("MySQL JDBC Driver Registered!");
        Connection connection = null;

        try {
            connection = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/"+ db +"", "root", "");

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }

        if (connection != null) {
            System.out.println("Base valide !");
        } else {
            //Erreur lors de ce cas, a corriger
            System.out.println("La base de données n'existe pas !");
            System.out.println("Veuillez vérifier le nom donné.");
            System.out.println("--------------------------------------------------------------------");
            connection = null;
        }
        return connection;
    }

    public void genererForm(String db, String contenu[], String label[], String choix, String type, String col[]){
        //créé un fichier dans un dossier sur le C
        // Plus simple pour test sur différentes machines plutot que de faire ca dans les docs ou sur le bureau
        File fichier = new File("C:/"+db.toUpperCase()+"_"+choix.toLowerCase()+".html");

        //PrintWriter = outil pour écrire à l'intérieur du fichier
        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter(fichier);

            pw.println("<form name='"+db+"' method="+type.toUpperCase()+"><table>");
            for(int i=0; i < contenu.length; i++){
                if((contenu[i].equals("text")) || ((contenu[i].equals("textbox")))){
                    pw.println("<tr><td>"+label[i]+"</td><td><input type='text' name='"+col[i]+"'></td></tr>");
                }else if(contenu[i].equals("checkbox")){
                    pw.println("<tr><td>"+label[i]+"</td><td><input type='checkbox' name='"+col[i]+"'></td></tr>");
                }else if((contenu[i].equals("radio")) || ((contenu[i].equals("radiobutton")))){
                    pw.println("<tr><td>"+label[i]+"</td><td><input type='radio' name='"+col[i]+"'></td></td>");
                }else if((contenu[i].equals("liste déroulante")) || ((contenu[i].equals("liste")))){
                    pw.println("<tr><td>"+label[i]+"</td><td><select><option>Option 1</option><option>Option 2</option></select>");
                }
            }

            pw.println("</table></br><input type='submit' value='Enregistrer'></form>");

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if ( pw != null )
            {
                pw.close();
            }
        }

        //ouvre le fichier tout juste créé
        Desktop desk = Desktop.getDesktop();
        try{
            desk.open(fichier);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public String[] choixTable(Connection cnx){


        //Les tableaux commencent à 1 en sql !
        int i=1;

        // initialisation du tableau qui contiendra tous les noms de tables
        // obligé de le déclarer hors try/catch, sinon tu peux pas le retourner
        // + tu dois obligatoirement selectionner une taille en java, d'où le
        // null maintenant, et la bonne valeur dans le bloc en dessous

        String[] tab = null;

        try {
            // formulation pour récupérer les DatabaseMetaData et ResultSet
            // ResultSet = tableau, le 3 correspond au 3e argument passé,
            // soit le "%" dans la requête en dessous, qui correspond
            // au nom des tables

            String[] types = {"TABLE"};
            DatabaseMetaData dbm = cnx.getMetaData();
            ResultSet rs = dbm.getTables(null,null,"%",types);

            // modification du tableau, on lui attribut un nombre de valeur
            // en fonction du nombre de résultats générés par le ResultSet

            int nb = rs.getMetaData().getColumnCount();
            tab = new String[nb];

            System.out.println("Quelle table souhaitez vous utiliser?");

            // Temps que y'a des résultats, ca les enregistrera dans le tableau
            // en fonction de i

            // + Affiche une sorte de liste à l'écran pour que l'utilisateur
            // puisse sélectionner la bonne table

            //Ca peut poser problème sur de grosses bdd si y'a 42 tables j'pense. :D

            while(rs.next()){
                tab[i]=rs.getString(3);
                System.out.println(i+". "+rs.getString(3));
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Retourne le tableau de sorte à ce que l'on puisse reprendre le
        // nom de table depuis le Scanner du runMenu()
        return tab;
    }


    public String[] nomsColonnes(Connection cnx, String table) throws SQLException{
        String[] col = null;
        Statement stmt = cnx.createStatement();
        String query="Select * from "+table;

        try {
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();

            col = new String[rsmd.getColumnCount()];
            for(int i = 0; i < col.length; i++){
                String nomColonne = rsmd.getColumnName(i+1);
                col[i] = nomColonne;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return col;

    }


    public void runMenu(){
        Scanner sc = new Scanner(System.in);
        String db = "";
        Connection cnx = null;

        //Vérifie la saisie de la bdd
        while (db==""){
            System.out.println("Quelle base de données voulez-vous utiliser?");
            db=sc.nextLine();
            //Verification, voir si ça passe !
            cnx = this.getConnection(db);
            if (cnx==null){
                db = "";
            }
        }
            //Pour le choix de la table
            String tab[];
            tab = this.choixTable(cnx);

            int choixTable = sc.nextInt();
            String choix = tab[choixTable];

            //Affichage colonnes
            try{
            String col[];
            col = this.nomsColonnes(cnx, choix);

            int longueur = col.length;

            //Enregistrement du label + contenu pour chaque colonne
            String type;
            String contenu[], label[];
            contenu = new String[longueur];
            label = new String[longueur];

            System.out.println("De quel type sera le formulaire généré? (POST, GET)");
            type = sc.nextLine();

            for (int i=0; i < longueur; i++){

                System.out.println("Quel type de composant voulez vous utiliser pour '"+col[i]+"' ?");
                contenu[i]= sc.nextLine();
                System.out.println("Quel label devrait correspondre à '"+col[i]+"' ?");
                label[i]=sc.nextLine();

            }


            /* Utilisé pour des test
            System.out.println("----------------------------------------------");

            System.out.println("Blablabla");
            for (int i=0; i < col.length; i++){
                System.out.println(contenu[i]+" - "+label[i]);
            }
            */

            // Appel a la fonction pour créer le fichier + écrire dedans
            this.genererForm(db,contenu,label, choix, type,col);

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
