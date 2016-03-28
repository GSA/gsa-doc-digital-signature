package com.gsa.signingtool.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import static com.gsa.signingtool.app.Gui.dateFormat;
import static com.gsa.signingtool.app.Gui.logger;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_INFO;
import sun.security.pkcs11.wrapper.CK_SLOT_INFO;
import sun.security.pkcs11.wrapper.CK_TOKEN_INFO;
import sun.security.pkcs11.wrapper.PKCS11;

public class SelectSlot {
    private static PKCS11 pkcs11 = null;
    private static long[] slotList = null;
    private static ArrayList<Long> slotReturn = new ArrayList<Long>();
    private static TerminalFactory factory = null;
    private static List<CardTerminal> terminals = null;
    private static ListIterator<CardTerminal> terminalsIterator = null;
    private static CardTerminal terminal = null;
    private static boolean cardPresent = false;
    private static ArrayList<String> readers = new ArrayList<String>();
    private static int slotCount = 0;
    private static CK_C_INITIALIZE_ARGS initArgs = null;
    private static CK_SLOT_INFO slotInfo = null;
    private static CK_TOKEN_INFO tokenInfo = null;
    private static CK_INFO info = null;
    private static String compareReader = null;
    
    protected static ArrayList<Long> selectCardTerminal() {    
        try {   
            try {
                logger.info("Retrieving the list of readers available on system.");
                factory = TerminalFactory.getDefault();
                terminals = factory.terminals().list();
            } catch (CardException ex) {
                logger.severe("No readers found. Please verify "
                        + "your card reader is connected.");
                Gui.status.append(dateFormat.format(new Date()) 
                        + " - No readers found.\n" 
                        + dateFormat.format(new Date()) 
                        + " - Please verify your card reader is connected.\n");
                Gui.errors = true;
                logger.log(Level.FINEST, ex.getMessage(), ex);
                Gui.progress.setValue(100);
                return null;
            }
  
            terminalsIterator = terminals.listIterator();
            logger.info("Checking # of readers on system connected");
            if(terminals.size() == 1) {
                logger.info("Found only 1 reader connected on the system");
                while(terminalsIterator.hasNext()) {
                    terminal = terminalsIterator.next();
                    logger.info("Checking to see if a card is inserted " 
                            + "into the reader");
                    cardPresent = terminal.isCardPresent();
                    Gui.status.append(dateFormat.format(new Date()) 
                            + " - Verifying card is inserted into the reader.\n");
                    Gui.progress.setValue(30);
                    if(cardPresent) {
                        logger.info("Found a card present in: " 
                                + terminal.getName());
                        Gui.status.append(dateFormat.format(new Date()) 
                                + " - Found reader with card present.\n" 
                                + dateFormat.format(new Date()) + " - " 
                                + terminal.getName() + "\n");
                        readers.add(terminal.getName());
                        Gui.progress.setValue(40);
                    } else {
                        logger.severe("No card present, please insert card");
                        Gui.status.append(dateFormat.format(new Date()) 
                                + " - No card present, please insert card.\n");
                        Gui.errors = true;
                        Gui.progress.setValue(100);
                        return null;
                    }
                }
            } else {
                logger.info("More than 1 reader found connected on system");
                int i = 0;
                while(terminalsIterator.hasNext()) {
                    terminal = terminalsIterator.next();
                    cardPresent = terminal.isCardPresent();
                    logger.info("Detecting if more than 1 reader has a card inserted");
                    if (cardPresent) {
                        readers.add(terminal.getName());
                        System.out.println("Inside If = "+ i);
                        logger.info("Found a card present in: " 
                                + terminal.getName());
                        Gui.status.append(dateFormat.format(new Date()) 
                                + " - Found reader with card present.\n" 
                                + dateFormat.format(new Date()) 
                                + " - " 
                                + terminal.getName() 
                                + "\n");
                        Gui.progress.setValue(40);
                        break;
                    } else {
                        System.out.println("Outside If = " + i);
                        logger.info("No card found in the following reader: " 
                                + terminal.getName());
                        System.err.println("No card present in this reader" 
                                + terminal.getName());
                        //Gui.progress.setValue(100);
                        //return null;
                    }
                    i++;
                }
                
            }
                        
            if (readers.size() == 1) {
                logger.info("Found 1 reader and determining slot id#");
                // Setting up the items necessary for determining the # of slots
                logger.info("initializing reader arguements");
                initArgs = new CK_C_INITIALIZE_ARGS();
                initArgs.flags = 0;
                
                String libFile = new File("").getAbsolutePath() 
                        + "/lib/opensc-pkcs11.so";
                //libFile = libFile.replace("\\", "/");
                pkcs11 = PKCS11.getInstance(libFile, "C_GetFunctionList", initArgs, false);
                slotList = pkcs11.C_GetSlotList(true);
                slotCount = slotList.length;
                //loop through the available slots and return slot id
                logger.info("looping through the available slots");
                if (slotCount >= 1) {
                    for (long slot : slotList) {
                        slotInfo = pkcs11.C_GetSlotInfo(slot);
                        tokenInfo = pkcs11.C_GetTokenInfo(slot);
                        info = pkcs11.C_GetInfo();
                        compareReader = new String(slotInfo.slotDescription);
                        for (String readerMatch : readers) {
                            long[] temp = null;
                            readerMatch = readerMatch.replaceAll("\\s+","");
                            compareReader =compareReader.replaceAll("\\s","");
                            if (readerMatch.equals(compareReader)) {
                                slotReturn.add(slot);
                            }
                        }  
                    }
                } else {
                    logger.severe("Unable to find a slot id");
                    Gui.status.append(dateFormat.format(new Date()) 
                            + " - Issue with card reader.\n" 
                            + dateFormat.format(new Date()) 
                            + " - Please check connection or try a new reader.\n");
                    Gui.errors = true;
                    Gui.progress.setValue(100);
                    return null;
                }
            }
        } catch (Exception e) {
            logger.log(Level.FINEST, e.getMessage(), e);
            Gui.status.append(dateFormat.format(new Date()) 
                    + " - Error Getting Card Information From Reader.\n" 
                            + dateFormat.format(new Date()) 
                    + " - Please check reader and card connection.\n");
            Gui.errors = true;
            Gui.progress.setValue(100);
            return null;
        }
        logger.info("Returning an ArrayList of slot IDs");
        return slotReturn;
    }
}
