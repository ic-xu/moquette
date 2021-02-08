package pushmanager.test.study;

import java.util.Arrays;

public class LengthOfLongestSubstring {


    public static int lengthOfLongestSubstring(String s) {
        int[] container = new int[128];
        Arrays.fill(container,-1);
        int slow = 0;
        int max = 0;
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {

            if (-1 <  container[chars[i]]) {
                slow = Math.max(slow,  container[chars[i]] + 1);
            }
            container[chars[i]] = i;
            max = Math.max(max, i - slow + 1);
        }
        return max;
    }

    public static int lengthOfLongestSubstringMethod1(String s) {
        //max表示最长无重复子串的长度，t表示上一次无重复字符的下标，开始时从下标为0的字符开始
        int max = 0, t = 0;
        //str[]的下标为字符时表示该字符的ASCII值,标准ASCII字符总共的编码有128个
        int[] str = new int[128];
        //i表示正在访问的字符在串中的位置的下标（s.charAt(i)表示正在访问的字符）
        for (int i = 0; i < s.length(); i++) {
            //如果不等于0表示前边已存在该字符，如过该字符的在串中的位置大于t表示该字符在正在判断的子串中
            if (str[s.charAt(i)] != 0 && str[s.charAt(i)] > t) {
                //更新max的值
                //i-t表示：当前无重复子串的长度（第i个下标表示的字符与第t个下标表示的字符相同）
                max = Math.max(max, i - t);
                //更新t的值为str[s.charAt(i)]的值,即t的值为上一次无重复元素的位置+1
                t = str[s.charAt(i)];
            } else str[s.charAt(i)] = i + 1;//上一次无重复字符在串中的位置
        }
        return Math.max(max, s.length() - t);
    }


    public static void main(String[] args) {
        int i = lengthOfLongestSubstring(" ");
        System.out.println(i);

    }

}

