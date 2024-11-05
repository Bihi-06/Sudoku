package Sudoku;

import java.util.Date;

public class Backtracking {
    
	public static String getVariable(ST<String, String> config) {
		
	// Retrieve a variable based on a heuristic or the next 'unfilled' one if there is no heuristic
        for (String s : config) {
        	if(config.get(s).equalsIgnoreCase(""))
        		return s;
        }
		
        // Get variable failed (all variables have been coloured)
		return null;
	}
	
	public static SET<String> orderDomainValue(String variable, ST<String, SET<String>> domain) {
		
		// Return the SET of domain values for the variable
		return domain.get(variable);
	}
	
	public static boolean complete(ST<String, String> config) {
		
		for(String s: config) {
			//if we find a variable in the config with no value, then this means that the config is NOT complete
			if(config.get(s).equalsIgnoreCase(""))
				return false;
		}
		
		//ALL variables in config have a value, so the configuration is complete
		return true;
	}
	
	
	public static boolean consistent(String value, String variable, ST<String, String> config, Graph g) {
		
		//we need to get the adjacency list for the variable
		for(String adj: g.adjacentTo(variable)) {
			//if the adjacency list member's value is equal to the variable's selected value, then consistency fails
		    if(config.get(adj)!=null && config.get(adj).equalsIgnoreCase(value)) {
				//consistency check fail
				return false;
			}
		}
			
		//consistency check passed according to the variable's adjacancy list
		return true;
	}
	
	public static boolean consistent(String value, String variable, ST<String, String> config,
									ST<String, ST<String, ST<String, SET<String>>>> constraintsTable) {
		//we need to get the constraint list for the variable
		for(String constraints: constraintsTable.get(variable)) {
			//if the adjacency list member's value is equal to the variable's selected value, then consistency fails
			if(!config.get(constraints).equals("") && !(constraintsTable.get(constraints).get(value).contains(config.get(constraints)))) {
				return false;
			}
		}
			
		//consistency check passed according to the variable's adjacancy list
		return true;
	}
	
	public static ST<String, String> backtracking(ST<String, String> config, ST<String, SET<String>> domain, Graph g){
		
		//recursion base case - check configuration completeness
		if(complete(config))
			return config;
		       
		ST<String, String> result = null;
		
		//get a variable
		String v = getVariable(config);
		
		//get a SET of all the variable's values
		SET<String> vu = orderDomainValue(v, domain);
		
		//loop through all the variable's values
		for(String u: vu) {
			//if(consistent(u, v, config, g)) {
                    
			if(consistent(u, v, config, g)) {
                             	config.put(v, u);
                                 
				result = backtracking(config, domain, g);
				if(result != null)
					return result;
				config.put(v,"");                               
			}
		}
		return null;		
	}	
	
	/**
	 * @param args
	 */
     public static void main(String[] args) {
       
            Graph G = new Graph();
            String grille [][]={    { "", "" ,  "", "", "", "", "7", "2", ""},
                                    { "", "1" , "3", "",  "" , "7" , "" , "4" , ""},
                                    { "2", "" ,  "", "", "5", "", "", "", "8"},
                                    { "" , "" , "", "", "" , "6" , "4" , "7" , ""},
                                    { "7", "8" ,  "", "4", "", "1", "", "3", "6"},
                                    { "" , "9", "4",  "3", "", "" , "" , "" , ""},
                                    { "3", "",  "", "", "6", "", "", "", "4"},
                                    { "" , "7" , "",  "5", "" , "" , "8", "1" , ""},
                                    { "", "5", "8", "", "", "", "", "", "" }};

            // Affichage de la grille initiale
             for(int i=0;i<9;i++){  // Ligne 
                System.out.println("");    
                for(int j=0;j<9;j++)   // Colonne
                    if(((String)grille[i][j]).length()!=0)
                       System.out.print(grille[i][j]);
                    else 
                        System.out.print(".");
            };

            // Contraintes au niveau des lignes
            for(int i=1;i<=9;i++)       // Ligne 
               for(int j=1;j<=8;j++)    // Colonne
                    for(int k=j+1;k<=9;k++){
                        String var1 ="x"+i+""+j;
                        String var2 ="x"+i+""+k;

                        G.addEdge(var1, var2);
                    }

            // Contaraintes au niveau des colonnes
            for(int i=1;i<=9;i++)       // Colonne 
               for(int j=1;j<=8;j++)      // Ligne
                    for(int k=j+1;k<=9;k++){
                        String var1 ="x"+j+""+i;
                        String var2 ="x"+k+""+i;

                        G.addEdge(var1, var2);
                    }

            // Contraintes au niveau des sous-grilles
            for(int ii=1;ii<=3;ii++)       // Ligne de la sous-grille 
               for(int jj=1;jj<=3;jj++) {    // Colonne de la sous-grille
                   int toplefti=(ii-1)*3;
                   int topleftj=(jj-1)*3;

                   for(int i=1;i<=3;i++)       // Colonne dans la sous-grille
                   for(int j=1;j<=3;j++)      // Ligne dans de la sous-grille
                    {
                        for(int k=1;k<=3;k++)       // Lignes et colonnes impliquées dans des contraintes dans la sous-grille 
                        for(int l=1;l<=3;l++) 
                            if(k!=i && l!=j){
                                String var1 ="x"+(toplefti+i)+""+(topleftj+j);
                                String var2 ="x"+(toplefti+k)+""+(topleftj+l);

                                G.addEdge(var1, var2);
                            }
                    }
               }

          // Tables des domaines
          ST<String, SET<String>> domainTable = new ST<String, SET<String>>();

            // Remplir les Domaines
            Object[][] domains = new Object[9][9];

            for(int i=0;i<9;i++)       // Colonne 
                for(int j=0;j<9;j++)      // Ligne
                    domains[i][j] = new SET<String>();

            for(int i=0;i<9;i++)       // Colonne 
                for(int j=0;j<9;j++){     // Ligne

                    if(((String)grille[i][j]).length()!=0)
                        ((SET<String>)domains[i][j]).add(new String((String)grille[i][j])); // Domaine avec une seule valeur (case remplie )
                    else{

                        for(int k=1;k<=9;k++)
                            ((SET<String>)domains[i][j]).add(""+k);// Domaine avec {1, 2, 3, ..., 9} ( case vide)
                    }                    
                }

            // Ajouter les domaines à la table       
            for(int i=1;i<=9;i++)       
                for(int j=1;j<=9;j++)
                    domainTable.put("x"+i+""+j, ((SET<String>)domains[i-1][j-1]));

            // Configuration initiale
            ST<String, String> config = new ST<String, String>();

           for(int i=1;i<=9;i++)       // Ligne 
           for(int j=1;j<=9;j++)   // Colonne
               config.put("x"+i+""+j,""); // Variables non affectées

           System.out.println("\nCalcul en cours ... ");   
           long temps1 = new Date().getTime();
           ST<String, String> result = backtracking(config, domainTable, G);


           // Afficher la solution
           for(int i=1;i<=9;i++){  // Ligne 
                System.out.println("");    
                for(int j=1;j<=9;j++)   // Colonne
                    System.out.print(config.get("x"+i+""+j)+" ");
            }; 
	}        
}
