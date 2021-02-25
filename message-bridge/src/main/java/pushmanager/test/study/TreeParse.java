package pushmanager.test.study;

import java.util.ArrayList;
import java.util.List;

/**
 * 找到表达式中优先级最低的运算符作为树根（注意括号会提升内部的优先级）,并将原表达式分解成左右两个表达式;
 * <p>
 * 分别对左右表达式做步骤1, 左边生成的树为树根的左子树,右边生成的树为树根的右子树；
 * <p>
 * 重复步骤1,2, 直到分解的表达式里没有运算符（只剩下数字）为止；
 * <p>
 * 注意一点：在遇到像本式中a后面的+号和c后面的+的优先级问题，在正常计算时a后面的+会先使用所以他的优先级比c后面的+优先级高。所以能得到上面的二叉树。
 */
public class TreeParse implements ParseExpresstion {

    String rootValue;

    TreeParse leftChild;

    TreeParse rightChild;

    public void setExpresstion(String value) {
//        this.prefixString = null;
//        this.suffixString = null;
        value = value.replaceAll("&+", "&").replaceAll("\\|+", "|").trim();
        if (isExpresstion(value) || (!isExpresstion(value))&&value.startsWith("!")) {
            String[] expresstion = subExpresstion(value);
            if (null != expresstion) {
                rootValue = expresstion[1];
                String s = expresstion[0];
                if (null != s && !"".equals(s.trim())) {
                    leftChild = new TreeParse();
                    leftChild.setExpresstion(s);
                }
                String s1 = expresstion[2];
                if (null != s1 && !"".equals(s1.trim())) {
                    rightChild = new TreeParse();
                    rightChild.setExpresstion(expresstion[2]);
                }
            }
        } else {
            rootValue = value;
        }
    }


    private boolean isExpresstion(String values) {
        return values.contains("*") || values.contains("/") || values.contains("+") || values.contains("-")
            || values.contains("&") || values.contains("|") || values.contains("∪")
            || values.contains("∩") ||values.contains(String.valueOf((char)1520))
            ;
    }


    private String[] subExpresstion(String valueString) {
        String tmpValue;
        String[] arr = new String[3];
        if (valueString.startsWith("(") && valueString.endsWith(")"))
            tmpValue = valueString.substring(1, valueString.length() - 1);
        else if (valueString.startsWith("!(") && valueString.endsWith(")")) {
            arr[1] = "!";
            arr[2] = valueString.substring(1);
            return arr;
        } else if(valueString.startsWith("!") && !isExpresstion(valueString)){
            arr[1] = "!";
            arr[2] = valueString.substring(1);
            return arr;
        }else tmpValue = valueString;
        StringBuilder tem = new StringBuilder();
        int count = 0;
        for (int i = 0; i < tmpValue.length(); i++) {
            String c = String.valueOf(tmpValue.charAt(i));
            if (c.equalsIgnoreCase("(")) {
                tem.append(tmpValue.charAt(i));
                count++;
            } else if (c.equalsIgnoreCase(")")) {
                tem.append(tmpValue.charAt(i));
                count--;
            } else if (
                !c.equalsIgnoreCase("&")
                    && !c.equalsIgnoreCase("|")
                    && !c.equalsIgnoreCase("∩")
                    && !c.equalsIgnoreCase("∪")
                    && !c.equalsIgnoreCase("*")
                    && !c.equalsIgnoreCase("-")
                    && !c.equalsIgnoreCase("+")
                    && !c.equalsIgnoreCase("/")
                    && tmpValue.charAt(i) != 1520
            ) {
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


    public List<String> prefix() {
        ArrayList<String> objects = new ArrayList<>();
        objects.add(rootValue);
        if (leftChild != null) {
            objects.addAll(leftChild.prefix());
        }
        if (rightChild != null) {
            objects.addAll(rightChild.prefix());
        }
        return objects;
    }


    public List<String> suffix() {
        ArrayList<String> objects = new ArrayList<>();
        if (leftChild != null) {
            objects.addAll(leftChild.suffix());
        }
        if (rightChild != null) {
            objects.addAll(rightChild.suffix());
        }
        objects.add(rootValue);
        return objects;
    }

    public List<String> suffix(String operation) {
        setExpresstion(operation);
        return suffix();
    }

    public List<String> prefixString(String operation) {
        setExpresstion(operation);
        return prefix();
    }


    public String toPrefixString() {
        return prefix().toString();
    }

    public String toSuffixString() {
        return suffix().toString();
    }
}
