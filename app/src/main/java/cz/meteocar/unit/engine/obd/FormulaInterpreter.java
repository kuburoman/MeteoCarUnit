package cz.meteocar.unit.engine.obd;

import android.util.Log;

import java.util.ArrayList;

import cz.meteocar.unit.engine.log.AppLog;

/**
 * Interpretation of OBD formula
 * <p/>
 * Výkon
 * Setup: 1M in 5500ms
 * Interpretation: 1M in 400ms
 * <p/>
 * Performance
 * Supported operations: + - * /
 * Supported operands: A B C D numbers
 *
 * @author Toms, 2014
 */
public class FormulaInterpreter {

    private Node rootNode;
    public int errorCode = STATUS_BRACES_OK;

    public static final int STATUS_BRACES_OK = 0;
    public static final int STATUS_BRACES_MISMATCH = 1;

    FormulaInterpreter(String formula) {
        createTree(formula);
    }


    private void createTree(String expression) {
        if (expression == null) {
            Log.d(AppLog.LOG_TAG_OBD, "Expression NULL!");
            return;
        }

        expression = expression
                .replaceAll(",", ".")
                .replaceAll("\\(", " ( ")
                .replaceAll("\\)", " ) ")
                .replaceAll("\\+", " + ")
                .replaceAll("\\-", " - ")
                .replaceAll("\\*", " * ")
                .replaceAll("\\\\", "/")
                .replaceAll("\\/", " / ");

        String[] splitArrWithSpaces = expression.split(" ");

        ArrayList<String> splitArrClear = new ArrayList<>();
        for (String part : splitArrWithSpaces) {
            if (part.isEmpty()) {
                continue;
            }
            splitArrClear.add(part);
        }

        rootNode = parseExpression(splitArrClear);
    }

