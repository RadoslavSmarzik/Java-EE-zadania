package com.company;

import java.util.*;

public class Main {

    public static void main(String[] args) throws PrettyPrintableException {
        Address address = new Address("Oxford Street", 105, "London");
        String[] jobs = {"waiter", "teacher"};
        Person person = new Person("John", "Smith", 50, "711206/1423", address, jobs);
        University university = new University("Comenius University", "The best university in Slovakia.", 10000, "Private info.");
        PrettyPrinter prettyPrinter = new PrettyPrinter();

        System.out.println("FIRST EXAMPLE: ");
        prettyPrinter.print(person);
        System.out.println();
        System.out.println("SECOND EXAMPLE: ");
        prettyPrinter.print(university);

    }
}
