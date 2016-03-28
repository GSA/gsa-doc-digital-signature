package com.gsa.signingtool.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import static com.gsa.signingtool.app.Gui.dateFormat;
import static com.gsa.signingtool.app.Gui.logger;
import sun.security.pkcs11.SunPKCS11;


public class SigningTool {

    //public static boolean errors = false;
    protected String errorDesc = null;
    protected String filenameDest = null;
    protected String readerStatus = null;
    protected String cardStatus = null;
    protected String fileStatus = null;
    protected long slotID = 0;
    protected X509Certificate userCert = null;
    private PrivateKey privateKey = null;
    private Provider sun = null;
    private Provider bc = null;
    protected ArrayList<Long> readers = null;
    private KeyStore piv = null;
    protected byte[] fileBytes = null;
    protected StringBuffer sb = new StringBuffer();
    protected ArrayList<Certificate> signingCerts = new ArrayList<Certificate>();
    protected static boolean expired = false;
    
    protected SigningTool(File file, char[] pin) {
        if (file != null) {
            logger.info("Converting selected file to bytes");
            fileBytes = getFileContentBytes(file);
        } else {
            logger.severe("File selected is empty");
            Gui.status.append(dateFormat.format(new Date()) 
                    + " - File is empty.\n");
            return;
        }
        try {
            if (sun != null) {
                logger.info("Sun Provider is not null. Logging out of card");
                ((SunPKCS11) sun).logout();
            }
            logger.info("Initiating communication with reader");
            Gui.progress.setValue(15);
            Gui.status.append(dateFormat.format(new Date()) 
                    + " - Detecting card reader.\n");
            readers = SelectSlot.selectCardTerminal();

            logger.info("Selecting the first card reader slot found in ArrayList");
            if (readers.isEmpty()) {
                logger.severe("Issue selecting card reader and verifying card is inserted.");
                Gui.status.append(dateFormat.format(new Date()) 
                        + " - Issue detecting card reader and/or if card is inserted.\n");
                Gui.errors = true;
                Gui.progress.setValue(100);
                return;
            } else {
                slotID = readers.get(0);
            }
            
            String libFile = "\"" + new File("").getAbsolutePath() 
                    + "/lib/opensc-pkcs11.so";
            libFile = libFile.replace("\\", "/");
            logger.info(libFile);
            String configName = "name=" 
                    + "opensc" 
                    + "\n" 
                    + "library=" 
                    + libFile 
                    + "\n" 
                    + "slot=" 
                    + slotID;
            logger.info("Output of information needed to connect SunPKCS11 Provider.\n" 
                    + configName);
            byte[] pkcs11configBytes = configName.getBytes();
            ByteArrayInputStream confStream = new ByteArrayInputStream(pkcs11configBytes);
            bc = new org.bouncycastle.jce.provider.BouncyCastleProvider();
            logger.info("Adding a Bouncy Castle provider");
            Security.addProvider(bc);
            logger.info("Trying to add SunPKCS11 provider");
            sun = new sun.security.pkcs11.SunPKCS11(confStream);
            Security.addProvider(sun);
            try {
                //KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(pin);
                piv = KeyStore.getInstance("PKCS11", sun);
               
                logger.info("Trying to connect to smart card using SunPKCS11 and user PIN");
                
                piv.load(null, pin);
                
                Gui.progress.setValue(60);
                Gui.status.append(dateFormat.format(new Date()) 
                        + " - Validating PIN number.\n");
            } catch (Exception ex) {
                logger.log(Level.FINEST, ex.getMessage(), ex);
                ((SunPKCS11) sun).logout();
                Security.removeProvider(sun.getName());
                Security.removeProvider(bc.getName());
                Gui.status.append(dateFormat.format(new Date()) 
                        + " - Unable to validate PIN. Verify PIN is correct.\n");
                Gui.progress.setValue(100);
                Gui.errors = true;
                return;
            }
            
            Gui.logger.info("Looping through certificates on card");
            Enumeration aliasesEnum = piv.aliases();
                while (aliasesEnum.hasMoreElements()) {
                   boolean[] keyUsage = null;
                   String alias = (String)aliasesEnum.nextElement();
                   Certificate[] certificationChain = piv.getCertificateChain(alias);
                   
                   for (X509Certificate certificate : (X509Certificate[]) certificationChain) {
                       Gui.logger.finest(certificate.getSubjectDN().toString());
                   }
                   Certificate cert = piv.getCertificate(alias);
                   X509Certificate x509 = (X509Certificate)cert;
                   keyUsage = x509.getKeyUsage();
                   if (keyUsage[0] && keyUsage[1]) {
                       logger.info("Found a signing certificate with Digital Signature & Non-repudiation!");
                       logger.info("Serial Number of Signing Certificate: " 
                               + x509.getSerialNumber());
                        privateKey = (PrivateKey) piv.getKey(alias, null);
                        Gui.notAfterDate = x509.getNotAfter().toString();
                        Gui.serialNum = x509.getSerialNumber().toString();
                        signingCerts.add(x509);
                        Gui.progress.setValue(70);
                        Gui.status.append(dateFormat.format(new Date()) 
                                + " - Found a signing certificate.\n");
                   } 
                } 

            if (signingCerts.size()==1) {  
                logger.info("Found only 1 signing certificate");
                byte[] envelopedBytes = null;
                userCert = (X509Certificate)signingCerts.get(0);
                X509Certificate issuerCert = null;
                String revokeStatus = null;

                try {
                    logger.info("Checking the expiration date of the signing certificate");
                    userCert.checkValidity();
                    Gui.progress.setValue(80);
                    Gui.status.append(dateFormat.format(new Date()) 
                            + " - Checking expiration date of signing certificate.\n");
                } catch (Exception exc) {
                    Gui.logger.log(Level.FINEST, exc.getMessage(), exc);
                    Gui.errors = true;
                    expired = true;
                    Gui.status.append(dateFormat.format(new Date()) 
                            + " - Unable to sign. The signing certificate is expired.\n" 
                            + dateFormat.format(new Date()) 
                            + " - Expires: " 
                            + userCert.getNotAfter() 
                            + "\n"); 
                    ((SunPKCS11) sun).logout();
                    Security.removeProvider(sun.getName());
                    Security.removeProvider(bc.getName());
                    Gui.progress.setValue(100);
                    return;
                }
                if (Gui.revocationCheckBox.getState()) {
                    try {
                        logger.info("Checking Revocation Status of signing certificate...");
                        issuerCert = CheckRevocationStatus.fetchIssuerCert(userCert);
                        revokeStatus = CheckRevocationStatus.checkRevocation(userCert, issuerCert);
                        
                        Gui.revocationStatus = revokeStatus;  
                        if (revokeStatus == "REVOKED") {
                            Gui.status.append(dateFormat.format(new Date()) 
                                    + " - Unable to sign. Signing Certificate is REVOKED");
                        }
                    } catch (Exception e) {
                        Gui.status.append(dateFormat.format(new Date()) 
                                + " - Signing Certificate Revocation Status: " 
                                + revokeStatus + "\n");
                        logger.log(Level.FINEST, e.getMessage(), e);
                        
    //                    ((SunPKCS11) sun).logout();
    //                    Security.removeProvider(sun.getName());
    //                    Security.removeProvider(bc.getName());   
                    }
                }
                Store signers = new JcaCertStore(signingCerts);
                CMSTypedData msg = new CMSProcessableByteArray(fileBytes);
                CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
                logger.info("Trying to create a ContentSigner using SHA256RSA " 
                        + "algorithm, SunPKCS11 Provider, and Signing Cert Private Key...");
                ContentSigner sha2 = 
                        new JcaContentSignerBuilder("SHA256withRSA").setProvider("SunPKCS11-opensc").build(privateKey);
                logger.info("Trying to add SignerInfo data to CMSSignedDataGenerator...");
                gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(sha2, userCert));
                Gui.progress.setValue(90);
                gen.addCertificates(signers);
                logger.info("Trying to generate CMSSignedData object...");
                
                    
                CMSSignedData sigData = gen.generate(msg, true);
                logger.info("Completed sigData");
                Gui.errors = false;
                
                envelopedBytes = sigData.getEncoded();
                logger.info("Logging out of card/reader");
                ((SunPKCS11) sun).logout();
                Security.removeProvider(sun.getName());
                Security.removeProvider(bc.getName());

                if (envelopedBytes != null) {
                    filenameDest = file.toString() + ".p7m";
                    String result = writeBytes(envelopedBytes, filenameDest);
                    if (result == null) {
                        System.out.println("Done");
                    } else {
                        System.out.println(result + "\n");
                    }
                    Gui.progress.setValue(100);
                }                         
            } else if (signingCerts.size() > 1) {
                String tooManySigners = "Unable to sign. More than 1 signing certificate found.";
                Gui.errors = true;
                errorDesc = tooManySigners;
                Gui.status.append(dateFormat.format(new Date()) 
                        + " - " + tooManySigners + "\n");
                logger.severe(tooManySigners);
                Gui.progress.setValue(100);
                return;
            } else {
                Gui.errors = true;
                errorDesc = "Unable to sign. No signing certificates found on the card.";
                Gui.status.append(dateFormat.format(new Date()) 
                        + " - " + errorDesc 
                        + "\n"); 
                logger.severe(errorDesc);
                Gui.progress.setValue(100);
                return;
            }
        } catch(Exception e) {
            logger.log(Level.FINEST, e.getMessage(), e);
            Gui.errors=true;
            Gui.progress.setValue(100);
            return;
        } 
     }

    public byte[] getFileContentBytes(File file) {
        byte[] fileBytes = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            fileBytes = new byte[(int)file.length()];
            fis.read(fileBytes);
            fis.close();
        }
            catch (Exception x) {
                Gui.errors=true;
                logger.log(Level.FINEST, x.getMessage(), x);
        }
        return fileBytes;
    }
    
    public String writeBytes(byte[] dataToSign, String outputFilename) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFilename);
            fos.write(dataToSign);
            fos.close();
            return null;
        } catch(Exception x) {
            Gui.errors=true;
            logger.log(Level.FINEST, x.getMessage(), x);
            Gui.status.append(dateFormat.format(new Date()) 
                    + " - Error Writing File " 
                    + outputFilename + ": " 
                    + x.toString() + "\n");
            return "Error Writing File " 
                    + outputFilename 
                    + ": " 
                    + x.toString();
        }
    }
}
