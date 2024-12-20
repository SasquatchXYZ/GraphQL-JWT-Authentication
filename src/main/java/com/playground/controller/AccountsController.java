package com.playground.controller;

import com.playground.config.JwtUtils;
import com.playground.domain.AuthPayload;
import com.playground.domain.BankAccount;
import com.playground.domain.Client;
import com.playground.domain.User;
import com.playground.service.BankService;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
public class AccountsController {
    @Autowired
    BankService bankService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.systemUser.login.user}")
    private String systemUser;

    @Value("${app.systemUser.login.password}")
    private String systemPassword;

    @Autowired
    private JwtUtils jwtUtils;

    @QueryMapping
    List<BankAccount> accounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Is User Authenticated : {}", authentication.isAuthenticated());

        return bankService.getAccounts();
    }

    @QueryMapping(name = "login")
    public AuthPayload loginQuery(@Argument String email, @Argument String password) {
        /* No authentication required as this is a public query */
        if (systemUser.equals(email) && systemPassword.equals(password)) {
            return new AuthPayload(jwtUtils.generateJWTToken(), new User("Login User", email, email));
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    @SchemaMapping(typeName = "BankAccount", field = "client")
    Client getClient(BankAccount account) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Getting Client for Account {} for User: {}", account.id(), authentication.getName());
        return bankService.getClientByAccountId(account.id());
    }

    @GraphQlExceptionHandler
    public GraphQLError handle(@NonNull Exception ex, @NonNull DataFetchingEnvironment environment) {
        return GraphQLError
                .newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .path(environment.getExecutionStepInfo().getPath())
                .location(environment.getField().getSourceLocation())
                .build();
    }
}
