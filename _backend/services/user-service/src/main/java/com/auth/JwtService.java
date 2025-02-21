package com.auth;

import com.accounts.AccountService;
import com.ibm.websphere.security.jwt.*;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;


public class JwtService {

    public static String buildJwt(String email) {
        try {
            AccountService accountService = new AccountService();
            Document user = accountService.accountCollection.find(eq("email", email)).first();

            String[] groups;
            if (user == null) {
                groups = new String[]{"user"};
            } else if (user.getInteger("admin") == 1) {
                    groups = new String[]{"admin"};
            } else {
                groups = new String[]{"user"};
            }

            return JwtBuilder.create("jwtFrontEndBuilder")
                    .claim(Claims.SUBJECT, email)
                    .claim("upn", email)
                    .claim("groups", groups)
                    .claim("aud", "frontend")
                    .buildJwt()
                    .compact();
        } catch (JwtException | InvalidClaimException | InvalidBuilderException e) {
            throw new RuntimeException(e);
        }
    }

}
