package pushmanager.test.study;

public class Main {

    private String ff ;


    static {
        aa = 20;
    }

    private static int aa =10;


    public static void main(String[] args) {
        System.out.println(aa);
        String values = "A+B*(C-(D+F))/E";
        ParseExpresstion tree = new TreeParse();
        tree.setExpresstion(values);

        System.out.println(tree.toSuffixString());
        System.out.println(tree.toPrefixString());
    }

}
