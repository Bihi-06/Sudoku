package Sudoku;

import java.util.*;

public class Backtracking {

    public static String getVariable(ST<String, String> config) {

        // Retrieve a variable based on a heuristic or the next 'unfilled' one if there is no heuristic
        for (String s : config) {
            if (config.get(s).equalsIgnoreCase(""))
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

        for (String s : config) {
            //if we find a variable in the config with no value, then this means that the config is NOT complete
            if (config.get(s).equalsIgnoreCase(""))
                return false;
        }

        //ALL variables in config have a value, so the configuration is complete
        return true;
    }


    public static boolean consistent(String value, String variable, ST<String, String> config, Graph g) {

        //we need to get the adjacency list for the variable
        for (String adj : g.adjacentTo(variable)) {
            //if the adjacency list member's value is equal to the variable's selected value, then consistency fails
            if (config.get(adj) != null && config.get(adj).equalsIgnoreCase(value)) {
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
        for (String constraints : constraintsTable.get(variable)) {
            //if the adjacency list member's value is equal to the variable's selected value, then consistency fails
            if (!config.get(constraints).equals("") && !(constraintsTable.get(constraints).get(value).contains(config.get(constraints)))) {
                return false;
            }
        }

        //consistency check passed according to the variable's adjacancy list
        return true;
    }

    public static boolean arcConsistency(ST<String, SET<String>> domain, Graph g, int acType) {
        switch (acType) {
            case 1:
                return AC1(domain, g);
            case 3:
                return AC3(domain, g);
            case 4:
                return AC4(domain, g);
            default:
                return false;
        }
    }

    // AC-1: Réexamine les arcs jusqu'à stabilisation
    public static boolean AC1(ST<String, SET<String>> domain, Graph g) {
        boolean changed;
        do {
            changed = false;
            for (String x : domain) {
                for (String y : g.adjacentTo(x)) {
                    if (revise(domain, x, y)) {
                        changed = true;
                        if (domain.get(x).isEmpty()) return false;
                    }
                }
            }
        } while (changed);
        return true;
    }

    // AC-3: Utilise une file d'attente pour vérifier seulement les arcs nécessaires
    public static boolean AC3(ST<String, SET<String>> domain, Graph g) {
        Queue<String[]> queue = new LinkedList<>();
        for (String x : domain) {
            for (String y : g.adjacentTo(x)) {
                queue.add(new String[]{x, y});
            }
        }

        while (!queue.isEmpty()) {
            String[] pair = queue.poll();
            String x = pair[0], y = pair[1];

            if (revise(domain, x, y)) {
                if (domain.get(x).isEmpty()) return false;
                for (String z : g.adjacentTo(x)) {
                    if (!z.equals(y)) queue.add(new String[]{z, x});
                }
            }
        }
        return true;
    }

    private static List<String> convertIterableToList(Iterable<String> iterable) {
        List<String> list = new ArrayList<>();
        for (String item : iterable) {
            list.add(item);
        }
        return list;
    }

    // AC-4: Utilise des comptages pour gérer les domaines restants
    public static boolean AC4(ST<String, SET<String>> domain, Graph g) {
        Map<String, Integer> supportCount = new HashMap<>();
        Queue<String> queue = new LinkedList<>(convertIterableToList(domain.keys()));

        while (!queue.isEmpty()) {
            String x = queue.poll();
            for (String y : g.adjacentTo(x)) {
                if (revise(domain, x, y)) {
                    if (domain.get(x).isEmpty()) return false;
                    queue.add(y);
                }
            }
        }
        return true;
    }


    // Helper pour réduire le domaine de x par rapport à y
    public static boolean revise(ST<String, SET<String>> domain, String x, String y) {
        boolean revised = false;
        Iterator<String> iterX = domain.get(x).iterator();

        while (iterX.hasNext()) {
            String vx = iterX.next();
            boolean satisfiable = false;
            for (String vy : domain.get(y)) {
                if (!vx.equals(vy)) {  // Si une contrainte est satisfaite
                    satisfiable = true;
                    break;
                }
            }
            if (!satisfiable) {
                iterX.remove();  // Réduire le domaine
                revised = true;
            }
        }
        return revised;
    }

    public static ST<String, String> backtracking(ST<String, String> config, ST<String, SET<String>> domain, Graph g) {

        //recursion base case - check configuration completeness
        if (complete(config))
            return config;

        ST<String, String> result = null;

        //get a variable
        String v = getVariable(config);

        //get a SET of all the variable's values
        SET<String> vu = orderDomainValue(v, domain);

        //loop through all the variable's values
        for (String u : vu) {
            //if(consistent(u, v, config, g)) {

            if (consistent(u, v, config, g)) {
                config.put(v, u);

                result = backtracking(config, domain, g);
                if (result != null)
                    return result;
                config.put(v, "");
            }
        }
        return null;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // Initialisation du Sudoku et du graphe
        String[][] simpleGrid = {
                {"5", "3", "", "", "7", "", "", "", ""},
                {"6", "", "", "1", "9", "5", "", "", ""},
                {"", "9", "8", "", "", "", "", "6", ""},
                {"8", "", "", "", "6", "", "", "", "3"},
                {"4", "", "", "8", "", "3", "", "", "1"},
                {"7", "", "", "", "2", "", "", "", "6"},
                {"", "6", "", "", "", "", "2", "8", ""},
                {"", "", "", "4", "1", "9", "", "", "5"},
                {"", "", "", "", "8", "", "", "7", "9"}
        };

        Graph G = new Graph();
        // Initialisez G en ajoutant les contraintes appropriées (voisins dans les lignes, colonnes et blocs)

        // Initialisation des domaines
        ST<String, SET<String>> domainTable = initializeDomains(simpleGrid);

        // Mesure du temps pour AC-1
        System.out.println("\nRunning AC-1...");
        long startAC1 = System.nanoTime();
        boolean ac1Result = AC1(domainTable, G);
        long endAC1 = System.nanoTime();
        System.out.println("AC-1 result: " + ac1Result + " - Time: " + (endAC1 - startAC1) / 1e6 + " ms");
        printDomains(domainTable);

        // Réinitialisation des domaines
        domainTable = initializeDomains(simpleGrid);

        // Mesure du temps pour AC-3
        System.out.println("\nRunning AC-3...");
        long startAC3 = System.nanoTime();
        boolean ac3Result = AC3(domainTable, G);
        long endAC3 = System.nanoTime();
        System.out.println("AC-3 result: " + ac3Result + " - Time: " + (endAC3 - startAC3) / 1e6 + " ms");
        printDomains(domainTable);

        // Réinitialisation des domaines
        domainTable = initializeDomains(simpleGrid);

        // Mesure du temps pour AC-4
        System.out.println("\nRunning AC-4...");
        long startAC4 = System.nanoTime();
        boolean ac4Result = AC4(domainTable, G);
        long endAC4 = System.nanoTime();
        System.out.println("AC-4 result: " + ac4Result + " - Time: " + (endAC4 - startAC4) / 1e6 + " ms");
        printDomains(domainTable);

        // Comparaison des résultats
        System.out.println("\nComparison of execution times:");
        System.out.println("AC-1 Time: " + (endAC1 - startAC1) / 1e6 + " ms");
        System.out.println("AC-3 Time: " + (endAC3 - startAC3) / 1e6 + " ms");
        System.out.println("AC-4 Time: " + (endAC4 - startAC4) / 1e6 + " ms");
    }

    // Fonction utilitaire pour initialiser les domaines
    public static ST<String, SET<String>> initializeDomains(String[][] grid) {
        ST<String, SET<String>> domainTable = new ST<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String var = "x" + (i + 1) + (j + 1);
                SET<String> domain = new SET<>();
                if (!grid[i][j].equals("")) {
                    domain.add(grid[i][j]);
                } else {
                    for (int k = 1; k <= 9; k++) {
                        domain.add(String.valueOf(k));
                    }
                }
                domainTable.put(var, domain);
            }
        }
        return domainTable;
    }

    // Fonction utilitaire pour imprimer les domaines
    public static void printDomains(ST<String, SET<String>> domainTable) {
        for (String key : domainTable) {
            System.out.println(key + ": " + domainTable.get(key));
        }
    }

}
