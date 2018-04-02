package com.redstar.gifttime;

/**
 * Information about card
 */
public class SaleCard {
    public String cardId = null;
    public String companyName = null;
    public String cardDescription = null;
    public byte[] cardPhoto = null;
    public byte[] cardCodePhoto = null;


    public SaleCard(String cardId, String companyName, String cardDescription, byte[] cardPhoto, byte[] cardCodePhoto) {
        this.cardId = cardId;
        this.companyName = companyName;
        this.cardDescription = cardDescription;
        this.cardPhoto = cardPhoto;
        this.cardCodePhoto = cardCodePhoto;
    }

    public SaleCard() {

    }
}
