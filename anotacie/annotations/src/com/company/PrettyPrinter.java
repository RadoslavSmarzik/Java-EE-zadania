package com.company;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

class PrettyPrintableException extends Exception {
    public PrettyPrintableException(String errorMessage) {
        super(errorMessage);
    }
}

public class PrettyPrinter {

    private void checkIfPrintable(Object object) throws PrettyPrintableException {
        if (Objects.isNull(object)) {
            throw new PrettyPrintableException("The object to print is null");
        }

        Class<?> objectClass = object.getClass();
        if (!objectClass.isAnnotationPresent(PrettyPrintable.class)) {
            throw new PrettyPrintableException("The class "
                    + objectClass.getSimpleName()
                    + " is not annotated with PrettyPrintable");
        }
    }

    private Method getMethodForPrettyPrint(Object object) {
        Class<?> objectClass = object.getClass();
        for (Method method : objectClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(HowToPrettyPrint.class)) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }


    private void printFields(Object object) throws InvocationTargetException, IllegalAccessException {
        Class<?> objectClass = object.getClass();

        for (Field field : objectClass.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(NotPrettyPrinted.class)) {


            } else if (field.isAnnotationPresent(CustomPrettyPrint.class)){
                Method method = getMethodForPrettyPrint(object);
                if(method != null){
                    CustomPrettyPrint customPrettyPrint = field.getAnnotation(CustomPrettyPrint.class);
                    int numberOfSymbols = customPrettyPrint.numberOfSymbols();
                    HowToPrettyPrint howToPrettyPrint = method.getAnnotation(HowToPrettyPrint.class);
                    int numberOfSpaces = howToPrettyPrint.numberOfSpaces();
                    method.invoke(object, field.getName(), field.get(object).toString(), numberOfSymbols);
                    for(int i = 0; i < numberOfSpaces; i++){
                        System.out.println();
                    }
                }

            } else {
                System.out.println(field.getName() + ": " + field.get(object).toString());
            }
        }
    }

    public void print(Object object) throws PrettyPrintableException{
        try {
            checkIfPrintable(object);
            printFields(object);
        } catch (PrettyPrintableException e) {
            throw new PrettyPrintableException(e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
