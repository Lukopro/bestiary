package net.luko.bestia.util;

import net.luko.bestia.Bestia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class FormulaParser {
    public static String VAR = "L";

    // Evaluate a formula given a variable var
    public static double evaluate(String formula, double var){
        List<String> tokens = tokenize(formula);
        List<String> rpn = toRPN(tokens);
        return evalRPN(rpn, var);
    }

    // Split formula into tokens
    private static List<String> tokenize(String formula){
        List<String> tokens = new ArrayList<>();
        int i = 0;
        String prevToken = null;

        while(i < formula.length()){
            char c = formula.charAt(i);
            if(Character.isWhitespace(c)){
                i++;
                continue;
            }

            String token = null;

            if("+-*/^()".indexOf(c) >= 0){
                token = "" + c;
                i++;
            } else if(Character.isDigit(c) || c == '.'){
                int start = i;
                while(i < formula.length() && (Character.isDigit(formula.charAt(i)) || formula.charAt(i) == '.')) i++;
                token = formula.substring(start, i);
            } else if(Character.isLetter(c)){
                int start = i;
                while(i < formula.length() && Character.isLetter(formula.charAt(i))) i++;
                token = formula.substring(start, i);
            } else error("Unexpected character: " + c);

            if(prevToken != null){
                boolean prevIsValue = prevToken.matches("\\d+(\\.\\d+)?") || prevToken.matches("[a-zA-Z]+") || prevToken.equals(")");
                boolean currentIsValue = token.matches("\\d+(\\.\\d+)?") || token.matches("[a-zA-Z]+") || token.equals("(");

                if(prevIsValue && currentIsValue) tokens.add("*");
            }

            tokens.add(token);
            prevToken = token;

        }

        return tokens;
    }

    // Convert tokens to Reverse Polish Notation (shunting yard)
    private static List<String> toRPN(List<String> tokens){
        List<String> output = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        Map<String, Integer> precedence = Map.of(
                "+", 1, "-", 1,
                "*", 2, "/", 2,
                "u-", 3,
                "^", 4
        );

        String prev = null;
        for(String token : tokens){
            if(token.matches("\\d+(\\.\\d+)?") || token.equals(VAR)){
                output.add(token);
            } else if(precedence.containsKey(token)){
                // Detect unary minus
                if(token.equals("-") && (prev == null || precedence.containsKey(prev) || prev.equals("("))){
                    token = "u-";
                }
                while(!stack.isEmpty() && precedence.containsKey(stack.peek())
                && shouldPop(token, stack.peek(), precedence)){
                    output.add(stack.pop());
                }
                stack.push(token);
            } else if(token.equals("(")){
                stack.push(token);
            } else if(token.equals(")")){
                while(!stack.isEmpty() && !stack.peek().equals("(")){
                    output.add(stack.pop());
                }
                if(stack.isEmpty()) error("Mismatched parenthesis in formula: " + tokens);

                stack.pop();
            } else error("Unknown token in formula: " + token);

            prev = token;
        }

        while (!stack.isEmpty()) {
            if(stack.peek().equals("(") || stack.peek().equals(")"))
                error("Mismatched parenthesis in formula: " + tokens);

            output.add(stack.pop());
        }

        return output;
    }

    // Check for associativity rules for popping operators
    private static boolean shouldPop(String token, String top, Map<String, Integer> precedence) {
        // ^ and u- are right-associative, others are left-associative
        if(token.equals("^") || token.equals("u-")){
            return precedence.get(token) < precedence.get(top);
        } else{
            return precedence.get(token) <= precedence.get(top);
        }
    }

    // Evaluate RPN
    private static double evalRPN(List<String> rpn, double var){
        Stack<Double> stack = new Stack<>();
        for(String token : rpn){
            if(token.matches("\\d+(\\.\\d+)?")) {
                stack.push(Double.parseDouble(token));
            } else if(token.equals(VAR)){
                stack.push(var);
            } else if(token.equals("u-")){
                if(stack.isEmpty()) error("Invalid expression: unary minus without value");

                double a = stack.pop();
                stack.push(-a);
            } else{
                if(stack.size() < 2) error("Invalid expression: insufficient values for operator " + token);

                double b = stack.pop();
                double a = stack.pop();
                switch(token){
                    case "+": stack.push(a + b); break;
                    case "-": stack.push(a - b); break;
                    case "*": stack.push(a * b); break;
                    case "/": stack.push(a / b); break;
                    case "^": stack.push(Math.pow(a, b)); break;
                    default: error("Unknown operator: " + token);
                }
            }
        }

        if(stack.size() != 1) error("Invalid expression: leftover values in stack -> " + stack);

        return stack.pop();
    }

    // Log error and throw
    private static void error(String e){
        Bestia.LOGGER.error(e);
        throw new RuntimeException(e);
    }
}
