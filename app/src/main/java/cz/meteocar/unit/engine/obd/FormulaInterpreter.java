package cz.meteocar.unit.engine.obd;

import android.util.Log;

import java.util.ArrayList;

import cz.meteocar.unit.engine.log.AppLog;

// TODO - přidat možnost kontroly vzorce, jestli je OK, např. by se vytvořila instance a neznámé nody by házely chybu

/**
 * Interpret OBD vzorců
 * v1.1 - bug fixy, testování
 * v1.0 - základní verze
 * <p/>
 * Výkon
 * Setup (sestavení intepr. stuktury): 1M za 5500ms
 * Interpretace (dosazení dat do striktury a vrácení hodnoty): 1M za 400ms
 * <p/>
 * Vzorce
 * Podporované operátory: + - * /
 * Podporované operandy: A B C D čísla
 * A,B,C,D odpovídají příslišné častí hexadecimální vstupního pole po 8 bitech s klesající významn.
 *
 * @author Toms, 2014
 */
public class FormulaInterpreter {

    private Node rootNode;
    private boolean isOK;
    public int errorCode = STATUS_BRACES_OK;

    public static final int STATUS_BRACES_OK = 0;
    public static final int STATUS_BRACES_MISMATCH = 1;

    FormulaInterpreter(String formula) {
        createTree(formula);
    }


    private void createTree(String expression) {
        isOK = true;    // not guilty until proven otherwise :o)

        // máme výraz?
        if (expression == null) {

            // nemáme, označíme vzorec jako neplatný a zkončíme
            AppLog.i(AppLog.LOG_TAG_OBD, "Expression NULL!");
            isOK = false;
            return;
        } else {

            //
            AppLog.i(AppLog.LOG_TAG_OBD, "Parsing expression: " + expression);
        }

        // předzpracování
        expression = expression.replaceAll(",", ".");       // desetinná čísla
        expression = expression.replaceAll("\\(", " ( ");   // závorky
        expression = expression.replaceAll("\\)", " ) ");   // závorky
        expression = expression.replaceAll("\\+", " + ");
        expression = expression.replaceAll("\\-", " - ");
        expression = expression.replaceAll("\\*", " * ");
        expression = expression.replaceAll("\\\\", "/");    // dělění backsplash
        expression = expression.replaceAll("\\/", " / ");

        // dělění pole
        String[] splitArrWithSpaces = expression.split(" ");

        // čištění prázdných položek
        ArrayList<String> splitArrClear = new ArrayList();
        for (String part : splitArrWithSpaces) {
            if (part.isEmpty()) continue;
            splitArrClear.add(part);
        }

        // rekursivni parsováni
        rootNode = parseExpression(splitArrClear);
    }

