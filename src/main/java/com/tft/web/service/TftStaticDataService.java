package com.tft.web.service;

public interface TftStaticDataService {
    String getTacticianImgUrl(int itemId);
    String getUnitImgUrl(String characterId);
    String getItemImgUrl(int itemId);
    String getItemImgUrlByName(String itemName);
    String getTraitIconUrl(String traitName);
    String getTraitKoName(String traitId);
    String getItemKoName(String itemName);
    String getUnitKoName(String englishName);
}
