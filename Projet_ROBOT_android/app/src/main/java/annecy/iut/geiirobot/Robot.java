package annecy.iut.geiirobot;


public class Robot {

    private int num_id;
    private String val_moteur ;

    public Robot(int id, String donnees) {
        this.num_id = id;  //Recording ID
        this.val_moteur = donnees;   //Recording of the state of the motors
    }

    public int getId_robot() {
        return num_id;
    }

    public void setId_robot(int id) {
        this.num_id = id;
    }

    public String getVal_donnees() {
        return val_moteur;
    }

    public void setVal_speed(String donnees) {
        this.val_moteur = donnees;
    }


}
