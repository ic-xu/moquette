package pushmanager.test.study;


public class TreeParse implements ParseExpresstion {

    String rootValue;

    TreeParse leftChild;

    TreeParse rightChild;

    private String prefixString;

    private String suffixString;

    @Override
    public void setExpresstion(String value) {
        this.prefixString = null;
        this.suffixString = null;
        value = value.trim();
        if (isExpresstion(value)) {
            String[] expresstion = subExpresstion(value);
            if (null != expresstion) {
                rootValue = expresstion[1];
                leftChild = new TreeParse();
                leftChild.setExpresstion(expresstion[0]);

                rightChild = new TreeParse();
                rightChild.setExpresstion(expresstion[2]);
            }
        } else {
            rootValue = value;
        }
    }


    private boolean isExpresstion(String values) {
        return values.contains("*") || values.contains("/") || values.contains("+") || values.contains("-");
    }


    private String[] subExpresstion(String valueString) {
        String tmpValue;
        if (valueString.startsWith("(") && valueString.endsWith(")"))
            tmpValue = valueString.substring(1, valueString.length() - 1);
        else tmpValue = valueString;
        StringBuilder tem = new StringBuilder();
        String[] arr = new String[3];
        int count = 0;
        for (int i = 0; i < tmpValue.length(); i++) {
            String c = String.valueOf(tmpValue.charAt(i));
            if (c.equalsIgnoreCase("(")) {
                tem.append(tmpValue.charAt(i));
                count++;
            } else if (c.equalsIgnoreCase(")")) {
                tem.append(tmpValue.charAt(i));
                count--;
            } else if (!c.equalsIgnoreCase("*")
                    && !c.equalsIgnoreCase("-")
                    && !c.equalsIgnoreCase("+")
                    && !c.equalsIgnoreCase("/")) {
                tem.append(tmpValue.charAt(i));
            } else if (count == 0) {
                arr[1] = String.valueOf(tmpValue.charAt(i));
                arr[0] = tem.toString();
                arr[2] = tmpValue.substring(++i);
                return arr;
            } else {
                tem.append(tmpValue.charAt(i));
            }
        }
        return null;
    }


    public String prefixString() {
        if (null != prefixString)
            return prefixString;

        String result = rootValue;
        if (leftChild != null) {
            result = result + leftChild.prefixString();
        }
        if (rightChild != null) {
            result = result + rightChild.prefixString();
        }
        return result;
    }


    public String suffixString() {
        if (suffixString != null) {
            return suffixString;
        }

        String result = "";
        if (leftChild != null) {
            result = result + leftChild.suffixString();
        }
        if (rightChild != null) {
            result = result + rightChild.suffixString();
        }
        return result + rootValue;
    }


    @Override
    public String toPrefixString() {
        return prefixString();
    }

    @Override
    public String toSuffixString() {
        return suffixString();
    }
}
