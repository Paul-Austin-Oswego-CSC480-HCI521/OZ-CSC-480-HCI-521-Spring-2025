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
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            String[] groups;
            if (user.getInteger("admin") == 1) {
                groups = new String[]{"admin", "user"};
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