    /**
     * Binary parsing , we can split into two
     * halves and the further parse the rest
     *
     * @param expParts part of expression for parsing
     * @return {@link Node}
     */
    private Node parseExpression(ArrayList<String> expParts) {

        // linearized list of nods
        ArrayList<Node> expNodes = new ArrayList<>();

        // Elimination of brackets
        boolean inBraces = false;           // are we in brackets
        boolean justInBraces = false;       // just step in brackets
        boolean justOutOfBraces = false;    // just left brackets
        int bracesOpened = 0;
        int bracesClosed = 0;
        ArrayList<String> inBracesParts = new ArrayList<>();
        for (String part : expParts) {

            if ("(".equals(part)) {
                bracesOpened++;
                justInBraces = (bracesOpened == 1) && (bracesClosed == 0);
                inBraces = true;
            }
            if (")".equals(part)) {
                bracesClosed++;
                justOutOfBraces = bracesOpened == bracesClosed;
            }

            if (inBraces) {

                if (!justInBraces && !justOutOfBraces) {
                    inBracesParts.add(part);
                }

                if (justOutOfBraces) {
                    expNodes.add(parseExpression(inBracesParts));
                    inBracesParts = new ArrayList<>();
                    inBraces = false;
                    bracesOpened = 0;
                    bracesClosed = 0;
                }
            } else {

                Node newNode;
                switch (part) {
                    case "+":
                        newNode = new Plus();
                        break;
                    case "-":
                        newNode = new Minus();
                        break;
                    case "*":
                        newNode = new Multiply();
                        break;
                    case "/":
                        newNode = new Divide();
                        break;
                    case "A":
                        newNode = new A(this);
                        break;
                    case "B":
                        newNode = new B(this);
                        break;
                    case "C":
                        newNode = new C(this);
                        break;
                    case "D":
                        newNode = new D(this);
                        break;
                    default:
                        newNode = new Number(part);
                }
                expNodes.add(newNode);
            }

            // reset of flags
            justInBraces = false;
            justOutOfBraces = false;
        }

        if (inBraces) {
            errorCode = FormulaInterpreter.STATUS_BRACES_MISMATCH;
            Log.e(AppLog.LOG_TAG_OBD, "Braces don't match");
            return null;
        }

        Integer opIndex;

        do {
            opIndex = null;
            for (int i = 0; i < expNodes.size(); i++) {
                if (expNodes.get(i).getCode() == Node.DIVIDE && ((Operator) expNodes.get(i)).isEmpty()) {
                    opIndex = i;
                    break;

                }
            }

            if (opIndex != null) {

                boolean useSpareBefore = false;
                boolean useSpareAfter = false;

                if (opIndex == 0) {
                    useSpareBefore = true;
                }
                if (opIndex == expNodes.size() - 1) {
                    useSpareAfter = true;
                }

                if (!useSpareBefore && expNodes.get(opIndex - 1).isOperator()) {
                    useSpareBefore = ((Operator) expNodes.get(opIndex - 1)).isEmpty();
                }
                if (!useSpareAfter && expNodes.get(opIndex + 1).isOperator()) {
                    useSpareAfter = ((Operator) expNodes.get(opIndex + 1)).isEmpty();

                }

                Node before = useSpareBefore ? new Number("1.0") : expNodes.get(opIndex - 1);
                Node after = useSpareAfter ? new Number("1.0") : expNodes.get(opIndex + 1);
                ((Operator) expNodes.get(opIndex)).child1 = before;
                ((Operator) expNodes.get(opIndex)).child2 = after;

                if (!useSpareBefore) {
                    expNodes.remove(before);
                }
                if (!useSpareAfter) {
                    expNodes.remove(after);
                }
            }
        } while (opIndex != null);

        do {

            opIndex = null;
            for (int i = 0; i < expNodes.size(); i++) {
                if (expNodes.get(i).getCode() == Node.MULTIPLY && ((Operator) expNodes.get(i)).isEmpty()) {
                    opIndex = i;
                    break;
                }
            }

            if (opIndex != null) {

                boolean useSpareBefore = false;
                boolean useSpareAfter = false;

                if (opIndex == 0) {
                    useSpareBefore = true;
                }
                if (opIndex == expNodes.size() - 1) {
                    useSpareAfter = true;
                }

                if (!useSpareBefore && expNodes.get(opIndex - 1).isOperator()) {
                    useSpareBefore = ((Operator) expNodes.get(opIndex - 1)).isEmpty();
                }
                if (!useSpareAfter && expNodes.get(opIndex + 1).isOperator()) {
                    useSpareAfter = ((Operator) expNodes.get(opIndex + 1)).isEmpty();
                }

                Node before = useSpareBefore ? new Number("1.0") : expNodes.get(opIndex - 1);
                Node after = useSpareAfter ? new Number("1.0") : expNodes.get(opIndex + 1);
                ((Operator) expNodes.get(opIndex)).child1 = before;
                ((Operator) expNodes.get(opIndex)).child2 = after;

                if (!useSpareBefore) {
                    expNodes.remove(before);
                }
                if (!useSpareAfter) {
                    expNodes.remove(after);
                }
            }
        } while (opIndex != null);

        do {

            opIndex = null;
            for (int i = 0; i < expNodes.size(); i++) {
                if (expNodes.get(i).getCode() == Node.MINUS) {
                    if (((Operator) expNodes.get(i)).isEmpty()) {
                        opIndex = i;
                        break;
                    }
                }
            }

            if (opIndex != null) {

                boolean useSpareBefore = false;
                boolean useSpareAfter = false;

                // jsou indexy mimo pole?
                if (opIndex == 0) {
                    useSpareBefore = true;
                }
                if (opIndex == expNodes.size() - 1) {
                    useSpareAfter = true;
                }

                if (!useSpareBefore) {
                    if (expNodes.get(opIndex - 1).isOperator()) {
                        useSpareBefore = ((Operator) expNodes.get(opIndex - 1)).isEmpty();
                    }
                }
                if (!useSpareAfter) {
                    if (expNodes.get(opIndex + 1).isOperator()) {
                        useSpareAfter = ((Operator) expNodes.get(opIndex + 1)).isEmpty();
                    }
                }

                Node before = useSpareBefore ? new Number("0.0") : expNodes.get(opIndex - 1);
                Node after = useSpareAfter ? new Number("0.0") : expNodes.get(opIndex + 1);
                ((Operator) expNodes.get(opIndex)).child1 = before;
                ((Operator) expNodes.get(opIndex)).child2 = after;

                if (!useSpareBefore) {
                    expNodes.remove(before);
                }
                if (!useSpareAfter) {
                    expNodes.remove(after);
                }
            }
        } while (opIndex != null);

        do {

            opIndex = null;
            for (int i = 0; i < expNodes.size(); i++) {
                if (expNodes.get(i).getCode() == Node.PLUS) {
                    if (((Operator) expNodes.get(i)).isEmpty()) {
                        opIndex = i;
                        break;
                    }
                }
            }

            if (opIndex != null) {

                boolean useSpareBefore = false;
                boolean useSpareAfter = false;

                if (opIndex == 0) {
                    useSpareBefore = true;
                }
                if (opIndex == expNodes.size() - 1) {
                    useSpareAfter = true;
                }

                if (!useSpareBefore) {
                    if (expNodes.get(opIndex - 1).isOperator()) {
                        useSpareBefore = ((Operator) expNodes.get(opIndex - 1)).isEmpty();
                    }
                }
                if (!useSpareAfter) {
                    if (expNodes.get(opIndex + 1).isOperator()) {
                        useSpareAfter = ((Operator) expNodes.get(opIndex + 1)).isEmpty();
                    }
                }

                Node before = useSpareBefore ? new Number("0.0") : expNodes.get(opIndex - 1);
                Node after = useSpareAfter ? new Number("0.0") : expNodes.get(opIndex + 1);
                ((Operator) expNodes.get(opIndex)).child1 = before;
                ((Operator) expNodes.get(opIndex)).child2 = after;

                if (!useSpareBefore) {
                    expNodes.remove(before);
                }
                if (!useSpareAfter) {
                    expNodes.remove(after);
                }
            }
        } while (opIndex != null);

        return expNodes.get(0);
    }

