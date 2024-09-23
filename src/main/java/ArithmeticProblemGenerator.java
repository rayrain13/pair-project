import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class ArithmeticProblemGenerator {
    private static final Random random = new Random();
    private static final int MAX_OPERATORS = 3;



    static class Fraction {
        int numerator;
        int denominator;

        public Fraction(int numerator, int denominator) {
            if (denominator == 0) {
                throw new IllegalArgumentException("Denominator cannot be zero");
            }
            int gcd = gcd(Math.abs(numerator), Math.abs(denominator));
            this.numerator = numerator / gcd;
            this.denominator = denominator / gcd;
            if (this.denominator < 0) {
                this.numerator = -this.numerator;
                this.denominator = -this.denominator;
            }
        }

        private static int gcd(int a, int b) {
            return b == 0 ? a : gcd(b, a % b);
        }

        public Fraction add(Fraction other) {
            int newNumerator = this.numerator * other.denominator + other.numerator * this.denominator;
            int newDenominator = this.denominator * other.denominator;
            return new Fraction(newNumerator, newDenominator);
        }

        public Fraction subtract(Fraction other) {
            int newNumerator = this.numerator * other.denominator - other.numerator * this.denominator;
            int newDenominator = this.denominator * other.denominator;
            return new Fraction(newNumerator, newDenominator);
        }

        public Fraction multiply(Fraction other) {
            return new Fraction(this.numerator * other.numerator, this.denominator * other.denominator);
        }

        public Fraction divide(Fraction other) {
            if (other.numerator == 0) {
                throw new ArithmeticException("Cannot divide by zero");
            }
            return new Fraction(this.numerator * other.denominator, this.denominator * other.numerator);
        }

        @Override
        public String toString() {
            if (denominator == 1) {
                return String.valueOf(numerator);
            } else if (Math.abs(numerator) > denominator) {
                int wholePart = numerator / denominator;
                int remainder = Math.abs(numerator % denominator);
                return wholePart + "'" + remainder + "/" + denominator;
            } else {
                return numerator + "/" + denominator;
            }
        }

        public boolean isLessThan(Fraction other) {
            return this.numerator * other.denominator < other.numerator * this.denominator;
        }
    }

    static class Expression {
        Fraction value;
        String repr;

        public Expression(Fraction value, String repr) {
            this.value = value;
            this.repr = repr;
        }
    }

    private static Fraction generateNumber(int maxValue) {
        if (random.nextBoolean()) {
            return new Fraction(random.nextInt(maxValue - 1) + 1, 1);
        } else {
            int numerator = random.nextInt(maxValue - 1) + 1;
            int denominator = random.nextInt(maxValue - numerator) + numerator + 1;
            return new Fraction(numerator, denominator);
        }
    }

    private static Expression generateExpression(int maxValue, int remainingOperators) {
        if (remainingOperators == 0 || random.nextDouble() < 0.3) {
            Fraction num = generateNumber(maxValue);
            return new Expression(num, num.toString());
        }

        Expression left = generateExpression(maxValue, remainingOperators - 1);
        Expression right = generateExpression(maxValue, remainingOperators - 1);
        char op = "+-×÷".charAt(random.nextInt(4));

        Fraction result;
        String expr;

        switch (op) {
            case '+':
                result = left.value.add(right.value);
                expr = String.format("(%s + %s)", left.repr, right.repr);
                break;
            case '-':
                if (left.value.isLessThan(right.value)) {
                    Expression temp = left;
                    left = right;
                    right = temp;
                }
                result = left.value.subtract(right.value);
                expr = String.format("(%s - %s)", left.repr, right.repr);
                break;
            case '×':
                result = left.value.multiply(right.value);
                expr = String.format("(%s × %s)", left.repr, right.repr);
                break;
            case '÷':
                if (right.value.numerator == 0 || !left.value.divide(right.value).isLessThan(new Fraction(1, 1))) {
                    return generateExpression(maxValue, remainingOperators);
                }
                result = left.value.divide(right.value);
                expr = String.format("(%s ÷ %s)", left.repr, right.repr);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + op);
        }

        return new Expression(result, expr);
    }

}
