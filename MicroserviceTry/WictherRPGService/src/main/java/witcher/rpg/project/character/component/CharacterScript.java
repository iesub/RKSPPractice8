package witcher.rpg.project.character.component;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import witcher.rpg.project.game.object.GameObjectComponent;

import javax.persistence.*;
import java.util.Random;

@Getter
@Setter
public class CharacterScript extends GameObjectComponent {

    //Basic info
    String name;
    @JsonBackReference
    @Transient
    String[] availableRaces = {"Witcher", "Elf", "Dwarf", "Human"};
    String race;
    String gender;
    String age;

    //Lifepath
    @JsonBackReference
    @Transient
    String[] availableRegions = {"Northern Kingdoms", "Nilfgaard", "Elderlands"};
    String region;
    //Keep names of localization params for frontend
    @JsonBackReference
    @Transient
    String[] availableOriginNorthernKingdoms = {"Redania", "Kaedwen", "Temeria", "Aedirn", "Lyria_&_Rivia",
            "Kovir_&_Poviss", "Skellige", "Cidaris", "Verden", "Cintra"};
    @JsonBackReference
    @Transient
    String[] availableOriginNilfgaard = {"The_Heart_of_Nilfgaard", "Vicovaro", "Angren", "Nazair", "Mettina",
            "Mag_Turga", "Gheso", "Ebbing", "Maecht", "Gemmeria", "Etolia"};
    @JsonBackReference
    @Transient
    String[] availableOriginElderlands = {"Dol_Blathanna", "Mahakam"};
    String origin;
    boolean familyAlright;
    //Keep names of localization params for frontend
    @JsonBackReference
    @Transient
    String[] availableFamilyFate = {"Family_fate_1", "Family_fate_2", "Family_fate_3", "Family_fate_4", "Family_fate_5",
            "Family_fate_6", "Family_fate_7", "Family_fate_8", "Family_fate_9", "Family_fate_10", "Family_alright"};
    String familyFate;
    boolean parentsAlright;
    //Keep names of localization params for frontend
    @JsonBackReference
    @Transient
    String[] availableParentsFate = {"Parents_fate_1", "Parents_fate_2", "Parents_fate_3", "Parents_fate_4",
            "Parents_fate_5", "Parents_fate_6", "Parents_fate_7", "Parents_fate_8", "Parents_fate_9", "Parents_fate_10"
            , "Parents_alright"};
    String parentsFate;
    @JsonBackReference
    @Transient
    String[] availableFamilyTitle = {"Family_Title_1", "Family_Title_2", "Family_Title_3", "Family_Title_4",
            "Family_Title_5", "Family_Title_6", "Family_Title_7", "Family_Title_8", "Family_Title_9", "Family_Title_10"};
    String familyTitle;
    @JsonBackReference
    @Transient
    String[] availableFriendInfluence = {"Friend_Influence_1", "Friend_Influence_2", "Friend_Influence_3",
            "Friend_Influence_4", "Friend_Influence_5", "Friend_Influence_6", "Friend_Influence_7",
            "Friend_Influence_8", "Friend_Influence_9", "Friend_Influence_10"};
    String friendInfluence;
    Integer brotherAndSisterCount;

    //Life events and style

    //Class info

    //Statistics
    Integer intelligence = 1;
    Integer reflexes = 1;
    Integer dexterity = 1;
    Integer body = 1;
    Integer speed = 1;
    Integer empathy = 1;
    Integer craft = 1;
    Integer will = 1;
    Integer luck = 1;

    //Skills

    @Override
    public void Start() {

    }

    @Override
    public void Update() {

    }

    public void setRace(int raceId){
        race = availableRaces[raceId];

        //Set perks
    }

    public void lifePathRoll(){
        //If witcher - other generator
        if (race.equals(availableRaces[0])){
            generateLifepathWitcher();
            return;
        }

        //Generate region and origin
        Random generator = new Random();
        //For human
        if (race.equals(availableRaces[3])){
            int roll = generator.nextInt(2);
            //0 - Northern Kingdoms
            if (roll == 0){
                region = availableRegions[0];
                origin = availableOriginNorthernKingdoms[generator.nextInt(availableOriginNorthernKingdoms.length)];
            }
            //1 - Nilfgaard
            else {
                region = availableRegions[1];
                origin = availableOriginNilfgaard[generator.nextInt(availableOriginNilfgaard.length)];
            }
        }
        //For nonhuman
        else if (race.equals(availableRaces[1]) || race.equals(availableRaces[2])){
            int roll = generator.nextInt(3);
            // 0 - Northern Kingdom
            if (roll == 0){
                region = availableRegions[0];
                origin = availableOriginNorthernKingdoms[generator.nextInt(availableOriginNorthernKingdoms.length)];
            }
            //1 - Nilfgaard
            else if (roll == 1){
                region = availableRegions[1];
                origin = availableOriginNilfgaard[generator.nextInt(availableOriginNilfgaard.length)];
            }
            //2 - Elderland
            else {
                region = availableRegions[2];
                //Elf - DolBlatanna
                if (race.equals(availableRaces[1])){
                    origin = availableOriginElderlands[0];
                }
                //Dwarf - Mahakam
                else if (race.equals(availableRaces[2])){
                    origin = availableOriginElderlands[1];
                }
            }
        }

        //Next generator - family and parents
        int roll = generator.nextInt(2);
        //0 - Smth happened to family
        if (roll == 0){
            familyAlright = false;
            familyFate = availableFamilyFate[generator.nextInt(10)];
        }
        //1 - Family alright
        else {
            familyAlright = true;
            familyFate = availableFamilyFate[10];
        }

        roll = generator.nextInt(2);
        //0 - Smth happened to parents
        if (roll == 0){
            parentsAlright = false;
            parentsFate = availableParentsFate[generator.nextInt(10)];
        }
        //1 - Parents alright
        else {
            parentsAlright = true;
            parentsFate = availableParentsFate[10];
        }

        //Family Title
        //cases = region and title items
        roll = generator.nextInt(10);
        familyTitle = availableFamilyTitle[generator.nextInt(10)];
        switch (roll){
            case 0:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 1:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 2:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 3:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 4:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 5:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 6:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 7:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 8:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 9:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
        }

        //Friend influence
        //cases = region and friend gifts
        roll = generator.nextInt(10);
        friendInfluence = availableFriendInfluence[generator.nextInt(10)];
        switch (roll){
            case 0:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 1:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 2:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 3:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 4:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 5:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 6:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 7:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 8:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
            case 9:
                if (region.equals(availableRegions[0])){}
                else if(region.equals(availableRegions[1])){}
                else if(region.equals(availableRegions[2])){}
                break;
        }

        //amount of brothers and sisters
        if (region.equals(availableRegions[0])){brotherAndSisterCount = generator.nextInt(8)+1;}
        else if(region.equals(availableRegions[1])){brotherAndSisterCount = generator.nextInt(5)+1;}
        else if(region.equals(availableRegions[2])){brotherAndSisterCount = generator.nextInt(2)+1;}
    }

    public void generateLifepathWitcher(){

    }

}
