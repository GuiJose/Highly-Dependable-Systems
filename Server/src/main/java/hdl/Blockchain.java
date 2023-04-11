package hdl;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hdl.messages.SignatureRequest;
import hdl.messages.SignatureResponse;

public class Blockchain {
    private List<Block> blocks;
    public Blockchain(){
        blocks = new ArrayList<>();
    }

    public synchronized void appendBlock(Block block){
        blocks.add(block);
    }

    public synchronized void appendSpecialBlock(){
        if (blocks.size()%4 == 0){
            Block b = new Block(Server.getid(), true, Server.getAccounts());
            appendBlock(b);
        }
    }

    public List<Block> getBlocks(){
        return this.blocks;
    }

    public synchronized byte[] receivedSignatureRequest(SignatureRequest M) throws Exception{
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        if (blocks.get(M.getBlockNumber()-1).getIsSpecial()){
            byte[] bytes = ByteArraysOperations.SerializeObject(blocks.get(M.getBlockNumber()-1).getAccounts()); 
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            if (Arrays.equals(hash, M.getAccountsHash())){
                byte[] signature = ByteArraysOperations.CreateSignature(hash, key);
                return signature;
            }
        }
        return null;
    }

    public synchronized void receivedSignatureResponse(SignatureResponse M){
        if (!blocks.get(M.getBlockNumber()-1).getSignatures().containsKey(M.getServerId())){
            blocks.get(M.getBlockNumber()-1).getSignatures().put(M.getServerId(), M.getSignature());
        }
    }

    public synchronized void printBlockchain(){
        for(Block b : blocks){
            System.out.print("[");
            b.printBlock(b);
            System.out.print("] =====> ");
        }
        System.out.print("\n");
    }
}