    /**
     * Binární parsování, najednou můžeme výraz rozdělit
     * jen do dvou polovin a ty dále parsovat
     *
     * @param expParts
     * @return
     */
    private Node parseExpression(ArrayList<String> expParts) {

        // debug
        /*System.out.print("parseE:");
        for (String expPart : expParts) {
            System.out.print(" "+expPart+" |");
        }
        System.out.print("< end");System.out.println("");*/

        // linearizovaný seznam nodů
        ArrayList<Node> expNodes = new ArrayList();

        // eliminace závorek
        boolean inBraces = false;           // jsme v závorkách?
        boolean justInBraces = false;       // právě jsme vstoupili do závorek
        boolean justOutOfBraces = false;    // právě jsme opustili závorky
        int bracesOpened = 0;
        int bracesClosed = 0;
        ArrayList<String> inBracesParts = new ArrayList();
        for (String part : expParts) {

            // počítání otevřených a uzavřených závorek, nastavování flagů
            if (part.equals("(")) {
                bracesOpened++;
                justInBraces = (bracesOpened == 1) && (bracesClosed == 0);
                inBraces = true;
            }
            if (part.equals(")")) {
                bracesClosed++;
                justOutOfBraces = (bracesOpened == bracesClosed);
            }

            // v závorkách
            if (inBraces) {

                // přidávání částí do seznam
                if (!justInBraces && !justOutOfBraces) {
                    inBracesParts.add(part);
                }

                // ukončení závorky
                if (justOutOfBraces) {
                    expNodes.add(parseExpression(inBracesParts));
                    inBracesParts = new ArrayList();
                    inBraces = false;
                    bracesOpened = 0;
                    bracesClosed = 0;
                }
            } else {

                // mimo závorky přidáme rovnou node udělaný z výrazu
                Node newNode = null;
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

            // reset flagů
            justInBraces = false;
            justOutOfBraces = false;
        }

        //  zachycení chyby, neodpovídající počet závorek
        if (inBraces) {
            isOK = false;
            errorCode = FormulaInterpreter.STATUS_BRACES_MISMATCH;
            Log.e(AppLog.LOG_TAG_OBD, "Braces don't match");
            return null;
        }

        // proměnné pro odtranění operátorů
        Integer opIndex;

        // odstranění operátoru
        //<editor-fold defaultstate="collapsed" desc="DĚLENO">        
        do {

            // nalezne operátor v listu
            opIndex = null;
            for (int i = 0; i < expNodes.size(); i++) {
                if (expNodes.get(i).getCode() == Node.DIVIDE && ((Operator) expNodes.get(i)).isEmpty()) {
                    opIndex = i;
                    break;

                }
            }

            // řešení nalezeného operátoru
            if (opIndex != null) {

                // mají se použít náhradní nody?
                boolean useSpareBefore = false;
                boolean useSpareAfter = false;

                // jsou indexy mimo pole?
                if (opIndex == 0) {
                    useSpareBefore = true;
                }
                if (opIndex == expNodes.size() - 1) {
                    useSpareAfter = true;
                }

                // ověří zda operandy nejsou zároveň operátory
                // a pokud ano, že jsou již naplněny
                if (!useSpareBefore && expNodes.get(opIndex - 1).isOperator()) {
                    useSpareBefore = ((Operator) expNodes.get(opIndex - 1)).isEmpty();
                }
                if (!useSpareAfter && expNodes.get(opIndex + 1).isOperator()) {
                    useSpareAfter = ((Operator) expNodes.get(opIndex + 1)).isEmpty();

                }

                // nastavení operandů
                Node before = useSpareBefore ? new Number("1.0") : expNodes.get(opIndex - 1);
                Node after = useSpareAfter ? new Number("1.0") : expNodes.get(opIndex + 1);
                ((Operator) expNodes.get(opIndex)).child1 = before;
                ((Operator) expNodes.get(opIndex)).child2 = after;

                // odstranění původních operandů z listu
                if (!useSpareBefore) {
                    expNodes.remove(before);
                }
                if (!useSpareAfter) {
                    expNodes.remove(after);
                }
            }
        } while (opIndex != null);

        // odstranění operátoru
        do {

            // nalezne operátor v listu
            opIndex = null;
            for (int i = 0; i < expNodes.size(); i++) {
                if (expNodes.get(i).getCode() == Node.MULTIPLY && ((Operator) expNodes.get(i)).isEmpty()) {
                    opIndex = i;
                    break;
                }
            }

            // řešení nalezeného operátoru
            if (opIndex != null) {

                // mají se použít náhradní nody?
                boolean useSpareBefore = false;
                boolean useSpareAfter = false;

                // jsou indexy mimo pole?
                if (opIndex == 0) {
                    useSpareBefore = true;
                }
                if (opIndex == expNodes.size() - 1) {
                    useSpareAfter = true;
                }

                // ověří zda operandy nejsou zároveň operátory
                // a pokud ano, že jsou již naplněny
                if (!useSpareBefore && expNodes.get(opIndex - 1).isOperator()) {
                    useSpareBefore = ((Operator) expNodes.get(opIndex - 1)).isEmpty();
                }
                if (!useSpareAfter && expNodes.get(opIndex + 1).isOperator()) {
                    useSpareAfter = ((Operator) expNodes.get(opIndex + 1)).isEmpty();
                }

                // nastavení operandů
                Node before = useSpareBefore ? new Number("1.0") : expNodes.get(opIndex - 1);
                Node after = useSpareAfter ? new Number("1.0") : expNodes.get(opIndex + 1);
                ((Operator) expNodes.get(opIndex)).child1 = before;
                ((Operator) expNodes.get(opIndex)).child2 = after;

                // odstranění původních operandů z listu
                if (!useSpareBefore) {
                    expNodes.remove(before);
                }
                if (!useSpareAfter) {
                    expNodes.remove(after);
                }
            }
        } while (opIndex != null);


        // odstranění operátoru
        //<editor-fold defaultstate="collapsed" desc="MÍNUS">
        do {

            // nalezne operátor v listu
            opIndex = null;
            for (int i = 0; i < expNodes.size(); i++) {
                if (expNodes.get(i).getCode() == Node.MINUS) {
                    if (((Operator) expNodes.get(i)).isEmpty()) {
                        opIndex = i;
                        break;
                    }
                }
            }

            // řešení nalezeného operátoru
            if (opIndex != null) {
                //System.out.println("Exp contains -");

                // mají se použít náhradní nody?
                boolean useSpareBefore = false;
                boolean useSpareAfter = false;

                // jsou indexy mimo pole?
                if (opIndex == 0) {
                    useSpareBefore = true;
                }
                if (opIndex == expNodes.size() - 1) {
                    useSpareAfter = true;
                }

                // ověří zda operandy nejsou zároveň operátory
                // a pokud ano, že jsou již naplněny
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

                // nastavení operandů
                Node before = useSpareBefore ? new Number("0.0") : expNodes.get(opIndex - 1);
                Node after = useSpareAfter ? new Number("0.0") : expNodes.get(opIndex + 1);
                ((Operator) expNodes.get(opIndex)).child1 = before;
                ((Operator) expNodes.get(opIndex)).child2 = after;

                // odstranění původních operandů z listu
                if (!useSpareBefore) {
                    expNodes.remove(before);
                }
                if (!useSpareAfter) {
                    expNodes.remove(after);
                }
            }
        } while (opIndex != null);
        //</editor-fold>

        // odstranění operátoru
        //<editor-fold defaultstate="collapsed" desc="PLUS">
        do {

            // nalezne operátor v listu
            opIndex = null;
            for (int i = 0; i < expNodes.size(); i++) {
                if (expNodes.get(i).getCode() == Node.PLUS) {
                    if (((Operator) expNodes.get(i)).isEmpty()) {
                        opIndex = i;
                        break;
                    }
                }
            }

            // řešení nalezeného operátoru
            if (opIndex != null) {
                //System.out.println("Exp contains +");

                // mají se použít náhradní nody?
                boolean useSpareBefore = false;
                boolean useSpareAfter = false;

                // jsou indexy mimo pole?
                if (opIndex == 0) {
                    useSpareBefore = true;
                }
                if (opIndex == expNodes.size() - 1) {
                    useSpareAfter = true;
                }

                // ověří zda operandy nejsou zároveň operátory
                // a pokud ano, že jsou již naplněny
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

                // nastavení operandů
                Node before = useSpareBefore ? new Number("0.0") : expNodes.get(opIndex - 1);
                Node after = useSpareAfter ? new Number("0.0") : expNodes.get(opIndex + 1);
                ((Operator) expNodes.get(opIndex)).child1 = before;
                ((Operator) expNodes.get(opIndex)).child2 = after;

                // odstranění původních operandů z listu
                if (!useSpareBefore) {
                    expNodes.remove(before);
                }
                if (!useSpareAfter) {
                    expNodes.remove(after);
                }
            }
        } while (opIndex != null);
        //</editor-fold>

        // vrátí jediný zbylý node (root) výrazu
        return expNodes.get(0);
    }

    /**
     * Flag označující zda je syntaxe poslední interpretované hodnoty v pořádku
     */
    private boolean isSyntaxAllRight = true;

    /**
     * Je poslední interpretovaná hodnota v pořádku co se týče syntaxe?
     *
     * @return True - pokud je v pořádku, False - pokud ne
     */
    public boolean isSyntaxOK() {
        return isSyntaxAllRight;
    }

    public void traceTree() {
        AppLog.log("RootTrace: " + traceTree(rootNode, true));
    }

    private String traceTree(Node n) {
        return traceTree(n, false);
    }

    private String traceTree(Node n, boolean getVal) {

        boolean opFull = false;
        boolean isOp = false;
        if (n.isOperator()) {
            isOp = true;
            if (!((Operator) n).isEmpty()) {
                opFull = true;
            }
        }

        String ret = "";
        if (isOp) {
            if (opFull) {
                ret = "•";
            } else {
                ret = "◦";
            }
        }
        ret += "Node " + (new String[]{"+", "-", "*", "/", "A", "B", "C", "D", "#"})[n.getCode()];

        if (getVal) {
            ret += "(" + n.getValue() + ")";
        }

        if (opFull) {
            ret += "[" + traceTree(((Operator) n).child1, getVal) + "]";
            ret += "[" + traceTree(((Operator) n).child2, getVal) + "]";
        }

        return ret;
    }

    /**
     * Interpretuje textovou zprávu tvořenou čtyřmi hexadecimálními částmi, jež jsou oddělené
     * mezerou, každá po dvou znacích 0-F
     *
     * @param toInterpret Hexadecimální zpráva
     * @return Číselná interpretace dle vzorce
     */
    public double interpretString(String toInterpret) {

        // rozdělíme zprávu po mezerách a ověříme počet částí
        String[] splitArr = toInterpret.split(" ");

        // 3 a více bytů
        if (splitArr.length <= 2) {
            isSyntaxAllRight = false;
            return 0.0;
        } else {
            isSyntaxAllRight = true;
        }

        // parsujeme byty a castujeme na double
        // - začínáme od indexu 2, 0 a 1 je echo příkazu
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

        // vracíme hodnotu zjištěnou interpr. stromem
        return getValue();
    }

    /**
     * Vrátí hodnotu výrazu s aktuálními parametry (ABCD)
     *
     * @return Číselná hodnota
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
    } // TODO - 41 0C na začátku se nepočají, upravit

    public double getD() {
        return abcdBytes[3];
    } // TODO - A je tak až třetí byte v pořadí :(

    //<editor-fold defaultstate="collapsed" desc="Basic">
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
        public Node child1 = null;
        public Node child2 = null;

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
    //</editor-fold>

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
