/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw5;

import org.bitcoinj.params.MainNetParams;


public class WalletInitMain extends WalletInit{
    public static final String NAME = "main";

    public WalletInitMain() {
        super(MainNetParams.get(), NAME);
    }

    public static void main(String[] args) {
        new WalletInitMain();
    }
}
