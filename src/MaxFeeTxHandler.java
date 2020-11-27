import java.util.*;

public class MaxFeeTxHandler {
    public static class TransactionSorter {

        private Transaction[] possibleTxs;

        private ArrayList<ArrayList<Integer>> adjacency_list;

        private ArrayList<int[]> starting_points;

        TransactionSorter(Transaction[] possibleTxs) {
            this.starting_points = new ArrayList<>();
            for (int i = 0; i < possibleTxs.length; ++i) { starting_points.add(new int[]{i, 0}); }

            this.possibleTxs = possibleTxs;
            adjacency_list = new ArrayList<>(possibleTxs.length);
            for (int i = 0; i < possibleTxs.length; ++i)
                adjacency_list.add(new ArrayList<>());

            for (int i = 0; i < possibleTxs.length; ++i) {
                for (int j = i+1; j < possibleTxs.length; ++j) {
                    Transaction parent = ret_parent(possibleTxs[i], possibleTxs[j]);
                    if (parent != null && parent.equals(possibleTxs[i])) {
                        adjacency_list.get(i).add(j);
                        starting_points.get(j)[1] += 1;
                    } else if (parent != null) {
                        adjacency_list.get(j).add(i);
                        starting_points.get(i)[1] += 1;
                    }
                }
            }

            starting_points.sort((o1, o2) -> -1 * Integer.compare(o1[1], o2[1]));


        }

        private Transaction ret_parent (Transaction tx1, Transaction tx2) {

            for (Transaction.Input input : tx1.getInputs()) {
                if (Arrays.equals(input.prevTxHash, tx2.getHash())) {
                    return tx2;
                }
            }

            for (Transaction.Input input : tx2.getInputs()) {
                if (Arrays.equals(input.prevTxHash, tx1.getHash())) {
                    return tx1;
                }
            }

            return null;
        }

        void DepthFirstSearch(int v, boolean[] visited, Stack<Integer> stack) {
            visited[v] = true;
            Integer i;
            for (Integer integer : adjacency_list.get(v)) {
                i = integer;
                if (!visited[i])
                    DepthFirstSearch(i, visited, stack);
            }
            stack.push(v);
        }

        ArrayList<Transaction> topologicalSort() {
            Stack<Integer> stack = new Stack<>();
            ArrayList<Integer> sorted = new ArrayList<>();

            boolean[] visited = new boolean[possibleTxs.length];
            for (int i = 0; i < possibleTxs.length; ++i)
                visited[i] = false;

            for (int[] starting_point : starting_points)
                if (!visited[starting_point[0]])
                    DepthFirstSearch(starting_point[0], visited, stack);

            while (!stack.empty()) {
                sorted.add(stack.pop());
            }

            ArrayList<Transaction> sorted_txs = new ArrayList<>();
            for (int index : sorted) {
                sorted_txs.add(possibleTxs[index]);
            }

            return sorted_txs;
        }

    }

    public static class TxHandlerHelper {

        //private HashSet<UTXO> Spent_UTXO;
        private UTXOPool utxo_pool;
        private Transaction tx;

        public TxHandlerHelper(UTXOPool utxo_pool) {
            this.utxo_pool = new UTXOPool(utxo_pool);
        }
        public void set_Transaction(Transaction tx) {
            this.tx = new Transaction(tx);
        }
        public UTXOPool getUTXOPool() { return utxo_pool; }


        private boolean check_inputs_UTXOPool() {
            ArrayList<Transaction.Input> inputs = tx.getInputs();

            for (Transaction.Input input : inputs) {

                byte[] prev_tx_hash = input.prevTxHash;
                int spent_UTXO_index = input.outputIndex;

                UTXO spent_UTXO = new UTXO(prev_tx_hash, spent_UTXO_index);

                if (!utxo_pool.contains(spent_UTXO)) {
                    return false;
                }

            }

            return true;
        }

