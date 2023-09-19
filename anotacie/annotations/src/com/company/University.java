package com.company;

@PrettyPrintable
public class University {

    @CustomPrettyPrint(numberOfSymbols = 4)
    private String name;
    //@CustomPrettyPrint(numberOfSymbols = 10)
    private String description;
    private int numberOfStudents;
    @NotPrettyPrinted
    private String privateInformation;

    public University(String name, String description, int numberOfStudents, String privateInformation){
        this.name = name;
        this.description = description;
        this.numberOfStudents = numberOfStudents;
        this.privateInformation = privateInformation;
    }

    private void printDashes(int number){
        for(int i = 0; i < number; i++){
            System.out.print("-");
        }
    }

    @HowToPrettyPrint(numberOfSpaces = 1)
    public String getNiceFormat(String name, String value, int numberOfSymbols){
        if(name != null && value != null){
            int number = name.length() + value.length() + 4 + 2 * numberOfSymbols;
            printDashes(number);
            System.out.println();
            printDashes(numberOfSymbols);
            System.out.print(" " + name + ": " + value + " ");
            printDashes(numberOfSymbols);
            System.out.println();
            printDashes(number);
            System.out.println();
        }
        return null;
    }

}
