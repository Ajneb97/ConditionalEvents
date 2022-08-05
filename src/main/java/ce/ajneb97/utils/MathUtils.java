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
            return expression.evaluate()+"";
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
        if (value > 0) {
            return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue();
        } else {
            return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_CEILING).doubleValue();
        }
    }
}
