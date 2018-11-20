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
import static org.bitcoinj.script.ScriptOpCodes.OP_1;
import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKMULTISIG;

public class MultiSigTransaction extends ScriptTester {

   private DeterministicKey key1;
   private DeterministicKey key2;
   private DeterministicKey key3;

   public MultiSigTransaction(WalletAppKit kit) {
       super(kit);
       key1 = kit.wallet().freshReceiveKey();
       key2 = kit.wallet().freshReceiveKey();
       key3 = kit.wallet().freshReceiveKey();
   }

   @Override
   public Script createLockingScript() {
       //create a multisig locking script that unlocks when exactly one signature for one of the public keys (key1, key2, key3) are given
       //create a multisig locking script that unlocks when exactly one signature for one of the public keys (key1, key2, key3) are given
       int noOfSignatures=3;//Total number of signatures from the multiple participants
       int noOfSignRequired = 1;//Any 1 sign required for the transactions
       ScriptBuilder builder = new ScriptBuilder();
       //builder.op(OP_1);
       builder.number(noOfSignRequired);
       builder.data(key1.getPubKey());
       builder.data(key2.getPubKey());
       builder.data(key3.getPubKey());
       //builder.op(OP_3);
       builder.number(noOfSignatures);
       builder.op(OP_CHECKMULTISIG);
       return builder.build();
       //return null;
   }

   @Override
   public Script createUnlockingScript(Transaction unsignedTransaction) {
       //create a corresponding unclocking script.
       TransactionSignature txSig1 = sign(unsignedTransaction, key1);
       //TransactionSignature txSig2 = sign(unsignedTransaction, key2);
       //TransactionSignature txSig3 = sign(unsignedTransaction, key3);
       ScriptBuilder builder = new ScriptBuilder();
       builder.op(OP_1);
       builder.data(txSig1.encodeToBitcoin());
       //builder.data(txSig2.encodeToBitcoin());
       return builder.build();
       //return null;
   }

   public static void main(String[] args) throws InsufficientMoneyException, InterruptedException {
       WalletInitTest wit = new WalletInitTest();
       new MultiSigTransaction(wit.getKit()).run();
       wit.monitor();
   }

}