package pl.edu.agh.xdcs.grpc;

import io.grpc.Metadata;
import pl.edu.agh.xdcs.security.Token;
import pl.edu.agh.xdcs.security.TokenIssuer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class DefinedHeaders {
    @Inject
    private TokenIssuer tokenIssuer;

    public Metadata.Key<Token> authorization(){
        return Metadata.Key.of("authorization", new Metadata.AsciiMarshaller<Token>() {
            @Override
            public String toAsciiString(Token value) {
                return value.toString();
            }

            @Override
            public Token parseAsciiString(String serialized) {
                return tokenIssuer.validateToken(serialized, TokenIssuer.TokenType.AGENT).orElse(null);
            }
        });
    }
}
