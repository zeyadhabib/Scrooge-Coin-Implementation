import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

@SuppressWarnings("FinalizeCalledExplicitly")
public class MySampleCase {

//
//	public static void main(String[] args) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
//
//
//		long start_global = System.currentTimeMillis();
//		KeyPair first_User_keypair = generateNewKeyPair();
//		KeyPair second_User_keypair = generateNewKeyPair();
//		KeyPair third_User_keypair = generateNewKeyPair();
//		KeyPair fourth_User_keypair = generateNewKeyPair();
//		KeyPair fifth_User_keypair = generateNewKeyPair();
//		KeyPair sixth_User_keypair = generateNewKeyPair();
//		KeyPair seventh_User_keypair = generateNewKeyPair();
//		long end_1 = System.currentTimeMillis();
//
//		// Create a new Genesis give first user 25.0 coins
//
//		Block genesis = new Block(null, first_User_keypair.getPublic());
//		genesis.finalize();
//		BlockChain block_chain = new BlockChain(genesis);
//		BlockHandler blockHandler = new BlockHandler(block_chain);
//
//		// user 1 Create transaction_0 inputs -> coin base
//		//							   outputs -> 10.0 coins user 3
//		//							   outputs -> 15.0 coins user 2
//
//		Transaction transaction_0 = new Transaction();
//		transaction_0.addInput(genesis.getCoinbase().getHash(), 0);
//		transaction_0.addOutput(15.0, second_User_keypair.getPublic());
//		transaction_0.addOutput(10.0, third_User_keypair.getPublic());
//
//		// user 1 signs the Input
//
//		byte[] sig = sign(first_User_keypair.getPrivate(), transaction_0.getRawDataToSign(0));
//		transaction_0.addSignature(sig, 0);
//		transaction_0.finalize();
//
//		// user 2 Create transaction_1 inputs -> transaction_0 output 0
//		//							   outputs -> 15.0 coins user 3
//
//		Transaction transaction_1 = new Transaction();
//		transaction_1.addInput(transaction_0.getHash(), 0);
//		transaction_1.addOutput(15.0, third_User_keypair.getPublic());
//
//		// user 2 signs the Input
//
//		byte[] sig_1 = sign(second_User_keypair.getPrivate(), transaction_1.getRawDataToSign(0));
//		transaction_1.addSignature(sig_1, 0);
//		transaction_1.finalize();
//
//		// user 3 Create transaction_2 inputs -> transaction_0 output 1
//		//							   inputs -> transaction_1 output 0
//		//							   outputs -> 25.0 coins user 5
//
//		Transaction transaction_2 = new Transaction();
//		transaction_2.addInput(transaction_0.getHash(), 1);
//		transaction_2.addInput(transaction_1.getHash(), 0);
//		transaction_2.addOutput(25.0, fifth_User_keypair.getPublic());
//
//		// user 3 signs the Inputs
//
//		byte[] sig_2 = sign(third_User_keypair.getPrivate(), transaction_2.getRawDataToSign(0));
//		transaction_2.addSignature(sig_2, 0);
//		byte[] sig_2_2 = sign(third_User_keypair.getPrivate(), transaction_2.getRawDataToSign(1));
//		transaction_2.addSignature(sig_2_2, 1);
//		transaction_2.finalize();
//
//		// Adding transactions to the transaction pool
//		// They are all valid so they should be accepted
//
//
//		// user 4 mines the block
//
//		//blockHandler.createBlock(fourth_User_keypair.getPublic());
//
//
//		// user 5 Create transaction_3 inputs -> transaction_2 output 0
//		//							   outputs -> 5.0 coins user 1
//		//							   outputs -> 5.0 coins user 2
//		//							   outputs -> 5.0 coins user 3
//		//							   outputs -> 5.0 coins user 4
//		//							   outputs -> 5.0 coins user 5
//
//
//		Transaction transaction_3 = new Transaction();
//		transaction_3.addInput(transaction_2.getHash(), 0);
//		transaction_3.addOutput(5.0, first_User_keypair.getPublic());
//		transaction_3.addOutput(5.0, second_User_keypair.getPublic());
//		transaction_3.addOutput(5.0, third_User_keypair.getPublic());
//		transaction_3.addOutput(5.0, fourth_User_keypair.getPublic());
//		transaction_3.addOutput(5.0, fifth_User_keypair.getPublic());
//
//		// user 5 signs the Input
//
//		byte[] sig_3 = sign(fifth_User_keypair.getPrivate(), transaction_3.getRawDataToSign(0));
//		transaction_3.addSignature(sig_3, 0);
//		transaction_3.finalize();
//
//		Transaction transaction_4 = new Transaction();
//		transaction_4.addInput(transaction_3.getHash(), 0);
//		transaction_4.addOutput(5.0, sixth_User_keypair.getPublic());
//
//		byte[] sig_4 = sign(first_User_keypair.getPrivate(), transaction_4.getRawDataToSign(0));
//		transaction_4.addSignature(sig_4, 0);
//		transaction_4.finalize();
//
//		Transaction transaction_5 = new Transaction();
//		transaction_5.addInput(transaction_3.getHash(), 1);
//		transaction_5.addOutput(5.0, sixth_User_keypair.getPublic());
//
//		byte[] sig_5 = sign(second_User_keypair.getPrivate(), transaction_5.getRawDataToSign(0));
//		transaction_5.addSignature(sig_5, 0);
//		transaction_5.finalize();
//
//
//		// transaction 6 and 7 depend on each other so they should be invalid transactions.
//
//		Transaction transaction_6 = new Transaction();
//		Transaction transaction_7 = new Transaction();
//		transaction_6.addInput(transaction_7.getHash(), 0);
//		transaction_7.addInput(transaction_6.getHash(), 0);
//		transaction_6.addOutput(5.0, third_User_keypair.getPublic());
//		transaction_7.addOutput(5.0, fourth_User_keypair.getPublic());
//		byte[] sig_6 = sign(fourth_User_keypair.getPrivate(), transaction_6.getRawDataToSign(0));
//		transaction_6.addSignature(sig_6, 0);
//		byte[] sig_7 = sign(third_User_keypair.getPrivate(), transaction_7.getRawDataToSign(0));
//		transaction_7.addSignature(sig_7, 0);
//		transaction_6.finalize();
//		transaction_7.finalize();
//		transaction_6.getInput(0).prevTxHash = transaction_7.getHash();
//		transaction_7.getInput(0).prevTxHash = transaction_6.getHash();
//
//		block_chain.addTransaction(transaction_0);
//		block_chain.addTransaction(transaction_1);
//		Block b1 = blockHandler.createBlock(first_User_keypair.getPublic());
//		block_chain.addTransaction(transaction_2);
//		block_chain.addTransaction(transaction_3);
//		Block b2 = blockHandler.createBlock(first_User_keypair.getPublic());
//		block_chain.addTransaction(transaction_4);
//		block_chain.addTransaction(transaction_5);
//		long start_create_block = System.currentTimeMillis();
//		Block b3 = blockHandler.createBlock(first_User_keypair.getPublic());
//		long end_create_block = System.currentTimeMillis();
//		block_chain.addTransaction(transaction_6);
//		block_chain.addTransaction(transaction_7);
//		blockHandler.createBlock(first_User_keypair.getPublic());
//
//		long end_global = System.currentTimeMillis();
//		System.out.println("\n" + "Key generation time: " + (end_1 - start_global) + "ms");
//		System.out.println("Create block time: " + (end_create_block - start_create_block) + "ms");
//		System.out.println("Execution time: " + (end_global - end_1) + "ms");
//		System.out.println("Total time: " + (end_global - start_global) + "ms" + "\n");
//
//
//
//		System.out.println("transaction_0: " + transaction_0);
//		System.out.println("transaction_1: " + transaction_1);
//		System.out.println("transaction_2: " + transaction_2);
//		System.out.println("transaction_3: " + transaction_3);
//		System.out.println("transaction_4: " + transaction_4);
//		System.out.println("transaction_5: " + transaction_5);
//		System.out.println("transaction_6: " + transaction_6);
//		System.out.println("transaction_7: " + transaction_7);
//		System.out.println();
//
//		block_chain.print_memory();
//
//
//	}
//
	public static KeyPair generateNewKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		return keyGen.genKeyPair();
	}

	public static byte[] sign(PrivateKey privKey, byte[] message)
			throws NoSuchAlgorithmException, SignatureException,
			InvalidKeyException {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privKey);
		signature.update(message);
		return signature.sign();
	}

}
