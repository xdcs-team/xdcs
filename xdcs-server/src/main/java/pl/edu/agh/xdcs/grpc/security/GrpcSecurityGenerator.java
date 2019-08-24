package pl.edu.agh.xdcs.grpc.security;

import com.google.common.net.InetAddresses;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import pl.edu.agh.xdcs.config.AgentSecurityConfiguration;
import pl.edu.agh.xdcs.config.AllowedHosts;
import pl.edu.agh.xdcs.config.Configured;

import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
public class GrpcSecurityGenerator {
    @Inject
    @Configured
    private AgentSecurityConfiguration agentSecurityConfiguration;

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("No RSA algorithm", e);
        }
    }

    public X509Certificate generateSelfSignedCertificate(KeyPair keys) {
        try {
            X509V3CertificateGenerator gen = new X509V3CertificateGenerator();
            gen.setSerialNumber(BigInteger.ONE);
            gen.setIssuerDN(new X500Principal("CN=XDCS"));
            gen.setNotBefore(Date.from(Instant.now().minus(365, ChronoUnit.DAYS)));
            gen.setNotAfter(Date.from(Instant.now().plus(365, ChronoUnit.DAYS)));
            gen.setSubjectDN(new X500Principal("CN=XDCS"));
            gen.setPublicKey(keys.getPublic());
            gen.setSignatureAlgorithm("SHA512WITHRSA");
            gen.addExtension(X509Extension.subjectKeyIdentifier, false,
                    new SubjectKeyIdentifierStructure(keys.getPublic()));
            gen.addExtension(X509Extension.subjectAlternativeName, false, generateSAN());
            return gen.generate(keys.getPrivate(), "BC");
        } catch (CertificateEncodingException | NoSuchProviderException |
                NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new AssertionError("Unexpected exception while generating server certificate", e);
        }
    }

    private GeneralNames generateSAN() {
        Stream<ASN1Encodable> localNames = Stream.of(
                new GeneralName(GeneralName.iPAddress, "127.0.0.1"),
                new GeneralName(GeneralName.dNSName, "localhost"));

        Stream<ASN1Encodable> configNames = Optional.ofNullable(agentSecurityConfiguration.getAllowedHosts())
                .map(AllowedHosts::getHost)
                .orElse(Collections.emptyList())
                .stream()
                .map(host -> InetAddresses.isInetAddress(host) ?
                        new GeneralName(GeneralName.iPAddress, host) :
                        new GeneralName(GeneralName.dNSName, host));

        return new GeneralNames(new DERSequence(Stream.concat(localNames, configNames)
                .toArray(ASN1Encodable[]::new)));
    }
}
