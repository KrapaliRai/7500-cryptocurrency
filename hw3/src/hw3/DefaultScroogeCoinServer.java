package hw3;

import java.security.*;
import java.util.*;

//Scrooge creates coins by adding outputs to a transaction to his public key.
//In ScroogeCoin, Scrooge can create as many coins as he wants.
//No one else can create a coin.
//A user owns a coin if a coin is transfer to him from its current owner
public class DefaultScroogeCoinServer implements ScroogeCoinServer {

    private KeyPair scroogeKeyPair;
    private ArrayList<Transaction> ledger = new ArrayList();

    //Set scrooge's key pair
    @Override
    public synchronized void init(KeyPair scrooge) {

        scroogeKeyPair = new KeyPair(scrooge.getPublic(), scrooge.getPrivate());

    }

    //For every 10 minute epoch, this method is called with an unordered list of proposed transactions
    // 		submitted during this epoch.
    //This method goes through the list, checking each transaction for correctness, and accepts as
    // 		many transactions as it can in a "best-effort" manner, but it does not necessarily return
    // 		the maximum number possible.
    //If the method does not accept an valid transaction, the user must try to submit the transaction
    // 		again during the next epoch.
    //Returns a list of hash pointers to transactions accepted for this epoch
    public synchronized List<HashPointer> epochHandler(List<Transaction> txs) {

        List<HashPointer> hp_List = new ArrayList<HashPointer>();
        while (!txs.isEmpty()) 
        {
            List<Transaction> newTempTxn = new ArrayList<Transaction>();
            for (Transaction tx : txs) 
            {
                //here, checking each transaction for correctness
                if (!isValid(tx))
                {
                    newTempTxn.add(tx);
                } 
                
                else 
                {

                    ledger.add(tx);
                    HashPointer hPointer = new HashPointer(tx.getHash(), ledger.size() - 1);
                    hp_List.add(hPointer);
                    
                }
                
            }
            
            if (txs.size() == newTempTxn.size())
            {
                break;
            }
            txs = newTempTxn;
        }
        return hp_List;
    }

   

    @Override
    public synchronized boolean isValid(Transaction tx) {
        Transaction t = tx;
        switch (t.getType()) {
            //conditions for creating scrooge coins
            
            case Create:
                
                //System should not have any inputs during coin creation
                if (t.numInputs() > 0) 
                {
                    return false;
                }
                
                for (Transaction.Output op : t.getOutputs()) 
                {   
                    // all of txâ€™s output values are positive

                    if (op.getValue() <= 0) 
                    {
                        return false;
                    }
                    //all outputs are given to Scrooge's public key
                    if (op.getPublicKey() != scroogeKeyPair.getPublic()) 
                    {
                        return false;
                    }
                }
                //Scrooge's signature of the transaction is included
                try 
                {
                    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
                    signature.initVerify(scroogeKeyPair.getPublic());
                    signature.update(tx.getRawBytes());
                    
                    if (!signature.verify(t.getSignature())) 
                    {
                        return false;
                    }
                } 
                
                catch (Exception e) 
                {
                    throw new RuntimeException(e);
                }
                
                return true;
                
                    //PayCoin transaction
  
    
    
    
    
                
                //conditions for paying scrooge coins
            case Pay:
                
                Set<UTXO> utxo = getUTXOs();

                double ip = 0;
                  //	(1) all inputs claimed by tx are in the current unspent (i.e. in getUTOXs()),
                for (int i = 0; i < t.numInputs(); i++) 
                {                       
                    //	(2) the signatures on each input of tx are valid,

                    Transaction.Input input = t.getInputs().get(i);
                    int opindex = input.getIndexOfTxOutput();
                    int indexofledger = getLedgerIndex(input.getHashOfOutputTx(), utxo, opindex, input);
                    
                    if (indexofledger == -1)
                        
                    {
                        return false;
                    }
                    //	(3) no UTXO is claimed multiple times by tx,
                    Transaction.Output ipop = ledger.get(indexofledger).getOutput(opindex);
                    ip += ipop.getValue();
                    PublicKey pk = ipop.getPublicKey();
                    
                    try 
                    {
                        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
                        signature.initVerify(pk);
                        signature.update(t.getRawDataToSign(i));
                        if (!signature.verify(input.getSignature())) {
                            return false;
                        }
                    } 
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                
                double opsum = 0;
                for (Transaction.Output op : t.getOutputs()) 
                {   //	(4) all of txâ€™s output values are positive, and
                    if (op.getValue() <= 0) 
                    {
                        return false;
                    }
                    opsum += op.getValue();
                }
                
                if (Math.abs(ip - opsum) < .000001) 
                {
                    return true;
                } 
                
                else 
                {
                    return false;
                }
        }
        return false;

    }

    //	(5) the sum of txâ€™s input values is equal to the sum of its output values;
    private int getLedgerIndex(byte[] hashOfOutputTx, Set<UTXO> utxo, int opindex, Transaction.Input ip) 
    {
        for (int i = 0; i < ledger.size(); i++) 
        {
            if (Arrays.equals(ledger.get(i).getHash(), hashOfOutputTx)) 
            {
                HashPointer iphp = new HashPointer(ip.getHashOfOutputTx(), i);
                UTXO iputxo = new UTXO(iphp, opindex);
                if (utxo.contains(iputxo)) 
                {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public synchronized Set<UTXO> getUTXOs() 
    {
        Set<UTXO> utxo = new HashSet<UTXO>();
        for (int ledgerindex = 0; ledgerindex < ledger.size(); ledgerindex++) 
        {
            Transaction trans = ledger.get(ledgerindex);
            
            switch (trans.getType()) 
            {
                case Create:
                    for (Transaction.Output create : trans.getOutputs()) 
                    {
                        int index = trans.getIndex(create);
                        HashPointer createhp = new HashPointer(trans.getHash(), ledgerindex);
                        UTXO createutxo = new UTXO(createhp, index);
                        utxo.add(createutxo);
                    }
                    
                    break;
                    
                case Pay:

                    for (int i = 0; i < trans.numInputs(); i++) 
                    {
                        Transaction.Input ip = trans.getInputs().get(i);
                        int opindex = ip.getIndexOfTxOutput();
                        HashPointer iphp = new HashPointer(ip.getHashOfOutputTx(), getLedgerIndex(ip.getHashOfOutputTx(), utxo, opindex, ip));

                        UTXO iputxo = new UTXO(iphp, opindex);
                        utxo.remove(iputxo);
                    }
                    for (Transaction.Output op : trans.getOutputs()) 
                    {
                        int index = trans.getIndex(op);
                        HashPointer ophp = new HashPointer(trans.getHash(), ledgerindex);
                        UTXO oputxo = new UTXO(ophp, index);
                        utxo.add(oputxo);
                    }
                    break;
            }

        }
        return utxo;
    }

}
