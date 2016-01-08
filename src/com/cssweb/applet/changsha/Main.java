package com.cssweb.applet.changsha;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.DESKey;
import javacard.security.Key;
import javacard.security.KeyBuilder;
import javacard.security.RandomData;
import javacard.security.Signature;
import javacardx.crypto.Cipher;


/**
 *
 * @author chenhf
 */
public class Main extends Applet {
    
    MyAPDU  apduin;
   
    Changsha changsha;
    MyRandom random;
    
    
    
    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        
        
        
        new Main(bArray, bOffset, bLength);
    }

   
    protected Main(byte[] bArray, short bOffset, byte bLength) {
       
    	random = new MyRandom();
    	
     
        changsha = new Changsha(random);
        apduin = new MyAPDU(random);
        
       
       
    
        
        /*
        short aidLen = bArray[bOffset];
        byte[] aid = new byte[aidLen];
        Util.arrayCopy(bArray, (short)(bOffset+1), aid, (short)0, aidLen);
        
        short ctrlInfoLen = bArray[bOffset + aidLen + 1];
        byte[] ctrlInfo = new byte[ctrlInfoLen];
        Util.arrayCopy(bArray, (short)(bOffset + aidLen + 2), ctrlInfo, (short)0, ctrlInfoLen);
        
        short argsLen = bArray[bOffset + aidLen + ctrlInfoLen + 2];
        byte[] args = new byte[argsLen];
        Util.arrayCopy(bArray, (short)(bOffset + aidLen + ctrlInfoLen + 2), args, (short)0, argsLen);
        */
        
        byte aidLen = bArray[bOffset];
        if (aidLen== (byte)0){
            register();
        } else {
            register(bArray, (short)(bOffset+1), aidLen);
        }
    }
    
    
    public boolean select()
    {
        return true;
    }
    
   
    public void deselect()
    {
       
    }

   
    public void process(APDU apdu) throws ISOException {
        short bytesRead;
        short echoOffset;
        short  dl;
        boolean  rc=false;
        
        byte[] apduBuffer = apdu.getBuffer();
        
        apduin.cla = (byte)apduBuffer[ISO7816.OFFSET_CLA];
        apduin.ins = (byte)apduBuffer[ISO7816.OFFSET_INS];
        apduin.p1 = (byte)apduBuffer[ISO7816.OFFSET_P1];
        apduin.p2 = (byte)apduBuffer[ISO7816.OFFSET_P2];
        apduin.lc = (short)(apduBuffer[ISO7816.OFFSET_LC]& 0x0FF);
        

        // select AID (instance id) return AID FCI;
        
        if( apduin.APDUContainData(apduin.ins)) 
        {
           bytesRead = apdu.setIncomingAndReceive();
           echoOffset = (short)0;

           while ( bytesRead > 0 ) {
              Util.arrayCopyNonAtomic(apduBuffer, ISO7816.OFFSET_CDATA, apduin.buffer, echoOffset, bytesRead);
              echoOffset += bytesRead;
              bytesRead = apdu.receiveBytes(ISO7816.OFFSET_CDATA);
           }
           apduin.lc = echoOffset;

        }
        else 
        {
           apduin.le = apduin.lc;
           apduin.lc = (short)0;
        }
        
        switch (apduin.ins) {
            //case INS.CREATE:
           //     cos.createFile(apduin);
             //   break;
                
                
            case INS.SELECT:
            	changsha.select(apduin);
                break;
            case INS.READ_BINARY:
                changsha.readBinary(apduin);
                break;
            case INS.WRITE_BINARY:
            	changsha.writeBinary(apduin);
                break;
            case INS.READ_RECORD:
                changsha.readRecord(apduin);
                break;
           
            case INS.WRITE_KEY:
                changsha.writeKey(apduin);
                break;
            case INS.PERSONAL_END:
            	changsha.personalEnd(apduin);
                break;
                
                
            case INS.GET_CHALLENGE:
                changsha.challenge(apduin);
                break;
            case INS.EXTERNAL_AUTH:
                changsha.externalAuth(apduin);
                break;      
                
                
            case INS.WRITE_UID:
                changsha.writeUID(apduin);
                break;
            case INS.GET_MESSAGE:
                changsha.getMessage(apduin);
                break;
                
                
            case INS.GET_BALANCE:
                changsha.getBalance(apduin);
                break;
            case INS.INIT_PURCHASE_CHARGE:
            {
                if (apduBuffer[ISO7816.OFFSET_P1] == (byte)0x00)
                {
                	changsha.loadInit(apduin);
                }
                else if (apduBuffer[ISO7816.OFFSET_P1] == (byte)0x01)
                {
                	changsha.purchaseInit(apduin);
                }
                else
                {
                
                }
                    
                break;
            }
            case INS.LOAD:
                changsha.load(apduin);
                break;
            case INS.PURCHASE:
                changsha.purchase(apduin);
                break;

            case (byte)0xDC:
                if (apduin.cla == (byte)0x00 || apduin.cla == (byte)0x04)
                    changsha.updateRecord(apduin);
                if (apduin.cla == (byte)0x80)
                    changsha.cappPurchaseUpdate(apduin);
                break;
            

            
            case INS.APP_BLOCK:
            	changsha.appBlock(apduin);
                break;
            case INS.APP_UNBLOCK:
            	changsha.appUnBlock(apduin);
                break;
            case INS.CARD_BLOCK:
            	changsha.cardBlock(apduin);
            	break;
            case INS.VERIFY:
            	changsha.verify(apduin);
            	break;
                
           
                
            case INS.DES_TEST:
                ALG.testDES(apduin);
                break;
            case INS.MAC_TEST:
                ALG.testMAC(apduin);
                break;
            case INS.TAC_TEST:
                ALG.testTAC(apduin);
                break;
            case (byte)0x03:
            	changsha.getKey(apduin);
            	break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
	}
    
    
       // if (rc) {
           dl = apduin.le;
           if(dl>(short)0) {
              Util.arrayCopyNonAtomic(apduin.buffer,(short)0, apduBuffer,(short)0,dl);
              apdu.setOutgoingAndSend((short)0, apduin.le);
           }
      //  }
        
	
    }//end process
    
    
    
}
