import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class ArithmeticProblemGenerator {
    private static final Random random = new Random();
    private static final int MAX_OPERATORS = 3;

    public static void main(String[] args) {


        if (args.length < 2) {
            printHelp();
            return;
        }

        if (args[0].equals("-n") && args[2].equals("-r")) {
            int count = Integer.parseInt(args[1]);
            int range = Integer.parseInt(args[3]);
            generateProblems(count, range);
        } else if (args[0].equals("-e") && args[2].equals("-a")) {
            String exerciseFile = args[1];
            String answerFile = args[3];
            gradeExercises(exerciseFile, answerFile);
        } else {
            printHelp();
        }
    }


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
    private static void generateProblems(int count, int range) {
        Set<String> uniqueProblems = new HashSet<>();
        List<String> problems = new ArrayList<>();
        List<String> answers = new ArrayList<>();

        while (problems.size() < count) {
            Expression expr = generateExpression(range, MAX_OPERATORS);
            String problem = expr.repr + " = ";

            if (!uniqueProblems.contains(problem)) {
                uniqueProblems.add(problem);
                problems.add(problem);
                answers.add(expr.value.toString());
            }
        }

        writeToFile("Exercises.txt", problems);
        writeToFile("Answers.txt", answers);
    }
    private static void writeToFile(String filename, List<String> lines) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (int i = 0; i < lines.size(); i++) {
                writer.println((i + 1) + ". " + lines.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void gradeExercises(String exerciseFile, String answerFile) {
        List<String> exercises = readFile(exerciseFile);
        List<String> answers = readFile(answerFile);
        List<Integer> correct = new ArrayList<>();
        List<Integer> wrong = new ArrayList<>();

        for (int i = 0; i < exercises.size(); i++) {
            String exercise = exercises.get(i).substring(exercises.get(i).indexOf(" ") + 1);
            String answer = answers.get(i).substring(answers.get(i).indexOf(" ") + 1);

            if (evaluateExpression(exercise).equals(answer)) {
                correct.add(i + 1);
            } else {
                wrong.add(i + 1);
            }
        }

        writeGradeToFile(correct, wrong);
    }

    private static List<String> readFile(String filename) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private static void writeGradeToFile(List<Integer> correct, List<Integer> wrong) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("Grade.txt"))) {
            writer.println("Correct: " + correct.size() + " " + correct);
            writer.println("Wrong: " + wrong.size() + " " + wrong);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static Fraction evaluateExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");
        return evalExpr(expression);
    }

    private static Fraction evalExpr(String expr) {
        if (!expr.contains("(")) {
            return parseFraction(expr);
        }

        int openParenIndex = expr.lastIndexOf('(');
        int closeParenIndex = expr.indexOf(')', openParenIndex);
        String subExpr = expr.substring(openParenIndex + 1, closeParenIndex);

        Fraction result = evalSimpleExpr(subExpr);
        String newExpr = expr.substring(0, openParenIndex) + result + expr.substring(closeParenIndex + 1);
        return evalExpr(newExpr);
    }

    private static Fraction evalSimpleExpr(String expr) {
        String[] tokens = expr.split("(?<=[-+×÷])|(?=[-+×÷])");
        Fraction result = parseFraction(tokens[0]);

        for (int i = 1; i < tokens.length; i += 2) {
            char op = tokens[i].charAt(0);
            Fraction operand = parseFraction(tokens[i + 1]);

            switch (op) {
                case '+':
                    result = result.add(operand);
                    break;
                case '-':
                    result = result.subtract(operand);
                    break;
                case '×':
                    result = result.multiply(operand);
                    break;
                case '÷':
                    result = result.divide(operand);
                    break;
            }
        }

        return result;
    }

    private static Fraction parseFraction(String s) {
        if (s.contains("'")) {
            String[] parts = s.split("'");
            int whole = Integer.parseInt(parts[0]);
            String[] fracParts = parts[1].split("/");
            int num = Integer.parseInt(fracParts[0]);
            int denom = Integer.parseInt(fracParts[1]);
            return new Fraction(whole * denom + num, denom);
        } else if (s.contains("/")) {
            String[] parts = s.split("/");
            return new Fraction(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } else {
            return new Fraction(Integer.parseInt(s), 1);
        }
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("Generate problems: java ArithmeticProblemGenerator -n <count> -r <range>");
        System.out.println("Grade exercises: java ArithmeticProblemGenerator -e <exercisefile>.txt -a <answerfile>.txt");
    }
}

