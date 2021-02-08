package pushmanager.test.study;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class StackParse implements ParseExpresstion {

    private String expresstion;


    @Override
    public void setExpresstion(String values) {
        this.expresstion = values;
    }

    @Override
    public String toPrefixString() {
        Stack<String> operation = new Stack<>();
        List<String> result = new ArrayList<>();
        StringBuilder tmpString = null;
        for (int i = 0; i < expresstion.length(); i++) {
            String value = String.valueOf(expresstion.charAt(i));
            switch (value) {
                case "+":
                    break;

                case "-":
                    break;

                case "*":
                    break;

                case "/":
                    break;

                case "(":
                    if (null != tmpString) {
                        result.add(tmpString.toString());
                        tmpString = null;
                    }
                    operation.push(value);
                    break;

                case ")":
                    break;

                default:
                    if (null == tmpString) {
                        tmpString = new StringBuilder();
                    }
                    tmpString.append(value);
                    break;
            }

        }

        return null;
    }

    @Override
    public String toSuffixString() {
        return null;
    }
}
