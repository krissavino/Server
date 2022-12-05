package Server;

import Net.LocalNetManager;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

public class Main
{

    public static void main(String[] args)
    {
        System.out.println("Select ipv4 to listen to:");

        var ipv4Addresses = LocalNetManager.GetLocalInetAddresses();

        ShowIpv4Selections(ipv4Addresses);

        var maxCorrectNumber = ipv4Addresses.size() - 1;
        var inputtedNumber = GetInputtedNumber(maxCorrectNumber);
        var selectedInetAddress = ipv4Addresses.get(inputtedNumber);

        ServerContainer.getServer().start(selectedInetAddress);
    }

    private static void ShowIpv4Selections(ArrayList<InetAddress> localInetAddresses)
    {
        var text = "";
        InetAddress inetAddress = null;

        for (int counter = 0; counter < localInetAddresses.size(); counter++)
        {
            inetAddress = localInetAddresses.get(counter);
            text = String.format("%s) %s",counter, inetAddress.getHostAddress());
            System.out.println(text);
        }
    }

    private static int GetInputtedNumber(int maxCorrectNumber)
    {
        var isSelectionCorrect = false;
        var scanner = new Scanner(System.in);

        while (!isSelectionCorrect) {
            var input = scanner.nextLine();

            if(input.length() != 1)
            {
                printNotCorrection(maxCorrectNumber);
                continue;
            }

            var number = (int)input.toCharArray()[0];
            var maxNumber = maxCorrectNumber + (int)'0';
            var tesxt = (int)'0';

            if((int)'0' <= number &&  number <= maxNumber)
            {
                number = number - (int)'0';
                return number;
            }

            printNotCorrection(maxCorrectNumber);
        }

        return 0;
    }

    private static void printNotCorrection(int maxCorrectNumber)
    {
        var text = String.format("Not correct input, please select from 0 to %s: ",maxCorrectNumber);
        System.out.print(text);
    }
}
