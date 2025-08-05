package net.luko.bestia.util;

public class RomanUtil {
    private static final int[] VALUES = {
            1000, 900, 500, 400,
            100, 90, 50, 40,
            10, 9, 5, 4, 1
    };

    private static final String[] SYMBOLS = {
            "M", "CM", "D", "CD",
            "C", "XC", "L", "XL",
            "X", "IX", "V", "IV", "I"
    };

    public static String toRoman(int n){
        if(n <= 0) return String.valueOf(n);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < VALUES.length; i++){
            while(n >= VALUES[i]){
                n -= VALUES[i];
                builder.append(SYMBOLS[i]);
            }
        }
        return builder.toString();
    }
}
