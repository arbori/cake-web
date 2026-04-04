package com.bank.loan;

import cake.web.CakeWebApplication;

public class LoanApplication {
    public static void main(String[] args) throws Exception {
        CakeWebApplication.run(config -> {
            config.setPort(8080);
        });
    }
}
