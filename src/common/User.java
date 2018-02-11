/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author mariusz
 */
public class User {

    private int id;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private boolean isActive;

    public User(String firstName, String lastName, String passwordHash) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
    }

    public User(String firstName, String lastName, String password, String algName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = makeHash(password, algName);
    }

    public User(int id, String firstName, String lastName, boolean isActive) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
    }

    public static String makeHash(String plain, String algName) {
        try {
            MessageDigest alg = java.security.MessageDigest.getInstance(algName);
            byte[] array = alg.digest(plain.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
