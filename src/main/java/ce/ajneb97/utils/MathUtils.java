package ce.ajneb97.utils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Random;

public class MathUtils {

    public static String calculate(final String str) {
        try{
            Expression expression = new ExpressionBuilder(str).build();
            if(!expression.validate().isValid()){
                return str;
            }
            return Double.toString(expression.evaluate());
        }catch(Exception e){
            return str;
        }
    }

    private static final Random r = new Random();

    public static int getRandomNumber(int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }
}
