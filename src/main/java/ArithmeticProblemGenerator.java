public class ArithmeticProblemGenerator {
    class Fraction {
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
}
