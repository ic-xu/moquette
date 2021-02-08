package pushmanager.test.study;


public class ListNode {
    int val;
    ListNode next;

    ListNode() {
    }

    ListNode(int val) {
        this.val = val;
    }

    ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }
}

class Solution {

    public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        int tmp = 0;
        int value = l1.val + l2.val + tmp;
        if (value >= 10) {
            value = value - 10;
            tmp = 1;
        }
        ListNode tmpResult = new ListNode(value);
        ListNode result = null;
        l1 = l1.next;
        l2 = l2.next;

        for (; ; ) {
            if (l1 == null && l2 == null && tmp == 0) {
                break;
            }
            value = 0;
            if (l1 != null) {
                value = value + l1.val;
            }
            if (l2 != null) {
                value = value + l2.val;
            }
            value = value + tmp;
            if (value >= 10) {
                value = value - 10;
                tmp = 1;
            } else tmp = 0;

            tmpResult.next = new ListNode(value);
            if (null == result) {
                result = tmpResult;
            }
            tmpResult = tmpResult.next;
            if (l1 != null)
                l1 = l1.next;
            if (l2 != null)
                l2 = l2.next;
        }
        if(null!=result)
            return result;
        else return tmpResult;
    }


    public static void main(String[] args) {
        //[9,9,9,9,9,9,9]
        ListNode l1 = new ListNode(9);
        l1.next = new ListNode(9);
        l1.next.next = new ListNode(9);
        l1.next.next.next = new ListNode(9);
        l1.next.next.next.next = new ListNode(9);
        l1.next.next.next.next.next = new ListNode(9);
        l1.next.next.next.next.next.next = new ListNode(9);

        ListNode l2 = new ListNode(9);
        l2.next = new ListNode(9);
        l2.next.next = new ListNode(9);
        l2.next.next.next = new ListNode(9);

        ListNode listNode = addTwoNumbers(l1, l2);

        System.out.println(listNode);
    }

}
