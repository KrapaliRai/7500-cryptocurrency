/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw5;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;


public class test {

    private static final NetworkParameters netParams = MainNetParams.get();
    static double attempt = 0;
    public static int check = 0;

    public static String get(String prefix) {

        ECKey key = new ECKey();

        while (check == 0) {
            String customAddress = key.toAddress(netParams).toString();
            System.out.println(customAddress);
            if (customAddress.charAt(0) == prefix.charAt(0)) {
                {
                    if (customAddress.charAt(1) == prefix.charAt(1)) {
                        if (customAddress.charAt(2) == prefix.charAt(2)) {
                            if (customAddress.charAt(3) == prefix.charAt(3)) {
                                if (customAddress.charAt(4) == prefix.charAt(4)) {
                                    check = 1;
                                    return "Required Key : " + customAddress;
                                }

                            }
                        }
                    }
                }
            }
            key = new ECKey();
        }
        return "Required Key : " + " Not found";
    }

    public static void main(String[] args) {
        System.out.println(test.get("1GANG"));
    }

}
