package ce.ajneb97.utils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;

public class MathUtils {

    public static String calculate(final String str) {
        try{
            Expression expression = new ExpressionBuilder(str).build();
            if(!expression.validate().isValid()){
                return str;
            }

            double result = expression.evaluate();
            DecimalFormat df = new DecimalFormat("0.#");
            df.setMaximumFractionDigits(10);

            return df.format(result).replace(",", ".");
        }catch(Exception e){
            return str;
        }
    }

    public static int getRandomNumber(int min, int max) {
        Random r = new Random();
        int num = r.nextInt((max - min) + 1) + min;
        return num;
    }

    public static double truncate(double value){
        try{
            if (value > 0) {
                return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue();
            } else {
                return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_CEILING).doubleValue();
            }
        }catch(NumberFormatException e){
            return value;
        }
    }

    // org.apache.commons.lang3.math.NumberUtils
    public static boolean isParsable(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        } else if (str.charAt(str.length() - 1) == '.') {
            return false;
        } else if (str.charAt(0) == '-') {
            return str.length() == 1 ? false : withDecimalsParsing(str, 1);
        } else {
            return withDecimalsParsing(str, 0);
        }
    }

    // org.apache.commons.lang3.math.NumberUtils
    private static boolean withDecimalsParsing(String str, int beginIdx) {
        int decimalPoints = 0;

        for(int i = beginIdx; i < str.length(); ++i) {
            boolean isDecimalPoint = str.charAt(i) == '.';
            if (isDecimalPoint) {
                ++decimalPoints;
            }

            if (decimalPoints > 1) {
                return false;
            }

            if (!isDecimalPoint && !Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