    /**
     * Flag that represents if syntax is all right.
     */
    private boolean isSyntaxAllRight = true;

    /**
     * Is syntax of last expression all right.
     *
     * @return True - is syntax is ok.
     */
    public boolean isSyntaxOK() {
        return isSyntaxAllRight;
    }

    /**
     * Interprets message consistent of 4 hexadecimal parts  that is split by gap.
     *
     * @param toInterpret Hexadecimal message
     * @return Interpretation of expression
     */
    public double interpretString(String toInterpret) {

        String[] splitArr = toInterpret.split(" ");

        // 3 or more bytes
        if (splitArr.length <= 2) {
            isSyntaxAllRight = false;
            return 0.0;
        } else {
            isSyntaxAllRight = true;
        }

        // parsing of bytes, start with 2, 0 a 1 is echo command
        abcdBytes = new double[4];
        abcdBytes[0] = (double) Integer.parseInt(splitArr[2], 16);
        if (splitArr.length > 3) {
            abcdBytes[1] = (double) Integer.parseInt(splitArr[3], 16);
            if (splitArr.length > 4) {
                abcdBytes[2] = (double) Integer.parseInt(splitArr[4], 16);
                if (splitArr.length > 5) {
                    abcdBytes[3] = (double) Integer.parseInt(splitArr[5], 16);
                }
            }
        }

        return getValue();
    }

    /**
     * Returns value of expression with actual values (ABCD).
     *
     * @return Value of expression
     */
    private double getValue() {
        return rootNode.getValue();
    }

    double[] abcdBytes;

    public double getA() {
        return abcdBytes[0];
    }

    public double getB() {
        return abcdBytes[1];
    }

    public double getC() {
        return abcdBytes[2];
    }

    public double getD() {
        return abcdBytes[3];
    }

    private abstract class Node {
        public static final int PLUS = 0;
        public static final int MINUS = 1;
        public static final int MULTIPLY = 2;
        public static final int DIVIDE = 3;
        public static final int A = 4;
        public static final int B = 5;
        public static final int C = 6;
        public static final int D = 7;
        public static final int NUMBER = 8;

        abstract double getValue();

        abstract int getCode();

        boolean isOperator() {
            return false;
        }
    }

    private abstract class Operator extends Node {
        protected Node child1 = null;
        protected Node child2 = null;

        @Override
        boolean isOperator() {
            return true;
        }

        boolean isEmpty() {
            return (child1 == null) && (child2 == null);
        }
    }

    private class Plus extends Operator {
        @Override
        double getValue() {
            return child1.getValue() + child2.getValue();
        }

        @Override
        int getCode() {
            return Node.PLUS;
        }
    }

    private class Minus extends Operator {
        @Override
        double getValue() {
            return child1.getValue() - child2.getValue();
        }

        @Override
        int getCode() {
            return Node.MINUS;
        }
    }

    private class Multiply extends Operator {
        @Override
        double getValue() {
            return child1.getValue() * child2.getValue();
        }

        @Override
        int getCode() {
            return Node.MULTIPLY;
        }
    }

    private class Divide extends Operator {
        @Override
        double getValue() {
            return child1.getValue() / child2.getValue();
        }

        @Override
        int getCode() {
            return Node.DIVIDE;
        }
    }

    private class A extends Node {
        FormulaInterpreter interpreter;

        A(FormulaInterpreter i) {
            interpreter = i;
        }

        @Override
        double getValue() {
            return interpreter.getA();
        }

        @Override
        int getCode() {
            return Node.A;
        }
    }

    private class B extends Node {
        FormulaInterpreter interpreter;

        B(FormulaInterpreter i) {
            interpreter = i;
        }

        @Override
        double getValue() {
            return interpreter.getB();
        }


        @Override
        int getCode() {
            return Node.B;
        }
    }

    private class C extends Node {
        FormulaInterpreter interpreter;

        C(FormulaInterpreter i) {
            interpreter = i;
        }

        @Override
        double getValue() {
            return interpreter.getC();
        }


        @Override
        int getCode() {
            return Node.C;
        }
    }

    private class D extends Node {
        FormulaInterpreter interpreter;

        D(FormulaInterpreter i) {
            interpreter = i;
        }

        @Override
        double getValue() {
            return interpreter.getD();
        }


        @Override
        int getCode() {
            return Node.D;
        }
    }

    private class Number extends Node {

        private final double value;

        Number(String num) {
            double newValue = 0.0;
            try {
                newValue = Double.parseDouble(num);
            } catch (Exception e) {
                Log.d(AppLog.LOG_TAG_DEFAULT, "Cannot parse number", e);
            }
            value = newValue;
        }

        @Override
        double getValue() {
            return value;
        }

        @Override
        int getCode() {
            return Node.NUMBER;
        }
    }
}
