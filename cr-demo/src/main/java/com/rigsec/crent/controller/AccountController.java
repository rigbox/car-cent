package com.rigsec.crent.controller;

import com.rigsec.crent.message.ResultCode;
import com.rigsec.crent.message.ResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.http.HttpService;

@RestController
public class AccountController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${node.url}")
    private String nodeUrl = "";

    private static Admin admin;

    public ResultResponse createAccount(String password) {
        try {
            admin = Admin.build(new HttpService(nodeUrl));
            NewAccountIdentifier newAccountIdentifier = admin.personalNewAccount(password).send();
            String address = newAccountIdentifier.getAccountId();
            System.out.println("new account address " + address);
            return this.resultSuccess();
        } catch (Exception e) {
            log.error("Create Account Exception!", e);
            return this.resultFailed(ResultCode.SYS_EXCEPTION);
        }
    }

}
