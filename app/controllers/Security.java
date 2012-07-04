package controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import models.LgUser;
import play.Logger;

import java.math.BigInteger; 

public class Security extends Secure.Security {

    static boolean authenticate( String username, String password ) {
        // create admin user if no users currently exist
        // don't forget to change the password and remove this code later
        System.out.println("received user:");
        System.out.println(username);
        System.out.println("pass");
        System.out.println(password);

        if ( LgUser.count() == 0 ) {
            LgUser adminLgUser = new LgUser();
            /*
             * adminLgUser.userID = "admin"; adminLgUser.setPassword("admin");
             * adminLgUser.save();
             */
        }
        LgUser user = LgUser.find( "byUsername", username ).first();
        if ( user == null ) {
            flash.error( "Invalid userid or password." );
            return false;
        }
        System.out.println("dbpass:");
        System.out.println( user.password);
        String passwordHash = md5( password );
        System.out.println( passwordHash );
        System.out.println(String.format("%040x", new BigInteger(
                                user.password.getBytes())));
        System.out.println(String.format("%040x", new BigInteger(
                                passwordHash.getBytes()))); //"latin-1"
        //System.out.println();
        
        
        
        
        boolean match = user != null && (user.password.equals( passwordHash )
        //TO DO XXXremove next line!!
                                         || user.password.equals(password) );
        if ( match ) {
            //user.loginCount++;
            user.merge();
            user.save();
        }
        return match;
    }

    public static String md5( String password ) {
        byte[] bytesOfMessage = password.getBytes();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance( "MD5" );
        } catch ( NoSuchAlgorithmException e ) {
            Logger.fatal( e, "System configuration error" );
            return null;
        }
        byte[] thedigest = md.digest( bytesOfMessage );
        String passwordHash = new String( thedigest );
        return passwordHash;
    }

    static boolean check( String profile ) {
        //LgUser user = LgUser.find( "byLgUserID", connected() ).first();
        //connected() returns username passed to authenticate()
        LgUser user = LgUser.find( "byUsername", connected() ).first();
        //return user.roles != null && user.roles.name().equalsIgnoreCase(profile);
        return true;
    }
}
