package com.company;

import java.lang.reflect.Field;
import java.util.*;

class Address {

    private String street;
    private int number;
    private String city;

    public Address(String street, int number, String city){
        this.street = street;
        this.number = number;
        this.city = city;
    }

    @Override
    public String toString() {
        return street + " " + number + ", " + city;
    }
}

@PrettyPrintable
public class Person {

    @CustomPrettyPrint
    private String name;
    @CustomPrettyPrint(numberOfSymbols = 6)
    private String surname;
    private int age;
    @NotPrettyPrinted
    private String identificationNumber;
    private Address address;
    private String[] jobs;

    public Person(String name, String surname, int age, String identificationNumber, Address address, String[] jobs){
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.identificationNumber = identificationNumber;
        this.address = address;
        this.jobs = jobs;
    }

    private void printStars(int number){
        for(int i = 0; i < number; i++){
            System.out.print("*");
        }
    }

    @HowToPrettyPrint
    public void printSomeValue(String name, String value, int numberOfSymbols){
        printStars(numberOfSymbols);
        System.out.print(" " + name + ": " + value + " ");
        printStars(numberOfSymbols);
        System.out.println();
    }

}
