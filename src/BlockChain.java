// The BlockChain class should maintain only limited block nodes to satisfy the functionality.
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("SpellCheckingInspection")
public class BlockChain {

    public static class Tuple {
        public Block block;
        public int block_height;
        public UTXOPool utxo_pool;

        public Tuple(Block block, int block_height, UTXOPool utxoPool) {
            this.block = block;
            this.block_height = block_height;
            this.utxo_pool = utxoPool;
        }

    }

    public static final int CUT_OFF_AGE = 10;
    private HashMap<ByteArrayWrapper, Tuple> blocks_in_memory;
    private TransactionPool txpool;
    private Tuple max_height_tuple;
    /**
     * create an empty blockchain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        blocks_in_memory = new HashMap<>();
        txpool = new TransactionPool();
        max_height_tuple = new Tuple(genesisBlock, 1, get_genesis_UTXO_Pool(genesisBlock));
        blocks_in_memory.put(new ByteArrayWrapper(genesisBlock.getHash()), max_height_tuple);
    }


    public UTXOPool get_genesis_UTXO_Pool(Block genesisBlock) {

        UTXOPool new_utxo_pool = new UTXOPool();
        new_utxo_pool.addUTXO(new UTXO(genesisBlock.getCoinbase().getHash(), 0),
                genesisBlock.getCoinbase().getOutput(0));

        return new_utxo_pool;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return max_height_tuple.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return max_height_tuple.utxo_pool;
    }

    public void UpdateMaxTupple() {
        int max_height = 0;
        Tuple max_height_tup = null;
        for (Map.Entry<ByteArrayWrapper, Tuple> tup : blocks_in_memory.entrySet()) {
            if (tup.getValue().block_height > max_height) {
                max_height = tup.getValue().block_height;
                max_height_tup = tup.getValue();
            }
        }
        this.max_height_tuple = max_height_tup;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return txpool;
    }

    /**
     * Add {@code block} to the blockchain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}, where maxHeight is 
     * the current height of the blockchain.
	 * <p>
	 * Assume the Genesis block is at height 1.
     * For example, you can try creating a new block over the genesis block (i.e. create a block at 
	 * height 2) if the current blockchain height is less than or equal to CUT_OFF_AGE + 1. As soon as
	 * the current blockchain height exceeds CUT_OFF_AGE + 1, you cannot create a new block at height 2.
     * 
     * @return true if block is successfully added
     */

    public boolean addBlock(Block block) {

        if (block.getPrevBlockHash() == null ||
                !blocks_in_memory.containsKey(new ByteArrayWrapper(block.getPrevBlockHash()))) {
            return false;
        }

        int block_height = blocks_in_memory.get(new ByteArrayWrapper(block.getPrevBlockHash())).block_height + 1;
        ArrayList<Transaction> all_txs = block.getTransactions();

        TxHandler handler = new TxHandler(
                blocks_in_memory.get(new ByteArrayWrapper(block.getPrevBlockHash())).utxo_pool
        );
        Transaction[] txs = all_txs.toArray(new Transaction[0]);
        Transaction[] accepted_txs = handler.handleTxs(txs);

        if (accepted_txs.length != txs.length || (block_height < max_height_tuple.block_height - CUT_OFF_AGE)) {
            return false;
        }

        handler.getUTXOPool().addUTXO(
                new UTXO(block.getCoinbase().getHash(), 0), block.getCoinbase().getOutput(0)
        );

        blocks_in_memory.put(
                new ByteArrayWrapper(block.getHash()), new Tuple(block, block_height, handler.getUTXOPool())
        );


        UpdateMaxTupple();
        UpdateTxPool(all_txs);
        UpdateMemory();
        return true;
    }

    public void UpdateTxPool(ArrayList<Transaction> txs) {
        for (Transaction tx : txs) {
            txpool.removeTransaction(tx.getHash());
        }
    }

    public void UpdateMemory() {
        ArrayList<ByteArrayWrapper> to_be_removed = new ArrayList<>();

        for (Map.Entry<ByteArrayWrapper, Tuple> tup : blocks_in_memory.entrySet()) {
            if (tup.getValue().block_height < max_height_tuple.block_height - CUT_OFF_AGE) {
                to_be_removed.add(tup.getKey());
            }
        }
        for (ByteArrayWrapper block_hash : to_be_removed) {
            blocks_in_memory.remove(block_hash);
        }
    }


    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        txpool.addTransaction(tx);
    }
}
