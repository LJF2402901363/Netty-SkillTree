package com.hks.netty;

import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        Scanner s = new Scanner(System.in);
        while (true){
            String line  = s.nextLine();
            if ("quit".equals(line)) break;
        }
    }
}
