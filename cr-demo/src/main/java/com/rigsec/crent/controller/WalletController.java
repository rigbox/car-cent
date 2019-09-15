package com.rigsec.crent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rigsec.crent.message.ResultCode;
import com.rigsec.crent.message.ResultResponse;
import com.rigsec.crent.utils.RigWallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.*;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@RestController
public class WalletController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${node.url}")
    private String nodeUrl = "";

    private static Web3j web3j;

    @RequestMapping(value = "/wallet/create", method = RequestMethod.POST)
    public ResultResponse createWallet(@RequestParam String password) {
        try {
            web3j = Web3j.build(new HttpService(nodeUrl));
            WalletFile walletFile;
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            walletFile = Wallet.createStandard(password, ecKeyPair);
            System.out.println("address " + walletFile.getAddress()+","+ecKeyPair.getPrivateKey().toString(16));
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            String jsonStr = objectMapper.writeValueAsString(walletFile);
            ColdWallet w = new ColdWallet();
            String address = walletFile.getAddress();
            if(address.startsWith("0x")) {
                w.setAddress(address);
            } else {
                w.setAddress("0x"+address);
            }
            w.setKeystore(jsonStr);
            //可以修改为持久化存储，最好使用加密机加密后存储
            RigWallet.WalletMap.put(w.getAddress(),w.getKeystore());
            return this.resultSuccess(w);
        } catch (Exception e) {
            log.error("Create Wallet Exception!",e);
            return this.resultFailed(ResultCode.SYS_EXCEPTION);
        }
    }



    public class ColdWallet {
        private String address;
        private String keystore;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getKeystore() {
            return keystore;
        }

        public void setKeystore(String keystore) {
            this.keystore = keystore;
        }
    }
}
