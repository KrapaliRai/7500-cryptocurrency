/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw5;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import static org.bitcoinj.script.ScriptOpCodes.*;
public class GroupMultiSigTransaction extends ScriptTester {

   private DeterministicKey keyBank;
   private DeterministicKey keyCus1;
   private DeterministicKey keyCus2;
   private DeterministicKey keyCus3;

   public GroupMultiSigTransaction(WalletAppKit kit) {
       super(kit);
       keyBank = kit.wallet().freshReceiveKey();
       keyCus1 = kit.wallet().freshReceiveKey();
       keyCus2 = kit.wallet().freshReceiveKey();
       keyCus3 = kit.wallet().freshReceiveKey();
   }

   @Override
   public Script createLockingScript() {
       ScriptBuilder builder = new ScriptBuilder();
       int noOfSignatures=3;//Total number of signatures from the multiple participants
       int noOfSignRequired = 1;//One Signs required for the transaction.
       builder.data(keyBank.getPubKey());
       builder.op(OP_CHECKSIG);
       builder.op(OP_IF);
       builder.number(noOfSignRequired);
       builder.data(keyCus1.getPubKey());
       builder.data(keyCus2.getPubKey());
       builder.data(keyCus3.getPubKey());
       builder.number(noOfSignatures);
       builder.op(OP_CHECKMULTISIG);
       builder.op(OP_ELSE);
       builder.op(OP_RETURN);
       builder.op(OP_ENDIF);
       return builder.build();
   }

   @Override
   public Script createUnlockingScript(Transaction unsignedTransaction) {
       TransactionSignature txSigBank = sign(unsignedTransaction, keyBank );
       TransactionSignature txSigCus1 = sign(unsignedTransaction, keyCus1);
       //TransactionSignature txSigCus2 = sign(unsignedTransaction, keyCus2);
       ScriptBuilder builder = new ScriptBuilder();
       builder.op(OP_1);
       builder.data(txSigCus1.encodeToBitcoin());
       builder.data(txSigBank.encodeToBitcoin());
       //builder.data(txSigCus2.encodeToBitcoin());
       return builder.build();
   }

   public static void main(String[] args) throws InsufficientMoneyException, InterruptedException {
       WalletInitTest wit = new WalletInitTest();
       new GroupMultiSigTransaction(wit.getKit()).run();
       wit.monitor();
   }

}