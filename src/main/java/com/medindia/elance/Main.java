package com.medindia.elance;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        char[] specifiedPages = new char[args.length];
        for (int i = 0; i<args.length; i++){
            specifiedPages[i] = args[i].charAt(0);
        }

        Medindia medindia = new Medindia(specifiedPages);
        //medindia.getGenericList();
        medindia.getBrandedList();
    }
}
