package com.medindia.elance;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class DrugInfo {
    private String genericName;
    private String ICDcode;
    private String therapeuticClassification;
    private String tradeNames;

    private String internationalName;
    private String whyItIsPrescribed;
    private String whenItIsNotBeTaken;
    private String pregnancyCategory;
    private String category;
    private String dosageAndWhenItIsToBeTaken;
    private String howItShouldBeTaken;
    private String warningPrecaution;
    private String sideEffect;
    private String storageConditions;

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getICDcode() {
        return ICDcode;
    }

    public void setICDcode(Document doc) {
        String icdCode = doc.select("body > div.container > div.page-content > div > div.item-details").text();
        if (icdCode.contains(Constants.ICD_CODE)) {
            if (icdCode.contains(Constants.THERAPEUTIC_CLASSIFICATION)) {
                this.ICDcode = icdCode.substring(icdCode.indexOf(Constants.ICD_CODE) + Constants.ICD_CODE.length() + 1, icdCode.indexOf(Constants.THERAPEUTIC_CLASSIFICATION)).trim().replace("|", "");
            }else{
                this.ICDcode = icdCode.substring(icdCode.indexOf(Constants.ICD_CODE) + Constants.ICD_CODE.length() + 1).trim().replace("|", "");
            }
        }else{
            this.ICDcode = "-";
        }
    }

    public String getTherapeuticClassification() {
        return therapeuticClassification;
    }

    public void setTherapeuticClassification(Document doc) {
        String therapeuticClassification = doc.select("body > div.container > div.page-content > div > div.item-details").text();
        if (therapeuticClassification.contains(Constants.THERAPEUTIC_CLASSIFICATION)){
            this.therapeuticClassification = therapeuticClassification.substring(therapeuticClassification.indexOf(Constants.THERAPEUTIC_CLASSIFICATION) + Constants.THERAPEUTIC_CLASSIFICATION.length() + 1).trim();
        }else {
            this.therapeuticClassification = "-";
        }
    }

    public String getTradeNames() {
        return tradeNames;
    }

    public void setTradeNames(Elements tradeNames) {
        String tradeNamesList = "";
        for (int i = 0; i<tradeNames.size(); i++){
            tradeNamesList += tradeNames.get(i).text();
            if (i != tradeNames.size() - 1){
                tradeNamesList += ", ";
            }
        }

        if (tradeNamesList.isEmpty()) {
            this.tradeNames = "-";
        }else {
            this.tradeNames = tradeNamesList;
        }
    }

    public String getInternationalName() {
        return internationalName;
    }

    public void setInternationalName(String details) {
        int from = details.indexOf(Constants.INTERNATIONAL, details.indexOf(Constants.TRADE_NAMES));
        int to = details.indexOf(Constants.WHY_IT_IS_PRESCRIBED);

        if (to == -1) {
            to = details.indexOf(Constants.WHEN_IT_IS_NOT_BE_TAKEN);
            if (to == -1) {
                to = details.indexOf(Constants.PREGNANCY_CATEGORY);
                if (to == -1) {
                    to = details.indexOf(Constants.DOSAGE_WHEN_IT_IS_TO_BE_TAKEN);
                    if (to == -1) {
                        to = details.indexOf(Constants.HOW_IT_SHOULD_BE_TAKEN);
                        if (to == -1) {
                            to = details.indexOf(Constants.WARNINGS_AND_PRECAUTIONS);
                            if (to == -1) {
                                to = details.indexOf(Constants.SIDE_EFFECTS);
                                if (to == -1) {
                                    to = details.indexOf(Constants.STORAGE_CONDITIONS);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (from != -1 && to != -1) {
            this.internationalName = details.substring(from + Constants.INTERNATIONAL.length() + 2, to).trim(); //3 == <space>: or <space>-*/
        }else{
            this.internationalName = "-";
        }
    }

    public String getWhyItIsPrescribed() {
        return whyItIsPrescribed;
    }

    public void setWhyItIsPrescribed(String details) {
        int from = details.indexOf(Constants.WHY_IT_IS_PRESCRIBED);
        int to = details.indexOf(Constants.WHEN_IT_IS_NOT_BE_TAKEN);

        if (to == -1) {
            to = details.indexOf(Constants.PREGNANCY_CATEGORY);
            if (to == -1) {
                to = details.indexOf(Constants.DOSAGE_WHEN_IT_IS_TO_BE_TAKEN);
                if (to == -1) {
                    to = details.indexOf(Constants.HOW_IT_SHOULD_BE_TAKEN);
                    if (to == -1) {
                        to = details.indexOf(Constants.WARNINGS_AND_PRECAUTIONS);
                        if (to == -1) {
                            to = details.indexOf(Constants.SIDE_EFFECTS);
                            if (to == -1) {
                                to = details.indexOf(Constants.STORAGE_CONDITIONS);
                            }
                        }
                    }
                }
            }
        }

        if (from != -1 && to != -1) {
            this.whyItIsPrescribed = details.substring(from + Constants.WHY_IT_IS_PRESCRIBED.length() + 1, to).trim();
        }else{
            this.whyItIsPrescribed = "-";
        }
    }

    public String getWhenItIsNotBeTaken() {
        return whenItIsNotBeTaken;
    }

    public void setWhenItIsNotBeTaken(String details) {
        int from = details.indexOf(Constants.WHEN_IT_IS_NOT_BE_TAKEN);
        int to = details.indexOf(Constants.PREGNANCY_CATEGORY);

        if (to == -1){
            to = details.indexOf(Constants.DOSAGE_WHEN_IT_IS_TO_BE_TAKEN);
            if (to == -1){
                to = details.indexOf(Constants.HOW_IT_SHOULD_BE_TAKEN);
                if (to == -1){
                    to = details.indexOf(Constants.WARNINGS_AND_PRECAUTIONS);
                    if (to == -1){
                        to = details.indexOf(Constants.SIDE_EFFECTS);
                        if (to == -1){
                            to = details.indexOf(Constants.STORAGE_CONDITIONS);
                        }
                    }
                }
            }
        }

        if (from != -1 && to != -1) {
            this.whenItIsNotBeTaken = details.substring(from + Constants.WHEN_IT_IS_NOT_BE_TAKEN.length() + 1, to).trim();
        } else{
            this.whenItIsNotBeTaken = "-";
        }
    }

    public String getPregnancyCategory() {
        return pregnancyCategory;
    }

    public void setPregnancyCategory(String pregnancyCategory) {
        if (pregnancyCategory.contains("Category A :")) {
            this.pregnancyCategory = "A";
        }else if (pregnancyCategory.contains("Category B :")) {
            this.pregnancyCategory = "B";
        }else if (pregnancyCategory.contains("Category C :")) {
            this.pregnancyCategory = "C";
        }else if (pregnancyCategory.contains("Category D :")) {
            this.pregnancyCategory = "D";
        }else if (pregnancyCategory.contains("Category X :")) {
            this.pregnancyCategory = "X";
        }else if (pregnancyCategory.contains("Category N :")) {
            this.pregnancyCategory = "N";
        }else this.pregnancyCategory = "-";
    }

    public String getCategory() {
        return category;
    }

    public void setCategory() {
        switch (getPregnancyCategory()) {
            case "A": {
                this.category = "Adequate and well-controlled human studies have failed to demonstrate a risk to the fetus in the first trimester of pregnancy (and there is no evidence of risk in later trimesters).";
                break;
            }
            case "B": {
                this.category = "Animal reproduction studies have failed to demonstrate a risk to the fetus and there are no adequate and well-controlled studies in pregnant women OR Animal studies have shown an adverse effect, but adequate and well-controlled studies in pregnant women have failed to demonstrate a risk to the fetus in any trimester.";
                break;
            }
            case "C": {
                this.category = "Animal reproduction studies have shown an adverse effect on the fetus and there are no adequate and well-controlled studies in humans, but potential benefits may warrant use of the drug in pregnant women despite potential risks.";
                break;
            }
            case "D": {
                this.category = "There is positive evidence of human fetal risk based on adverse reaction data from investigational or marketing experience or studies in humans, but potential benefits may warrant use of the drug in pregnant women despite potential risks.";
                break;
            }
            case "X": {
                this.category = "Studies in animals or humans have demonstrated fetal abnormalities and/or there is positive evidence of human fetal risk based on adverse reaction data from investigational or marketing experience, and the risks involved in use of the drug in pregnant women clearly outweigh potential benefits.";
                break;
            }
            case "N": {
                this.category = "Not yet classified.";
                break;
            }
            default: {
                this.category = "-";
            }
        }
    }

    public String getDosageAndWhenItIsToBeTaken() {
        return dosageAndWhenItIsToBeTaken;
    }

    public void setDosageAndWhenItIsToBeTaken(String details) {
        int from = details.indexOf(Constants.DOSAGE_WHEN_IT_IS_TO_BE_TAKEN);
        int to = details.indexOf(Constants.HOW_IT_SHOULD_BE_TAKEN);

        if (to == -1){
            to = details.indexOf(Constants.WARNINGS_AND_PRECAUTIONS);
            if (to == -1){
                to = details.indexOf(Constants.SIDE_EFFECTS);
                if (to == -1){
                    to = details.indexOf(Constants.STORAGE_CONDITIONS);
                }
            }
        }

        if (from != -1 && to != -1) {
            this.dosageAndWhenItIsToBeTaken = details.substring(from + Constants.DOSAGE_WHEN_IT_IS_TO_BE_TAKEN.length() + 1, to).trim();
        } else{
            this.dosageAndWhenItIsToBeTaken = "-";
        }
    }

    public String getHowItShouldBeTaken() {
        return howItShouldBeTaken;
    }

    public void setHowItShouldBeTaken(String details) {
        int from = details.indexOf(Constants.HOW_IT_SHOULD_BE_TAKEN);
        int to = details.indexOf(Constants.WARNINGS_AND_PRECAUTIONS);

        if (to == -1){
            to = details.indexOf(Constants.SIDE_EFFECTS);
            if (to == -1){
                to = details.indexOf(Constants.STORAGE_CONDITIONS);
            }
        }

        if (from != -1 && to != -1) {
            this.howItShouldBeTaken = details.substring(from + Constants.HOW_IT_SHOULD_BE_TAKEN.length() + 1, to).trim();
        } else{
            this.howItShouldBeTaken = "-";
        }
    }

    public String getWarningPrecaution() {
        return warningPrecaution;
    }

    public void setWarningPrecaution(String details) {
        int from = details.indexOf(Constants.WARNINGS_AND_PRECAUTIONS);
        int to = details.indexOf(Constants.SIDE_EFFECTS);

        if (to == -1){
            to = details.indexOf(Constants.STORAGE_CONDITIONS);
        }

        if (from != -1 && to != -1) {
            this.warningPrecaution = details.substring(from + Constants.WARNINGS_AND_PRECAUTIONS.length() + 1, to).trim();
        } else{
            this.warningPrecaution = "-";
        }
    }

    public String getSideEffect() {
        return sideEffect;
    }

    public void setSideEffect(String details) {
        int from = details.indexOf(Constants.SIDE_EFFECTS);
        int to = details.indexOf(Constants.STORAGE_CONDITIONS);

        if (from != -1 && to != -1) {
            this.sideEffect = details.substring(from + Constants.SIDE_EFFECTS.length() + 1, to).trim();
        } else{
            this.sideEffect = "-";
        }
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(String details) {
        int from = details.indexOf(Constants.STORAGE_CONDITIONS);

        if (from != -1) {
            int to = details.indexOf(Constants.LAST_UPDATED, from);

            if (to == -1) {
                to = details.indexOf(Constants.ADVERTISEMENT, from);
                if (to == -1) {
                    to = details.indexOf(Constants.RELATED_LINKS, from);
                    if (to == -1) {
                        to = details.indexOf(Constants.POST_YOUR_COMMENTS, from);
                    }
                }
            }

            if (to != -1) {
                this.storageConditions = details.substring(from + Constants.STORAGE_CONDITIONS.length() + 1, to).trim();
            } else {
                this.storageConditions = "-";
            }
        } else {
            this.storageConditions = "-";
        }
    }

    @Override
    public String toString() {
        return  "[Generic name]                    :" + genericName + '\n' +
                "[ICD Code]                        :" + ICDcode + '\n' +
                "[Therapeutic Classification]      :" + therapeuticClassification + '\n' +
                "[Trade Name(s)]                   :" + tradeNames + '\n' +
                "[International]                   :" + internationalName + '\n' +
                "[Why it is prescribed]            :" + whyItIsPrescribed + '\n' +
                "[When it is not to be taken]      :" + whenItIsNotBeTaken + '\n' +
                "[Pregnancy Category]              :" + pregnancyCategory + '\n' +
                "[Category]                        :" + category + '\n' +
                "[Dosage & When it is to be taken] :" + dosageAndWhenItIsToBeTaken + '\n' +
                "[How it should be taken]          :" + howItShouldBeTaken + '\n' +
                "[Warnings and Precautions]        :" + warningPrecaution + '\n' +
                "[Side Effects]                    :" + sideEffect + '\n' +
                "[Storage Conditions]              :" + storageConditions + '\n';
    }
}