        private boolean check_input_signatures () {
            ArrayList<Transaction.Input> inputs = tx.getInputs();

            for (int index = 0; index < inputs.size(); ++index) {

                Transaction.Output spent_output = utxo_pool.getTxOutput(
                        new UTXO(inputs.get(index).prevTxHash, inputs.get(index).outputIndex)
                );

                if (!Crypto.verifySignature(spent_output.address, tx.getRawDataToSign(index), inputs.get(index).signature)) {
                    return false;
                }

            }

            return true;
        }

        private boolean check_inputs_claimed_once() {
            ArrayList<Transaction.Input> inputs = tx.getInputs();
            HashSet<UTXO> Spent_UTXO_local = new HashSet<>();

            for (Transaction.Input input : inputs) {

                byte[] prev_tx_hash = input.prevTxHash;
                int spent_UTXO_index = input.outputIndex;
                UTXO spent_UTXO = new UTXO(prev_tx_hash, spent_UTXO_index);

                if (Spent_UTXO_local.contains(spent_UTXO)) {
                    return false;
                }
                Spent_UTXO_local.add(spent_UTXO);
            }

            return true;
        }

        private boolean check_outputs_non_negative() {
            ArrayList<Transaction.Output> outputs = tx.getOutputs();

            for (Transaction.Output output : outputs) {
                if (output.value < 0) {
                    return false;
                }
            }

            return true;
        }

        private boolean check_sums_input_output() {
            ArrayList<Transaction.Input> inputs = tx.getInputs();
            ArrayList<Transaction.Output> outputs = tx.getOutputs();
            double input_sum = 0, output_sum = 0;
            for (Transaction.Input input : inputs) {

                Transaction.Output spent_output = utxo_pool.getTxOutput(
                        new UTXO(input.prevTxHash, input.outputIndex)
                );

                if (spent_output.value < 0)
                    return false;

                input_sum += spent_output.value;
            }

            for (Transaction.Output output : outputs) {
                output_sum += output.value;
            }

            return input_sum >= output_sum;
        }

        public boolean check_all(Transaction tx) {
            set_Transaction(tx);
            return check_inputs_UTXOPool() && check_input_signatures() &&
                    check_inputs_claimed_once() && check_outputs_non_negative() && check_sums_input_output();
        }

        public void handle_inputs_outputs(Transaction tx) {
            set_Transaction(tx);
            ArrayList<Transaction.Input> inputs = tx.getInputs();
            ArrayList<Transaction.Output> outputs = tx.getOutputs();

            for (Transaction.Input input : inputs){
                UTXO spent = new UTXO(input.prevTxHash, input.outputIndex);
                this.utxo_pool.removeUTXO(spent);
            }

            for (int index = 0; index < outputs.size(); ++index) {
                this.utxo_pool.addUTXO(new UTXO(tx.getHash(), index), outputs.get(index));
            }
        }
    }

    private MaxFeeTxHandler.TxHandlerHelper txHandlerHelper;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}.
     */
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        txHandlerHelper = new MaxFeeTxHandler.TxHandlerHelper(utxoPool);
    }
    public UTXOPool getUTXOPool() { return txHandlerHelper.getUTXOPool(); }
    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */


    public boolean isValidTx(Transaction tx) {
        return txHandlerHelper.check_all(tx);
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */


    public Transaction[] handleTxs(Transaction[] possibleTxs) {


        MaxFeeTxHandler.TransactionSorter transactionSorter = new MaxFeeTxHandler.TransactionSorter(possibleTxs);
        ArrayList<Transaction> possibleTxs_sorted = transactionSorter.topologicalSort();
        ArrayList<Transaction> out_TXs = new ArrayList<>();

        for (Transaction transaction : possibleTxs_sorted) {
            if (isValidTx(transaction)) {
                out_TXs.add(transaction);
                txHandlerHelper.handle_inputs_outputs(transaction);
            }
        }

        return out_TXs.toArray(new Transaction[0]);
    }

}
