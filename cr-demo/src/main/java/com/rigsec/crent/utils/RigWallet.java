package com.rigsec.crent.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.web3j.crypto.*;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.tx.ChainId;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RigWallet {

    private RigWallet(){}

    public static ConcurrentMap<String, String> WalletMap = new ConcurrentHashMap<>();

    /**
     * 解密keystore 得到私钥
     *
     * @param keystore
     * @param password
     */
    public static String decryptWallet(String keystore, String password) {
        String privateKey = null;
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        try {
            WalletFile walletFile = objectMapper.readValue(keystore, WalletFile.class);
            ECKeyPair ecKeyPair = null;
            ecKeyPair = Wallet.decrypt(password, walletFile);
            privateKey = ecKeyPair.getPrivateKey().toString(16);
        } catch (CipherException e) {
            if ("Invalid password provided".equals(e.getMessage())) {
                System.out.println("密码错误");
            }
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /**
     * 签名交易
     */
    public static String signTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to,
                                         BigInteger value, String data, byte chainId, String password, String uAddress) throws IOException, CipherException {
        byte[] signedMessage;
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                to,
                value,
                data);
        String keystore = RigWallet.WalletMap.get(uAddress);
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        WalletFile walletFile = objectMapper.readValue(keystore, WalletFile.class);
        ECKeyPair ecKeyPair = Wallet.decrypt(password, walletFile);
        Credentials credentials = Credentials.create(ecKeyPair);
        if (chainId > ChainId.NONE) {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        } else {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        }

        String hexValue = Numeric.toHexString(signedMessage);
        return hexValue;
    }

}
