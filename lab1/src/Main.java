import ThreadParts.Core;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Core core = new Core(6060);

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter multi-cast group address: ");
        // read multi-cast group address
        String address = sc.nextLine();
        sc.close();
        // start executing
        core.execute(address);
    }
}
