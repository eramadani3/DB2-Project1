import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception{
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the Buffer Pool size: ");
        int poolSize = sc.nextInt();
        BufferPool pool = new BufferPool(poolSize);
        sc.nextLine();

        while (true) {
            System.out.println("Enter the command: ");
            String command = sc.nextLine();
            String[] commandArray = command.split(" ",3);
            if(commandArray[0].toUpperCase().equals("GET") && commandArray.length == 2) {
               int recordNumber = Integer.parseInt(commandArray[1]);
               String record = pool.GET(recordNumber);
               System.out.println(record);
            }
            else if(commandArray[0].toUpperCase().equals("PIN")){
                int recordNumber = Integer.parseInt(commandArray[1]);
                String record = commandArray[1];
                System.out.println(pool.PIN(recordNumber));
            } 
            else if(commandArray[0].toUpperCase().equals("UNPIN")) {
                int recordNumber = Integer.parseInt(commandArray[1]);
                String record = commandArray[1];
                System.out.println(pool.UNPIN(recordNumber));
            } else if(commandArray[0].toUpperCase().equals("SET")){
                int recordNumber = Integer.parseInt(commandArray[1]);
                String record = commandArray[2];
                System.out.println(pool.SET(recordNumber, record));
            }
            else if(commandArray[0].toUpperCase().equals("EXIT")){
                System.out.println("Exiting the program");
                pool.clearAll();
                break;
            } else {
                System.out.println("Invalid command");
            }
        }
        sc.close();
    }
}
