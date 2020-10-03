package xyz.spedcord.server.test;

import java.text.DecimalFormat;

public class FunctionTest {

    public static void main(String[] args) {
        double a = 1000.0;
        double b = 1.1;
        double c = 0;

        DecimalFormat df = new DecimalFormat("###,###.00");
        for (int i = 0; i < 100; i++) {
            System.out.println(String.format("x = %d: y = %s", i, df.format(fun(a, b, c, i))));
        }
    }

    private static double fun(double a, double b, double c, int x) {
        return a * (Math.pow(b, x)) + c;
    }

}
