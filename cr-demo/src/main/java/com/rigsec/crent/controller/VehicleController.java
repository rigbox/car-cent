package com.rigsec.crent.controller;


import com.rigsec.crent.message.ResultCode;
import com.rigsec.crent.message.ResultDataTable;
import com.rigsec.crent.message.ResultResponse;
import com.rigsec.crent.utils.RigWallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainId;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
public class VehicleController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${node.url}")
    private String nodeUrl = "";

    private static Web3j web3j;
    private static Admin admin;
    private static String emptyAddress = "0x0000000000000000000000000000000000000000";

    @RequestMapping(value = "/vehicle/create")
    public ResultResponse createVehicle(String mAddress, String password, String cAddress, String vAddress, String vId, String vColor, String vType) {
        if(StringUtils.isEmpty(vAddress)||StringUtils.isEmpty(mAddress)||StringUtils.isEmpty(cAddress)||StringUtils.isEmpty(password)||StringUtils.isEmpty(vId)) {
            log.error("Request params is null!");
            return this.resultFailed(ResultCode.INVALID_PARAM,"Request param is Null!");
        }
        String txHash = null;
        web3j = Web3j.build(new HttpService(nodeUrl));
        admin = Admin.build(new HttpService(nodeUrl));
        try {
            PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(
                    mAddress, password, BigInteger.valueOf(10L)).send();
            if (personalUnlockAccount.accountUnlocked()) {
                String methodName = "addVehicle";
                List<Type> inputParameters = new ArrayList<>();
                List<TypeReference<?>> outputParameters = new ArrayList<>();

                Address tAddress = new Address(vAddress);
                inputParameters.add(tAddress);
                inputParameters.add(new Utf8String(vId));
                inputParameters.add(new Utf8String(vColor));
                inputParameters.add(new Utf8String(vType));

                TypeReference<Bool> typeReference = new TypeReference<Bool>() {
                };
                outputParameters.add(typeReference);

                Function function = new Function(methodName, inputParameters, outputParameters);

                String data = FunctionEncoder.encode(function);

                EthGetTransactionCount ethGetTransactionCount = web3j
                        .ethGetTransactionCount(mAddress, DefaultBlockParameterName.PENDING).sendAsync().get();
                BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                BigInteger gasPrice = Convert.toWei(BigDecimal.valueOf(0), Convert.Unit.GWEI).toBigInteger();
                Transaction transaction = Transaction.createFunctionCallTransaction(mAddress, nonce, gasPrice,
                        BigInteger.valueOf(3000000), cAddress, data);
                EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(transaction).send();
                txHash = ethSendTransaction.getTransactionHash();
                return this.resultSuccess(txHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.resultFailed(ResultCode.SYS_EXCEPTION);
    }

    @RequestMapping(value = "/vehicles")
    public ResultDataTable vehicles(int start, int length, String uAddress, String cAddress) {
        long total = 0;
        List<Vehicle> list = new ArrayList<>();
        if(StringUtils.isEmpty(cAddress)||StringUtils.isEmpty(uAddress)) {
            log.error("Request params is null!");
            return this.resultDataTable(total, list);
        }
        web3j = Web3j.build(new HttpService(nodeUrl));
        Map<String, Vehicle> tMap = new HashMap<>();
        for (int i= start;i<(start + length);i++ ) {
            Vehicle temp = this.getUnusedVehicle(i, uAddress, cAddress);
            if(temp!=null&&!StringUtils.isEmpty(temp.getvId())) {
                if(tMap.get(temp.getvAddress()) == null) {
                    list.add(temp);
                    tMap.put(temp.getvAddress(),temp);
                    total ++;
                }
            } else {
                break;
            }
        }
        tMap.clear();
        return this.resultDataTable(total, list);
    }

    private Vehicle getUnusedVehicle(long index, String fromAddr, String cAddress) {
        String methodName = "getUnusedVehicle";
        Vehicle vehicle = null;
        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new Uint(BigInteger.valueOf(index)));

        List<TypeReference<?>> outputParameters = new ArrayList<>();

        outputParameters.add(new TypeReference<Address>() {});
        outputParameters.add(new TypeReference<Utf8String>() {});
        outputParameters.add(new TypeReference<Utf8String>() {});
        outputParameters.add(new TypeReference<Utf8String>() {});
        outputParameters.add(new TypeReference<Uint>() {});

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, cAddress, data);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            if(results!=null&&results.size()>1) {
                System.out.println(results.get(0).toString());
                vehicle = new Vehicle();
                vehicle.setvAddress(results.get(0).toString());
                vehicle.setvId(results.get(1).toString());
                vehicle.setvColor(results.get(2).toString());
                vehicle.setvType(results.get(3).toString());
                vehicle.setvStatus(results.get(4).toString());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return vehicle;
    }

    private Vehicle getVehicleById(long index, String fromAddr, String cAddress) {
        String methodName = "getVehicleByIndex";
        Vehicle vehicle = null;
        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new Uint(BigInteger.valueOf(index)));

        List<TypeReference<?>> outputParameters = new ArrayList<>();

        outputParameters.add(new TypeReference<Address>() {});
        outputParameters.add(new TypeReference<Utf8String>() {});
        outputParameters.add(new TypeReference<Utf8String>() {});
        outputParameters.add(new TypeReference<Utf8String>() {});

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, cAddress, data);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            if(results!=null&&results.size()>1) {
                System.out.println(results.get(0).toString());
                vehicle = new Vehicle();
                vehicle.setvAddress(results.get(0).toString());
                vehicle.setvId(results.get(1).toString());
                vehicle.setvColor(results.get(2).toString());
                vehicle.setvType(results.get(3).toString());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return vehicle;
    }

    @RequestMapping(value = "/vehicle/book")
    public ResultResponse orderVehicle(String uAddress, String password, String vAddress, String cAddress) {
        if(StringUtils.isEmpty(vAddress)||StringUtils.isEmpty(uAddress)||StringUtils.isEmpty(password)) {
            log.error("Request params is null!");
            return this.resultFailed(ResultCode.INVALID_PARAM,"Request param is Null!");
        }
        String keystore = RigWallet.WalletMap.get(uAddress);
        if(StringUtils.isEmpty(keystore)) {
            log.error("Request params is null!");
            return this.resultFailed(ResultCode.INVALID_PARAM,"Request param is Error!");
        }

        String txHash = null;

        try {
            String methodName = "orderVehicle";
            List<Type> inputParameters = new ArrayList<>();
            List<TypeReference<?>> outputParameters = new ArrayList<>();

            Address tAddress = new Address(vAddress);
            inputParameters.add(tAddress);

            TypeReference<Bool> typeReference = new TypeReference<Bool>() {
            };
            outputParameters.add(typeReference);

            Function function = new Function(methodName, inputParameters, outputParameters);

            String data = FunctionEncoder.encode(function);

            EthGetTransactionCount ethGetTransactionCount = web3j
                    .ethGetTransactionCount(uAddress, DefaultBlockParameterName.PENDING).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            BigInteger gasPrice = Convert.toWei(BigDecimal.valueOf(0), Convert.Unit.GWEI).toBigInteger();
            String signedData = RigWallet.signTransaction(nonce,gasPrice,BigInteger.valueOf(3000000),cAddress, BigInteger.valueOf(0), data, ChainId.NONE, password, uAddress);
            if (signedData != null) {
                EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedData).send();
                System.out.println(ethSendTransaction.getTransactionHash());
                txHash = ethSendTransaction.getTransactionHash();
                return this.resultSuccess(this.getVehicle(uAddress,vAddress,cAddress));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return this.resultFailed(ResultCode.SYS_EXCEPTION,"Book Car Failed!");
        }
        return this.resultFailed(ResultCode.SYS_EXCEPTION,"Book Car Failed!");
    }

    @RequestMapping(value = "/vehicle/cancel")
    public ResultResponse cancelBook(String uAddress, String password, String vAddress, String cAddress) {
        if(StringUtils.isEmpty(vAddress)||StringUtils.isEmpty(uAddress)||StringUtils.isEmpty(password)) {
            log.error("Request params is null!");
            return this.resultFailed(ResultCode.INVALID_PARAM,"Request param is Null!");
        }
        String keystore = RigWallet.WalletMap.get(uAddress);
        if(StringUtils.isEmpty(keystore)) {
            log.error("Request params is null!");
            return this.resultFailed(ResultCode.INVALID_PARAM,"Request param is Error!");
        }

        String txHash = null;

        try {
            String methodName = "cancelVehicle";
            List<Type> inputParameters = new ArrayList<>();
            List<TypeReference<?>> outputParameters = new ArrayList<>();

            Address tAddress = new Address(vAddress);
            inputParameters.add(tAddress);

            TypeReference<Bool> typeReference = new TypeReference<Bool>() {
            };
            outputParameters.add(typeReference);

            Function function = new Function(methodName, inputParameters, outputParameters);

            String data = FunctionEncoder.encode(function);

            EthGetTransactionCount ethGetTransactionCount = web3j
                    .ethGetTransactionCount(uAddress, DefaultBlockParameterName.PENDING).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            BigInteger gasPrice = Convert.toWei(BigDecimal.valueOf(0), Convert.Unit.GWEI).toBigInteger();
            String signedData = RigWallet.signTransaction(nonce,gasPrice,BigInteger.valueOf(3000000),cAddress, BigInteger.valueOf(0), data, ChainId.NONE, password, uAddress);
            if (signedData != null) {
                EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedData).send();
                System.out.println(ethSendTransaction.getTransactionHash());
                txHash = ethSendTransaction.getTransactionHash();
                return this.resultSuccess(txHash);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return this.resultFailed(ResultCode.SYS_EXCEPTION,"Cancel Book Failed!");
        }
        return this.resultFailed(ResultCode.SYS_EXCEPTION,"Cancel Book Failed!");
    }

    @RequestMapping(value = "/vehicle/open")
    public ResultResponse openDoor(String uAddress, String password, String vAddress, String cAddress) {
        if(StringUtils.isEmpty(vAddress)||StringUtils.isEmpty(uAddress)||StringUtils.isEmpty(password)) {
            log.error("Request params is null!");
            return this.resultFailed(ResultCode.INVALID_PARAM,"Request param is Null!");
        }
        String keystore = RigWallet.WalletMap.get(uAddress);
        if(StringUtils.isEmpty(keystore)) {
            log.error("Request params is null!");
            return this.resultFailed(ResultCode.INVALID_PARAM,"Request param is Error!");
        }

        String txHash = null;

        try {
            String methodName = "openVehicle";
            List<Type> inputParameters = new ArrayList<>();
            List<TypeReference<?>> outputParameters = new ArrayList<>();

            Address tAddress = new Address(vAddress);
            inputParameters.add(tAddress);

            TypeReference<Bool> typeReference = new TypeReference<Bool>() {
            };
            outputParameters.add(typeReference);

            Function function = new Function(methodName, inputParameters, outputParameters);

            String data = FunctionEncoder.encode(function);

            EthGetTransactionCount ethGetTransactionCount = web3j
                    .ethGetTransactionCount(uAddress, DefaultBlockParameterName.PENDING).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            BigInteger gasPrice = Convert.toWei(BigDecimal.valueOf(0), Convert.Unit.GWEI).toBigInteger();
            String signedData = RigWallet.signTransaction(nonce,gasPrice,BigInteger.valueOf(3000000),cAddress, BigInteger.valueOf(0), data, ChainId.NONE, password, uAddress);
            if (signedData != null) {
                EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedData).send();
                System.out.println(ethSendTransaction.getTransactionHash());
                txHash = ethSendTransaction.getTransactionHash();
                return this.resultSuccess(this.getVehicle(uAddress,vAddress,cAddress));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return this.resultFailed(ResultCode.SYS_EXCEPTION,"Book Car Failed!");
        }
        return this.resultFailed(ResultCode.SYS_EXCEPTION,"Book Car Failed!");
    }

    @RequestMapping(value = "/vehicle/start")
    public ResultResponse startDoor(String password, String vAddress, String cAddress) {
        if(StringUtils.isEmpty(vAddress)||StringUtils.isEmpty(password)) {
            log.error("Request params is null!");
            return this.resultFailed(ResultCode.INVALID_PARAM,"Request param is Null!");
        }
        String keystore = RigWallet.WalletMap.get(vAddress);
        if(StringUtils.isEmpty(keystore)) {
            log.error("Request params is null!");
            return this.resultFailed(ResultCode.INVALID_PARAM,"Request param is Error!");
        }

        String txHash = null;

        try {
            String methodName = "startVehicle";
            List<Type> inputParameters = new ArrayList<>();
            List<TypeReference<?>> outputParameters = new ArrayList<>();

            Address tAddress = new Address(vAddress);
            inputParameters.add(tAddress);

            TypeReference<Bool> typeReference = new TypeReference<Bool>() {
            };
            outputParameters.add(typeReference);

            Function function = new Function(methodName, inputParameters, outputParameters);

            String data = FunctionEncoder.encode(function);

            EthGetTransactionCount ethGetTransactionCount = web3j
                    .ethGetTransactionCount(vAddress, DefaultBlockParameterName.PENDING).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            BigInteger gasPrice = Convert.toWei(BigDecimal.valueOf(0), Convert.Unit.GWEI).toBigInteger();
            String signedData = RigWallet.signTransaction(nonce,gasPrice,BigInteger.valueOf(3000000),cAddress, BigInteger.valueOf(0), data, ChainId.NONE, password, vAddress);
            if (signedData != null) {
                EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedData).send();
                System.out.println(ethSendTransaction.getTransactionHash());
                txHash = ethSendTransaction.getTransactionHash();
                return this.resultSuccess(txHash);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return this.resultFailed(ResultCode.SYS_EXCEPTION,"Start Car Failed!");
        }
        return this.resultFailed(ResultCode.SYS_EXCEPTION,"Start Car Failed!");
    }

    public Vehicle getVehicle(String uAddress, String vAddress, String cAddress) {
        String methodName = "getVehicle";
        Vehicle vehicle = null;
        List<Type> inputParameters = new ArrayList<>();
        Address tAddress = new Address(vAddress);
        inputParameters.add(tAddress);

        List<TypeReference<?>> outputParameters = new ArrayList<>();

        outputParameters.add(new TypeReference<Utf8String>() {});
        outputParameters.add(new TypeReference<Utf8String>() {});
        outputParameters.add(new TypeReference<Utf8String>() {});
        outputParameters.add(new TypeReference<Uint>() {});

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(uAddress, cAddress, data);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            if(results!=null&&results.size()>1) {
                System.out.println(results.get(0).toString());
                vehicle = new Vehicle();
                vehicle.setvAddress(vAddress);
                vehicle.setvId(results.get(0).toString());
                vehicle.setvColor(results.get(1).toString());
                vehicle.setvType(results.get(2).toString());
                vehicle.setvStatus(results.get(3).toString());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return vehicle;
    }

    public class Vehicle {
        private String vId;
        private String vAddress;
        private String vColor;
        private String vType;
        private String vStatus;

        public String getvId() {
            return vId;
        }

        public void setvId(String vId) {
            this.vId = vId;
        }

        public String getvAddress() {
            return vAddress;
        }

        public void setvAddress(String vAddress) {
            this.vAddress = vAddress;
        }

        public String getvColor() {
            return vColor;
        }

        public void setvColor(String vColor) {
            this.vColor = vColor;
        }

        public String getvType() {
            return vType;
        }

        public void setvType(String vType) {
            this.vType = vType;
        }

        public String getvStatus() {
            return vStatus;
        }

        public void setvStatus(String vStatus) {
            this.vStatus = vStatus;
        }
    }
}
